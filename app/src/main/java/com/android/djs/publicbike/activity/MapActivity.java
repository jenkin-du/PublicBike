package com.android.djs.publicbike.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.application.App;
import com.android.djs.publicbike.bean.Address;
import com.android.djs.publicbike.bean.BikeStation;
import com.android.djs.publicbike.constant.URL;
import com.android.djs.publicbike.constant.Velocity;
import com.android.djs.publicbike.service.AnalysisService;
import com.android.djs.publicbike.util.MyUtil;
import com.android.djs.publicbike.view.LineGuildButton;
import com.supermap.android.commons.EventStatus;
import com.supermap.android.maps.BoundingBox;
import com.supermap.android.maps.CoordinateReferenceSystem;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.LineOverlay;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.Overlay;
import com.supermap.android.maps.Point2D;
import com.supermap.android.maps.PointOverlay;
import com.supermap.android.maps.query.FilterParameter;
import com.supermap.android.maps.query.QueryByDistanceParameters;
import com.supermap.android.maps.query.QueryByDistanceService;
import com.supermap.android.maps.query.QueryEventListener;
import com.supermap.android.maps.query.QueryResult;
import com.supermap.services.components.commontypes.Feature;
import com.supermap.services.components.commontypes.Geometry;
import com.supermap.services.components.commontypes.GeometryType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapActivity extends Activity implements View.OnClickListener, Overlay.OverlayTapListener {

    private MapView mMapView;

    //定位有关
    private LocationManager mLocationManager;
    private String mProvider;
    private LocationListener mLocationListener;
    private Point2D mLocation;
    private PointOverlay mLocOverlay;

    //查询有关
    public static int SEARCH_CODE = 0;
    public static int RESULT_CODE = 1;
    private static final int QUERY_SUCCESS = 0;
    private static final int QUERY_FAILED = 1;
    private static final int QUERY_DISTANCE = 1000;//单位为米

    private ArrayList<PointOverlay> mBikeOverlays;
    private Handler mHandler;

    //与路径导航有关
    private TextView mStationNameTV;

    private TextView mStationAddressTV;
    private TextView mBikeNumberTV;
    private TextView mLeftNumberTV;
    private TextView mGuildStationNameTV;

    private TextView mGuildDistanceTV;
    private TextView mGuildTimeTV;
    private String mStationName = "";
    private LinearLayout mAddressPanel;

    private LinearLayout mGuildPanel;
    private Point2D mNearByBikePt;

    private ArrayList<BikeStation> mNearbyBikeStations;
    //路径
    private List<LineOverlay> mPathOverlays;

    //面板上的关闭图标
    private ImageView mAPDelete;
    private ImageView mGPDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        //初始化控件
        initView();
        //初始化地图
        initMap();
        //定位
        locate();

    }


    /**
     * 初始化视图
     */

    private void initView() {


        App.mActivityList.add(this);

        RelativeLayout zoomInRL = (RelativeLayout) findViewById(R.id.map_zoom_in);
        RelativeLayout zoomOutRL = (RelativeLayout) findViewById(R.id.map_zoom_out);
        RelativeLayout locationRl = (RelativeLayout) findViewById(R.id.map_location);

        LinearLayout searchLL = (LinearLayout) findViewById(R.id.map_search);
        //规划路线
        LineGuildButton lineGuild = (LineGuildButton) findViewById(R.id.map_line_guild);

        //与路径导航有关
        mStationNameTV = (TextView) findViewById(R.id.map_station_name);
        mStationAddressTV = (TextView) findViewById(R.id.map_address);
        mBikeNumberTV = (TextView) findViewById(R.id.map_bike_number);
        mLeftNumberTV = (TextView) findViewById(R.id.map_left_number);
        mGuildStationNameTV = (TextView) findViewById(R.id.map_guild_name);
        mGuildDistanceTV = (TextView) findViewById(R.id.map_guild_distance);
        mGuildTimeTV = (TextView) findViewById(R.id.map_guild_time);

        mAddressPanel = (LinearLayout) findViewById(R.id.map_address_panel);
        mGuildPanel = (LinearLayout) findViewById(R.id.map_guild_panel);

        mAPDelete = (ImageView) findViewById(R.id.map_address_panel_delete);
        mGPDelete = (ImageView) findViewById(R.id.map_guild_panel_delete);

        mGPDelete.setOnClickListener(this);
        mAPDelete.setOnClickListener(this);

        Button goBtn = (Button) findViewById(R.id.map_guild_go);
        goBtn.setOnClickListener(this);

        zoomInRL.setOnClickListener(this);
        zoomOutRL.setOnClickListener(this);
        locationRl.setOnClickListener(this);
        searchLL.setOnClickListener(this);
        lineGuild.setOnClickListener(this);

        //创建地图窗口
        mMapView = (MapView) this.findViewById(R.id.map_view);
        mLocOverlay = new PointOverlay();
        Bitmap locBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.location_marker);
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(locBmp, 0, 0, locBmp.getWidth(), locBmp.getHeight(), matrix, true);
        mLocOverlay.setBitmap(resizeBmp);
        mMapView.getOverlays().add(mLocOverlay);

        mLocation = new Point2D();

        mHandler = new ShowQueryResultHandler();
        mNearByBikePt = new Point2D();
        mNearbyBikeStations = new ArrayList<>();

        mBikeOverlays = new ArrayList<>();
        mPathOverlays = new ArrayList<>();


    }

    /**
     * 初始化地图
     */
    private void initMap() {

        //创建地图图层，并指向iServer提供的地图服务
        LayerView baseLayerView = new LayerView(this);
        baseLayerView.setURL(URL.BASE_MAP);
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem();
        crs.wkid = 3857;
        baseLayerView.setCRS(crs);

        //加载地图图层
        mMapView.addLayer(baseLayerView);
        mMapView.getController().setZoom(16);
        mMapView.setUseScrollEvent(true);
        mMapView.setClickable(true);

        mMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddressPanel.setVisibility(View.INVISIBLE);
                mGuildPanel.setVisibility(View.INVISIBLE);
            }
        });

    }


    /**
     * 定位
     */
    private void locate() {

        mLocationListener = new LocationListener() {
            //当坐标改变时触发此函数
            public void onLocationChanged(Location location) {

                if (location != null) {
                    Toast.makeText(MapActivity.this, "null", Toast.LENGTH_LONG).show();
                }
                updateLocation(location);
            }

            //Provider被disable时触发此函数，比如网路被关闭
            public void onProviderDisabled(String provider) {
            }

            //Provider被enable时触发此函数，比如网路被打开
            public void onProviderEnabled(String provider) {
            }

            //Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        //无线网获取当前位置
        //获取到LocationManager对象
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //创建一个Criteria对象
        Criteria criteria = new Criteria();
        //设置粗略精确度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否需要返回海拔信息
        criteria.setAltitudeRequired(false);
        //设置是否需要返回方位信息
        criteria.setBearingRequired(false);
        //设置是否允许付费服务
        criteria.setCostAllowed(true);
        //设置电量消耗等级
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        //设置是否需要返回速度信息
        criteria.setSpeedRequired(false);

        //根据设置的Criteria对象，获取最符合此标准的provider对象
        mProvider = mLocationManager.getBestProvider(criteria, true);
        //根据当前provider对象获取最后一次位置信息
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location currentLocation = mLocationManager.getLastKnownLocation(mProvider);
        //如果位置信息为null，则请求更新位置信息
//        if (currentLocation != null) {

        Log.i("MAP", "locate: !!!!!!!!!!!!!!!!");
        updateLocation(currentLocation);
        mMapView.getController().setCenter(mLocation);
        //第一次加载默认数据
        querySurroundingBikes();

//        }

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mLocationManager.requestLocationUpdates(mProvider, 0, 0, mLocationListener);
            }
        };
        handler.postDelayed(runnable, 100);
    }


    /**
     * 更新位置
     */
    private void updateLocation(Location location) {

//        if (location != null) {
//            location.setLatitude(13238800.302279);
//            location.setLongitude(3778354.484748);
//            mLocation = Util.lonLat2Mercator(location.getLongitude(), location.getLatitude());
        mLocation.x = 13238790.747662;
        mLocation.y = 3778220.122845;
        mLocOverlay.setData(mLocation);
//        }
    }


    @Override
    protected void onDestroy() {

        if (mMapView != null) {
            mMapView.destroy();
        }
        super.onDestroy();
    }


    /**
     * 显示周围的自行车
     */
    private void querySurroundingBikes() {


        QueryByDistanceParameters p = new QueryByDistanceParameters();
        p.distance = QUERY_DISTANCE;// 必设，查询距离，单位为地理单位
        Geometry geo = new Geometry();
        // 构建点地物，必设
        geo.type = GeometryType.POINT;
        com.supermap.services.components.commontypes.Point2D point;
        point = new com.supermap.services.components.commontypes.Point2D(mLocation.getX(), mLocation.getY());
        com.supermap.services.components.commontypes.Point2D[] points;
        points = new com.supermap.services.components.commontypes.Point2D[]{point};
        geo.points = points;
        geo.parts = new int[]{1};
        p.geometry = geo;
        FilterParameter fp = new FilterParameter();

        fp.name = "bikeStation@Xianlin";// 必设，图层名称（图层名称格式：数据集名称@数据源别名）
        p.filterParameters = new FilterParameter[]{fp};
        QueryByDistanceService qs = new QueryByDistanceService(URL.QUERY);
        qs.process(p, new MyQueryEventListener());
    }


    @Override
    public void onClick(View v) {

        Intent intent;
        switch (v.getId()) {
            //放大
            case R.id.map_zoom_in:
                mMapView.getController().zoomIn();
                break;
            //缩小
            case R.id.map_zoom_out:
                mMapView.getController().zoomOut();
                break;

            //搜索
            case R.id.map_search:
                intent = new Intent(MapActivity.this, SearchActivity.class);
                startActivityForResult(intent, SEARCH_CODE);

                break;
            //我的位置
            case R.id.map_location:
                cleanPath();

                mMapView.getController().setCenter(mLocation);
                querySurroundingBikes();
                break;

            //路径导航
            case R.id.map_line_guild:

                openPathGuildActivity();
                break;
            //到这去
            case R.id.map_guild_go:

                mAddressPanel.setVisibility(View.INVISIBLE);
                goThere();
                break;

            //隐藏面板
            case R.id.map_address_panel_delete:
                mAddressPanel.setVisibility(View.INVISIBLE);
                mAPDelete.setVisibility(View.INVISIBLE);
                break;
            case R.id.map_guild_panel_delete:
                mGuildPanel.setVisibility(View.INVISIBLE);
                mGPDelete.setVisibility(View.INVISIBLE);
                break;
        }
    }


    /**
     * 打开路径导航页面
     */
    private void openPathGuildActivity() {
        Intent intent = new Intent(MapActivity.this, BusPathSearchActivity.class);

        Address addr = new Address();
        addr.setX(mLocation.x);
        addr.setY(mLocation.y);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("nearByBikeStations", mNearbyBikeStations);
        bundle.putParcelable("location", addr);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    /**
     * 到指定地点
     */
    private void goThere() {

        //清除以前的路径
        cleanPath();

        //找到离我最近的最有路径
        ArrayList<ArrayList<Point2D>> paths = optimalPathAnalysis(mLocation, mNearByBikePt);
        //显示距离和时间
        if (paths != null && paths.size() != 0) {

            double distance = MyUtil.calculatePathDistance(paths);
            //计算时间
            double time = distance / Velocity.WALK;

            mGuildDistanceTV.setText((int) distance + "米");
            mGuildTimeTV.setText((int) time + "分钟");
            mGuildStationNameTV.setText(mStationName);
            mAddressPanel.setVisibility(View.INVISIBLE);
            mAPDelete.setVisibility(View.INVISIBLE);
            mGuildPanel.setVisibility(View.VISIBLE);
            mGPDelete.setVisibility(View.VISIBLE);

        } else {
            showToast("抱歉！没有找到路径");
            mAddressPanel.setVisibility(View.INVISIBLE);
        }

    }


    /**
     * <p>
     * 可视化查询结果的处理器
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    public class ShowQueryResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case QUERY_SUCCESS:
                    QueryResult queryResult = (QueryResult) msg.obj;
                    showQueryResult(queryResult);
                    break;
                case QUERY_FAILED:
                    Toast.makeText(MapActivity.this, "数据加载失败，或许你周围没有自行车", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    public class MyQueryEventListener extends QueryEventListener {

        /**
         * <p>
         * 查询完成回调该接口，用户根据需要处理结果sourceObject
         * </p>
         *
         * @param sourceObject 查询结果
         * @param status       查询结果状态
         */
        @Override
        public void onQueryStatusChanged(Object sourceObject, EventStatus status) {
            Message msg = new Message();
            if (sourceObject instanceof QueryResult && status.equals(EventStatus.PROCESS_COMPLETE)) {
                msg.obj = sourceObject;
                msg.what = QUERY_SUCCESS;
            } else {
                msg.what = QUERY_FAILED;
            }
            // 子线程不能直接调用UI相关控件，所以只能通过把结果以消息的方式告知UI主线程展示结果
            mHandler.sendMessage(msg);
        }
    }


    /**
     * <p>
     * 可视化查询结果
     * </p>
     */
    public void showQueryResult(QueryResult qr) {
        clean();

        if (qr != null && qr.quertyResultInfo != null && qr.quertyResultInfo.recordsets != null) {
            for (int i = 0; i < qr.quertyResultInfo.recordsets.length; i++) {
                Feature[] features = qr.quertyResultInfo.recordsets[i].features;
                if (features != null) {

                    mNearbyBikeStations.clear();
                    BikeStation station;
                    for (Feature feature : features) {
                        if (feature != null && feature.geometry != null) {

                            Geometry geometry = feature.geometry;
                            GeometryType type = geometry.type;
                            Point2D point;

                            if (type.equals(GeometryType.POINT)) {

                                //将站点信息保存起来
                                station = new BikeStation();
                                String[] names = feature.fieldNames;
                                String[] values = feature.fieldValues;

                                HashMap<String, String> featureAttr;
                                featureAttr = new HashMap<>();
                                for (int k = 0; k < names.length; k++) {
                                    String name = names[k];
                                    String value = values[k];

                                    featureAttr.put(name, value);
                                }

                                station.setId(featureAttr.get("SmID"));
                                station.setStationName(featureAttr.get("stationName"));
                                station.setTotalNumber(stringToInt(featureAttr.get("bikeNumber")));
                                station.setLeftNumber(stringToInt(featureAttr.get("leftNumber")));

                                Address address = new Address();
                                address.setX(Double.parseDouble(featureAttr.get("SmX")));
                                address.setY(Double.parseDouble(featureAttr.get("SmY")));
                                address.setProvince(featureAttr.get("province"));
                                address.setCity(featureAttr.get("city"));
                                address.setDistrict(featureAttr.get("district"));
                                address.setDetailAddr(featureAttr.get("detailAddress"));
                                station.setAddress(address);

                                mNearbyBikeStations.add(station);

                                point = new Point2D(address.getX(), address.getY());

                                //显示点数据
                                showPointOverlay(point, station.getId());

                            }
                        }
                    }
                }
            }
            this.mMapView.invalidate();
        } else {
            Toast.makeText(MapActivity.this, "数据加载失败，或许你周围没有自行车", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * <p>
     * 可视化点对象
     * </p>
     */
    protected void showPointOverlay(Point2D point, String msID) {

        if (point != null) {

            PointOverlay overlay;
            Bitmap bmp = MyUtil.getPin(R.layout.pin_bike, MapActivity.this);


            overlay = new PointOverlay();
            overlay.setBitmap(bmp);

            overlay.setData(point);
            overlay.setKey(msID);
            overlay.setTapListener(this);
            mMapView.getOverlays().add(overlay);


            mBikeOverlays.add(overlay);
            mMapView.invalidate();
        }

    }


    @Override
    public void onTap(Point2D point2D, MapView mapView) {

    }

    @Override
    public void onTap(Point2D point2D, Overlay overlay, MapView mapView) {

        //清楚路径
        if (mPathOverlays != null) {
            mMapView.getOverlays().removeAll(mPathOverlays);
            mPathOverlays.clear();
        }
        //先关提示信息
        mAddressPanel.setVisibility(View.INVISIBLE);
        mGuildPanel.setVisibility(View.INVISIBLE);
        mGPDelete.setVisibility(View.INVISIBLE);
        mAPDelete.setVisibility(View.INVISIBLE);

        PointOverlay po = (PointOverlay) overlay;
        if (po != null) {

            String smID = po.getKey();
            for (BikeStation station : mNearbyBikeStations) {
                if (station.getId().equals(smID)) {
                    //展示该点的详细信息
                    showBikeStationDetail(station);

                    mNearByBikePt.x = station.getAddress().getX();
                    mNearByBikePt.y = station.getAddress().getY();
                }
            }
        }


    }

    /**
     * 展示某自行车点的详细信息
     */
    private void showBikeStationDetail(BikeStation station) {

        if (station != null) {

            mStationName = station.getStationName();
            String bikeNum = station.getTotalNumber() + "";
            String leftNum = station.getLeftNumber() + "";
            String detailAddr = station.getAddress().getDetailAddr();


            mStationNameTV.setText(mStationName);
            mBikeNumberTV.setText(bikeNum);
            mLeftNumberTV.setText(leftNum);
            mStationAddressTV.setText(detailAddr);

            mAddressPanel.setVisibility(View.VISIBLE);
            mAPDelete.setVisibility(View.VISIBLE);
        }
    }


    /**
     * <p>
     * 清除结果的可视化
     * </p>
     */
    public void clean() {

        if (mBikeOverlays != null) {

            for (BikeStation station : mNearbyBikeStations) {
                String id = station.getId();
                mMapView.removeOverlayByKey(id);
            }

            mNearbyBikeStations.clear();
        }
        this.mMapView.invalidate();
    }

    /**
     * 清空路径
     */
    public void cleanPath() {

        //清除以前的路径
        if (mPathOverlays != null) {
            mMapView.getOverlays().removeAll(mPathOverlays);
            mPathOverlays.clear();
        }
    }


    /**
     * 最佳路径分析
     */
    private ArrayList<ArrayList<Point2D>> optimalPathAnalysis(Point2D src, Point2D dest) {

        ArrayList<Point2D> points = new ArrayList<>();
        points.add(src);
        points.add(dest);

        //执行最佳路径分析
        ArrayList<ArrayList<Point2D>> pointLists = AnalysisService.findOptimalPath(URL.ANALYSIS, points);
        //显示路径
        showPath(pointLists);

        return pointLists;
    }

    /**
     * 根据分析结果显示分析出来的路径
     *
     * @param pointLists ''
     */
    private void showPath(ArrayList<ArrayList<Point2D>> pointLists) {

        Paint paint = new Paint(1);
        // 设置绘制风格paint，默认风格
        paint.setColor(Color.argb(200, 0, 0, 250));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);

        Path p = new Path();
        p.addCircle(7f, 7f, 7f, Path.Direction.CCW);
        PathDashPathEffect effect = new PathDashPathEffect(p, 20, 0, PathDashPathEffect.Style.ROTATE);
        paint.setPathEffect(effect);

        if (pointLists != null) {

            for (int i = 0; i < pointLists.size(); i++) {

                List<Point2D> geoPointList = pointLists.get(i);
                LineOverlay lineOverlay = new LineOverlay();
                lineOverlay.setLinePaint(paint);
                lineOverlay.setData(geoPointList);
                lineOverlay.setShowPoints(false);

                mMapView.getOverlays().add(lineOverlay);
                mPathOverlays.add(lineOverlay);
            }
        } else {
            Toast.makeText(MapActivity.this, "没有找到路线", Toast.LENGTH_SHORT).show();
        }

        BoundingBox bound = MyUtil.getBound(pointLists);
        mMapView.setViewBounds(bound);

        mMapView.invalidate();
    }

    /**
     * String  to Int
     *
     * @param value String
     * @return Int
     */
    private int stringToInt(String value) {
        if (value == null || value.equals("")) {
            return 0;
        } else {
            return Integer.valueOf(value);
        }
    }

    double mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {

            //返回键
            case KeyEvent.KEYCODE_BACK:

                int addrVisibility = mAddressPanel.getVisibility();
                int guildVisibility = mGuildPanel.getVisibility();

                if (addrVisibility != View.INVISIBLE) {
                    mAddressPanel.setVisibility(View.INVISIBLE);
                    mAPDelete.setVisibility(View.INVISIBLE);
                    return true;
                }

                if (guildVisibility != View.INVISIBLE) {
                    mGuildPanel.setVisibility(View.INVISIBLE);
                    mGPDelete.setVisibility(View.INVISIBLE);
                    return true;
                }


                if (addrVisibility == View.INVISIBLE && guildVisibility == View.INVISIBLE) {
                    if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                        mExitTime = System.currentTimeMillis();
                    } else {
                        //  finish();
                        App.exitAllActivity();
                    }
                }

                break;
        }

        return true;
    }

    /**
     * 显示提示说明
     */
    private void showToast(String text) {
        Toast.makeText(MapActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SEARCH_CODE && resultCode == RESULT_CODE) {
            Bundle bundle = data.getBundleExtra("bundle");
            if (bundle != null) {
                ArrayList<BikeStation> nearStations = (ArrayList<BikeStation>) bundle.getSerializable("nearBikeStations");
                Point2D center = (Point2D) bundle.getSerializable("center");

                if (center != null && nearStations != null) {
                    showStations(nearStations, center);
                }
            }
        }
    }

    /**
     * 显示站点
     */
    private void showStations(ArrayList<BikeStation> nearStations, Point2D center) {

        clean();
        mMapView.getController().setCenter(center);
        for (BikeStation station : nearStations) {

            mNearbyBikeStations.add(station);

            String id = station.getId();
            Point2D point = station.getPoint();

            showPointOverlay(point, id);
        }
    }
}

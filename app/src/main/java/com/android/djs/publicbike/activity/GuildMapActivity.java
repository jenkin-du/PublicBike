package com.android.djs.publicbike.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.adapter.GuildPathAdapter;
import com.android.djs.publicbike.bean.GuildPath;
import com.android.djs.publicbike.bean.GuildSolution;
import com.android.djs.publicbike.constant.PathType;
import com.android.djs.publicbike.constant.URL;
import com.android.djs.publicbike.util.MyUtil;
import com.android.djs.publicbike.view.NavigationBar;
import com.supermap.android.maps.BoundingBox;
import com.supermap.android.maps.CoordinateReferenceSystem;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.LineOverlay;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.Point2D;
import com.supermap.android.maps.PointOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * 导航地图
 * Created by DJS on 2017/6/20.
 */
public class GuildMapActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private MapView mMapView;
    private GuildSolution mGuildSolution;

    private TextView mSolutionNameTV;
    private ListView mPathsLV;

    private ArrayList<GuildPath> mPaths;
    private List<LineOverlay> mPathOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guild_map);
        //初始化
        initView();
        //打开地图
        openMap();
        //显示路径
        showGuildLine();
    }

    /**
     * 显示路径
     */
    private void showGuildLine() {

        if (mGuildSolution != null) {

            String name = mGuildSolution.getName();
            mSolutionNameTV.setText(name);

            for (GuildPath path : mGuildSolution.getPaths()) {
                //显示路径
                showPath(path);
            }
            //显示起点
            showPointOverlay(mGuildSolution.getStartPoint(), R.layout.pin_start);
            //显示终点
            showPointOverlay(mGuildSolution.getEndPoint(), R.layout.pin_end);

            mPaths = mGuildSolution.getPaths();
            if (mPaths != null) {

                GuildPathAdapter adapter = new GuildPathAdapter(this, mPaths);
                mPathsLV.setAdapter(adapter);
            }
        }
    }


    /**
     * 打开地图
     */
    private void openMap() {

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

        if (mGuildSolution != null) {
            BoundingBox bound = mGuildSolution.getBound();
            if (bound != null) {
                mMapView.setViewBounds(bound);
            }
        }

        PointOverlay overlay = new PointOverlay();
        Bitmap locBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.location_marker);
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(locBmp, 0, 0, locBmp.getWidth(), locBmp.getHeight(), matrix, true);
        overlay.setBitmap(resizeBmp);
        mMapView.getOverlays().add(overlay);

    }

    /**
     * 初始化
     */
    private void initView() {

        Bundle bundle = getIntent().getBundleExtra("bundle");
        mGuildSolution = (GuildSolution) bundle.getSerializable("solution");

        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.guild_map_nav_view);
        navigationBar.registerListener(new NavigationBar.OnClickListener() {
            @Override
            public void onClickBack() {
                GuildMapActivity.this.finish();
            }

            @Override
            public void onClickImg() {

            }

            @Override
            public void onClickRightText() {

            }
        });
        mMapView = (MapView) findViewById(R.id.guild_map_view);
        mPathsLV = (ListView) findViewById(R.id.guild_map_path_list);
        mSolutionNameTV = (TextView) findViewById(R.id.guild_map_solution_name);

        RelativeLayout zoomInRL = (RelativeLayout) findViewById(R.id.guild_zoom_in);
        RelativeLayout zoomOutRL = (RelativeLayout) findViewById(R.id.guild_zoom_out);

        mPathsLV.setOnItemClickListener(this);
        mPathOverlays = new ArrayList<>();

        zoomInRL.setOnClickListener(this);
        zoomOutRL.setOnClickListener(this);
    }


    /**
     * 根据分析结果显示分析出来的路径
     */
    private void showPath(GuildPath path) {


        if (path != null) {

            Paint paint = new Paint(1);
            PathType type = path.getPathType();

            Path p = new Path();
            p.addCircle(7f, 7f, 7f, Path.Direction.CCW);
            PathDashPathEffect effect = new PathDashPathEffect(p, 20, 0, PathDashPathEffect.Style.ROTATE);

            switch (type) {
                case WALK:
                case FIRST_PATH:
                case BUS_WALK:
                    paint.setColor(Color.rgb(30, 144, 255));
                    paint.setPathEffect(effect);
                    break;
                case BIKE:
                    paint.setColor(Color.rgb(0, 255, 127));
                    paint.setPathEffect(null);
                    break;
                case BUS:
                    paint.setColor(Color.rgb(255, 140, 0));
                    paint.setPathEffect(null);
                    break;
            }

            // 设置绘制风格paint，默认风格
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(10);
            paint.setDither(true);
            paint.setAntiAlias(true);


            ArrayList<ArrayList<Point2D>> paths = path.getPathSegments();
            if (paths != null) {
                for (int i = 0; i < paths.size(); i++) {

                    List<Point2D> geoPointList = paths.get(i);
                    LineOverlay lineOverlay = new LineOverlay();
                    lineOverlay.setLinePaint(paint);
                    lineOverlay.setData(geoPointList);
                    lineOverlay.setShowPoints(false);

                    mMapView.getOverlays().add(lineOverlay);
                    mPathOverlays.add(lineOverlay);

                    Point2D start=path.getStartPoint();
                    switch (path.getPathType()){

                        case BIKE:

                            showPointOverlaySmall(start,R.layout.pin_bike_small);
                            break;
                        case BUS:

                            showPointOverlaySmall(start,R.layout.pin_bus_small);
                            break;
                    }
                }

            }

            mMapView.invalidate();
        }
    }


    /**
     * <p>
     * 可视化点对象
     * </p>
     */
    protected void showPointOverlay(Point2D point, int resID) {

        if (point != null) {

            PointOverlay overlay;
            Bitmap bmp = MyUtil.getPin(resID, GuildMapActivity.this);

            overlay = new PointOverlay();
            overlay.setBitmap(bmp);

            overlay.setData(point);
            mMapView.getOverlays().add(overlay);

            mMapView.invalidate();
        }
    }

    /**
     * <p>
     * 可视化点对象
     * </p>
     */
    protected void showPointOverlaySmall(Point2D point, int resID) {

        if (point != null) {

            PointOverlay overlay;
            Bitmap bmp = MyUtil.getSmallPin(resID, GuildMapActivity.this);

            overlay = new PointOverlay();
            overlay.setBitmap(bmp);

            overlay.setData(point);
            mMapView.getOverlays().add(overlay);

            mMapView.invalidate();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        GuildPath path = mPaths.get(position);
        if (path != null) {
            BoundingBox bound = path.getBound();
            mMapView.setViewBounds(bound);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //放大
            case R.id.guild_zoom_in:
                mMapView.getController().zoomIn();
                break;
            //缩小
            case R.id.guild_zoom_out:
                mMapView.getController().zoomOut();
                break;
        }
    }
}

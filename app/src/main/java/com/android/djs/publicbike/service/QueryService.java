package com.android.djs.publicbike.service;

import com.android.djs.publicbike.bean.Address;
import com.android.djs.publicbike.bean.BikeStation;
import com.android.djs.publicbike.bean.BusStation;
import com.android.djs.publicbike.constant.URL;
import com.android.djs.publicbike.util.MyUtil;
import com.supermap.android.commons.EventStatus;
import com.supermap.android.data.GetFeaturesByGeometryService;
import com.supermap.android.data.GetFeaturesBySQLParameters;
import com.supermap.android.data.GetFeaturesBySQLService;
import com.supermap.android.data.GetFeaturesResult;
import com.supermap.android.maps.Point2D;
import com.supermap.android.maps.query.FilterParameter;
import com.supermap.android.maps.query.QueryByDistanceParameters;
import com.supermap.android.maps.query.QueryByDistanceService;
import com.supermap.android.maps.query.QueryBySQLParameters;
import com.supermap.android.maps.query.QueryBySQLService;
import com.supermap.android.maps.query.QueryEventListener;
import com.supermap.android.maps.query.QueryOption;
import com.supermap.android.maps.query.QueryResult;
import com.supermap.services.components.commontypes.Feature;
import com.supermap.services.components.commontypes.Geometry;
import com.supermap.services.components.commontypes.GeometryType;
import com.supermap.services.components.commontypes.QueryParameter;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 查询服务
 * Created by DJS on 2017/6/20.
 */
public class QueryService {

    /**
     * <p>
     * 设置距离查询参数，并构建范围查询服务执行查询，查询完毕将回调查询监听器onQueryStatusChanged接口处理结果，该接口由用户实现
     * </p>
     */
    public static ArrayList<BikeStation> queryBikeStationByDistance(int distance, Point2D center) {

        QueryByDistanceParameters p = new QueryByDistanceParameters();
        p.distance = distance;// 必设，查询距离，单位为地理单位
        Geometry geo = new Geometry();
        // 构建点地物，必设
        geo.type = GeometryType.POINT;
        com.supermap.services.components.commontypes.Point2D point;
        point = new com.supermap.services.components.commontypes.Point2D(center.getX(), center.getY());
        com.supermap.services.components.commontypes.Point2D[] points;
        points = new com.supermap.services.components.commontypes.Point2D[]{point};
        geo.points = points;
        geo.parts = new int[]{1};
        p.geometry = geo;
        FilterParameter fp = new FilterParameter();

        fp.name = "bikeStation@Xianlin";// 必设，图层名称（图层名称格式：数据集名称@数据源别名）
        p.filterParameters = new FilterParameter[]{fp};

        MyQueryEventListener listener = new MyQueryEventListener();
        QueryByDistanceService qs = new QueryByDistanceService(URL.QUERY);
        qs.process(p, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }

        QueryResult result = listener.getQueryResult();
        ArrayList<BikeStation> bikeStations = new ArrayList<>();
        BikeStation station;

        if (result != null && result.quertyResultInfo != null && result.quertyResultInfo.recordsets != null) {
            for (int i = 0; i < result.quertyResultInfo.recordsets.length; i++) {
                Feature[] features = result.quertyResultInfo.recordsets[i].features;
                if (features != null) {
                    for (Feature feature : features) {
                        if (feature != null && feature.geometry != null) {
                            Geometry geometry = feature.geometry;
                            GeometryType type = geometry.type;
                            if (type.equals(GeometryType.POINT)) {

                                //将站点信息保存起来
                                station = new BikeStation();
                                HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);

                                station.setId(attr.get("SmID"));
                                station.setStationName(attr.get("stationName"));
                                station.setTotalNumber(Integer.parseInt(attr.get("bikeNumber")));
                                station.setLeftNumber(Integer.parseInt(attr.get("leftNumber")));

                                Address address = new Address();
                                address.setX(Double.parseDouble(attr.get("SmX")));
                                address.setY(Double.parseDouble(attr.get("SmY")));
                                address.setProvince(attr.get("province"));
                                address.setCity(attr.get("city"));
                                address.setDistrict(attr.get("district"));
                                address.setDetailAddr(attr.get("detailAddress"));
                                station.setAddress(address);

                                bikeStations.add(station);
                            }
                        }
                    }
                }
            }
        }

        return bikeStations;


    }


    /**
     * <p>
     * 设置距离查询参数，并构建范围查询服务执行查询，查询完毕将回调查询监听器onQueryStatusChanged接口处理结果，该接口由用户实现
     * </p>
     */
    public static ArrayList<BusStation> queryBusStationByDistance(int distance, Point2D center) {

        QueryByDistanceParameters p = new QueryByDistanceParameters();
        p.distance = distance;// 必设，查询距离，单位为地理单位
        Geometry geo = new Geometry();
        // 构建点地物，必设
        geo.type = GeometryType.POINT;
        com.supermap.services.components.commontypes.Point2D point;
        point = new com.supermap.services.components.commontypes.Point2D(center.getX(), center.getY());
        com.supermap.services.components.commontypes.Point2D[] points;
        points = new com.supermap.services.components.commontypes.Point2D[]{point};
        geo.points = points;
        geo.parts = new int[]{1};
        p.geometry = geo;
        FilterParameter fp = new FilterParameter();

        fp.name = "busStation@Xianlin";// 必设，图层名称（图层名称格式：数据集名称@数据源别名）
        p.filterParameters = new FilterParameter[]{fp};

        MyQueryEventListener listener = new MyQueryEventListener();
        QueryByDistanceService qs = new QueryByDistanceService(URL.QUERY);
        qs.process(p, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }

        QueryResult result = listener.getQueryResult();
        ArrayList<BusStation> busStations = new ArrayList<>();
        BusStation station;

        if (result != null && result.quertyResultInfo != null && result.quertyResultInfo.recordsets != null) {
            for (int i = 0; i < result.quertyResultInfo.recordsets.length; i++) {
                Feature[] features = result.quertyResultInfo.recordsets[i].features;
                if (features != null) {
                    for (Feature feature : features) {
                        if (feature != null && feature.geometry != null) {
                            Geometry geometry = feature.geometry;
                            GeometryType type = geometry.type;
                            if (type.equals(GeometryType.POINT)) {

                                //将站点信息保存起来
                                station = new BusStation();
                                HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);

                                station.setId(Integer.parseInt(attr.get("stopId")));
                                station.setName(attr.get("name"));

                                Address address = new Address();
                                address.setX(Double.parseDouble(attr.get("SmX")));
                                address.setY(Double.parseDouble(attr.get("SmY")));
                                address.setProvince(attr.get("province"));
                                address.setCity(attr.get("city"));
                                address.setDistrict(attr.get("district"));
                                address.setDetailAddr(attr.get("detailAddress"));
                                station.setAddress(address);

                                busStations.add(station);
                            }
                        }
                    }
                }
            }
        }

        return busStations;


    }


    /**
     * <p>
     * 查询公交线路ID号
     * </p>
     */
    public static ArrayList<Integer> queryBusLineId(int stopId) {
        // 定义SQL查询参数
        GetFeaturesBySQLParameters params = new GetFeaturesBySQLParameters();
        params.datasetNames = new String[]{"Xianlin:busLineRelation"};
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.attributeFilter = "stopID=" + stopId;
        queryParameter.name = "busLineRelation@Xianlin";
        params.queryParameter = queryParameter;

        // 与服务器交互
        GetFeaturesBySQLService geoSQLService = new GetFeaturesBySQLService(URL.DATA);
        MyGetFeaturesEventListener listener = new MyGetFeaturesEventListener();
        geoSQLService.process(params, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GetFeaturesResult result = listener.getReult();

        if (result == null || result.features == null) {
            return null;
        }
        // 存储查询记录的几何对象的点
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < result.features.length; i++) {
            Feature feature = result.features[i];
            HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);
            ids.add(Integer.valueOf(attr.get("LINEID")));
        }
        return ids;
    }

    /**
     * <p>
     * 根据名字查询自行车站点位置
     * </p>
     */
    public static Point2D getBikeStopLocByName(String name) {

        if (name != null && !name.equals("")) {
            // 定义SQL查询参数
            GetFeaturesBySQLParameters params = new GetFeaturesBySQLParameters();

            params.datasetNames = new String[]{"Xianlin:bikeStation"};
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.attributeFilter = "stationName = '" + name + "'";
            queryParameter.name = "bikeStation@Xianlin";
            params.queryParameter = queryParameter;

            // 与服务器交互
            GetFeaturesBySQLService geoSQLService = new GetFeaturesBySQLService(URL.DATA);
            MyGetFeaturesEventListener listener = new MyGetFeaturesEventListener();
            geoSQLService.process(params, listener);
            try {
                listener.waitUntilProcessed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            GetFeaturesResult result = listener.getReult();

            if (result == null || result.features == null) {
                return null;
            }
            Point2D point;
            for (int i = 0; i < result.features.length; i++) {
                Feature feature = result.features[i];
                if (feature != null) {
                    HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);

                    point = new Point2D();
                    point.x = Double.parseDouble(attr.get("SMX"));
                    point.y = Double.parseDouble(attr.get("SMY"));
                    return point;
                }
            }
        }
        return null;
    }

    /**
     * <p>
     * 根据名字查询公交车站点位置
     * </p>
     */
    public static Point2D getBusStopLocByName(String name) {

        if (name != null && !name.equals("")) {
            // 定义SQL查询参数
            GetFeaturesBySQLParameters params = new GetFeaturesBySQLParameters();

            params.datasetNames = new String[]{"Xianlin:busStation"};
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.attributeFilter = "name = '" + name + "'";
            queryParameter.name = "busStation@Xianlin";
            params.queryParameter = queryParameter;

            // 与服务器交互
            GetFeaturesBySQLService geoSQLService = new GetFeaturesBySQLService(URL.DATA);
            MyGetFeaturesEventListener listener = new MyGetFeaturesEventListener();
            geoSQLService.process(params, listener);
            try {
                listener.waitUntilProcessed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            GetFeaturesResult result = listener.getReult();

            if (result == null || result.features == null) {
                return null;
            }
            Point2D point;
            for (int i = 0; i < result.features.length; i++) {
                Feature feature = result.features[i];
                if (feature != null) {
                    HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);
                    point = new Point2D();
                    point.x = Double.parseDouble(attr.get("SMX"));
                    point.y = Double.parseDouble(attr.get("SMY"));
                    return point;
                }

            }
        }
        return null;
    }


    /**
     * <p>
     * 根据公交线路id查公交线路的名字
     * </p>
     */
    public static String queryBusLineName(int lineId) {


        QueryBySQLParameters p = new QueryBySQLParameters();
        FilterParameter fp = new FilterParameter();
        // 属性过滤条件
        fp.attributeFilter = "lineId = " + lineId;// SMID > 169 AND SMID < 174
        fp.name = "BusLine@Xianlin";
        p.filterParameters = new FilterParameter[]{fp};
        p.expectCount = 20;// 期望返回的条数
        p.queryOption = QueryOption.ATTRIBUTEANDGEOMETRY;// 设置返回结果类型，默认是返回属性和地物，可以根据需要值返回其一
        MyQueryEventListener listener = new MyQueryEventListener();
        QueryBySQLService qs = new QueryBySQLService(URL.QUERY);// totalCount:4,currentCount:4
        qs.process(p, listener);// 执行查询，必须设置 用户实现的查询监听器对象
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueryResult result = listener.getQueryResult();
        String name = null;
        if (result != null && result.quertyResultInfo != null && result.quertyResultInfo.recordsets != null) {
            for (int i = 0; i < result.quertyResultInfo.recordsets.length; i++) {
                Feature[] features = result.quertyResultInfo.recordsets[i].features;
                if (features != null) {
                    for (Feature feature : features) {
                        if (feature != null && feature.geometry != null) {

                            //将站点信息保存起来
                            HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);
                            name = attr.get("name");
                        }
                    }
                }
            }
        }

        return name;
    }


    public static class MyQueryEventListener extends QueryEventListener {

        private QueryResult queryResult;

        public QueryResult getQueryResult() {
            return queryResult;
        }

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

            if (sourceObject instanceof QueryResult && status.equals(EventStatus.PROCESS_COMPLETE)) {
                queryResult = (QueryResult) sourceObject;
            }
        }
    }


    /**
     * <p>
     * 实现查询结果的监听器，自己实现处理结果接口
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    static class MyGetFeaturesEventListener extends GetFeaturesByGeometryService.GetFeaturesEventListener {
        private GetFeaturesResult lastResult;

        public MyGetFeaturesEventListener() {
            super();
            // TODO Auto-generated constructor stub
        }

        public GetFeaturesResult getReult() {
            return lastResult;
        }

        @Override
        public void onGetFeaturesStatusChanged(Object sourceObject, EventStatus status) {
            if (sourceObject instanceof GetFeaturesResult) {
                lastResult = (GetFeaturesResult) sourceObject;
            }
        }

    }


    /**
     * <p>
     * <p/>
     * 检查公交车旁边是否有公共直行车站点
     * </p>
     */
    public static boolean checkBikeStation(Point2D center, BikeStation station) {

        QueryByDistanceParameters p = new QueryByDistanceParameters();
        p.distance = 200;// 必设，查询距离，单位为地理单位
        Geometry geo = new Geometry();
        // 构建点地物，必设
        geo.type = GeometryType.POINT;
        com.supermap.services.components.commontypes.Point2D point;
        point = new com.supermap.services.components.commontypes.Point2D(center.getX(), center.getY());
        com.supermap.services.components.commontypes.Point2D[] points;
        points = new com.supermap.services.components.commontypes.Point2D[]{point};
        geo.points = points;
        geo.parts = new int[]{1};
        p.geometry = geo;
        FilterParameter fp = new FilterParameter();

        fp.name = "bikeStation@Xianlin";// 必设，图层名称（图层名称格式：数据集名称@数据源别名）
        p.filterParameters = new FilterParameter[]{fp};

        MyQueryEventListener listener = new MyQueryEventListener();
        QueryByDistanceService qs = new QueryByDistanceService(URL.QUERY);
        qs.process(p, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }

        QueryResult result = listener.getQueryResult();
        boolean has = false;

        if (result != null && result.quertyResultInfo != null && result.quertyResultInfo.recordsets != null) {
            for (int i = 0; i < result.quertyResultInfo.recordsets.length; i++) {
                Feature[] features = result.quertyResultInfo.recordsets[i].features;
                if (features != null) {
                    for (Feature feature : features) {
                        if (feature != null && feature.geometry != null) {

                            Geometry geometry = feature.geometry;
                            GeometryType type = geometry.type;

                            if (type.equals(GeometryType.POINT)) {
                                has = true;
                                HashMap<String, String> attr = MyUtil.getFeatureAttr(feature);

                                station.setId(attr.get("SmID"));
                                station.setStationName(attr.get("stationName"));
                                station.setTotalNumber(Integer.parseInt(attr.get("bikeNumber")));
                                station.setLeftNumber(Integer.parseInt(attr.get("leftNumber")));

                                Address address = new Address();
                                address.setX(Double.parseDouble(attr.get("SmX")));
                                address.setY(Double.parseDouble(attr.get("SmY")));
                                address.setProvince(attr.get("province"));
                                address.setCity(attr.get("city"));
                                address.setDistrict(attr.get("district"));
                                address.setDetailAddr(attr.get("detailAddress"));
                                station.setAddress(address);
                            }
                        }
                    }
                }
            }
        }

        return has;


    }


}

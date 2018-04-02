package com.android.djs.publicbike.service;

import com.supermap.android.commons.EventStatus;
import com.supermap.android.maps.Point2D;
import com.supermap.android.networkAnalyst.ClosestFacilityPath;
import com.supermap.android.networkAnalyst.FindClosestFacilitiesParameters;
import com.supermap.android.networkAnalyst.FindClosestFacilitiesResult;
import com.supermap.android.networkAnalyst.FindClosestFacilitiesService;
import com.supermap.android.networkAnalyst.FindPathParameters;
import com.supermap.android.networkAnalyst.FindPathResult;
import com.supermap.android.networkAnalyst.FindPathService;
import com.supermap.android.networkAnalyst.TransportationAnalystParameter;
import com.supermap.android.networkAnalyst.TransportationAnalystResultSetting;
import com.supermap.services.components.commontypes.Path;
import com.supermap.services.components.commontypes.Route;

import java.util.ArrayList;

/**
 * 分析服务类
 * Created by DJS on 2017/6/11.
 */
public class AnalysisService {

    /**
     * <p>
     * 执行最佳路径分析
     * </p>
     *
     * @param url       网路分析服务地址
     * @param geoPoints 用户选择的分析站点
     * @return 路由对象的Points数组
     */
    public static ArrayList<ArrayList<Point2D>> findOptimalPath(String url, ArrayList<Point2D> geoPoints) {
        if (url == null || "".equals(url) || geoPoints == null) {
            return null;
        }
        // 定义最佳路径分析参数
        FindPathParameters params = new FindPathParameters();
        Point2D[] nodes = new Point2D[geoPoints.size()];
        for (int i = 0; i < geoPoints.size(); i++) {
            nodes[i] = geoPoints.get(i);
        }
        params.nodes = nodes;
        params.parameter = getGeneralParam();

        // 执行最佳路径分析
        FindPathService path = new FindPathService(url);
        MyFindPathEventListener listener = new MyFindPathEventListener();
        path.process(params, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 解析最佳路径分析结果，获取Route对象中的Points数组，用于绘制路径显示在地图上
        FindPathResult pathResult = listener.getResult();
        if (pathResult != null && pathResult.pathList != null) {
            Path[] pathList = pathResult.pathList;
            ArrayList<ArrayList<Point2D>> pointsList = new ArrayList<>();

            for (Path aPathList : pathList) {
                ArrayList<Point2D> points = new ArrayList<>();
                Route route = aPathList.route;
                if (route != null && route.points != null) {
                    for (int k = 0; k < route.points.length; k++) {
                        points.add(new Point2D(route.points[k].x, route.points[k].y));
                    }
                }
                pointsList.add(points);
            }
            return pointsList;
        }
        return null;
    }


    /**
     * <p>
     * 执行最近设施分析
     * </p>
     *
     * @param url           网路分析服务地址
     * @param geoFacilities 设施点集合
     * @param event         事件点
     * @return 路由对象的Points数组
     */
    public static int findClosestFacilities(String url, ArrayList<Point2D> geoFacilities, Point2D event, ArrayList<ArrayList<Point2D>> pointLists) {

        int index = -1;

        if (url == null || "".equals(url) || geoFacilities == null || event == null) {
            return index;
        }
        // 定义最近设施查找分析参数
        FindClosestFacilitiesParameters params = new FindClosestFacilitiesParameters();
        Point2D[] facilities = new Point2D[geoFacilities.size()];
        for (int i = 0; i < geoFacilities.size(); i++) {
            facilities[i] = geoFacilities.get(i);
        }
        params.facilities = facilities;
        params.event = event;
        params.expectFacilityCount = 1;
        params.parameter = getGeneralParam();

        // 执行最近设施分析
        FindClosestFacilitiesService cft = new FindClosestFacilitiesService(url);
        MyFindClosestFacilitiesEventListener listener = new MyFindClosestFacilitiesEventListener();
        cft.process(params, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 解析最近设施查找分析结果，获取Route对象中的Points数组，用于绘制路径显示在地图上
        FindClosestFacilitiesResult closestResult = listener.getResult();

        if (closestResult != null && closestResult.facilityPathList != null) {

            ClosestFacilityPath[] closestList = closestResult.facilityPathList;

            for (ClosestFacilityPath closestPath : closestList) {
                ArrayList<Point2D> points = new ArrayList<>();
                Route route = closestPath.route;
                //找到索引号
                index = closestPath.facilityIndex;
                if (route != null && route.points != null) {
                    for (int k = 0; k < route.points.length; k++) {
                        points.add(new Point2D(route.points[k].x, route.points[k].y));
                    }
                    pointLists.add(points);
                }
            }
            return index;
        }
        return index;
    }



    /**
     * <p>
     * 交通网络分析通用参数
     * </p>
     */
    public static TransportationAnalystParameter getGeneralParam() {

        // 定义交通网络分析结果参数，这些参数用于指定返回的结果内容
        TransportationAnalystResultSetting resultSetting = new TransportationAnalystResultSetting();
        resultSetting.returnEdgeFeatures = false;
        resultSetting.returnEdgeGeometry = false;
        resultSetting.returnRoutes = true;

        // 定义交通网络分析通用参数
        TransportationAnalystParameter parameter = new TransportationAnalystParameter();
        parameter.weightFieldName = "SmLength";
        parameter.resultSetting = resultSetting;
        return parameter;

    }





    /**
     * <p>
     * 实现处理最佳路径分析结果的监听器，自己实现处理结果接口
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    static class MyFindPathEventListener extends FindPathService.FindPathEventListener {
        private FindPathResult pathResult;

        public MyFindPathEventListener() {
            super();
            // TODO Auto-generated constructor stub
        }

        public FindPathResult getResult() {
            return pathResult;
        }

        @Override
        public void onFindPathStatusChanged(Object sourceObject, EventStatus status) {
            if (sourceObject instanceof FindPathResult) {
                pathResult = (FindPathResult) sourceObject;
            }
        }

    }

    /**
     * <p>
     * 实现最近设施分析结果的监听器，自己实现处理结果接口
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    static class MyFindClosestFacilitiesEventListener extends FindClosestFacilitiesService.FindClosestFacilitiesEventListener {
        private FindClosestFacilitiesResult closestResult;

        public MyFindClosestFacilitiesEventListener() {
            super();
            // TODO Auto-generated constructor stub
        }

        public FindClosestFacilitiesResult getResult() {
            return closestResult;
        }

        @Override
        public void onFindClosestFacilitiesStatusChanged(Object sourceObject, EventStatus status) {
            if (sourceObject instanceof FindClosestFacilitiesResult) {
                closestResult = (FindClosestFacilitiesResult) sourceObject;
            }
        }
    }
}

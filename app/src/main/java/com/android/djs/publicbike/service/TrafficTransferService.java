package com.android.djs.publicbike.service;

import com.android.djs.publicbike.bean.GuildPath;
import com.android.djs.publicbike.bean.GuildSolution;
import com.android.djs.publicbike.constant.PathType;
import com.supermap.android.commons.EventStatus;
import com.supermap.android.maps.Point2D;
import com.supermap.android.trafficTransferAnalyst.StopQueryParameters;
import com.supermap.android.trafficTransferAnalyst.StopQueryResult;
import com.supermap.android.trafficTransferAnalyst.StopQueryService;
import com.supermap.android.trafficTransferAnalyst.StopQueryService.StopQueryEventListener;
import com.supermap.android.trafficTransferAnalyst.TransferGuideItem;
import com.supermap.android.trafficTransferAnalyst.TransferLine;
import com.supermap.android.trafficTransferAnalyst.TransferLines;
import com.supermap.android.trafficTransferAnalyst.TransferPathParameters;
import com.supermap.android.trafficTransferAnalyst.TransferPathResult;
import com.supermap.android.trafficTransferAnalyst.TransferPathService;
import com.supermap.android.trafficTransferAnalyst.TransferPathService.TransferPathEventListener;
import com.supermap.android.trafficTransferAnalyst.TransferSolution;
import com.supermap.android.trafficTransferAnalyst.TransferSolutionParameters;
import com.supermap.android.trafficTransferAnalyst.TransferSolutionResult;
import com.supermap.android.trafficTransferAnalyst.TransferSolutionService;
import com.supermap.android.trafficTransferAnalyst.TransferSolutionService.TransferSolutionEventListener;
import com.supermap.android.trafficTransferAnalyst.TransferStopInfo;
import com.supermap.android.trafficTransferAnalyst.TransferTactic;

import java.util.ArrayList;

/**
 * <p>
 * 封装交通换乘分析工具类，通过该类方法，可以得到交通换乘的换乘信息。
 * </p>
 *
 * @author ${Author}
 * @version ${Version}
 */
public class TrafficTransferService {
    private static final String TAG = "TAG";

    /**
     * <p>
     * 执行交通换乘方案分析，返回方案结果。
     * </p>
     *
     * @param url 交通换乘服务根地址
     * @return ''
     */
    public static ArrayList<GuildSolution> transferAnalyst(String url, int start, int end) {

        if (url == null || "".equals(url)) {
            return null;
        }

        TransferSolutionService tss = new TransferSolutionService(url);
        TransferSolutionParameters params = new TransferSolutionParameters();
        params.points = new Integer[]{start, end};
        params.transferTactic = TransferTactic.MIN_DISTANCE;

        MyTransferSolutionEventListener listener = new MyTransferSolutionEventListener();
        tss.process(params, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TransferSolutionResult result = listener.getLastResult();

        if (result != null) {
            //分析解决方案
            return AnalyseResult(url, start, end, result);
        }

        return null;

    }

    /**
     * 分析解决方案
     *
     * @param result 解决方案
     */
    private static ArrayList<GuildSolution> AnalyseResult(String url, int start, int end, TransferSolutionResult result) {

        ArrayList<GuildSolution> guildSolutions = new ArrayList<>();
        GuildSolution guildSolution;
        GuildPath path;

        TransferSolution transferSolutions[] = result.solutionItems;
        if (transferSolutions != null) {
            for (TransferSolution solution : transferSolutions) {

                guildSolution = new GuildSolution();

                TransferLines transferLinesArr[] = solution.linesItems;
                TransferLine transferLineArr[] = new TransferLine[transferLinesArr.length];

                int transferCount = solution.transferCount;
                for (int i = 0; i < transferLinesArr.length; i++) {

                    TransferLines lines = transferLinesArr[i];
                    TransferLine line = lines.lineItems[0];

                    transferLineArr[i] = line;
//                    int id = line.lineID;
//                    String startName = line.startStopName;
//                    String endName = line.endStopName;
//                    String lineName = line.lineName;


                    path = new GuildPath();
//                    path.setPathType(PathType.BUS);
//                    path.setId(id);
//                    path.setStartPointName(startName);
//                    path.setEndPointName(endName);
//                    path.setPathName(lineName);

                    guildSolution.addGuildPath(path);
                }

                //// TODO: 2017/6/19
                AnalyzePath(url, transferLineArr, transferCount, start, end, guildSolution);
                guildSolutions.add(guildSolution);
            }

        }

        return guildSolutions;
    }


    /**
     * <p>
     * 根据输入的关键字查询匹配的公交站点信息 对象集合
     * </p>
     *
     * @param url     交通换乘服务根地址
     * @param keyword 公交站点名
     * @return ''
     */
    public static TransferStopInfo[] queryStop(String url, String keyword) {
        StopQueryService sqs = new StopQueryService(url);
        StopQueryParameters params = new StopQueryParameters();
        params.keyWord = keyword;
        params.returnPosition = true;
        MyStopQueryEventListener listener = new MyStopQueryEventListener();
        sqs.process(params, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StopQueryResult queryResult = listener.getLastResult();
        if (queryResult == null) {
            return null;
        }
        return queryResult.transferStopInfos;
    }


    /**
     * <p>
     * 执行交通换乘路径分析
     * </p>
     *
     * @param url           交通换乘服务根地址
     * @param transferLines 换乘路线信息数组
     * @param transferCount 一个方案的路线换乘次数
     * @param start         出发站id
     * @param end           终点站id
     * @param guildSolution ''
     */
    private static void AnalyzePath(String url, TransferLine[] transferLines, int transferCount, int start, int end, GuildSolution guildSolution) {

        if (transferLines == null || (transferLines.length == 0)) {
            return;
        }
        TransferPathService tps = new TransferPathService(url);
        // points=[175,164]&transferLines=[{"lineID":27,"startStopIndex":7,"endStopIndex":9}]
        TransferPathParameters params = new TransferPathParameters();
        params.points = new Integer[]{start, end};
        if (transferCount == 0) {
            params.transferLines = new TransferLine[]{transferLines[0]};
        } else {
            params.transferLines = transferLines;
        }
        MyTransferPathEventListener listener = new MyTransferPathEventListener();
        tps.process(params, listener);
        try {
            listener.waitUntilProcessed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TransferPathResult pathResult = listener.getResult();
        TransferGuideItem[] transferGuideItems = pathResult.transferGuide.items;

        //过得引导项
        TransferGuideItem transferGuideItem;
        for (int m = 0; m < transferGuideItems.length; m++) {

            transferGuideItem = transferGuideItems[m];
            if (transferGuideItem.isWalking) {
                GuildPath path = new GuildPath();
                guildSolution.getPaths().add(path);
                guildSolution.getGuildPath(m).setPathType(PathType.BUS_WALK);
                guildSolution.getGuildPath(m).setPathName("步行");

            } else {
                guildSolution.getGuildPath(m).setPathType(PathType.BUS);
            }
            //起点
            Point2D startPoint = transferGuideItem.startPosition;
            //终点
            Point2D endPoint = transferGuideItem.endPosition;
            //获得起点和中点的名字
            String startName = transferGuideItem.startStopName;
            String endName = transferGuideItem.endStopName;
            //经过的站点数
            int count = transferGuideItem.passStopCount;
            //将这些设置给解决方案

            guildSolution.getGuildPath(m).setStartPoint(startPoint);
            guildSolution.getGuildPath(m).setStartPointName(startName);

            guildSolution.getGuildPath(m).setEndPoint(endPoint);
            guildSolution.getGuildPath(m).setEndPointName(endName);

            String lineName = transferGuideItem.lineName;
            if (lineName != null && !lineName.equals("")) {
                guildSolution.getGuildPath(m).setPathName(lineName);
            }
            guildSolution.getGuildPath(m).setPassStopCount(count);
            //设置路径
            ArrayList<Point2D> pathPoints = new ArrayList<>();
            for (int i = 0; i < transferGuideItem.route.points.length; i++) {
                Point2D point = new Point2D();

                point.x = transferGuideItem.route.points[i].x;
                point.y = transferGuideItem.route.points[i].y;
                pathPoints.add(point);
            }
            ArrayList<ArrayList<Point2D>> paths = new ArrayList<>();
            paths.add(pathPoints);
            guildSolution.getGuildPath(m).setPathSegments(paths);
        }
    }


    /**
     * <p>
     * 实现 处理线路查询结果的监听器，自己实现处理结果接口
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    public static class MyTransferPathEventListener extends TransferPathEventListener {
        private TransferPathResult pathResult;

        public MyTransferPathEventListener() {
            super();
            // TODO Auto-generated constructor stub
        }

        public TransferPathResult getResult() {
            return pathResult;
        }

        @Override
        public void onTransferPathStatusChanged(Object sourceObject, EventStatus status) {
            if (sourceObject instanceof TransferPathResult) {
                pathResult = (TransferPathResult) sourceObject;
            }
        }

    }

    /**
     * <p>
     * 实现 处理乘车方案结果的监听器，自己实现处理结果接口
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    public static class MyTransferSolutionEventListener extends TransferSolutionEventListener {
        private TransferSolutionResult lastResult;

        public MyTransferSolutionEventListener() {
            super();
        }

        public TransferSolutionResult getLastResult() {
            // 发送请求返回结果
            return lastResult;
        }

        @Override
        public void onTransferSolutionStatusChanged(Object sourceObject, EventStatus status) {

            if (sourceObject instanceof TransferSolutionResult) {

                lastResult = (TransferSolutionResult) sourceObject;

            }
        }

    }

    /**
     * <p>
     * 实现 处理站点查询结果的监听器，自己实现处理结果接口
     * </p>
     *
     * @author ${Author}
     * @version ${Version}
     */
    public static class MyStopQueryEventListener extends StopQueryEventListener {
        private StopQueryResult result;

        public MyStopQueryEventListener() {
            super();
        }

        public StopQueryResult getLastResult() {
            // 发送请求返回结果
            return result;
        }

        @Override
        public void onStopQueryStatusChanged(Object sourceObject, EventStatus status) {
            if (sourceObject instanceof StopQueryResult) {
                result = (StopQueryResult) sourceObject;
            }
        }
    }


}

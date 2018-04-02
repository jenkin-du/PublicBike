package com.android.djs.publicbike.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.bean.Address;
import com.android.djs.publicbike.bean.BikeStation;
import com.android.djs.publicbike.bean.BusStation;
import com.android.djs.publicbike.bean.GuildPath;
import com.android.djs.publicbike.bean.GuildSolution;
import com.android.djs.publicbike.constant.GuildFeature;
import com.android.djs.publicbike.constant.GuildMode;
import com.android.djs.publicbike.constant.PathType;
import com.android.djs.publicbike.constant.URL;
import com.android.djs.publicbike.service.AnalysisService;
import com.android.djs.publicbike.service.QueryService;
import com.android.djs.publicbike.service.TrafficTransferService;
import com.android.djs.publicbike.util.AnalysisTask;
import com.android.djs.publicbike.util.MyUtil;
import com.android.djs.publicbike.view.NavigationBar;
import com.supermap.android.maps.Point2D;
import com.supermap.android.trafficTransferAnalyst.TransferStopInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * 路径规划页面
 * Created by DJS on 2017/6/16.
 */
public class BusPathSearchActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private ListView mResultListView;
    private ListAdapter mStopAdapter;

    private ProgressDialog mProgress;

    //内容输入框，即查找的内容
    private EditText mConditionET;
    //显示站点名字的列表
    private ArrayList<String> mStationNames;
    //搜索的的公交车站点的集合
    private ArrayList<BusStation> mSearchedBusStations;
    //我的位置
    private Point2D location;

    private static final String TAG = "TAG";
    private boolean mAnalyzing;

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String tip = (String) msg.obj;
            mProgress.setMessage(tip);
        }
    };

    private ProgressThread progressThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.path_search);
        //初始化
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {

        Bundle bundle = getIntent().getBundleExtra("bundle");
        Address addr = bundle.getParcelable("location");

        assert addr != null;
        location = new Point2D();
        location.x = addr.getX();
        location.y = addr.getY();

        NavigationBar navigationBar = (NavigationBar) findViewById(R.id.path_search_navi_bar);
        navigationBar.registerListener(new NavigationBar.OnClickListener() {
            @Override
            public void onClickBack() {
                BusPathSearchActivity.this.finish();
            }

            @Override
            public void onClickImg() {

            }

            @Override
            public void onClickRightText() {

            }
        });
        mResultListView = (ListView) findViewById(R.id.path_search_result);
        mConditionET = (EditText) findViewById(R.id.path_search_condition);
        Button searchBtn = (Button) findViewById(R.id.path_search_btn);

        searchBtn.setOnClickListener(this);
        mResultListView.setOnItemClickListener(this);

        //初始化适配器
        mStationNames = new ArrayList<>();
        mStopAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStationNames);
        mSearchedBusStations = new ArrayList<>();

        mProgress = new ProgressDialog(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //搜索
            case R.id.path_search_btn:

                String keyword = mConditionET.getText().toString();
                if (!keyword.equals("")) {
                    //根据关键字搜索站公交站台
                    TransferStopInfo[] tfs = TrafficTransferService.queryStop(URL.TRANSFER, keyword);
                    //分析收搜到的结果
                    AnalyzeStops(tfs);
                } else {
                    Toast.makeText(BusPathSearchActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 分析查询到的站点信息
     *
     * @param tfs 站点信息
     */
    private void AnalyzeStops(TransferStopInfo[] tfs) {

        if (tfs != null) {
            //加载新的数据前，清除以前的数据
            mStationNames.clear();
            mSearchedBusStations.clear();

            for (TransferStopInfo tfi : tfs) {
                String stopId = tfi.stopID;
                String name = tfi.name;
                Point2D point = tfi.position;

                mStationNames.add(name);
                BusStation station = new BusStation();
                station.setId(Integer.parseInt(stopId));
                station.setName(name);
                Address addr = new Address();
                addr.setX(point.getX());
                addr.setY(point.getY());
                station.setAddress(addr);

                mSearchedBusStations.add(station);
            }
            if (mSearchedBusStations.size() > 0) {
                mResultListView.setAdapter(mStopAdapter);
            } else {
                Toast.makeText(BusPathSearchActivity.this, "没有找到车站，请重新输入查询条件", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = mStationNames.get(position);
        for (final BusStation station : mSearchedBusStations) {
            if (station.getName().equals(name)) {
                mProgress.show();
                progressThread = new ProgressThread("正在规划中");
                progressThread.start();
                //开启后台分析任务
                AnalysisTask task = new AnalysisTask(new Handler() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void handleMessage(Message msg) {
                        mProgress.dismiss();
                        mAnalyzing = false;
                        if (msg.what == 200) {

                            ArrayList<GuildSolution> solutions;
                            solutions = (ArrayList<GuildSolution>) msg.obj;
                            if (solutions != null && solutions.size() > 0) {


                                Bundle bundle = new Bundle();
                                bundle.putSerializable("solutions", solutions);
                                Intent intent = new Intent();
                                intent.setClass(BusPathSearchActivity.this, ShowSolutionActivity.class);
                                intent.putExtra("bundle", bundle);
                                startActivity(intent);
                            } else {
                                Toast.makeText(BusPathSearchActivity.this, "抱歉，没有找到换乘路线", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(BusPathSearchActivity.this, "抱歉，没有找到换乘路线", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
                    @Override
                    public Object doBackAnalysis() {
                        //分析混合交通模式
                        ArrayList<GuildSolution> guildSolutions = findSolutions(station);
                        //分析得到的解决方案
                        AnalyzeSolution(guildSolutions);
                        //分析骑行
                        GuildSolution bikeSolution = findBikeSolution(station);
                        if (guildSolutions == null) {
                            guildSolutions = new ArrayList<>();
                        }
                        if (bikeSolution != null) {
                            guildSolutions.add(bikeSolution);
                        }

                        //分析步行
                        GuildSolution walkSolution = findWalkSolution(station);
                        if (walkSolution != null) {
                            guildSolutions.add(walkSolution);
                        }

                        Log.i(TAG, "doBackAnalysis: size:"+guildSolutions.size());
                        return guildSolutions;
                    }


                };
                task.start();
            }
        }
    }


    /**
     * 分析只步行的方案
     */
    private GuildSolution findWalkSolution(BusStation station) {

        GuildSolution solution = new GuildSolution();
        solution.setMode(GuildMode.WALK);

        if (location != null && station.getPoint() != null) {
            Point2D startPoint = location;
            Point2D endPoint = station.getPoint();

            ArrayList<Point2D> geo = new ArrayList<>();
            geo.add(startPoint);
            geo.add(endPoint);

            ArrayList<ArrayList<Point2D>> pathSegs = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);
            if (pathSegs != null && pathSegs.size() > 0) {
                GuildPath path = new GuildPath();
                path.setPathType(PathType.WALK);
                path.setEndPoint(endPoint);
                path.setStartPoint(startPoint);
                path.setPathSegments(pathSegs);
                path.setPathName("步行");
                path.setStartPointName("此处");
                path.setEndPointName(station.getName());

                solution.addGuildPath(path);

                return solution;
            }
        }

        return null;

    }

    /**
     * 分析只骑行的方案
     */
    private GuildSolution findBikeSolution(BusStation station) {

        GuildSolution solution = new GuildSolution();
        solution.setMode(GuildMode.BIKE);
        ArrayList<GuildPath> paths = new ArrayList<>();
        solution.setPaths(paths);


        //公交车旁边的自行车站点
        ArrayList<BikeStation> nearBusBikeStations = QueryService.queryBikeStationByDistance(1500, station.getPoint());
        //如果公交车旁边有自行车站点
        if (nearBusBikeStations.size() > 0) {
            //找到公交车最近的自行车站点


            ArrayList<Point2D> points = new ArrayList<>();
            for (BikeStation s : nearBusBikeStations) {
                points.add(s.getPoint());
            }
            //构建返回的路径
            ArrayList<ArrayList<Point2D>> nearBusWalkPath = new ArrayList<>();
            int index1;
            index1 = AnalysisService.findClosestFacilities(URL.ANALYSIS, points, station.getPoint(), nearBusWalkPath);

            if (index1 == -1) {
                return null;
            }
            BikeStation busBike = nearBusBikeStations.get(index1);

            //再找我周围的自行车
            //找到我周围的自行车站点
            //找到离我最近的自行车
            ArrayList<BikeStation> nearBikeStations = QueryService.queryBikeStationByDistance(1500, location);
            //找到离我最近的自行车
            //构建点数据
            points.clear();
            for (BikeStation s : nearBikeStations) {
                points.add(s.getPoint());
            }
            //构建返回的路径
            ArrayList<ArrayList<Point2D>> nearBikeWalkPath = new ArrayList<>();
            index1 = AnalysisService.findClosestFacilities(URL.ANALYSIS, points, location, nearBikeWalkPath);
            if (index1 != -1) {
                //找到离我最近的自行车站点
                BikeStation nearBike = nearBikeStations.get(index1);

                //如果两个点是同一个点
                if (nearBike.equals(busBike)) {
                    return null;
                } else {

                    ArrayList<Point2D> geo = new ArrayList<>();
                    geo.add(location);
                    geo.add(busBike.getPoint());

                    Log.i(TAG, "findBikeSolution: GuildPath");
                    //加上最后一段步行路程
                    GuildPath secondWalkPath = new GuildPath();
                    secondWalkPath.setPathType(PathType.WALK);
                    secondWalkPath.setPathName("步行");
                    secondWalkPath.setStartPoint(busBike.getPoint());
                    secondWalkPath.setStartPointName(busBike.getStationName());
                    secondWalkPath.setEndPoint(station.getPoint());
                    secondWalkPath.setEndPointName(station.getName());
                    geo = new ArrayList<>();
                    geo.add(busBike.getPoint());
                    geo.add(station.getPoint());
                    secondWalkPath.setPathSegments(AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                    solution.getPaths().add(0, secondWalkPath);

                    //加上中间的骑行路程
                    GuildPath bikePath = new GuildPath();
                    bikePath.setPathType(PathType.BIKE);
                    bikePath.setPathName("骑行");
                    bikePath.setStartPoint(nearBike.getPoint());
                    bikePath.setStartPointName(nearBike.getStationName());
                    bikePath.setEndPoint(busBike.getPoint());
                    bikePath.setEndPointName(busBike.getStationName());
                    geo = new ArrayList<>();
                    geo.add(nearBike.getPoint());
                    geo.add(busBike.getPoint());
                    bikePath.setPathSegments(AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                    solution.getPaths().add(0, bikePath);

                    //加上第一段的步行路程
                    GuildPath firstWalkPath = new GuildPath();
                    firstWalkPath.setPathType(PathType.WALK);
                    firstWalkPath.setPathName("步行");
                    firstWalkPath.setStartPoint(location);
                    firstWalkPath.setStartPointName("此处");
                    firstWalkPath.setEndPoint(nearBike.getPoint());
                    firstWalkPath.setEndPointName(nearBike.getStationName());

                    geo = new ArrayList<>();
                    geo.add(location);
                    geo.add(nearBike.getPoint());
                    ArrayList<ArrayList<Point2D>> firstWalkPathSegs = (AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                    //纠正起点和终点错误的路径
                    Point2D p = nearBike.getPoint();
                    Point2D p2 = firstWalkPathSegs.get(0).get(0);
                    boolean near = isNear(p, p2);
                    if (near) {
                        Collections.reverse(firstWalkPathSegs);
                        for (int i = 0; i < firstWalkPathSegs.size(); i++) {
                            Collections.reverse(firstWalkPathSegs.get(i));
                        }
                                   }
                //把我的位置添加到路径的开头
                firstWalkPathSegs.get(0).add(0, location);
                firstWalkPath.setPathSegments(firstWalkPathSegs);

                solution.getPaths().add(0, firstWalkPath);

                return solution;

                }
            }

        }
        return null;
    }

    /**
     * 分析得到的解决方案
     *
     * @param solutions 解决方案
     */
    private void AnalyzeSolution(ArrayList<GuildSolution> solutions) {

        //第一种情况
        //去除个别没有点位的站点
        for (GuildSolution solution : solutions) {
            for (GuildPath path : solution.getPaths()) {
                Point2D startPoint = path.getStartPoint();
                Point2D endPoint = path.getEndPoint();
                PathType type = path.getPathType();
                switch (type) {
                    case BUS:
                        if (startPoint == null || (int) startPoint.x == 0 || (int) startPoint.y > 0) {
                            startPoint = QueryService.getBusStopLocByName(path.getStartPointName());
                            path.setStartPoint(startPoint);
                        }
                        if (endPoint == null || (int) endPoint.x == 0 || (int) endPoint.y == 0) {
                            endPoint = QueryService.getBusStopLocByName(path.getEndPointName());
                            path.setEndPoint(endPoint);
                        }
                        break;

                    case BIKE:
                        if (startPoint == null || (int) startPoint.x == 0 || (int) startPoint.y > 0) {
                            startPoint = QueryService.getBikeStopLocByName(path.getStartPointName());
                            path.setStartPoint(startPoint);
                        }
                        if (endPoint == null || (int) endPoint.x == 0 || (int) endPoint.y == 0) {
                            endPoint = QueryService.getBikeStopLocByName(path.getEndPointName());
                            path.setEndPoint(endPoint);
                        }
                        break;
                }
            }
        }


        //第二种情况
        //相同的解决方案
        HashSet<GuildSolution> set = new HashSet<>(solutions);
        solutions.clear();
        solutions.addAll(new ArrayList<>(set));


        //第三种情况
        //如果最后上的公交一样，则时间最短的保留
        class M {
            String key;
            double value;
        }

        M[] ms = new M[solutions.size()];
        for (int i = 0; i < solutions.size(); i++) {
            GuildSolution solution = solutions.get(i);

            if (solution.getPaths().size() - 2 > 0) {
                GuildPath path = solution.getGuildPath(solution.getPaths().size() - 1);
                if (path.getPathType() == PathType.BUS) {
                    M m = new M();
                    m.key = path.getPathName();
                    m.value = solution.getTime();

                    ms[i] = m;
                } else {
                    ms[i] = null;
                }
            } else {
                ms[i] = null;
            }
        }

        M m1, m2;
        if (ms.length >= 2) {
            for (int i = 0; i < ms.length; i++) {
                m1 = ms[i];
                if (m1 == null) {
                    continue;
                }
                for (int j = i + 1; j < ms.length; j++) {
                    m2 = ms[j];
                    if (m2 == null) {
                        continue;
                    }
                    if (m1.key.equals(m2.key)) {
                        if (m1.value <= m2.value) {
                            ms[j] = null;
                        } else {
                            ms[i] = null;
                            break;
                        }
                    }
                }
            }
        }

        for (int k = 0; k < ms.length; k++) {
            if (ms[k] == null) {
                solutions.set(k, null);
            }
        }

        for (int k = 0; k < solutions.size(); ) {
            if (solutions.get(k) == null) {
                solutions.remove(k);
                continue;
            }
            k++;
        }

        for (GuildSolution solution : solutions) {
            Log.i(TAG, "AnalyzeSolution: solution:" + solution);
        }

        //分析出换乘特点
        //如果换乘路线就两种以下情况，就不用分析特点了
        if (solutions.size() > 2) {

            int i, j = 0;

            //第一:时间最少
            double time = solutions.get(0).getTime();
            for (i = 1; i < solutions.size(); i++) {
                if (solutions.get(i).getTime() < time) {
                    time = solutions.get(i).getTime();
                    j = i;
                }
            }
            solutions.get(j).setFeature(GuildFeature.MIN_TIME);

            //第二:步行最少
            double distance = solutions.get(0).getWalkDistance();
            for (i = 1; i < solutions.size(); i++) {
                if (solutions.get(i).getWalkDistance() < distance) {
                    distance = solutions.get(i).getWalkDistance();
                    j = i;
                }
            }
            solutions.get(j).setFeature(GuildFeature.MIN_WALK);


            //第三:换乘最少
            int count = solutions.get(0).getTransferCount();
            for (i = 1; i < solutions.size(); i++) {
                if (solutions.get(i).getTransferCount() < count) {
                    count = solutions.get(i).getTransferCount();
                    j = i;
                }
            }
            solutions.get(j).setFeature(GuildFeature.MIN_TRANSFER);
        }

        //加上换乘模型
        for (GuildSolution solution : solutions) {
            solution.setMode(GuildMode.MIXTURE);
        }


    }


    /**
     * 根据终点站找到换乘路线的解决方案
     *
     * @param endBusStation 站点车站
     */
    private ArrayList<GuildSolution> findSolutions(BusStation endBusStation) {

        if (location != null) {
            //获得终点站的公交ID号
            int endBusId = endBusStation.getId();
            //找到离我周围一定范围内的公交车站点
            ArrayList<BusStation> nearBusStations = QueryService.queryBusStationByDistance(1500, location);
            //新建换乘方案
            ArrayList<GuildSolution> guildSolutions = new ArrayList<>();
            /**
             * 进行多次迭代，找到可能的换乘方案，然后去掉非优化的换乘路线
             */
            int i = 2;
            while (i > 0) {
                //查找离我最近的公交车站点
                //先构建点数据
                ArrayList<Point2D> nearPoints = new ArrayList<>();
                for (BusStation station : nearBusStations) {
                    nearPoints.add(station.getPoint());
                }
                //找到最近这是点返回的路径
                ArrayList<ArrayList<Point2D>> firstPathSegs = new ArrayList<>();
                int index = AnalysisService.findClosestFacilities(URL.ANALYSIS, nearPoints, location, firstPathSegs);
                //找到找到离我最近的公交站点

                if (index != -1) {
                    BusStation startBusStation = nearBusStations.get(index);
                    // 如果离我最近的公交车和我想要去的公交车是同一个地方，则不用公交分析，直接走过去或骑车过去
                    if (startBusStation.getId() == endBusStation.getId()) {
                        GuildSolution solution = new GuildSolution();
                        ArrayList<GuildPath> paths = new ArrayList<>();
                        solution.setPaths(paths);
                        addWalkOrBikePathToEnd(endBusStation, location, solution);
                        guildSolutions.add(solution);

                        break;
                    } else {
                        //否则就换乘分析
                        int startBusId = startBusStation.getId();
                        //两公交站点之间的换乘方案
                        ArrayList<GuildSolution> solutions = TrafficTransferService.transferAnalyst(URL.TRANSFER, startBusId, endBusId);
                        if (solutions == null || solutions.size() == 0) {
                            //由于换乘算法可能具有方向性，所以需改变起始位置重新试一遍
                            solutions = TrafficTransferService.transferAnalyst(URL.TRANSFER, endBusId, startBusId);
                            if (solutions != null) {
                                for (GuildSolution solution : solutions) {
                                    solution.reverse();
                                }
                            }
                            //如果还没找到解决方案,有可能是在短距离内无法找到一条公交线路上的导航信息，需自己分析
                            if (solutions == null || solutions.size() == 0) {
                                GuildSolution solution = findOneBusLineSolution(endBusStation, startBusStation);
                                if (solution != null) {
                                    if (solutions == null) {
                                        solutions = new ArrayList<>();
                                    }
                                    solutions.add(solution);
                                }
                            }
                        }

                        //如果找到了换乘方案，就在该方案中加入 人到公交站台的换乘方案(人是骑车到公交站台还是找到公交站台)
                        if (solutions != null && solutions.size() > 0) {

                            addWalkOrBikePath(solutions, startBusStation, location);
                            //将本次迭代得到的换乘方案加入到总的解决方案中
                            guildSolutions.addAll(solutions);
                            //去除本次找到的自行车，进行下一次迭代
                            nearBusStations.remove(startBusStation);
                        }
                    }

                }
                i--;
            }
            return guildSolutions;
        } else {
            Toast.makeText(BusPathSearchActivity.this, "定位有问题，请重试！", Toast.LENGTH_SHORT).show();
        }

        return null;
    }


    /**
     * 在已有的公交换乘方案中加上人到公交车的换乘方案
     *
     * @param solutions       已有的公交车之间的换乘方案
     * @param startBusStation 公交车的开始站点
     * @param location        我的位置
     */

    private void addWalkOrBikePath(ArrayList<GuildSolution> solutions, BusStation startBusStation, Point2D location) {

        //公交车旁边的自行车站点
        ArrayList<BikeStation> nearBusBikeStations = QueryService.queryBikeStationByDistance(300, startBusStation.getPoint());
        //如果公交车旁边有自行车站点
        if (nearBusBikeStations.size() > 0) {
            //找到公交车最近的自行车站点
            BikeStation busBike = nearBusBikeStations.get(0);
            for (GuildSolution solution : solutions) {
                ArrayList<GuildPath> paths = solution.getPaths();
                if (paths != null && paths.size() > 0) {

                    GuildPath path = paths.get(0);
                    switch (path.getPathType()) {
                        //如果在公交换乘的时候，是公交线路，加上骑行的线路
                        case BUS:
                            addBikePath(location, busBike, startBusStation, solution);
                            break;
                        case BUS_WALK:
                            //如果在公交换乘的时候，第一段路需要走过去，则分析在走过去的那个公交站台是旁边是否有自行车，
                            // 如果有，并且我的位置附近有自行车，则骑车过去
                            GuildPath path2 = paths.get(1);
                            if (path2 != null) {

                                Point2D point = path2.getStartPoint();
                                if (point == null || !(point.x > 0) || !(point.y > 0)) {
                                    point = QueryService.getBusStopLocByName(path2.getStartPointName());
                                }
                                ArrayList<BikeStation> nearBusBikeStations2 = QueryService.queryBikeStationByDistance(200, point);
                                //旁边有车，则构建我的位置到该自行车站点的位置
                                if (nearBusBikeStations2.size() > 0) {
                                    //从我这里骑车到公交车旁边的自行车
                                    //删除第一条走的路径，改为骑行
                                    solution.getPaths().remove(0);
                                    addBikePath(location, nearBusBikeStations2.get(0), startBusStation, solution);
                                } else {
                                    //如果没有则先骑行后再走过去
                                    BusStation secondBusStation = new BusStation();
                                    secondBusStation.setName(path2.getStartPointName());

                                    Address addr = new Address();
                                    addr.setX(point.getX());
                                    addr.setY(point.getY());
                                    secondBusStation.setAddress(addr);

                                    //删除第一条走的路径，改为骑行
                                    solution.getPaths().remove(0);
                                    addBikeWalkPath(location, busBike, secondBusStation, solution);
                                }
                            }
                            break;

                    }
                }
            }
        } else {
            //如果公交车旁边没有自行车站点，就直接走过去
            for (GuildSolution solution : solutions) {
                ArrayList<GuildPath> paths = solution.getPaths();
                if (paths != null && paths.size() > 0) {

                    GuildPath path = paths.get(0);
                    switch (path.getPathType()) {
                        //如果在公交换乘的时候，是公交线路，则直接加上走路的路径
                        case BUS:
                            addWalkPath(startBusStation, location, solution);
                            break;
                        case BUS_WALK:
                            //如果在公交换乘的时候，第一段路需要走过去，则分析在走过去的那个公交站台是旁边是否有自行车，
                            // 如果有，并且我的位置附近有自行车，则汽车过去
                            //否则，走过去
                            GuildPath path2 = paths.get(1);
                            if (path2 != null) {

                                Point2D point = path2.getStartPoint();
                                ArrayList<BikeStation> nearBusBikeStations2 = QueryService.queryBikeStationByDistance(200, point);
                                //旁边有车，则构建我的位置到该自行车站点的位置
                                if (nearBusBikeStations2.size() > 0) {

                                    BusStation secondBusStation = new BusStation();
                                    secondBusStation.setName(path2.getStartPointName());

                                    Address addr = new Address();
                                    addr.setX(point.getX());
                                    addr.setY(point.getY());
                                    secondBusStation.setAddress(addr);
                                    //加上骑行路线
                                    addBikePath(location, nearBusBikeStations2.get(0), secondBusStation, solution);
                                } else {
                                    //如果没有则走过去
                                    addWalkPath(startBusStation, location, solution);
                                }
                            }

                            break;
                    }
                }
            }
        }

    }

    /**
     * 从我附近的自行车站点骑到公交车旁边的自行车站点
     *
     * @param location 我的位置
     * @param busBike  离公交车站点旁边的自行车站点
     * @param solution 解决方案
     */
    private void addBikePath(Point2D location, BikeStation busBike, BusStation startBusStation, GuildSolution solution) {

        //找到我周围的自行车站点
        //找到离我最近的自行车
        ArrayList<BikeStation> nearBikeStations = QueryService.queryBikeStationByDistance(1500, location);
        //找到离我最近的自行车
        //构建点数据
        ArrayList<Point2D> points = new ArrayList<>();
        for (BikeStation s : nearBikeStations) {
            points.add(s.getPoint());
        }
        //构建返回的路径
        ArrayList<ArrayList<Point2D>> nearBikeWalkPath = new ArrayList<>();
        int index1 = AnalysisService.findClosestFacilities(URL.ANALYSIS, points, location, nearBikeWalkPath);
        if (index1 != -1) {
            //找到离我最近的自行车站点
            BikeStation nearBike = nearBikeStations.get(index1);

            //如果两个点是同一个点
            if (nearBike.getId().equals(busBike.getId())) {
                //则直接走到公交车站点，表明我里公交车应该很近，就没有必要器自行车
                addWalkPath(startBusStation, location, solution);
            } else {

                //算出我到最近自行车的距离
                double d1 = MyUtil.calculatePathDistance(nearBikeWalkPath);
                //算出我到公交站台旁边的直行车站点的之间的最短距离
                ArrayList<Point2D> geo = new ArrayList<>();
                geo.add(location);
                geo.add(busBike.getPoint());
                ArrayList<ArrayList<Point2D>> busBikeWalkPath = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);
                double d2 = MyUtil.calculatePathDistance(busBikeWalkPath);
                //离我最近的自行车和我到公交哦站台的旁边的自行车距离一样，或更长，直接走到公交站台
                if (d2 <= d1) {
                    //则直接走到公交车站点，表明我里公交车应该很近，就没有必要器自行车
                    addWalkPath(startBusStation, location, solution);
                }

                if (d2 < 300 && Math.abs(d2 - d1) < 150) {
                    addWalkPath(startBusStation, location, solution);
                }
                //此种情况下，需要骑行过去
                if ((d2 < 300 && Math.abs(d2 - d1) > 150) || d2 > 300) {

                    //加上最后一段步行路程
                    GuildPath secondWalkPath = new GuildPath();
                    secondWalkPath.setPathType(PathType.WALK);
                    secondWalkPath.setPathName("步行");
                    secondWalkPath.setStartPoint(busBike.getPoint());
                    secondWalkPath.setStartPointName(busBike.getStationName());
                    secondWalkPath.setEndPoint(startBusStation.getPoint());
                    secondWalkPath.setEndPointName(startBusStation.getName());
                    geo = new ArrayList<>();
                    geo.add(busBike.getPoint());
                    geo.add(startBusStation.getPoint());
                    secondWalkPath.setPathSegments(AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                    solution.getPaths().add(0, secondWalkPath);

                    //加上中间的骑行路程
                    GuildPath bikePath = new GuildPath();
                    bikePath.setPathType(PathType.BIKE);
                    bikePath.setPathName("骑行");
                    bikePath.setStartPoint(nearBike.getPoint());
                    bikePath.setStartPointName(nearBike.getStationName());
                    bikePath.setEndPoint(busBike.getPoint());
                    bikePath.setEndPointName(busBike.getStationName());
                    geo = new ArrayList<>();
                    geo.add(nearBike.getPoint());
                    geo.add(busBike.getPoint());
                    bikePath.setPathSegments(AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                    solution.getPaths().add(0, bikePath);

                    //加上第一段的步行路程
                    GuildPath firstWalkPath = new GuildPath();
                    firstWalkPath.setPathType(PathType.WALK);
                    firstWalkPath.setPathName("步行");
                    firstWalkPath.setStartPoint(location);
                    firstWalkPath.setStartPointName("此处");
                    firstWalkPath.setEndPoint(nearBike.getPoint());
                    firstWalkPath.setEndPointName(nearBike.getStationName());

                    geo = new ArrayList<>();
                    geo.add(location);
                    geo.add(nearBike.getPoint());
                    ArrayList<ArrayList<Point2D>> firstWalkPathSegs = (AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                    //纠正起点和终点错误的路径
                    Point2D p = nearBike.getPoint();
                    Point2D p2 = firstWalkPathSegs.get(0).get(0);
                    boolean near = isNear(p, p2);
                    if (near) {
                        Collections.reverse(firstWalkPathSegs);
                        for (int i = 0; i < firstWalkPathSegs.size(); i++) {
                            Collections.reverse(firstWalkPathSegs.get(i));
                        }
                    }
                    //把我的位置添加到路径的开头
                    firstWalkPathSegs.get(0).add(0, location);
                    firstWalkPath.setPathSegments(firstWalkPathSegs);

                    solution.getPaths().add(0, firstWalkPath);

                }
            }
        }
    }

    /**
     * 公交换乘的时候，第一段要走过去，但在走过的上公交车的地方没有自行车站点，
     * 但在第二个公交车旁边又有公交车站点，
     * 则在我的位置先骑行到第一个公交站点，在走到第二个公交站点
     */
    private void addBikeWalkPath(Point2D location, BikeStation busBike, BusStation startBusStation, GuildSolution solution) {

        //找到我周围的自行车站点
        //找到离我最近的自行车
        ArrayList<BikeStation> nearBikeStations = QueryService.queryBikeStationByDistance(1500, location);
        //找到离我最近的自行车
        //构建点数据
        ArrayList<Point2D> points = new ArrayList<>();
        for (BikeStation s : nearBikeStations) {
            points.add(s.getPoint());
        }
        //构建返回的路径
        ArrayList<ArrayList<Point2D>> nearBikeWalkPath = new ArrayList<>();
        int index1 = AnalysisService.findClosestFacilities(URL.ANALYSIS, points, location, nearBikeWalkPath);
        if (index1 != -1) {
            //找到离我最近的自行车站点
            BikeStation nearBike = nearBikeStations.get(index1);


            if (busBike.equals(nearBike)) {
                addWalkPath(startBusStation, location, solution);

            } else {

                //加上最后一段步行路程
                GuildPath secondWalkPath = new GuildPath();
                secondWalkPath.setPathType(PathType.WALK);
                secondWalkPath.setPathName("步行");
                secondWalkPath.setStartPoint(busBike.getPoint());
                secondWalkPath.setStartPointName(busBike.getStationName());

                Point2D startBusPoint = QueryService.getBusStopLocByName(startBusStation.getName());
                secondWalkPath.setEndPoint(startBusPoint);
                secondWalkPath.setEndPointName(startBusStation.getName());

                ArrayList<Point2D> geo = new ArrayList<>();
                geo.add(busBike.getPoint());
                geo.add(startBusPoint);
                ArrayList<ArrayList<Point2D>> pss = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);
                if (pss.size() > 0) {
                    secondWalkPath.setPathSegments(pss);
                    solution.getPaths().add(0, secondWalkPath);

                    //加上中间的骑行路程
                    GuildPath bikePath = new GuildPath();
                    bikePath.setPathType(PathType.BIKE);
                    bikePath.setPathName("骑行");
                    bikePath.setStartPoint(nearBike.getPoint());
                    bikePath.setStartPointName(nearBike.getStationName());
                    bikePath.setEndPoint(busBike.getPoint());
                    bikePath.setEndPointName(busBike.getStationName());
                    geo = new ArrayList<>();
                    geo.add(nearBike.getPoint());
                    geo.add(busBike.getPoint());
                    pss = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);
                    if (pss.size() > 0) {
                        bikePath.setPathSegments(pss);
                        solution.getPaths().add(0, bikePath);

                        //加上第一段的步行路程
                        GuildPath firstWalkPath = new GuildPath();
                        firstWalkPath.setPathType(PathType.WALK);
                        firstWalkPath.setPathName("步行");
                        firstWalkPath.setStartPoint(location);
                        firstWalkPath.setStartPointName("此处");
                        firstWalkPath.setEndPoint(nearBike.getPoint());
                        firstWalkPath.setEndPointName(nearBike.getStationName());


                        //纠正起点和终点错误的路径
                        Point2D p = nearBike.getPoint();
                        Point2D p2 = nearBikeWalkPath.get(0).get(0);
                        boolean near = isNear(p, p2);
                        if (near) {
                            Collections.reverse(nearBikeWalkPath);
                            for (int i = 0; i < nearBikeWalkPath.size(); i++) {
                                Collections.reverse(nearBikeWalkPath.get(i));
                            }
                        }
                        //把我的位置添加到路径的开头
                        nearBikeWalkPath.get(0).add(0, location);
                        firstWalkPath.setPathSegments(nearBikeWalkPath);

                        solution.getPaths().add(0, firstWalkPath);

                    }
                }
            }
        }

    }

    /**
     * 在乘公交车前加上走路的路径
     */
    private void addWalkPath(BusStation startBusStation, Point2D location, GuildSolution solution) {

        GuildPath walkPath = new GuildPath();
        walkPath.setPathType(PathType.WALK);
        walkPath.setStartPointName("此处");
        walkPath.setStartPoint(location);
        walkPath.setEndPointName(startBusStation.getName());
        walkPath.setEndPoint(startBusStation.getPoint());

        ArrayList<Point2D> geo = new ArrayList<>();
        geo.add(location);
        geo.add(startBusStation.getPoint());
        ArrayList<ArrayList<Point2D>> pathSegments = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);

        //纠正起点和终点错误的路径
        Point2D p = startBusStation.getPoint();
        Point2D p2 = pathSegments.get(0).get(0);
        boolean near = isNear(p, p2);
        if (near) {
            Collections.reverse(pathSegments);
            for (int i = 0; i < pathSegments.size(); i++) {
                Collections.reverse(pathSegments.get(i));
            }
        }
        //把我的位置添加到路径的开头
        pathSegments.get(0).add(0, location);
        walkPath.setPathSegments(pathSegments);

        if (solution.getPaths() == null) {
            solution.setPaths(new ArrayList<GuildPath>());
        }
        solution.getPaths().add(0, walkPath);
    }

    /**
     * 分析一条公交线路上的换乘信息
     */
    private GuildSolution findOneBusLineSolution(BusStation startBusStation, BusStation endBusStation) {


        GuildSolution solution = null;

        int endBusId = endBusStation.getId();
        int startBusId = startBusStation.getId();

        Point2D startPoint = startBusStation.getPoint();
        Point2D endPoint = endBusStation.getPoint();

        ArrayList<Point2D> point2Ds = new ArrayList<>();
        point2Ds.add(startPoint);
        point2Ds.add(endPoint);
        ArrayList<ArrayList<Point2D>> pathSegs = AnalysisService.findOptimalPath(URL.ANALYSIS_BUS, point2Ds);
        if (pathSegs != null) {

            GuildPath path = new GuildPath();
            path.setPathType(PathType.BUS);
            path.setStartPointName(startBusStation.getName());
            path.setEndPointName(endBusStation.getName());
            path.setStartPoint(startPoint);
            path.setEndPoint(endPoint);
            path.setPathSegments(pathSegs);

            ArrayList<Integer> lineIds1 = QueryService.queryBusLineId(startBusId);
            ArrayList<Integer> lineIds2 = QueryService.queryBusLineId(endBusId);

            ArrayList<Integer> intersect = MyUtil.getIntersection(lineIds1, lineIds2);
            if (intersect.size() > 0) {
                String name = QueryService.queryBusLineName(intersect.get(0));
                if (name != null) {
                    path.setId(intersect.get(0));
                    path.setPathName(name);

                    solution = new GuildSolution();
                    solution.addGuildPath(path);
                }
            }
        }
        return solution;
    }

    /**
     * 如果终点足够近，就直接走过去或骑车过去，就不用公交换乘
     *
     * @param endBusStation 终点站
     * @param location      我的位置
     * @param solution      解决方案
     */
    private void addWalkOrBikePathToEnd(BusStation endBusStation, Point2D location, GuildSolution solution) {

        //公交车旁边的自行车站点
        ArrayList<BikeStation> nearBusBikeStations = QueryService.queryBikeStationByDistance(300, endBusStation.getPoint());
        //如果公交车旁边有自行车站点
        if (nearBusBikeStations.size() > 0) {
            //找到公交车最近的自行车站点
            BikeStation busBike = nearBusBikeStations.get(0);

            //再找我周围的自行车
            //找到我周围的自行车站点
            //找到离我最近的自行车
            ArrayList<BikeStation> nearBikeStations = QueryService.queryBikeStationByDistance(1500, location);
            //找到离我最近的自行车
            //构建点数据
            ArrayList<Point2D> points = new ArrayList<>();
            for (BikeStation s : nearBikeStations) {
                points.add(s.getPoint());
            }
            //构建返回的路径
            ArrayList<ArrayList<Point2D>> nearBikeWalkPath = new ArrayList<>();
            int index1 = AnalysisService.findClosestFacilities(URL.ANALYSIS, points, location, nearBikeWalkPath);
            if (index1 != -1) {
                //找到离我最近的自行车站点
                BikeStation nearBike = nearBikeStations.get(index1);

                //如果两个点是同一个点
                if (nearBike.equals(busBike)) {
                    //则直接走到公交车站点，表明我里公交车应该很近，就没有必要器自行车
                    addWalkPath(endBusStation, location, solution);
                } else {

                    //算出我到最近自行车的距离
                    double d1 = MyUtil.calculatePathDistance(nearBikeWalkPath);
                    //算出我到公交站台旁边的直行车站点的之间的最短距离
                    ArrayList<Point2D> geo = new ArrayList<>();
                    geo.add(location);
                    geo.add(busBike.getPoint());
                    ArrayList<ArrayList<Point2D>> busBikeWalkPath = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);
                    double d2 = MyUtil.calculatePathDistance(busBikeWalkPath);
                    //离我最近的自行车和我到公交哦站台的旁边的自行车距离一样，或更长，直接走到公交站台
                    if (d2 <= d1) {
                        //则直接走到公交车站点，表明我里公交车应该很近，就没有必要器自行车
                        addWalkPath(endBusStation, location, solution);
                    }

                    if (d2 < 300 && Math.abs(d2 - d1) < 150) {
                        addWalkPath(endBusStation, location, solution);
                    }
                    //此种情况下，需要骑行过去
                    if ((d2 < 300 && Math.abs(d2 - d1) > 150) || d2 > 300) {

                        //加上最后一段步行路程
                        GuildPath secondWalkPath = new GuildPath();
                        secondWalkPath.setPathType(PathType.WALK);
                        secondWalkPath.setPathName("步行");
                        secondWalkPath.setStartPoint(busBike.getPoint());
                        secondWalkPath.setStartPointName(busBike.getStationName());
                        secondWalkPath.setEndPoint(endBusStation.getPoint());
                        secondWalkPath.setEndPointName(endBusStation.getName());
                        geo = new ArrayList<>();
                        geo.add(busBike.getPoint());
                        geo.add(endBusStation.getPoint());
                        ArrayList<ArrayList<Point2D>> pointLists = AnalysisService.findOptimalPath(URL.ANALYSIS, geo);
                        if (pointLists != null) {
                            secondWalkPath.setPathSegments(pointLists);
                        }

                        solution.getPaths().add(0, secondWalkPath);

                        //加上中间的骑行路程
                        GuildPath bikePath = new GuildPath();
                        bikePath.setPathType(PathType.BIKE);
                        bikePath.setPathName("骑行");
                        bikePath.setStartPoint(nearBike.getPoint());
                        bikePath.setStartPointName(nearBike.getStationName());
                        bikePath.setEndPoint(busBike.getPoint());
                        bikePath.setEndPointName(busBike.getStationName());
                        geo = new ArrayList<>();
                        geo.add(nearBike.getPoint());
                        geo.add(busBike.getPoint());
                        bikePath.setPathSegments(AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                        solution.getPaths().add(0, bikePath);

                        //加上第一段的步行路程
                        GuildPath firstWalkPath = new GuildPath();
                        firstWalkPath.setPathType(PathType.WALK);
                        firstWalkPath.setPathName("步行");
                        firstWalkPath.setStartPoint(location);
                        firstWalkPath.setStartPointName("此处");
                        firstWalkPath.setEndPoint(nearBike.getPoint());
                        firstWalkPath.setEndPointName(nearBike.getStationName());

                        geo = new ArrayList<>();
                        geo.add(location);
                        geo.add(nearBike.getPoint());
                        ArrayList<ArrayList<Point2D>> firstWalkPathSegs = (AnalysisService.findOptimalPath(URL.ANALYSIS, geo));

                        //纠正起点和终点错误的路径
                        Point2D p = nearBike.getPoint();
                        Point2D p2 = firstWalkPathSegs.get(0).get(0);
                        boolean near = isNear(p, p2);
                        if (near) {
                            Collections.reverse(firstWalkPathSegs);
                            for (int i = 0; i < firstWalkPathSegs.size(); i++) {
                                Collections.reverse(firstWalkPathSegs.get(i));
                            }
                        }
                        //把我的位置添加到路径的开头
                        firstWalkPathSegs.get(0).add(0, location);
                        firstWalkPath.setPathSegments(firstWalkPathSegs);

                        solution.getPaths().add(0, firstWalkPath);

                    }
                }
            }
        }
    }

    /**
     * 判断两个点是否足够近
     */
    private boolean isNear(Point2D p1, Point2D p2) {

        double x1 = p1.x;
        double y1 = p1.y;

        double x2 = p2.x;
        double y2 = p2.y;

        double dx = x2 - x1;
        double dy = y2 - y1;

        double d = Math.sqrt(dx * dx + dy * dy);

        return d <= 50;
    }

    /***
     * 显示tip的类
     */
    class ProgressThread extends Thread {

        String tip;

        public ProgressThread(String tip) {
            this.tip = tip;
        }

        @Override
        public void run() {
            mAnalyzing = true;
            int i = 0;
            String t = tip;
            while (mAnalyzing) {

                if (i % 6 == 0) {
                    t = tip;
                    i = 0;
                }
                t += ".";
                Message msg = new Message();
                msg.obj = t;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
    }

}

package com.android.djs.publicbike.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.application.App;
import com.android.djs.publicbike.bean.Address;
import com.android.djs.publicbike.bean.BikeStation;
import com.android.djs.publicbike.bean.BusStation;
import com.android.djs.publicbike.constant.URL;
import com.android.djs.publicbike.service.QueryService;
import com.android.djs.publicbike.service.TrafficTransferService;
import com.android.djs.publicbike.util.AnalysisTask;
import com.android.djs.publicbike.util.MyTextWatcher;
import com.android.djs.publicbike.view.NavigationBar;
import com.supermap.android.maps.Point2D;
import com.supermap.android.trafficTransferAnalyst.TransferStopInfo;

import java.util.ArrayList;

/**
 * 搜索界面
 * Created by DJS on 2017/5/30.
 */
public class SearchActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private NavigationBar mBar;
    private EditText mSearchContentET;
    private ListView mResultListView;//结果列表
    private ImageView mDeleteImg;

    private ProgressDialog mProgress;

    private ListAdapter mStopAdapter;
    //显示站点名字的列表
    private ArrayList<String> mStationNames;
    //搜索的的公交车站点的集合
    private ArrayList<BusStation> mSearchedBusStations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        //初始化
        init();
        //设置监听器
        setListener();
    }

    /**
     * 初始化各种view
     */
    private void init() {

        App.mActivityList.add(this);
        mBar = (NavigationBar) findViewById(R.id.search_navi_bar);

        Button searchBtn = (Button) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(this);

        mSearchContentET = (EditText) findViewById(R.id.search_condition);
        mResultListView = (ListView) findViewById(R.id.search_result);
        mDeleteImg = (ImageView) findViewById(R.id.search_delete);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("正在搜索中…………");


        //初始化适配器
        mStationNames = new ArrayList<>();
        mStopAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStationNames);
        mSearchedBusStations = new ArrayList<>();

    }

    /**
     * 给各种view添加监听器
     */
    private void setListener() {

        mBar.registerListener(new NavigationBar.OnClickListener() {
            @Override
            public void onClickBack() {
                SearchActivity.this.finish();
            }

            @Override
            public void onClickImg() {

            }

            @Override
            public void onClickRightText() {

            }
        });
        mResultListView.setOnItemClickListener(this);
        mDeleteImg.setOnClickListener(this);

        mSearchContentET.addTextChangedListener(new MyTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mDeleteImg.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //搜索
            case R.id.search_btn:
                String content = mSearchContentET.getText().toString();

                if (!content.equals("")) {
                    mProgress.show();
                    doSearch(content);
                } else {
                    showToast("内容不能为空");
                }


                break;
            //删除
            case R.id.search_delete:
                mSearchContentET.setText("");
                mDeleteImg.setVisibility(View.INVISIBLE);
                break;
        }

    }

    /**
     * 根据条件搜索
     *
     * @param condition 条件
     */
    private void doSearch(final String condition) {

        AnalysisTask task = new AnalysisTask(new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 200) {
                    mProgress.dismiss();
                    TransferStopInfo[] stopInfos = (TransferStopInfo[]) msg.obj;
                    AnalyzeStops(stopInfos);
                }

            }
        }) {
            @Override
            public Object doBackAnalysis() {
                //先根据搜索条件进行模糊匹配
                return TrafficTransferService.queryStop(URL.TRANSFER, condition);
            }
        };
        task.start();

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
                Toast.makeText(SearchActivity.this, "请重新输入查询条件", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 显示提示说明
     */
    private void showToast(String text) {
        Toast.makeText(SearchActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String name = mStationNames.get(position);
        for (final BusStation station : mSearchedBusStations) {
            if (station.getName().equals(name)) {
                mProgress.show();
                Point2D point = station.getPoint();
                searchNearStops(point);
            }
        }
    }

    /**
     * 搜索该位置周围的车站点
     *
     * @param point 该位置
     */
    private void searchNearStops(final Point2D point) {

        AnalysisTask task = new AnalysisTask(new Handler() {

            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 200) {
                    mProgress.dismiss();
                    ArrayList<BikeStation> stations = (ArrayList<BikeStation>) msg.obj;
                    if (stations != null && stations.size() > 0) {
                        showDataOnMap(stations, point);
                    }else{
                        showToast("抱歉！未在该位置周边搜索到自行车");
                    }
                }else {
                    showToast("抱歉！未在该位置周边搜索到自行车");
                }
            }
        }) {
            @Override
            public Object doBackAnalysis() {

                return QueryService.queryBikeStationByDistance(1000, point);
            }
        };
        task.start();
    }

    /**
     * 根据查到的结果显示在地图上
     */
    private void showDataOnMap(ArrayList<BikeStation> stations, Point2D point) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("nearBikeStations", stations);
        bundle.putSerializable("center", point);

        Intent intent = getIntent();
        intent.putExtra("bundle", bundle);
        setResult(MapActivity.RESULT_CODE, intent);
        SearchActivity.this.finish();
    }
}

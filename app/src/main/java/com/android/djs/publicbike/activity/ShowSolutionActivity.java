package com.android.djs.publicbike.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.adapter.PathSolutionAdapter;
import com.android.djs.publicbike.bean.GuildSolution;
import com.android.djs.publicbike.view.NavigationBar;

import java.util.ArrayList;

import static com.android.djs.publicbike.R.id.show_solution_item;

/**
 * 显示解决方案
 * Created by DJS on 2017/6/20.
 */
public class ShowSolutionActivity extends Activity {

    private ListView mSolutionLV;
    private PathSolutionAdapter mPathSolutionAdapter;
    private ArrayList<GuildSolution> mSolutions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_solution);

        initView();
    }

    @SuppressWarnings("unchecked")
    private void initView() {


        Bundle bundle = getIntent().getBundleExtra("bundle");
        mSolutions = (ArrayList<GuildSolution>) bundle.getSerializable("solutions");

        NavigationBar bar = (NavigationBar) findViewById(R.id.show_solution_nav_bar);
        bar.registerListener(new NavigationBar.OnClickListener() {
            @Override
            public void onClickBack() {
                ShowSolutionActivity.this.finish();
            }

            @Override
            public void onClickImg() {

            }

            @Override
            public void onClickRightText() {

            }
        });
        mSolutionLV = (ListView) findViewById(show_solution_item);

        mPathSolutionAdapter = new PathSolutionAdapter(mSolutions, this);
        mSolutionLV.setAdapter(mPathSolutionAdapter);

        mSolutionLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GuildSolution solution = mSolutions.get(position);
                Intent intent = new Intent(ShowSolutionActivity.this, GuildMapActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("solution", solution);
                intent.putExtra("bundle", b);
                startActivity(intent);
            }
        });

    }
}

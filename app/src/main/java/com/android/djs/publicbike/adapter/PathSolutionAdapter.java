package com.android.djs.publicbike.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.bean.GuildSolution;

import java.util.ArrayList;

/**
 * 路径搜索结果适配器
 * Created by DJS on 2017/6/16.
 */
public class PathSolutionAdapter extends BaseAdapter {


    private ArrayList<GuildSolution> mGuildSolutions;
    private LayoutInflater mInflater;

    public PathSolutionAdapter(ArrayList<GuildSolution> mGuildSolutions, Context context) {
        this.mGuildSolutions = mGuildSolutions;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mGuildSolutions.size();
    }

    @Override
    public Object getItem(int position) {
        return mGuildSolutions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        viewHolder holder;
        if (convertView == null) {
            //将布局文件绘制到列表元素的视图上
            convertView = mInflater.inflate(R.layout.path_solution_item, null);
            holder = new viewHolder();
            holder.timeTV = (TextView) convertView.findViewById(R.id.path_solution_item_time);
            holder.featureTV = (TextView) convertView.findViewById(R.id.path_solution_item_feature);
            holder.solutionNameTV = (TextView) convertView.findViewById(R.id.path_solution_item_name);
            holder.firstPathTV = (TextView) convertView.findViewById(R.id.path_solution_item_first_path);
            convertView.setTag(holder);
        } else {
            holder = (viewHolder) convertView.getTag();
        }

        holder.timeTV.setText((int) (mGuildSolutions.get(position).getTime()) + "分钟");

        holder.solutionNameTV.setText(mGuildSolutions.get(position).getName());
        holder.firstPathTV.setText(mGuildSolutions.get(position).getPaths().get(0).getDescription());
        //设置特点
        if (mGuildSolutions.get(position).getFeature() != null) {
            switch (mGuildSolutions.get(position).getFeature()) {
                case MIN_TIME:
                    holder.featureTV.setText("时间最少");
                    holder.featureTV.setTextColor(Color.BLUE);
                    break;
                case MIN_WALK:
                    holder.featureTV.setText("步行最短");
                    holder.featureTV.setTextColor(Color.YELLOW);
                    break;
                case MIN_TRANSFER:
                    holder.featureTV.setText("换乘少");
                    holder.featureTV.setTextColor(Color.rgb(238, 173, 14));
                    break;
            }

        }

        return convertView;
    }

    class viewHolder {

        TextView timeTV;
        TextView featureTV;
        TextView solutionNameTV;
        TextView firstPathTV;
    }
}

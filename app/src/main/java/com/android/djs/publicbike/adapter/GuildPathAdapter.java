package com.android.djs.publicbike.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.djs.publicbike.R;
import com.android.djs.publicbike.bean.GuildPath;
import com.android.djs.publicbike.constant.PathType;

import java.util.ArrayList;

/**
 * 导航路径适配器
 * Created by DJS on 2017/6/21.
 */
public class GuildPathAdapter extends BaseAdapter {

    private ArrayList<GuildPath> mPaths;
    private LayoutInflater mInflater;

    public GuildPathAdapter(Context context, ArrayList<GuildPath> mPaths) {
        this.mPaths = mPaths;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mPaths.get(position);
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
            convertView = mInflater.inflate(R.layout.guild_path_item, null);

            holder = new viewHolder();
            holder.type = (ImageView) convertView.findViewById(R.id.guild_path_type);
            holder.description = (TextView) convertView.findViewById(R.id.guild_path_description);
            convertView.setTag(holder);
        } else {
            holder = (viewHolder) convertView.getTag();
        }

        holder.description.setText(mPaths.get(position).getDescription());
       PathType type = mPaths.get(position).getPathType();
        switch (type) {
            case WALK:
            case FIRST_PATH:
                holder.type.setImageResource(R.mipmap.type_walk);
                break;
            case BIKE:
                holder.type.setImageResource(R.mipmap.type_bike);
                break;
            case BUS:
                holder.type.setImageResource(R.mipmap.type_bus);
                break;
        }

        return convertView;
    }

    class viewHolder {

        ImageView type;//出行方式
        TextView description;//消息描述
    }

}

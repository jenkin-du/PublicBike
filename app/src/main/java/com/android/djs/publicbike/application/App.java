package com.android.djs.publicbike.application;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;

/**
 * 自定义全局类
 * Created by Administrator on 2016/8/11.
 */
public class App extends Application {



    //用于保存所有的activity
    public static ArrayList<Activity> mActivityList =new ArrayList<>();


    /**
     * 退出所有的activity
     */
    public static void exitAllActivity(){

        if (mActivityList !=null){
            for (Activity activity: mActivityList){
                activity.finish();
            }
        }

    }

}

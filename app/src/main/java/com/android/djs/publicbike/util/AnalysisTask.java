package com.android.djs.publicbike.util;

import android.os.Handler;
import android.os.Message;

/**
 * 分析类
 * Created by DJS on 2017/6/12.
 */
public class AnalysisTask extends Thread {

    private Handler mHandler;

    public AnalysisTask(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        Message msg = new Message();
        Object obj = doBackAnalysis();
        if (obj != null) {
            msg.obj = obj;
            msg.what = 200;
            mHandler.sendMessage(msg);
        } else {
            msg.what = 400;
            mHandler.sendMessage(msg);
        }

    }

    public Object doBackAnalysis() {
        return null;
    }

}

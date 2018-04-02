package com.android.djs.publicbike.drawable;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.android.djs.publicbike.util.MyUtil;


/**
 * 自定义drawable
 * Created by DJS on 2017/4/6.
 */
public class LineGuildDrawable extends Drawable {

    private Paint mPaint;
    //圆半径
    private float mRadius;
    //偏移量
    private float mBias;
    //设置画笔遮罩
    private BlurMaskFilter mMaskFilter;


    public LineGuildDrawable(Context context) {

        //设置半径
        mRadius = MyUtil.dip2px(context, 40);
        //设置偏移量
        mBias = MyUtil.dip2px(context, 5);
        //初始化
        init();

    }


    /**
     * 初始化
     */
    private void init() {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        //防抖动
        mPaint.setDither(true);
        //设置颜色
        mPaint.setColor(Color.rgb(0, 191, 255));

        mMaskFilter = new BlurMaskFilter(5, BlurMaskFilter.Blur.OUTER);

    }

    @Override
    public void draw(Canvas canvas) {

        mPaint.setColor(Color.rgb(0, 191, 255));
        mPaint.setMaskFilter(mMaskFilter);
        canvas.drawCircle(mBias / 2 + mRadius, mBias / 2 + mRadius, mRadius, mPaint);
        mPaint.setMaskFilter(null);
        mPaint.setColor(Color.rgb(0, 191, 255));
        canvas.drawCircle(mBias / 2 + mRadius, mBias / 2 + mRadius, mRadius, mPaint);

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

        if (mPaint.getColorFilter() != colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}

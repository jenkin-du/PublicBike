package com.android.djs.publicbike.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.djs.publicbike.drawable.LineGuildDrawable;


/**自定义view
 * Created by DJS on 2017/4/6.
 */
public class LineGuildButton extends ImageView {

    private Context mContext;
    private LineGuildDrawable mDrawable;

    public LineGuildButton(Context context) {
        this(context,null);
    }

    public LineGuildButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LineGuildButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {

        mContext=context;
        setLayerType(LAYER_TYPE_SOFTWARE,null);//
        mDrawable=new LineGuildDrawable(mContext);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mDrawable.draw(canvas);
        super.onDraw(canvas);
    }
}

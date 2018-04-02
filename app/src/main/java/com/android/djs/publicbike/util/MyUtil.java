package com.android.djs.publicbike.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.supermap.android.maps.BoundingBox;
import com.supermap.android.maps.Point2D;
import com.supermap.services.components.commontypes.Feature;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 获得系统当前时间
 * Created by Administrator on 2016/8/24.
 */
public class MyUtil {


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * 根据resId获得相应pin
     *
     * @return 位图
     */
    public static Bitmap getPin(int resId, Context context) {


        //加载xml布局文件
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(resId, null);

        //启用绘图缓存
        view.setDrawingCacheEnabled(true);
        //调用下面这个方法非常重要，如果没有调用这个方法，得到的bitmap为null
        view.measure(View.MeasureSpec.makeMeasureSpec(MyUtil.dip2px(context, 60), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(MyUtil.dip2px(context, 60), View.MeasureSpec.EXACTLY));
        //这个方法也非常重要，设置布局的尺寸和位置
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        //获得绘图缓存中的Bitmap
        view.buildDrawingCache();

        return view.getDrawingCache();
    }

    /**
     * 根据resId获得相应pin
     *
     * @return 位图
     */
    public static Bitmap getSmallPin(int resId, Context context) {


        //加载xml布局文件
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(resId, null);

        //启用绘图缓存
        view.setDrawingCacheEnabled(true);
        //调用下面这个方法非常重要，如果没有调用这个方法，得到的bitmap为null
        view.measure(View.MeasureSpec.makeMeasureSpec(MyUtil.dip2px(context, 12), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(MyUtil.dip2px(context, 12), View.MeasureSpec.EXACTLY));
        //这个方法也非常重要，设置布局的尺寸和位置
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        //获得绘图缓存中的Bitmap
        view.buildDrawingCache();

        return view.getDrawingCache();
    }

    /**
     * 计算两点的距离
     */
    public static double calculateDistance(Point2D p1, Point2D p2) {

        double x1 = p1.x;
        double x2 = p2.x;
        double y1 = p1.y;
        double y2 = p2.y;

        double dx = Math.abs(x1 - x2);
        double dy = Math.abs(y1 - y2);

        return Math.sqrt(dx * dx + dy * dy);

    }

    /**
     * 计算两点的距离
     */
    public static double calculatePathDistance(ArrayList<ArrayList<Point2D>> paths) {

        double distance = 0;
        if (paths != null) {
            distance = 0;
            for (ArrayList<Point2D> path : paths) {
                Point2D p1 = path.get(0);
                Point2D p2;
                for (int i = 1; i < path.size(); i++) {
                    p2 = path.get(i);
                    distance += calculateDistance(p1, p2);
                    p1 = p2;
                }
            }
        }

        return distance;
    }

    /**
     * 得到属性的键值对
     */
    public static HashMap<String, String> getFeatureAttr(Feature feature) {

        if (feature != null) {
            String[] names = feature.fieldNames;
            String[] values = feature.fieldValues;

            HashMap<String, String> featureAttr;
            featureAttr = new HashMap<>();
            for (int k = 0; k < names.length; k++) {
                String name = names[k];
                String value = values[k];

                featureAttr.put(name, value);
            }
            return featureAttr;
        }
        return null;
    }

    /**
     * 获得屏幕中心点坐标
     *
     * @param manager WindowManager
     * @return ‘’
     */
    public static Point2D getDisplayCenter(WindowManager manager) {

        Display display = manager.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        double width = size.x;
        double height = size.y;

        double centerY = height / 2;
        double centerX = width / 2;

        return new Point2D(centerX, centerY);

    }

    /**
     * 获得范围
     * @param pathSegments 路径
     * @return 范围
     */
    public static BoundingBox getBound(ArrayList<ArrayList<Point2D>> pathSegments) {

        BoundingBox bound = null;

        double top;
        double left;
        double right;
        double bottom;

        if (pathSegments != null && pathSegments.size() > 0) {
            if (pathSegments.get(0) != null && pathSegments.get(0).size() > 0) {
                double tempX;
                double tempY;
                //获得 top值
                bottom = top = pathSegments.get(0).get(0).getY();
                right = left = pathSegments.get(0).get(0).getX();
                for (ArrayList<Point2D> segs : pathSegments) {
                    for (Point2D p : segs) {
                        tempX = p.getX();
                        tempY = p.getY();

                        if (tempY > top) {
                            top = tempY;
                        }

                        if (tempY < bottom) {
                            bottom = tempY;
                        }

                        if (tempX > right) {
                            right = tempX;
                        }

                        if (tempX < left) {
                            left = tempX;
                        }
                    }
                }

                Point2D leftTopPt = new Point2D(left, top);
                Point2D rightBottomPt = new Point2D(right, bottom);

                bound = new BoundingBox(leftTopPt, rightBottomPt);
                //展宽20%
                MyUtil.expandBound(0.6, bound);
            }
        }


        return bound;
    }

    /**
     * 将矩形展宽一定的比率
     *
     * @param ratio 希望展宽的比率 如 展宽原来的5%
     * @param bound 矩形
     */
    public static void expandBound(double ratio, BoundingBox bound) {

        double top = bound.getTop();
        double left = bound.getLeft();
        double bottom = bound.getBottom();
        double right = bound.getRight();

        double dx = right - left;
        double dy = top - bottom;

        double edx = dx * ratio + dx;
        double edy = dy * ratio + dy;

        double cx = (right + left) / 2;
        double cy = (top + bottom) / 2;

        double eTop = cy + edy / 2;
        double eBottom = cy - edy / 2;
        double eRight = cx + edx / 2;
        double eLeft = cx - edx / 2;

        Point2D leftTop = new Point2D(eLeft, eTop);
        Point2D rightBottom = new Point2D(eRight, eBottom);

        bound.leftTop = leftTop;
        bound.rightBottom = rightBottom;
    }


    /**
     * 获得两个集合的交集
     */
    public static ArrayList<Integer> getIntersection(ArrayList<Integer> list1,
                                                     ArrayList<Integer> list2) {
        ArrayList<Integer> result = new ArrayList<>();
        for (Integer integer : list2) {//遍历list1
            if (list1.contains(integer)) {//如果存在这个数
                result.add(integer);//放进一个list里面，这个list就是交集
            }
        }
        return result;
    }
}

package com.android.djs.publicbike.bean;

import android.util.Log;

import com.android.djs.publicbike.constant.PathType;
import com.android.djs.publicbike.constant.Velocity;
import com.android.djs.publicbike.service.QueryService;
import com.android.djs.publicbike.util.MyUtil;
import com.supermap.android.maps.BoundingBox;
import com.supermap.android.maps.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 路径
 * 注意：在新建一个路径时，必须先设置路劲的类型，否者出现错误概不负责
 * Created by DJS on 2017/6/16.
 */
public class GuildPath implements Serializable {

    private int id;
    private String pathName;

    private ArrayList<ArrayList<Point2D>> pathSegments;

    private String startPointName;
    private String endPointName;

    private double distance = 0;
    private double time = 0;
    private PathType pathType;
    private String description;

    private Point2D startPoint;
    private Point2D endPoint;
    private BoundingBox bound;

    private int passStopCount;

    public int getPassStopCount() {
        return passStopCount;
    }

    public void setPassStopCount(int passStopCount) {
        this.passStopCount = passStopCount;
    }

    public BoundingBox getBound() {

        if (bound == null) {
            double top;
            double left;
            double right;
            double bottom;

            if (getPathSegments() != null && pathSegments.size() > 0) {
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
                    //展宽10%
                    MyUtil.expandBound(0.2, bound);
                }
            }
        }

        return bound;
    }

    public void setBound(BoundingBox bound) {
        this.bound = bound;
    }

    public Point2D getStartPoint() {

        if (startPoint == null) {
            if (pathSegments != null && pathSegments.size() > 0) {
                ArrayList<Point2D> firstSeg = pathSegments.get(0);
                if (firstSeg.size() > 0) {
                    startPoint = firstSeg.get(0);
                }
            }
        }
        return startPoint;
    }

    public void setStartPoint(Point2D startPoint) {

        this.startPoint = startPoint;
    }

    public Point2D getEndPoint() {

        if (endPoint == null) {

            if (pathSegments != null && pathSegments.size() > 0) {
                ArrayList<Point2D> endSeg = pathSegments.get(pathSegments.size() - 1);
                if (endSeg.size() > 0) {
                    endPoint = endSeg.get(endSeg.size() - 1);
                }
            }
        }
        return endPoint;
    }

    public void setEndPoint(Point2D endPoint) {
        this.endPoint = endPoint;
    }

    public String getStartPointName() {
        return startPointName;
    }

    public void setStartPointName(String startPointName) {

        if (startPointName != null && !"".equals(startPointName)) {
            startPoint = findPoint(startPointName);
        }
        this.startPointName = startPointName;
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    /**
     * 根据名字找到点坐标
     *
     * @param name 名字
     */
    private Point2D findPoint(String name) {

        Point2D point;
        if (pathType != null && name != null && !name.equals("")) {

            point = new Point2D();
            switch (pathType) {
                //步行
                case WALK:
                case FIRST_PATH:
                    point = QueryService.getBikeStopLocByName(name);
                    break;
                //自行车
                case BIKE:
                    Log.i("TAG", "findPoint: BIKE");
                    point = QueryService.getBikeStopLocByName(name);
                    break;
            }

            return point;
        }
        return null;
    }


    public String getEndPointName() {
        return endPointName;
    }

    public void setEndPointName(String endPointName) {
        this.endPointName = endPointName;
    }

    public double getDistance() {

        if (distance > 0) {
            return distance;
        }
        if (pathSegments != null && pathSegments.size() > 0) {
            distance = 0;
            for (ArrayList<Point2D> path : pathSegments) {
                if (path != null && path.size() > 0) {
                    Point2D p1 = path.get(0);
                    Point2D p2;
                    for (int i = 1; i < path.size(); i++) {
                        p2 = path.get(i);
                        distance += MyUtil.calculateDistance(p1, p2);
                        p1 = p2;
                    }
                }

            }
        }

        return distance;
    }


    public double getTime() {

        if (pathType != null) {

            switch (pathType) {
                case WALK:
                    time = getDistance() / Velocity.WALK;
                    break;
                case BIKE:
                    time = getDistance() / Velocity.BIKE;
                    break;
                case BUS:
                    time = getDistance() / Velocity.BUS;
                    break;
            }
        }

        return time;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {

        if (pathType != null) {

            switch (pathType) {
                case WALK:
                case FIRST_PATH:
                    description = "从" + startPointName + "出发，大约走" + (int) getDistance() + "米，走到" + endPointName;
                    break;
                case BIKE:
                    description = "从" + startPointName + "骑车，大约骑" + (int) getTime() + "分钟，骑到" + endPointName;
                    break;
                case BUS:
                    description = "从" + startPointName + "上车，乘坐" + pathName + "，在" + endPointName + "下车";
                    break;
            }
        }

        return description;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void reverse() {

        String name = startPointName;
        startPointName = endPointName;
        endPointName = name;

        if (startPoint != null && endPoint != null) {
            Point2D p = startPoint;
            startPoint = endPoint;
            endPoint = p;
        }

        if (pathSegments != null) {
            Collections.reverse(pathSegments);
        }
    }


    public ArrayList<ArrayList<Point2D>> getPathSegments() {
        if (pathType.equals(PathType.FIRST_PATH)) {
            if (pathSegments != null && startPoint != null) {
                pathSegments.get(0).add(0, startPoint);
            }
        }
        return pathSegments;
    }

    public void setPathSegments(ArrayList<ArrayList<Point2D>> pathSegments) {
        this.pathSegments = pathSegments;
    }

    @Override
    public String toString() {

        return "GuildPath{" +
                "pathName='" + pathName + '\'' +
                ", startPointName='" + startPointName + '\'' +
                ", endPointName='" + endPointName + '\'' +
                ", distance=" + distance +
                ", time=" + time +
                ", pathType='" + pathType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuildPath)) return false;

        GuildPath path = (GuildPath) o;

        if (getId() != path.getId()) return false;
        if (getPathName() != null ? !getPathName().equals(path.getPathName()) : path.getPathName() != null)
            return false;
        if (getStartPointName() != null ? !getStartPointName().equals(path.getStartPointName()) : path.getStartPointName() != null)
            return false;
        if (getEndPointName() != null ? !getEndPointName().equals(path.getEndPointName()) : path.getEndPointName() != null)
            return false;
        return pathType == path.pathType;

    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getPathName() != null ? getPathName().hashCode() : 0);
        result = 31 * result + (getStartPointName() != null ? getStartPointName().hashCode() : 0);
        result = 31 * result + (getEndPointName() != null ? getEndPointName().hashCode() : 0);
        result = 31 * result + (pathType != null ? pathType.hashCode() : 0);
        return result;
    }
}

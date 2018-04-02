package com.android.djs.publicbike.bean;

import com.android.djs.publicbike.constant.GuildFeature;
import com.android.djs.publicbike.constant.GuildMode;
import com.android.djs.publicbike.constant.PathType;
import com.android.djs.publicbike.util.MyUtil;
import com.supermap.android.maps.BoundingBox;
import com.supermap.android.maps.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 路径搜索解决方案
 * Created by DJS on 2017/6/16.
 */
public class GuildSolution implements Serializable {


    private String name = "";
    private double time = 0;
    private ArrayList<GuildPath> paths;
    private int transferCount = 0;
    private double walkDistance;
    private Point2D startPoint;
    private Point2D endPoint;
    private String startName;
    private String endName;
    private BoundingBox bound;
    private GuildMode mode;
    private GuildFeature feature;

    public GuildFeature getFeature() {
        return feature;
    }

    public void setFeature(GuildFeature feature) {
        this.feature = feature;
    }

    public GuildMode getMode() {
        return mode;
    }

    public void setMode(GuildMode mode) {
        this.mode = mode;
    }

    public void setPaths(ArrayList<GuildPath> paths) {
        this.paths = paths;
    }

    /**
     * 获得路径的范围
     */
    public BoundingBox getBound() {

        if (bound == null) {
            if (paths != null && paths.size() > 0) {

                BoundingBox bd = paths.get(0).getBound();
                if (bd != null) {
                    double top = bd.getTop();
                    double left = bd.getLeft();
                    double bottom = bd.getBottom();
                    double right = bd.getRight();

                    double tempT;
                    double tempL;
                    double tempB;
                    double tempR;

                    for (GuildPath path : paths) {
                        BoundingBox b = path.getBound();

                        if (b != null) {
                            tempT = b.getTop();
                            tempL = b.getLeft();
                            tempB = b.getBottom();
                            tempR = b.getRight();

                            if (tempT > top) {
                                top = tempT;
                            }
                            if (tempL < left) {
                                left = tempL;
                            }
                            if (tempB < bottom) {
                                bottom = tempB;
                            }
                            if (tempR > right) {
                                right = tempR;
                            }

                            Point2D leftTopPt = new Point2D(left, top);
                            Point2D rightBottomPt = new Point2D(right, bottom);

                            bound = new BoundingBox(leftTopPt, rightBottomPt);
                            //展宽10%
                            MyUtil.expandBound(0.2, bound);

                        }

                    }
                }

            }
        }
        return bound;

    }

    public void setBound(BoundingBox bound) {
        this.bound = bound;
    }

    public Point2D getStartPoint() {
        if (paths != null && paths.size() > 0) {
            startPoint = paths.get(0).getStartPoint();
        }
        return startPoint;
    }


    public Point2D getEndPoint() {

        if (paths != null && paths.size() > 0) {
            endPoint = paths.get(paths.size() - 1).getEndPoint();
        }
        return endPoint;
    }

    public String getStartName() {
        if (paths != null && paths.size() > 0) {
            startName = paths.get(0).getStartPointName();
        }
        return startName;
    }

    public String getEndName() {
        if (paths != null && paths.size() > 0) {
            endName = paths.get(paths.size() - 1).getEndPointName();
        }
        return endName;
    }

    public void addGuildPath(GuildPath path) {
        if (paths == null) {
            paths = new ArrayList<>();
        }
        paths.add(path);
    }

    public ArrayList<GuildPath> getPaths() {
        return paths;
    }

    public GuildPath getGuildPath(int index) {
        return paths.get(index);
    }

    public String getName() {
        if (paths != null) {

            if (paths.size() == 1 && paths.get(0).getPathType().equals(PathType.WALK)) {
                name = "步行";
            } else {
                String temp = "";
                for (GuildPath path : paths) {
                    if (path.getPathType().equals(PathType.BIKE)) {
                        temp += "骑行→";
                    } else if (path.getPathType().equals(PathType.BUS)) {
                        temp += path.getPathName() + "→";
                    }
                }
                name = temp;
                if (name.length() > 0) {
                    temp = name.substring(0, name.length() - 1);
                    name = temp;
                }
            }

        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTime() {

        if ((int) time == 0 && paths != null) {

            for (GuildPath path : paths) {
                time += path.getTime();
            }
        }

        return time;
    }

    public double getWalkDistance() {

        if ((int) walkDistance == 0 && paths != null) {
            for (GuildPath path : paths) {
                if (path.getPathType().equals(PathType.WALK)) {
                    walkDistance += path.getDistance();
                }
            }
            return walkDistance;
        }
        return 0;
    }

    public int getTransferCount() {
        transferCount = 0;
        if (paths != null) {
            for (GuildPath path : paths) {
                if (!path.getPathType().equals(PathType.WALK)) {
                    transferCount++;
                }
            }
            transferCount--;
        }
        return transferCount;
    }

    public void setTransferCount(int transferCount) {
        this.transferCount = transferCount;
    }


    public void reverse() {

        if (paths != null) {
            Collections.reverse(paths);
            for (GuildPath path : paths) {
                path.reverse();
            }
        }
    }

    @Override
    public String toString() {
        return "GuildSolution{" +
                "mode=" + mode +
                ", endName='" + endName + '\'' +
                ", startName='" + startName + '\'' +
                ", walkDistance=" + walkDistance +
                ", time=" + time +
                ", name='" + name + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuildSolution)) return false;

        GuildSolution solution = (GuildSolution) o;

        if (getName() != null ? !getName().equals(solution.getName()) : solution.getName() != null)
            return false;
        if (getPaths() != null ? !getPaths().equals(solution.getPaths()) : solution.getPaths() != null)
            return false;
        if (getStartPoint() != null ? !getStartPoint().equals(solution.getStartPoint()) : solution.getStartPoint() != null)
            return false;
        if (getEndPoint() != null ? !getEndPoint().equals(solution.getEndPoint()) : solution.getEndPoint() != null)
            return false;
        if (getStartName() != null ? !getStartName().equals(solution.getStartName()) : solution.getStartName() != null)
            return false;
        if (getEndName() != null ? !getEndName().equals(solution.getEndName()) : solution.getEndName() != null)
            return false;
        return getMode() == solution.getMode();

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getPaths() != null ? getPaths().hashCode() : 0);
        result = 31 * result + (getStartPoint() != null ? getStartPoint().hashCode() : 0);
        result = 31 * result + (getEndPoint() != null ? getEndPoint().hashCode() : 0);
        result = 31 * result + (getStartName() != null ? getStartName().hashCode() : 0);
        result = 31 * result + (getEndName() != null ? getEndName().hashCode() : 0);
        result = 31 * result + (getMode() != null ? getMode().hashCode() : 0);
        return result;
    }
}

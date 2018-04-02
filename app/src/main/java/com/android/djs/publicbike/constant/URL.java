package com.android.djs.publicbike.constant;

/**
 * url常量
 * Created by DJS on 2017/6/20.
 */
public class URL {

    public static final String PREFIX = "http://192.168.191.1:8090/iserver/services";

    //地图URL
    public static final String BASE_MAP = PREFIX + "/map-OSM/rest/maps/normal";
    //查询URL
    public static final String QUERY = PREFIX + "/map-Xianlin/rest/maps/xianlin";
    //数据URL
    public static final String DATA = PREFIX + "/data-Xianlin/rest/data";
    //空间分析URL
    public static final String ANALYSIS = PREFIX + "/transportationAnalyst-Xianlin/rest/networkanalyst/loadNetwork@Xianlin";
    public static final String ANALYSIS_BUS = PREFIX + "/transportationAnalyst-Xianlin2/rest/networkanalyst/busNetwork@Xianlin";
    //公交换乘分析URL
    public static final String TRANSFER = PREFIX + "/trafficTransferAnalyst-Xianlin/restjsr/traffictransferanalyst/transferNetwork-Xianlin";
}

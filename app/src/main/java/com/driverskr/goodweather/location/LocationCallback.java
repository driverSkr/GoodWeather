package com.driverskr.goodweather.location;

import com.baidu.location.BDLocation;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 15:53
 * @Description: 定位接口$
 */
public interface LocationCallback {
    /**
     * 接收定位
     * @param bdLocation 定位数据
     */
    void onRonReceiveLocation(BDLocation bdLocation);
}

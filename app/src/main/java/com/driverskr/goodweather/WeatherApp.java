package com.driverskr.goodweather;

import com.baidu.location.LocationClient;
import com.driverskr.library.base.BaseApplication;
import com.driverskr.library.network.NetworkApi;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 15:52
 * @Description: 全局Application$
 */
public class WeatherApp extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //使用定位需要同意隐私合规政策
        LocationClient.setAgreePrivacy(true);

        //初始化网络框架
        NetworkApi.init(new NetworkRequiredInfo(this));
    }
}

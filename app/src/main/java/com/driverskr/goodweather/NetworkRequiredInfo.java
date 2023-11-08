package com.driverskr.goodweather;

import android.app.Application;

import com.driverskr.library.network.INetworkRequiredInfo;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:48
 * @Description: 初始化网络框架$
 */
public class NetworkRequiredInfo implements INetworkRequiredInfo {

    private final Application application;

    public NetworkRequiredInfo(Application application) {
        this.application = application;
    }

    /**
     * 获取App版本名
     */
    @Override
    public String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取App版本号
     */
    @Override
    public String getAppVersionCode() {
        return String.valueOf(BuildConfig.VERSION_CODE);
    }

    /**
     * 判断是否为Debug模式
     */
    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    /**
     * 获取全局上下文参数
     */
    @Override
    public Application getApplicationContext() {
        return application;
    }
}

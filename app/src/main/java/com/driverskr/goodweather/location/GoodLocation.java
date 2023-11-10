package com.driverskr.goodweather.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * @Author: driverSkr
 * @Time: 2023/11/10 11:21
 * @Description: 对于定位功能的封装$
 */
public class GoodLocation {

    /**
     * volatile 是一个关键字，用于修饰类的实例变量
     * @Description: 主要作用是告诉编译器和虚拟机，被 volatile 修饰的变量是共享的，可能会被多个线程同时访问，因此在进行读取和修改时需要特殊对待，以确保线程安全
     */
    private static volatile GoodLocation mInstance;

    @SuppressLint("StaticFieldLeak")
    private static LocationClient mLocationClient = null;
    //定位监听
    private GoodLocationListener goodLocationListener;
    //定位回调接口
    private static LocationCallback callback;

    public GoodLocation(Context context) {
        initLocation(context);
    }

    public static GoodLocation getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GoodLocation.class) {
                if (mInstance == null) {
                    mInstance = new GoodLocation(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化定位
     */
    private void initLocation(Context context) {
        try {
            goodLocationListener = new GoodLocationListener();
            mLocationClient = new LocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            //注册定位监听
            mLocationClient.registerLocationListener(goodLocationListener);
            LocationClientOption option = new LocationClientOption();
            //如果开发者需要获得当前点的地址信息，此处必须为true
            option.setIsNeedAddress(true);
            //可选，设置是否需要最新版本的地址信息。默认不需要，即参数为false
            option.setNeedNewVersionRgc(true);
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            mLocationClient.setLocOption(option);
        }
    }

    /**
     * 需要定位的页面调用此方法进行接口回调处理
     */
    public void setCallback(LocationCallback callback) {
        GoodLocation.callback = callback;
    }

    /**
     * 开始定位
     */
    public void startLocation() {
        if (mLocationClient != null) {
            mLocationClient.start();
        }
    }

    /**
     * 请求定位
     */
    private static void requestLocation() {
        if (mLocationClient != null) {
            mLocationClient.requestLocation();
        }
    }

    /**
     * 停止定位
     */
    private static void stopLocation() {
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    /**
     * 内部类实现百度定位结果接收
     */
    public static class GoodLocationListener extends BDAbstractLocationListener {

        private final String TAG = GoodLocationListener.class.getSimpleName();

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) return;
            if (bdLocation.getDistrict() == null) {
                Log.e(TAG, "onReceiveLocation: 未获取区/县数据，您可以重新断开连接网络再尝试定位。");
                requestLocation();
            }
            stopLocation();
            if (callback == null) {
                Log.e(TAG, "callback is Null!");
                return;
            }
            callback.onRonReceiveLocation(bdLocation);
        }
    }
}

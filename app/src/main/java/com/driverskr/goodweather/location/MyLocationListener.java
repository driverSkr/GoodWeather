package com.driverskr.goodweather.location;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 15:55
 * @Description: 自定义定位监听类$
 */
public class MyLocationListener extends BDAbstractLocationListener {

    private final String TAG = MyLocationListener.class.getSimpleName();

    /**
     * 定位回调
     */
    private LocationCallback callback;

    /**
     * 需要定位的页面调用此方法进行接口回调处理
     */
    public void setCallback(LocationCallback callback){
        this.callback = callback;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (callback == null) {
            Log.e(TAG,"callback is Null!");
            return;
        }
        callback.onRonReceiveLocation(bdLocation);
    }
}

package com.driverskr.library.base;

import androidx.viewbinding.ViewBinding;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:43
 * @Description: 访问网络的Activity$
 */
public abstract class NetworkActivity<VB extends ViewBinding> extends BaseVBActivity<VB> {

    @Override
    protected void initData() {
        onCreate();
        onObserveData();
    }

    protected abstract void onCreate();

    /**
     * 在使用LiveData的时候有一个观察数据返回的地方
     */
    protected abstract void onObserveData();
}

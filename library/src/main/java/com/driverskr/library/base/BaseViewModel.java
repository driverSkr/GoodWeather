package com.driverskr.library.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:46
 * @Description: ViewModel基类$
 */
public class BaseViewModel extends ViewModel {
    protected MutableLiveData<String> failed = new MutableLiveData<>();
}

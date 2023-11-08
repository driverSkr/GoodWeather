package com.driverskr.goodweather.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.driverskr.goodweather.bean.SearchCityResponse;
import com.driverskr.goodweather.repository.SearchCityRepository;
import com.driverskr.library.base.BaseViewModel;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 19:04
 * @Description: 对应MainActivity$
 */
public class MainViewModel extends BaseViewModel {

    public MutableLiveData<SearchCityResponse> searchCityResponseMutableLiveData = new MutableLiveData<>();

    /**
     * 搜索成功
     * @param cityName 城市名称
     * @param isExact 是否精准搜索
     */
    public void searchCity(String cityName, boolean isExact) {
        new SearchCityRepository().searchCity(searchCityResponseMutableLiveData, failed, cityName, isExact);
    }
}

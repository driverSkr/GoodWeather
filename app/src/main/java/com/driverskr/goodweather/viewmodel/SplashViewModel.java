package com.driverskr.goodweather.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.driverskr.goodweather.db.bean.Province;
import com.driverskr.goodweather.repository.CityRepository;
import com.driverskr.library.base.BaseViewModel;

import java.util.List;

/**
 * @Author: driverSkr
 * @Time: 2023/11/9 18:12
 * @Description: 对应SplashActivity$
 */
public class SplashViewModel extends BaseViewModel {

    public MutableLiveData<List<Province>> listMutableLiveData = new MutableLiveData<>();

    /**
     * 添加城市数据
     */
    public void addCityData(List<Province> provinceList) {
        CityRepository.getInstance().addCityData(provinceList);
    }

    /**
     * 获取所有城市数据
     */
    public void getAllCityData() {
        CityRepository.getInstance().getCityData(listMutableLiveData);
    }
}

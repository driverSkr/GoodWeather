package com.driverskr.goodweather.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.driverskr.goodweather.db.bean.MyCity;
import com.driverskr.goodweather.repository.CityRepository;
import com.driverskr.library.base.BaseViewModel;

import java.util.List;

/**
 * @Author: driverSkr
 * @Time: 2023/11/11 11:19
 * @Description: 对应ManageCityActivity$
 */
public class ManageCityViewModel extends BaseViewModel {

    public MutableLiveData<List<MyCity>> listMutableLiveData = new MutableLiveData<>();

    /**
     * 获取所有城市数据
     */
    public void getAllCityData() {
        CityRepository.getInstance().getMyCityData(listMutableLiveData);
    }

    /**
     * 添加我的城市数据，在定位之后添加数据
     */
    public void addMyCityData(String cityName) {
        CityRepository.getInstance().addMyCityData(new MyCity(cityName));
    }

    /**
     * 删除我的城市数据
     */
    public void deleteMyCityData(MyCity myCity) {
        CityRepository.getInstance().deleteMyCityData(myCity);
    }

    /**
     * 删除我的城市数据
     */
    public void deleteMyCityData(String cityName) {
        CityRepository.getInstance().deleteMyCityData(cityName);
    }
}

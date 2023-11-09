package com.driverskr.goodweather.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.driverskr.goodweather.db.bean.DailyResponse;
import com.driverskr.goodweather.db.bean.LifestyleResponse;
import com.driverskr.goodweather.db.bean.NowResponse;
import com.driverskr.goodweather.db.bean.Province;
import com.driverskr.goodweather.db.bean.SearchCityResponse;
import com.driverskr.goodweather.repository.CityRepository;
import com.driverskr.goodweather.repository.SearchCityRepository;
import com.driverskr.goodweather.repository.WeatherRepository;
import com.driverskr.library.base.BaseViewModel;

import java.util.List;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 19:04
 * @Description: 对应MainActivity$
 */
public class MainViewModel extends BaseViewModel {

    public MutableLiveData<SearchCityResponse> searchCityResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<NowResponse> nowResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<DailyResponse> dailyResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<LifestyleResponse> lifestyleResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<List<Province>> cityMutableLiveData = new MutableLiveData<>();

    /**
     * 搜索成功
     * @param cityName 城市名称
     * @param isExact 是否精准搜索
     */
    public void searchCity(String cityName, boolean isExact) {
        SearchCityRepository.getInstance().searchCity(searchCityResponseMutableLiveData, failed, cityName, isExact);
    }

    /**
     * 实时天气
     * @param cityId    城市ID：通过searchCity获得
     */
    public void nowWeather(String cityId) {
        WeatherRepository.getInstance().nowWeather(nowResponseMutableLiveData, failed, cityId);
    }

    /**
     * 天气预报 7天
     * @param cityId    城市ID：通过searchCity获得
     */
    public void dailyWeather(String cityId) {
        WeatherRepository.getInstance().dailyWeather(dailyResponseMutableLiveData, failed, cityId);
    }

    /**
     * 生活指数
     *
     * @param cityId 城市ID：通过searchCity获得
     */
    public void lifestyle(String cityId) {
        WeatherRepository.getInstance().lifestyle(lifestyleResponseMutableLiveData, failed, cityId);
    }

    public void getAllCity() {
        CityRepository.getInstance().getCityData(cityMutableLiveData);
    }
}

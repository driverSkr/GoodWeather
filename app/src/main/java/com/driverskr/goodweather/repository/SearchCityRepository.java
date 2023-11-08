package com.driverskr.goodweather.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.driverskr.goodweather.api.ApiService;
import com.driverskr.goodweather.Constant;
import com.driverskr.goodweather.bean.SearchCityResponse;
import com.driverskr.library.network.ApiType;
import com.driverskr.library.network.NetworkApi;
import com.driverskr.library.network.observer.BaseObserver;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:57
 * @Description: 搜索城市仓库$
 */

public class SearchCityRepository {

    private static final String TAG = SearchCityRepository.class.getSimpleName();

    /**
     * 这里就是用到了网络框架，OKHttp做网络请求，Retrofit做接口封装和解析，RxJava做线程切换调度。
     * 拿到数据之后我们在通过LiveData进行发送
     */
    @SuppressLint("CheckResult")
    public void searchCity(MutableLiveData<SearchCityResponse> responseLiveData,
                           MutableLiveData<String> failed , String cityName , boolean isExact) {
        NetworkApi.createService(ApiService.class, ApiType.SEARCH).searchCity(cityName, isExact ? Constant.EXACT : Constant.FUZZY)
                .compose(NetworkApi.applySchedulers(new BaseObserver<SearchCityResponse>() {
                    @Override
                    public void onSuccess(SearchCityResponse searchCityResponse) {
                        if (searchCityResponse == null) {
                            failed.postValue("搜索城市数据为null，请检查城市名称是否正确。");
                            return;
                        }
                        //请求接口成功返回数据，失败返回状态码
                        if (Constant.SUCCESS.equals(searchCityResponse.getCode())) {
                            responseLiveData.postValue(searchCityResponse);
                        } else {
                            failed.postValue(searchCityResponse.getCode());
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        failed.postValue(e.getMessage());
                    }
                }));
    }
}

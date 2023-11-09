package com.driverskr.goodweather.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.driverskr.goodweather.api.ApiService;
import com.driverskr.goodweather.Constant;
import com.driverskr.goodweather.db.bean.SearchCityResponse;
import com.driverskr.library.network.ApiType;
import com.driverskr.library.network.NetworkApi;
import com.driverskr.library.network.observer.BaseObserver;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:57
 * @Description: 搜索城市存储库，数据处理$
 */

public class SearchCityRepository {

    private static final String TAG = SearchCityRepository.class.getSimpleName();

    /**
     * 通过静态内部类的方式构建单例
     */
    private static final class SearchCityRepositoryHolder{
        private static final SearchCityRepository mInstance = new SearchCityRepository();
    }
    public static SearchCityRepository getInstance() {
        return SearchCityRepository.SearchCityRepositoryHolder.mInstance;
    }

    /**
     * 搜索城市
     * @param responseLiveData 成功数据
     * @param failed           错误信息
     * @param cityName         城市名称
     * 这里就是用到了网络框架，OKHttp做网络请求，Retrofit做接口封装和解析，RxJava做线程切换调度。
     * 拿到数据之后我们在通过LiveData进行发送
     */
    @SuppressLint("CheckResult")
    public void searchCity(MutableLiveData<SearchCityResponse> responseLiveData,
                           MutableLiveData<String> failed , String cityName , boolean isExact) {
        String type = "搜索城市-->";
        Log.d(TAG,"波哥，你访问到了这" + "城市名：" + cityName);
        NetworkApi.createService(ApiService.class, ApiType.SEARCH).searchCity(cityName, isExact ? Constant.EXACT : Constant.FUZZY)
                .compose(NetworkApi.applySchedulers(new BaseObserver<SearchCityResponse>() {
                    @Override
                    public void onSuccess(SearchCityResponse searchCityResponse) {
                        if (searchCityResponse == null) {
                            failed.postValue("搜索城市数据为null，请检查城市名称是否正确。");
                            return;
                        }
                        Log.d(TAG,"波哥，你访问到了这");
                        Log.d(TAG,searchCityResponse.toString());
                        //请求接口成功返回数据，失败返回状态码
                        if (Constant.SUCCESS.equals(searchCityResponse.getCode())) {
                            responseLiveData.postValue(searchCityResponse);
                        } else {
                            failed.postValue(type + searchCityResponse.getCode());
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        failed.postValue(type + e.getMessage());
                    }
                }));
    }
}

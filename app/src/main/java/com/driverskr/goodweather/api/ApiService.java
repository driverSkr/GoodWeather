package com.driverskr.goodweather.api;

import static com.driverskr.goodweather.Constant.API_KEY;

import com.driverskr.goodweather.bean.SearchCityResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:53
 * @Description: 网络接口$
 */
public interface ApiService {

    /**
     * 搜索城市  模糊搜索，国内范围 返回10条数据
     *
     * @param location 城市名
     * @param mode     exact 精准搜索  fuzzy 模糊搜索
     * @return NewSearchCityResponse 搜索城市数据返回
     */
    @GET("/v2/city/lookup?key=" + API_KEY + "&range=cn")
    Observable<SearchCityResponse> searchCity(@Query("location") String location,
                                              @Query("mode") String mode);
}

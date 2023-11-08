package com.driverskr.library.network;

import android.content.Intent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 17:28
 * @Description: 基础返回类$
 */
public class BaseResponse {

    /**
     * 结果码
     */
    @SerializedName("res_code")
    @Expose
    public Integer responseCode;

    /**
     * 返回的错误信息
     */
    @SerializedName("res_error")
    @Expose
    public String responseError;
}

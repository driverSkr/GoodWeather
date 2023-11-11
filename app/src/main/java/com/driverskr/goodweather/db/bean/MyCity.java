package com.driverskr.goodweather.db.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * @Author: driverSkr
 * @Time: 2023/11/11 10:39
 * @Description: 管理城市实体类$
 */
@Entity
public class MyCity {

    @NonNull
    @PrimaryKey
    private String cityName;

    @NonNull
    public String getCityName() {
        return cityName;
    }

    public void setCityName(@NonNull String cityName) {
        this.cityName = cityName;
    }

    @Ignore
    public MyCity(@NonNull String cityName) {
        this.cityName = cityName;
    }

    public MyCity() {}
}

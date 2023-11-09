package com.driverskr.goodweather.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.driverskr.goodweather.db.bean.Province;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * @Author: driverSkr
 * @Time: 2023/11/9 17:20
 * @Description: 省份Dao接口$
 */
@Dao
public interface ProvinceDao {

    /**
     * Flowable和Completable 都是RxJava中的内容，背压
     */

    /**
     * 查询所有
     */
    @Query("SELECT * FROM Province")
    Flowable<List<Province>> getAll();

    /**
     * 插入所有
     * @param provinces 所有行政区数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(Province... provinces);
}

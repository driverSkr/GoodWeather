package com.driverskr.goodweather.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.driverskr.goodweather.db.bean.MyCity;
import com.driverskr.goodweather.db.bean.Province;
import com.driverskr.goodweather.db.dao.MyCityDao;
import com.driverskr.goodweather.db.dao.ProvinceDao;

/**
 * @Author: driverSkr
 * @Time: 2023/11/9 17:19
 * @Description: 数据库$
 */
@Database(entities = {Province.class, MyCity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "GoodWeather";
    private static volatile AppDatabase mInstance;

    public abstract ProvinceDao provinceDao();

    public abstract MyCityDao myCityDao();

    /**
     * 版本升级迁移到2 新增我的城市表
     */
    static final Migration MIGRATION_1_2 = new Migration(1 ,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `myCity` (cityName TEXT NOT NULL, PRIMARY KEY(`cityName`))");
        }
    };

    /**
     * 单例模式
     */
    public static AppDatabase getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AppDatabase.class) {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return mInstance;
    }
}

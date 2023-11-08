package com.driverskr.library.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author: driverSkr
 * @Time: 2023/11/8 18:35
 * @Description: 对ViewBinding的封装$
 */
public abstract class BaseVBActivity<VB extends ViewBinding> extends BaseActivity {

    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        onRegister();
        super.onCreate(savedInstanceState);
        Type type = this.getClass().getGenericSuperclass();
        /**
         * 主要就是反射拿到具体的编译时类，然后设置内容视图
         */
        if (type instanceof ParameterizedType) {
            try {
                Class<VB> clazz = (Class<VB>) ((ParameterizedType) type).getActualTypeArguments()[0];
                //反射
                Method method = clazz.getMethod("inflate", LayoutInflater.class);
                binding = (VB) method.invoke(null, getLayoutInflater());
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert binding != null;
            setContentView(binding.getRoot());
        }
        initData();
    }

    protected void onRegister() {

    }

    protected abstract void initData();
}

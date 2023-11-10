package com.driverskr.goodweather.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.driverskr.goodweather.databinding.ItemHourlyRvBinding;
import com.driverskr.goodweather.db.bean.HourlyResponse;
import com.driverskr.goodweather.utils.EasyDate;
import com.driverskr.goodweather.utils.WeatherUtil;

import java.util.List;

/**
 * @Author: driverSkr
 * @Time: 2023/11/10 16:18
 * @Description: 逐小时天气预报适配器$
 */
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private final List<HourlyResponse.HourlyBean> hourlyBeans;

    private OnClickItemCallback onClickItemCallback;

    public void setOnClickItemCallback(OnClickItemCallback onClickItemCallback) {
        this.onClickItemCallback = onClickItemCallback;
    }

    public HourlyAdapter(List<HourlyResponse.HourlyBean> hourlyBeans) {
        this.hourlyBeans = hourlyBeans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHourlyRvBinding binding = ItemHourlyRvBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        ViewHolder holder = new ViewHolder(binding);
        //添加点击回调
        binding.getRoot().setOnClickListener( v -> {
            if (onClickItemCallback != null) {
                onClickItemCallback.onItemClick(holder.getAdapterPosition());
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyResponse.HourlyBean hourlyBean = hourlyBeans.get(position);
        String time = EasyDate.updateTime(hourlyBean.getFxTime());
        holder.binding.tvTime.setText(String.format("%s%s", EasyDate.showTimeInfo(time), time));
        WeatherUtil.changeIcon(holder.binding.ivStatus, Integer.parseInt(hourlyBean.getIcon()));
        holder.binding.tvTemperature.setText(String.format("%s℃", hourlyBean.getTemp()));
    }

    @Override
    public int getItemCount() {
        return hourlyBeans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ItemHourlyRvBinding binding;

        public ViewHolder(@NonNull ItemHourlyRvBinding itemHourlyRvBinding) {
            super(itemHourlyRvBinding.getRoot());
            this.binding = itemHourlyRvBinding;
        }
    }
}

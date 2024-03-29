package com.driverskr.goodweather.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDLocation;
import com.driverskr.goodweather.Constant;
import com.driverskr.goodweather.R;
import com.driverskr.goodweather.databinding.DialogDailyDetailBinding;
import com.driverskr.goodweather.databinding.DialogHourlyDetailBinding;
import com.driverskr.goodweather.databinding.DialogLifeIndexDetailBinding;
import com.driverskr.goodweather.db.bean.AirResponse;
import com.driverskr.goodweather.db.bean.HourlyResponse;
import com.driverskr.goodweather.location.GoodLocation;
import com.driverskr.goodweather.ui.adapter.DailyAdapter;
import com.driverskr.goodweather.ui.adapter.HourlyAdapter;
import com.driverskr.goodweather.db.bean.DailyResponse;
import com.driverskr.goodweather.db.bean.LifestyleResponse;
import com.driverskr.goodweather.db.bean.NowResponse;
import com.driverskr.goodweather.db.bean.SearchCityResponse;
import com.driverskr.goodweather.databinding.ActivityMainBinding;
import com.driverskr.goodweather.location.LocationCallback;
import com.driverskr.goodweather.utils.CityDialog;
import com.driverskr.goodweather.utils.DialogShowStyle;
import com.driverskr.goodweather.utils.EasyDate;
import com.driverskr.goodweather.utils.GlideUtils;
import com.driverskr.goodweather.utils.MVUtils;
import com.driverskr.goodweather.utils.SizeUtils;
import com.driverskr.goodweather.viewmodel.MainViewModel;
import com.driverskr.library.base.NetworkActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends NetworkActivity<ActivityMainBinding> implements LocationCallback, CityDialog.SelectedCityCallback {

    private final static String TAG = MainActivity.class.getSimpleName();
    //权限数组
    private final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求权限意图:这个组件也是Jetpack中的，意图可以做的事情是很多的，请求权限只是其中之一
    private ActivityResultLauncher<String[]> requestPermissionIntent;

    //对于定位功能的封装
    private GoodLocation goodLocation;

    private MainViewModel viewModel;

    //天气预报数据和适配器
    private final List<DailyResponse.DailyBean> dailyBeanList = new ArrayList<>();
    private final DailyAdapter dailyAdapter = new DailyAdapter(dailyBeanList);

    //逐小时天气预报数据和适配器
    private final List<HourlyResponse.HourlyBean> hourlyBeanList = new ArrayList<>();
    private final HourlyAdapter hourlyAdapter = new HourlyAdapter(hourlyBeanList);

    //城市弹窗
    private CityDialog cityDialog;

    //菜单
    private Menu mMenu;
    //城市信息来源标识  0：定位，   1：切换城市
    private int cityFlag = 0;

    //城市名称，定位和切换城市都会重新赋值。
    private String mCityName;
    private String locationCity = "";//保存定位的城市
    //是否正在刷新
    private boolean isRefresh;

    //跳转Activity
    private ActivityResultLauncher<Intent> jumpActivityIntent;

    /**
     * 注册意图
     */
    @Override
    protected void onRegister() {
        //请求权限意图
        requestPermissionIntent = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result ->{
            boolean fineLocation = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
            boolean writeStorage = Boolean.TRUE.equals(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE));
            if (fineLocation && writeStorage) {
                //权限已经获取到，开始定位
                startLocation();
            }
        });
        //跳转Activity
        jumpActivityIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                //获取上个页面返回的数据
                String city = result.getData().getStringExtra(Constant.CITY_RESULT);
                //检查返回的城市 , 如果返回的城市是当前定位城市，并且当前定位标志为0，则不需要请求
                if (city.equals(MVUtils.getString(Constant.LOCATION_CITY)) && cityFlag == 0) {
                    Log.d(TAG, "onRegister: 管理城市页面返回不需要进行天气查询");
                    return;
                }
                //反之就直接调用选中城市的方法进行城市天气搜索
                Log.d(TAG, "onRegister: 管理城市页面返回进行天气查询");
                selectedCity(city);
            }
        });
    }

    /**
     * 初始化
     */
    @Override
    protected void onCreate() {
        //全屏沉浸式
        setFullScreenImmersion();
        //这个意图有一个特别的地方需要在Activity初始化之前进行注册
        initLocation();
        //请求权限
        requestPermission();
        //初始化视图
        initView();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        //获取城市数据
        viewModel.getAllCity();
    }

    private void initView() {

        //自定义Toolbar图标
        setToolbarMoreIconCustom(binding.materialToolbar);

        //天气预报列表
        binding.rvDaily.setLayoutManager(new LinearLayoutManager(this));
        dailyAdapter.setOnClickItemCallback(position -> showDailyDetailDialog(dailyBeanList.get(position)));
        binding.rvDaily.setAdapter(dailyAdapter);

        //逐小时天气预报列表
        LinearLayoutManager hourlyLayoutManager = new LinearLayoutManager(this);
        hourlyLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.rvHourly.setLayoutManager(hourlyLayoutManager);
        hourlyAdapter.setOnClickItemCallback(position -> showHourlyDetailDialog(hourlyBeanList.get(position)));
        binding.rvHourly.setAdapter(hourlyAdapter);

        //下拉刷新监听
        binding.layRefresh.setOnRefreshListener(() -> {
            if (mCityName == null) {
                binding.layRefresh.setRefreshing(false);
                return;
            }
            //设置正在刷新
            isRefresh = true;
            viewModel.searchCity(mCityName,true);
        });

        //滑动监听
        binding.layScroll.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                //getMeasuredHeight() 表示控件的绘制高度
                if (scrollY > binding.layScrollHeight.getMeasuredHeight()) {
                    binding.tvTitle.setText((mCityName == null ? "城市天气" : mCityName));
                    if (mCityName.equals(locationCity)){
                        binding.ivLocationTool.setVisibility(View.VISIBLE);
                    }
                }
            } else if (scrollY < oldScrollY) {
                if (scrollY < binding.layScrollHeight.getMeasuredHeight()) {
                    //改回原来的
                    binding.tvTitle.setText("城市天气");
                    if (mCityName.equals(locationCity)){
                        binding.ivLocationTool.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //更新壁纸
        updateBgImage(MVUtils.getBoolean(Constant.USED_BING),MVUtils.getString(Constant.BING_URL));
    }

    /**
     * 数据观察
     */
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    protected void onObserveData() {
        if (viewModel != null) {
            //城市数据返回
            viewModel.searchCityResponseMutableLiveData.observe(this,searchCityResponse -> {
                List<SearchCityResponse.LocationBean> location = searchCityResponse.getLocation();
                if (location != null && location.size() > 0) {
                    String id = location.get(0).getId();
                    //根据cityFlag设置重新定位菜单项是否显示
                    mMenu.findItem(R.id.item_relocation).setVisible(cityFlag == 1);
                    //检查到正在刷新
                    if (isRefresh) {
                        showMsg("刷新完成");
                        binding.layRefresh.setRefreshing(false);
                        isRefresh = false;
                    }
                    Log.d(TAG, "城市ID: " + id);
                    if (id != null) {
                        //通过城市ID查询城市实时天气
                        viewModel.nowWeather(id);
                        //通过城市ID查询天气预报
                        viewModel.dailyWeather(id);
                        //通过城市ID查询生活指数
                        viewModel.lifestyle(id);
                        //通过城市ID查询逐小时天气预报
                        viewModel.hourlyWeather(id);
                        //通过城市ID查询空气质量
                        viewModel.airWeather(id);
                    }
                }
            });
            //实况天气返回
            viewModel.nowResponseMutableLiveData.observe(this, nowResponse -> {
                NowResponse.NowBean now = nowResponse.getNow();
                if (now != null) {
                    binding.tvWeek.setText(EasyDate.getTodayOfWeek());//星期
                    binding.tvWeatherInfo.setText(now.getText());
                    binding.tvTemp.setText(now.getTemp());
                    //精简更新时间
                    String time = EasyDate.updateTime(nowResponse.getUpdateTime());
                    binding.tvUpdateTime.setText(String.format("最近更新时间：%s%s", EasyDate.showTimeInfo(time), time));

                    binding.tvWindDirection.setText(String.format("风向     %s", now.getWindDir()));//风向
                    binding.tvWindPower.setText(String.format("风力     %s级", now.getWindScale()));//风力
                    binding.wwBig.startRotate();//大风车开始转动
                    binding.wwSmall.startRotate();//小风车开始转动
                }
            });
            //天气预报返回
            viewModel.dailyResponseMutableLiveData.observe(this, dailyResponse -> {
                List<DailyResponse.DailyBean> daily = dailyResponse.getDaily();
                if (daily != null) {
                    if (dailyBeanList.size() > 0) {
                        dailyBeanList.clear();
                    }
                    dailyBeanList.addAll(daily);
                    dailyAdapter.notifyDataSetChanged();
                    //设置当天最高温和最低温
                    binding.tvHeight.setText(String.format("%s℃", daily.get(0).getTempMax()));
                    binding.tvLow.setText(String.format(" / %s℃", daily.get(0).getTempMin()));
                }
            });
            //生活指数返回
            viewModel.lifestyleResponseMutableLiveData.observe(this, lifestyleResponse -> {
                List<LifestyleResponse.DailyBean> daily = lifestyleResponse.getDaily();
                if (daily != null) {
                    initLifeIndex(daily);
                }
            });
            //获取本地城市数据返回
            viewModel.cityMutableLiveData.observe(this, provinces -> {
                //城市弹窗初始化
                cityDialog = CityDialog.getInstance(MainActivity.this, provinces);
                cityDialog.setSelectedCityCallback(this);
            });
            //逐小时天气预报返回
            viewModel.hourlyResponseMutableLiveData.observe(this, hourlyResponse -> {
                List<HourlyResponse.HourlyBean> hourly = hourlyResponse.getHourly();
                if (hourly != null) {
                    if (hourlyBeanList.size() > 0) {
                        hourlyBeanList.clear();
                    }
                    hourlyBeanList.addAll(hourly);
                    hourlyAdapter.notifyDataSetChanged();
                }
            });
            //空气质量返回
            viewModel.airResponseMutableLiveData.observe(this, airResponse -> {
                //隐藏加载窗口
                dismissLoadingDialog();

                AirResponse.NowBean now = airResponse.getNow();
                if (now == null) return;
                binding.rpbAqi.setMaxProgress(300);//最大进度，用于计算
                binding.rpbAqi.setMinText("0");//设置显示最小值
                binding.rpbAqi.setMinTextSize(32f);
                binding.rpbAqi.setMaxText("300");//设置显示最大值
                binding.rpbAqi.setMaxTextSize(32f);
                binding.rpbAqi.setProgress(Float.parseFloat(now.getAqi()));//当前进度
                binding.rpbAqi.setArcBgColor(getColor(R.color.arc_bg_color));//圆弧的颜色
                binding.rpbAqi.setProgressColor(getColor(R.color.arc_progress_color_nice));//进度圆弧的颜色
                binding.rpbAqi.setFirstText(now.getCategory());//空气质量描述 取值范围：优，良，轻度污染，中度污染，重度污染，严重污染
                binding.rpbAqi.setFirstTextSize(44f);//第一行文本的字体大小
                binding.rpbAqi.setSecondText(now.getAqi());//空气质量值
                binding.rpbAqi.setSecondTextSize(64f);//第二行文本的字体大小
                binding.rpbAqi.setMinText("0");
                binding.rpbAqi.setMinTextColor(getColor(R.color.arc_progress_color));

                binding.tvAirInfo.setText(String.format("空气%s", now.getCategory()));

                binding.tvPm10.setText(now.getPm10());//PM10
                binding.tvPm25.setText(now.getPm2p5());//PM2.5
                binding.tvNo2.setText(now.getNo2());//二氧化氮
                binding.tvSo2.setText(now.getSo2());//二氧化硫
                binding.tvO3.setText(now.getO3());//臭氧
                binding.tvCo.setText(now.getCo());//一氧化碳
            });
            //错误信息返回
            viewModel.failed.observe(this, this::showLongMsg);
        }
    }

    /**
     * 生成菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        //根据cityFlag设置重新定位菜单项是否显示
        mMenu.findItem(R.id.item_relocation).setVisible(cityFlag == 1);
        //根据使用必应壁纸的状态，设置item项是否选中
        mMenu.findItem(R.id.item_bing).setChecked(MVUtils.getBoolean(Constant.USED_BING));
        return true;
    }

    /**
     * 菜单item选择事件
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_switching_cities:    //切换城市
                if (cityDialog != null) cityDialog.show();
                break;
            case R.id.item_relocation:      //重新定位
                startLocation();//点击重新定位item时，再次定位一下。
                break;
            case R.id.item_bing:        //是否使用必应壁纸
                item.setChecked(!item.isChecked());
                MVUtils.put(Constant.USED_BING, item.isChecked());
                String bingUrl = MVUtils.getString(Constant.BING_URL);
                //更新壁纸
                updateBgImage(item.isChecked(), bingUrl);
                break;
            case R.id.item_manage_city:     //管理城市
                jumpActivityIntent.launch(new Intent(mContext, ManageCityActivity.class));
                break;
        }
        return true;
    }

    /**
     * 初始化定位
     */
    private void initLocation(){
        goodLocation = GoodLocation.getInstance(this);
        goodLocation.setCallback(this);
    }

    /**
     * 接收定位
     *
     * @param bdLocation 定位数据
     */
    @Override
    public void onRonReceiveLocation(BDLocation bdLocation) {
        //显示加载弹窗
        showLoadingDialog();

        /*获取各种定位信息**/
        /*double latitude = bdLocation.getLatitude(); //获取纬度信息
        double longitude = bdLocation.getLongitude();   //获取经度信息
        float radius = bdLocation.getRadius();  //获取定位精度，默认值为0.0f
        String coorType = bdLocation.getCoorType();
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
        int errorCode = bdLocation.getLocType();    //161  表示网络定位结果
        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        String addr = bdLocation.getAddrStr();  //获取详细地址信息
        String country = bdLocation.getCountry();   //获取国家
        String province = bdLocation.getProvince(); //获取省份
        String city = bdLocation.getCity();     //获取城市
        String street = bdLocation.getStreet();     //获取街道信息
        String locationDescribe = bdLocation.getLocationDescribe(); //获取位置描述信息*/

        String district = bdLocation.getDistrict();     //获取区县

        if (viewModel != null && district != null) {
            mCityName = district; //定位后重新赋值
            locationCity = district; // 保存定位城市
            //更新顶部标题
            if (!binding.tvTitle.getText().equals("城市天气")){
                binding.tvTitle.setText(district);
                binding.ivLocationTool.setVisibility(View.VISIBLE);
            }
            //保存定位城市
            MVUtils.put(Constant.LOCATION_CITY, district);
            //保存到我的城市数据中
            viewModel.addMyCityData(district);
            //显示当前定位城市
            binding.tvCity.setText(district);
            //显示定位图标
            binding.ivLocationIcon.setVisibility(View.VISIBLE);
            //搜索城市
            viewModel.searchCity(district,true);
        } else {
            Log.e(TAG, "district: " + district);
        }
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        cityFlag = 0;   //定位时，重新定位图标隐藏
        goodLocation.startLocation();
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        //因为项目的最低版本API是23，所以肯定需要动态请求危险权限，只需要判断权限是否拥有即可
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //开始权限请求
            requestPermissionIntent.launch(permissions);
            return;
        }
        //开始定位
        startLocation();
    }

    /**
     * 添加菜单，修改默认的三个点图标
     */
    public void setToolbarMoreIconCustom(Toolbar toolbar) {
        if (toolbar == null) return;
        toolbar.setTitle("");
        Drawable moreIcon = ContextCompat.getDrawable(toolbar.getContext(), R.drawable.ic_round_add_32);
        if (moreIcon != null ) toolbar.setOverflowIcon(moreIcon);
        setSupportActionBar(toolbar);
    }

    /**
     * 切换城市
     * @param cityName 想切换的城市
     */
    @Override
    public void selectedCity(String cityName) {
        cityFlag = 1; //切换城市后，显示重新定位图标
        mCityName = cityName;//切换城市后赋值
        //更新顶部标题
        if (!binding.tvTitle.getText().equals("城市天气")){
            binding.tvTitle.setText(cityName);
            binding.ivLocationTool.setVisibility(View.GONE);
        }

        if (cityName.equals(locationCity)) {
            binding.ivLocationIcon.setVisibility(View.VISIBLE);
        } else {
            binding.ivLocationIcon.setVisibility(View.GONE);
        }
        //搜索城市
        viewModel.searchCity(cityName, true);
        //显示所选城市
        binding.tvCity.setText(cityName);
    }

    /**
     * 更新背景图片
     */
    private void updateBgImage(boolean usedBing, String bingUrl) {
        if (usedBing && !bingUrl.isEmpty()) {
            GlideUtils.loadImg(this, bingUrl, binding.layRoot);
        } else {
            binding.layRoot.setBackground(ContextCompat.getDrawable(this, R.drawable.main_bg));
        }
    }

    /**
     * 显示天气预报详情弹窗
     */
    private void showDailyDetailDialog(DailyResponse.DailyBean dailyBean) {
        BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        DialogDailyDetailBinding detailBinding = DialogDailyDetailBinding.inflate(LayoutInflater.from(MainActivity.this), null, false);
        //关闭弹窗
        detailBinding.ivClose.setOnClickListener(v -> dialog.dismiss());
        //设置数据显示
        detailBinding.toolbarDaily.setTitle(String.format("%s   %s", dailyBean.getFxDate(), EasyDate.getWeek(dailyBean.getFxDate())));
        detailBinding.toolbarDaily.setSubtitle("天气预报详情");
        detailBinding.tvTmpMax.setText(String.format("%s℃", dailyBean.getTempMax()));
        detailBinding.tvTmpMin.setText(String.format("%s℃", dailyBean.getTempMin()));
        detailBinding.tvUvIndex.setText(dailyBean.getUvIndex());
        detailBinding.tvCondTxtD.setText(dailyBean.getTextDay());
        detailBinding.tvCondTxtN.setText(dailyBean.getTextNight());
        detailBinding.tvWindDeg.setText(String.format("%s°", dailyBean.getWind360Day()));
        detailBinding.tvWindDir.setText(dailyBean.getWindDirDay());
        detailBinding.tvWindSc.setText(String.format("%s级", dailyBean.getWindScaleDay()));
        detailBinding.tvWindSpd.setText(String.format("%s公里/小时", dailyBean.getWindSpeedDay()));
        detailBinding.tvCloud.setText(String.format("%s%%", dailyBean.getCloud()));
        detailBinding.tvHum.setText(String.format("%s%%", dailyBean.getHumidity()));
        detailBinding.tvPres.setText(String.format("%shPa", dailyBean.getPressure()));
        detailBinding.tvPcpn.setText(String.format("%smm", dailyBean.getPrecip()));
        detailBinding.tvVis.setText(String.format("%skm", dailyBean.getVis()));
        dialog.setContentView(detailBinding.getRoot());
        dialog.show();
    }

    /**
     * 显示逐小时天气预报详情弹窗
     */
    private void showHourlyDetailDialog(HourlyResponse.HourlyBean hourlyBean) {
        BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        DialogHourlyDetailBinding detailBinding = DialogHourlyDetailBinding.inflate(LayoutInflater.from(MainActivity.this), null, false);
        //关闭弹窗
        detailBinding.ivClose.setOnClickListener(v -> dialog.dismiss());
        //设置数据显示
        String time = EasyDate.updateTime(hourlyBean.getFxTime());
        detailBinding.toolbarHourly.setTitle(EasyDate.showTimeInfo(time) + time);
        detailBinding.toolbarHourly.setSubtitle("逐小时预报详情");
        detailBinding.tvTmp.setText(String.format("%s℃", hourlyBean.getTemp()));
        detailBinding.tvCondTxt.setText(hourlyBean.getText());
        detailBinding.tvWindDeg.setText(String.format("%s°", hourlyBean.getWind360()));
        detailBinding.tvWindDir.setText(hourlyBean.getWindDir());
        detailBinding.tvWindSc.setText(String.format("%s级", hourlyBean.getWindScale()));
        detailBinding.tvWindSpd.setText(String.format("公里/小时%s", hourlyBean.getWindSpeed()));
        detailBinding.tvHum.setText(String.format("%s%%", hourlyBean.getHumidity()));
        detailBinding.tvPres.setText(String.format("%shPa", hourlyBean.getPressure()));
        detailBinding.tvPop.setText(String.format("%s%%", hourlyBean.getPop()));
        detailBinding.tvDew.setText(String.format("%s℃", hourlyBean.getDew()));
        detailBinding.tvCloud.setText(String.format("%s%%", hourlyBean.getCloud()));
        dialog.setContentView(detailBinding.getRoot());
        dialog.show();
    }

    /**
     * ”生活建议“数据填充
     * @param lifestyleList 数据包
     */
    private void initLifeIndex(List<LifestyleResponse.DailyBean> lifestyleList){
        binding.liveIndex.sportText.setText(lifestyleList.get(0).getCategory());
        binding.liveIndex.carwashingText.setText(lifestyleList.get(1).getCategory());
        binding.liveIndex.dressingText.setText(lifestyleList.get(2).getCategory());
        binding.liveIndex.fishingText.setText(lifestyleList.get(3).getCategory());
        binding.liveIndex.ultravioletText.setText(lifestyleList.get(4).getCategory());
        binding.liveIndex.tourText.setText(lifestyleList.get(5).getCategory());
        binding.liveIndex.coldriskText.setText(lifestyleList.get(8).getCategory());
        binding.liveIndex.comfortText.setText(lifestyleList.get(7).getCategory());

        //绑定点击事件
        binding.liveIndex.rlSport.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(0)));
        binding.liveIndex.rlCarWashing.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(1)));
        binding.liveIndex.rlDressing.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(2)));
        binding.liveIndex.rlFishing.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(3)));
        binding.liveIndex.rlUltraviolet.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(4)));
        binding.liveIndex.rlTour.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(5)));
        binding.liveIndex.rlColdRisk.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(8)));
        binding.liveIndex.rlComfort.setOnClickListener(v -> onClickLifeItem(lifestyleList.get(7)));
    }

    /**
     * 对"生活指数“弹窗的数据填充
     * @param dailyBean 数据源
     */
    private void onClickLifeItem(LifestyleResponse.DailyBean dailyBean){
        DialogShowStyle dialogShowStyle = new DialogShowStyle(mContext);

        DialogLifeIndexDetailBinding binding = DialogLifeIndexDetailBinding.inflate(LayoutInflater.from(mContext));
        binding.tvTitle.setText(dailyBean.getName());
        binding.tvKind.setText(dailyBean.getCategory());
        binding.tvText.setText(dailyBean.getText());

        switch (dailyBean.getType()){
            case "2":
                binding.imgContent.setImageResource(R.drawable.img_car_washing);
                break;
            case "3":
                binding.imgContent.setImageResource(R.drawable.img_dressing);
                break;
            case "4":
                binding.imgContent.setImageResource(R.drawable.img_fishing);
                break;
            case "5":
                binding.imgContent.setImageResource(R.drawable.img_ultraviolet);
                break;
            case "6":
                binding.imgContent.setImageResource(R.drawable.img_tour);
                break;
            case "8":
                binding.imgContent.setImageResource(R.drawable.img_comfort);
                break;
            case "9":
                binding.imgContent.setImageResource(R.drawable.img_cold_risk);
                break;
            default:
                binding.imgContent.setImageResource(R.drawable.img_sport);
                break;
        }

        dialogShowStyle.showCenterPopupWindow(binding.getRoot(), SizeUtils.dp2px(mContext, 250), SizeUtils.dp2px(mContext, 220), true);
    }
}
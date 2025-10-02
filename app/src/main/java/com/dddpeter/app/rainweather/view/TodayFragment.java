package com.dddpeter.app.rainweather.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
// import android.content.SharedPreferences; // 不再需要
import android.os.Build;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.H24Activity;
import com.dddpeter.app.rainweather.IndexActivity;
import com.dddpeter.app.rainweather.MainActivity;
import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.common.DataUtil;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.xuexiang.xui.XUI;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint({"DefaultLocale", "NonConstantResourceId", "UseCompatLoadingForDrawables"})
public class TodayFragment extends Fragment {

    LinearLayout topInfo;
    ImageView image;
    TextView city;
    TextView type;
    TextView wendu;
    TextView hpa;
    TextView wind;
    TextView ganmao;
    TextView shidu;
    LineChart temperatureTrendChart;
    LinearLayout topPic;
    Button h24Btn;
    Button mainBtn;
    Button backBtn;
    ImageView refreshIcon;
    
    // 流式布局中的新UI元素
    TextView airq;
    TextView airq1;
    TextView currentTemp;
    TextView humidityValue;
    TextView windSpeed;
    TextView windDirection;
    TextView pressureValue;
    TextView uvIndex;
    TextView uvLevel;

    private DatabaseManager databaseManager;
    String cityName;
    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH)) {
                Log.d("TodayFragment", "收到刷新广播");
                try {
                    renderContent();
                    renderRecent();
                    Log.d("TodayFragment", "刷新完成");
                    
                    // 显示刷新成功Toast
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "天气数据刷新成功！", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.w("TodayFragment", "刷新异常: ", e);
                    
                    // 显示刷新失败Toast
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "刷新失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
    View.OnClickListener l1 = v -> {
        Intent intent = new Intent(getContext(), H24Activity.class);
        intent.putExtra("IS_FROM_HOME", true);
        startActivity(intent);
    };
    View.OnClickListener l2 = v -> {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("IS_FROM_HOME", true);
        startActivity(intent);
    };
    View.OnClickListener l3 = v -> {
        Intent intent = new Intent(getContext(), IndexActivity.class);
        ParamApplication application = (ParamApplication) requireActivity().getApplication();
        intent.putExtra("currentTab", Objects.requireNonNull(application).getTAB_TAG_RECENT());
        startActivity(intent);
    };

    private void findViwes(View me) {
        topInfo = me.findViewById(R.id.top_info);
        image = me.findViewById(R.id.imageView1);
        city = me.findViewById(R.id.city);
        type = me.findViewById(R.id.type);
        wendu = me.findViewById(R.id.wendu);
        hpa = me.findViewById(R.id.hpa);
        wind = me.findViewById(R.id.wind);
        ganmao = me.findViewById(R.id.ganmao);
        shidu = me.findViewById(R.id.shidu);
        temperatureTrendChart = me.findViewById(R.id.temperature_trend_chart);
        
        // 初始化温度趋势图表
        initTemperatureTrendChart();
        topPic = me.findViewById(R.id.top_pic);
        h24Btn = me.findViewById(R.id.h24_btn);
        mainBtn = me.findViewById(R.id.main_btn);
        backBtn = me.findViewById(R.id.back_btn);
        refreshIcon = me.findViewById(R.id.refresh_icon);
        
        // 流式布局中的新UI元素

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, viewGroup, false);
        findViwes(view);
        LinearLayout infoB = view.findViewById(R.id.info_b);
        databaseManager = DatabaseManager.getInstance(getContext());
        cityName = requireActivity().getIntent().getStringExtra("city");
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        // 设置刷新图标点击事件
        if (refreshIcon != null) {
            refreshIcon.setOnClickListener(v -> {
                Log.d("TodayFragment", "用户点击刷新图标");
                refreshWeatherData();
            });
        }
        
        if (cityName == null || cityName.trim().equals("")) {
            h24Btn.setOnClickListener(l1);
            mainBtn.setOnClickListener(l2);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CacheKey.REFRESH);
            // Android 14+ 需要指定 RECEIVER_NOT_EXPORTED 标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ContextCompat.registerReceiver(requireContext(), mRefreshBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
            }
        } else {
            h24Btn.setVisibility(View.GONE);
            mainBtn.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(l3);
        }

        int[] size = CommonUtil.getScreenSize(requireActivity());
        int y = size[1];
        if (y >= 1500) {
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    (int) (y * 0.95 / 2.0)
            );
            topPic.setLayoutParams(params1);
            infoB.setPadding(0, (int) (y * 0.7 / 20), 0, 0);
        }
        
        // 立即尝试渲染内容
        try {
            renderContent();
            renderRecent();
        } catch (Exception e) {
            Log.w("TodayFragment", "onCreateView渲染失败: ", e);
        }
        
        // 延迟刷新，确保数据已加载
        view.postDelayed(() -> {
            try {
                renderContent();
                renderRecent();
                Log.d("TodayFragment", "延迟刷新完成");
            } catch (Exception e) {
                Log.w("TodayFragment", "延迟刷新失败: ", e);
            }
        }, 500); // 延迟500ms
        
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        try {
            renderContent();
            renderRecent();
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

    }

    /**
     * 刷新天气数据
     */
    private void refreshWeatherData() {
        Log.d("TodayFragment", "开始刷新天气数据");
        
        // 显示刷新状态
        if (refreshIcon != null) {
            refreshIcon.setEnabled(false);
            refreshIcon.setAlpha(0.5f);
            startRefreshAnimation();
        }
        
        // 显示Toast提示
        if (getContext() != null) {
            Toast.makeText(getContext(), "正在刷新天气数据...", Toast.LENGTH_SHORT).show();
        }
        
        // 启动后台定位服务进行刷新
        if (getActivity() != null) {
            Intent serviceIntent = new Intent(getActivity(), com.dddpeter.app.rainweather.service.BackgroundLocationService.class);
            getActivity().startService(serviceIntent);
        }
        
        // 延迟恢复刷新图标状态
        if (refreshIcon != null) {
            refreshIcon.postDelayed(() -> {
                refreshIcon.setEnabled(true);
                refreshIcon.setAlpha(1.0f);
                stopRefreshAnimation();
                Log.d("TodayFragment", "刷新图标状态已恢复");
            }, 3000); // 3秒后恢复
        }
    }
    
    /**
     * 开始刷新动画
     */
    private void startRefreshAnimation() {
        if (refreshIcon != null && getContext() != null) {
            android.view.animation.RotateAnimation rotateAnimation = new android.view.animation.RotateAnimation(
                0f, 360f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(android.view.animation.Animation.INFINITE);
            rotateAnimation.setRepeatMode(android.view.animation.Animation.RESTART);
            refreshIcon.startAnimation(rotateAnimation);
        }
    }
    
    /**
     * 停止刷新动画
     */
    private void stopRefreshAnimation() {
        if (refreshIcon != null) {
            refreshIcon.clearAnimation();
        }
    }
    
    /**
     * 根据AQI值设置空气指数颜色
     */
    private void setAirQualityColor(int aqiValue) {
        if (airq == null || airq1 == null) return;
        
        int colorRes;
        String level;
        
        if (aqiValue <= 50) {
            colorRes = R.color.air_quality_excellent;
            level = "优";
        } else if (aqiValue <= 100) {
            colorRes = R.color.air_quality_good;
            level = "良";
        } else if (aqiValue <= 150) {
            colorRes = R.color.air_quality_moderate;
            level = "轻度污染";
        } else if (aqiValue <= 200) {
            colorRes = R.color.air_quality_unhealthy;
            level = "中度污染";
        } else if (aqiValue <= 300) {
            colorRes = R.color.air_quality_hazardous;
            level = "重度污染";
        } else {
            colorRes = R.color.air_quality_hazardous;
            level = "严重污染";
        }
        
        if (getContext() != null) {
            int color = getContext().getResources().getColor(colorRes, null);
            airq.setTextColor(color);
            airq1.setTextColor(color);
            airq1.setText(level);
        }
    }

    /**
     * 显示默认内容
     */
    private void showDefaultContent(String cityName) {
        Log.d("TodayFragment", "显示默认内容，城市: " + cityName);
        
        // 设置城市名称
        if (city != null) {
            city.setText(cityName);
        }
        
        // 设置默认天气信息
        if (type != null) {
            type.setText("获取中...");
        }
        
        if (wendu != null) {
            wendu.setText("--°");
        }
        
        if (wind != null) {
            wind.setText("风力: --");
        }
        
        if (shidu != null) {
            shidu.setText("湿度: --%");
        }
        
        if (hpa != null) {
            hpa.setText("压强: --hpa");
        }
        
        // 设置流式布局的默认值
        if (airq != null) {
            airq.setText("--");
        }
        if (airq1 != null) {
            airq1.setText("--");
        }
        if (currentTemp != null) {
            currentTemp.setText("--°");
        }
        if (humidityValue != null) {
            humidityValue.setText("--%");
        }
        if (windSpeed != null) {
            windSpeed.setText("--");
        }
        if (windDirection != null) {
            windDirection.setText("--");
        }
        if (pressureValue != null) {
            pressureValue.setText("--");
        }
        if (uvIndex != null) {
            uvIndex.setText("--");
        }
        if (uvLevel != null) {
            uvLevel.setText("--");
        }
        
        if (ganmao != null) {
            ganmao.setText("正在获取天气数据，请稍候...");
        }
    }

    public void renderContent() {
        Log.d("TodayFragment", "开始渲染内容");
        JSONObject wAllJson;
        String cityCurrent;
        if (cityName == null || cityName.trim().equals("")) {
            //  JSONObject cityJson  = mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION);
            LocationVO locationVO = databaseManager.getLocationVO(CacheKey.CURRENT_LOCATION);
            if (locationVO != null) {
                cityCurrent = locationVO.getDistrict();
                Log.d("TodayFragment", "使用定位信息，城市: " + cityCurrent);
            } else {
                Log.w("TodayFragment", "定位信息为空，使用默认位置");
                cityCurrent = "东城区";
            }
        } else {
            cityCurrent = cityName;
            Log.d("TodayFragment", "使用传入城市名: " + cityCurrent);
        }
        String weatherJsonString = databaseManager.getCacheData(cityCurrent + ":" + CacheKey.WEATHER_ALL);
        Log.d("TodayFragment", "获取天气数据，城市: " + cityCurrent + ", 数据长度: " + (weatherJsonString != null ? weatherJsonString.length() : 0));
        
        try {
            // 检查weatherJsonString是否为null
            if (weatherJsonString == null) {
                Log.w("TodayFragment", "天气数据为空，显示默认内容");
                showDefaultContent(cityCurrent);
                return;
            }
            
            wAllJson = new JSONObject(weatherJsonString);
            
            // 检查wAllJson是否为null
            if (wAllJson == null) {
                Log.w("TodayFragment", "天气数据解析失败，显示默认内容");
                showDefaultContent(cityCurrent);
                return;
            }
            
            //JSONObject today = ((JSONObject) weatherJson.getJSONArray("forecast").get(0));
            JSONObject currentObj = wAllJson.optJSONObject("current");
            if (currentObj == null) {
                Log.w("TodayFragment", "current对象为空");
                return;
            }
            
            // 空气质量指数相关UI已移除，不再需要airJson
            
            JSONObject current = currentObj.optJSONObject("current");
            if (current == null) {
                Log.w("TodayFragment", "current.current对象为空");
                return;
            }
            String tempratureStr = current.getString("temperature") + "°";
            String pressureStr = "压强:" + current.getString("airpressure") + "hpa";
            String humidityStr = "湿度:" + current.getString("humidity") + "%";

            String wtype = current.getString("weather");
            city.setTypeface(CommonUtil.weatherIconFontFace(requireContext()));
            type.setTypeface(CommonUtil.weatherIconFontFace(requireContext()));
            city.setText(Html.fromHtml((cityName == null || cityName.trim().equals("") ? "<font>\ue71b</font> " : "") +
                    cityCurrent, Html.FROM_HTML_MODE_LEGACY));
            type.setText(Html.fromHtml(wtype, Html.FROM_HTML_MODE_LEGACY));

            wendu.setText(tempratureStr);
            hpa.setText(pressureStr);
            wind.setText(Html.fromHtml(current.getString("winddir") + current.getString("windpower"),
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            shidu.setText(humidityStr);
            ganmao.setText(wAllJson.getJSONObject("current").getString("tips"));
            
            // 设置流式布局中的新UI元素
            updateFlowLayoutData(current, wAllJson);
            // 使用数据库管理器替代SharedPreferences
            if (DataUtil.isDay()) {
                getContext();
                // preferences = requireContext().getSharedPreferences("day_picture", Context.MODE_PRIVATE);
            } else {
                getContext();
                // preferences = requireContext().getSharedPreferences("night_picture", Context.MODE_PRIVATE);
                topPic.setBackgroundColor(getResources().getColor(R.color.skyblue_night, null));
                topInfo.setBackground(getResources().getDrawable(R.drawable.nbackground, null));
            }
            if (wtype.equals("阴天") || wtype.equals("多云") || wtype.equals("阴")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skygrey, null));
            }
            if (wtype.contains("雨") && !wtype.contains("转")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skyrain, null));
            }
            if (wtype.contains("雪") && !wtype.contains("转")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skysnow, null));
            }
            if (wtype.equals("沙尘暴") || wtype.equals("扬沙") || wtype.equals("浮尘")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skydust, null));
            }
            String weatherImg;
            if (DataUtil.isDay()) {
                weatherImg = ParamApplication.databaseManager.getString("day_picture:" + current.getString("weather"), "notclear.png");
            } else {
                weatherImg = ParamApplication.databaseManager.getString("night_picture:" + current.getString("weather"), "notclear.png");
            }
            image.setImageDrawable(CommonUtil.drawableFromAssets(requireContext(), weatherImg));
        } catch (JSONException e) {
            Log.w("TodayFragment", "JSON解析异常: ", e);
        } catch (Exception e) {
            Log.w("TodayFragment", "其他异常: ", e);
        }

    }

    /**
     * 更新流式布局中的数据
     */
    private void updateFlowLayoutData(JSONObject current, JSONObject wAllJson) {
        try {
            // 设置空气质量指数
            if (wAllJson.has("air")) {
                JSONObject airJson = wAllJson.getJSONObject("air");
                if (airJson != null && airJson.has("AQI")) {
                    int aqiValue = airJson.getInt("AQI");
                    if (airq != null) {
                        airq.setText(String.valueOf(aqiValue));
                    }
                    setAirQualityColor(aqiValue);
                }
            }
            
            // 设置当前温度
            if (currentTemp != null && current.has("temperature")) {
                currentTemp.setText(current.getString("temperature") + "°");
            }
            
            // 设置湿度
            if (humidityValue != null && current.has("humidity")) {
                humidityValue.setText(current.getString("humidity") + "%");
            }
            
            // 设置风速和风向
            if (windSpeed != null && current.has("windpower")) {
                windSpeed.setText(current.getString("windpower"));
            }
            if (windDirection != null && current.has("winddir")) {
                windDirection.setText(current.getString("winddir"));
            }
            
            // 设置气压
            if (pressureValue != null && current.has("airpressure")) {
                pressureValue.setText(current.getString("airpressure"));
            }
            
            // 设置紫外线指数（如果有的话）
            if (uvIndex != null) {
                uvIndex.setText("5"); // 默认值，实际应该从API获取
            }
            if (uvLevel != null) {
                uvLevel.setText("中等"); // 默认值，实际应该根据UV指数计算
            }
            
        } catch (JSONException e) {
            Log.w("TodayFragment", "更新流式布局数据时发生JSON异常: ", e);
        }
    }

    private void setAirColor(Integer aqi, String index) {
        // 空气质量指数相关UI已移除
    }

    public void renderRecent() {
        try {
            String cityCurrent;
        if (cityName == null || cityName.trim().equals("")) {
            LocationVO locationVO = databaseManager.getLocationVO(CacheKey.CURRENT_LOCATION);
            if (locationVO != null) {
                cityCurrent = locationVO.getDistrict();
            } else {
                Log.w("TodayFragment", "定位信息为空，使用默认位置");
                cityCurrent = "东城区";
            }
        } else {
            cityCurrent = cityName;
        }
        String weatherJsonString = databaseManager.getCacheData(cityCurrent + ":" + CacheKey.WEATHER_ALL);
        
        // 检查weatherJsonString是否为null
        if (weatherJsonString == null) {
            Log.w("TodayFragment", "天气数据为空，无法渲染近期数据");
            return;
        }
        
        JSONObject weatherJson = new JSONObject(weatherJsonString);
        
        // 检查weatherJson是否为null
        if (weatherJson == null) {
            Log.w("TodayFragment", "天气数据为空，无法渲染近期数据");
            return;
        }
        
        JSONArray recentArray = weatherJson.optJSONArray("forecast15d");
        if (recentArray == null) {
            Log.w("TodayFragment", "forecast15d数组为空");
            return;
        }
        // 更新温度趋势图表
        updateTemperatureTrendChart(recentArray);
        // 通过Activity类中的getWindowManager()方法获取窗口管理，再调用getDefaultDisplay()方法获取获取Display对象
        } catch (JSONException e) {
            Log.w("TodayFragment", "JSON解析异常: ", e);
        } catch (Exception e) {
            Log.w("TodayFragment", "其他异常: ", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            requireContext().unregisterReceiver(mRefreshBroadcastReceiver);
        } catch (Exception e) {

        }
    }

    /**
     * 初始化温度趋势图表
     */
    private void initTemperatureTrendChart() {
        if (temperatureTrendChart == null) return;
        
        // 基本设置 - 禁用所有交互功能
        temperatureTrendChart.setTouchEnabled(false);
        temperatureTrendChart.setDragEnabled(false);
        temperatureTrendChart.setScaleEnabled(false);
        temperatureTrendChart.setPinchZoom(false);
        temperatureTrendChart.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        // 设置图表边框，与卡片边框保持一致
        temperatureTrendChart.setBorderWidth(0.3f); // 0.3dp边框
        temperatureTrendChart.setBorderColor(android.graphics.Color.parseColor("#33FFFFFF")); // 与卡片边框颜色一致
        
        // 隐藏图例
        temperatureTrendChart.getLegend().setEnabled(false);
        
        // 隐藏描述
        temperatureTrendChart.getDescription().setEnabled(false);
        
        // 设置X轴
        XAxis xAxis = temperatureTrendChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(android.graphics.Color.WHITE);
        xAxis.setTextSize(8f); // 减小字体大小
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelCount(7, true);
        xAxis.setYOffset(2f); // 减少X轴标签与轴线的距离
        
        // 设置Y轴
        YAxis leftAxis = temperatureTrendChart.getAxisLeft();
        leftAxis.setTextColor(android.graphics.Color.WHITE);
        leftAxis.setTextSize(8f); // 减小字体大小
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(android.graphics.Color.parseColor("#33FFFFFF"));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f); // 设置Y轴最小值
        leftAxis.setXOffset(10f); // 增加Y轴标签与轴线的距离
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value) + "°C";
            }
        });
        
        YAxis rightAxis = temperatureTrendChart.getAxisRight();
        rightAxis.setEnabled(false);
        
        // 设置动画
        temperatureTrendChart.animateX(1000);
    }

    /**
     * 更新温度趋势图表数据
     */
    private void updateTemperatureTrendChart(JSONArray recentArray) {
        if (temperatureTrendChart == null || recentArray == null || recentArray.length() <= 1) return;
        
        try {
            List<Entry> highTempEntries = new ArrayList<>();
            List<Entry> lowTempEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            
            // 限制为7天数据，从索引1开始（跳过今天）
            int maxDays = Math.min(8, recentArray.length()); // 最多取7天（索引1-7）
            Log.d("TodayFragment", "温度图表数据：总天数=" + recentArray.length() + ", 显示天数=" + (maxDays - 1));
            for (int i = 1; i < maxDays; i++) {
                int j = i - 1;
                JSONObject day = recentArray.getJSONObject(i);
                
                // 解析温度数据
                String lowTempStr = day.getString("temperature_pm")
                        .replace("低温", "")
                        .replace(" ", "")
                        .replace("℃", "");
                String highTempStr = day.getString("temperature_am")
                        .replace("高温", "")
                        .replace(" ", "")
                        .replace("℃", "");
                
                float lowTemp = Float.parseFloat(lowTempStr);
                float highTemp = Float.parseFloat(highTempStr);
                
                // 解析日期
                String forecastTime = day.getString("forecasttime");
                String dateLabel = extractDateFromTime(forecastTime);
                
                highTempEntries.add(new Entry(j, highTemp));
                lowTempEntries.add(new Entry(j, lowTemp));
                labels.add(dateLabel);
                
                Log.d("TodayFragment", "添加第" + (j+1) + "天数据: " + dateLabel + " 高温=" + highTemp + "°C 低温=" + lowTemp + "°C");
            }
            
            Log.d("TodayFragment", "温度图表数据收集完成: 共" + highTempEntries.size() + "天数据");
            
            // 创建最高温度数据集
            LineDataSet highTempDataSet = new LineDataSet(highTempEntries, "最高温度");
            highTempDataSet.setColor(getResources().getColor(R.color.weather_chart_high_temp, null));
            highTempDataSet.setLineWidth(3f);
            highTempDataSet.setCircleColor(getResources().getColor(R.color.weather_chart_high_temp, null));
            highTempDataSet.setCircleRadius(5f);
            highTempDataSet.setDrawCircleHole(false);
            highTempDataSet.setValueTextColor(android.graphics.Color.WHITE);
            highTempDataSet.setValueTextSize(7f); // 进一步减小字体大小
            highTempDataSet.setDrawValues(true);
            highTempDataSet.setValueTypeface(Typeface.DEFAULT); // 使用默认细字体
            highTempDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value) + "°";
                }
            });
            
            // 创建最低温度数据集
            LineDataSet lowTempDataSet = new LineDataSet(lowTempEntries, "最低温度");
            lowTempDataSet.setColor(getResources().getColor(R.color.weather_chart_low_temp, null));
            lowTempDataSet.setLineWidth(3f);
            lowTempDataSet.setCircleColor(getResources().getColor(R.color.weather_chart_low_temp, null));
            lowTempDataSet.setCircleRadius(5f);
            lowTempDataSet.setDrawCircleHole(false);
            lowTempDataSet.setValueTextColor(android.graphics.Color.WHITE);
            lowTempDataSet.setValueTextSize(7f); // 进一步减小字体大小
            lowTempDataSet.setDrawValues(true);
            lowTempDataSet.setValueTypeface(Typeface.DEFAULT); // 使用默认细字体
            lowTempDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value) + "°";
                }
            });
            
            // 创建数据
            LineData lineData = new LineData(highTempDataSet, lowTempDataSet);
            temperatureTrendChart.setData(lineData);
            
            // 设置X轴标签
            temperatureTrendChart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < labels.size()) {
                        return labels.get(index);
                    }
                    return "";
                }
            });
            
            // 刷新图表
            temperatureTrendChart.invalidate();
            
        } catch (Exception e) {
            Log.e("TodayFragment", "更新温度趋势图表失败", e);
        }
    }

    /**
     * 从时间字符串中提取日期部分
     */
    private String extractDateFromTime(String forecastTime) {
        if (forecastTime == null || forecastTime.trim().isEmpty()) {
            return "今天";
        }
        
        try {
            // 处理不同的时间格式
            String[] timeParts = forecastTime.split(" ");
            
            // 情况1: "2024-01-15 14:00:00" 或 "2024-01-15 14:00"
            if (timeParts.length > 0) {
                String datePart = timeParts[0];
                if (datePart.contains("-")) {
                    String[] dateComponents = datePart.split("-");
                    if (dateComponents.length >= 3) {
                        // 返回月-日格式
                        return dateComponents[1] + "-" + dateComponents[2];
                    }
                }
            }
            
            // 如果无法解析，返回原字符串的前10个字符
            return forecastTime.length() > 10 ? forecastTime.substring(0, 10) : forecastTime;
            
        } catch (Exception e) {
            Log.e("TodayFragment", "解析日期失败: " + forecastTime, e);
        }
        
        return "今天";
    }
}

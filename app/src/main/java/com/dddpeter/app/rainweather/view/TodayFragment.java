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

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

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
    RelativeLayout recent;
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
        recent = me.findViewById(R.id.recent_today);
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
                requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter);
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
        getContext();
        // 使用数据库管理器替代SharedPreferences
        int len = recentArray.length();
        int l = len - 1;
        String[] high = new String[l];
        String[] low = new String[l];
        int[] highInt = new int[l];
        int[] lowInt = new int[l];
        String[] weathers = new String[l];
        String[] days = new String[l];
        // 1, 构造显示用渲染图
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        // 2,进行显示
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // 2.1, 构建数据
        XYSeries seriesHigh = new XYSeries("最高温度");
        XYSeries seriesLow = new XYSeries("最低温度");
        for (int i = 1; i < len; i++) {
            int j = i - 1;
            JSONObject day = recentArray.getJSONObject(i);
            days[j] = day.getString("forecasttime");
            weathers[j] = day.getString("weather_am");
            low[j] = day.getString("temperature_pm")
                    .replace("低温", "")
                    .replace(" ", "")
                    .replace("℃", "");
            high[j] = day.getString("temperature_am")
                    .replace("高温", "")
                    .replace(" ", "")
                    .replace("℃", "");
            lowInt[j] = Integer.parseInt(low[j]);
            highInt[j] = Integer.parseInt(high[j]);
            String weather = ParamApplication.databaseManager.getString("weahter_icon:" + weathers[j], "\ue73e");
            renderer.addXTextLabel(j, days[j] + "\n" + weather);
            seriesHigh.add(j, highInt[j]);
            seriesLow.add(j, lowInt[j]);

            //System.out.println(Integer.parseInt(temps[0].trim()));
        }
        int max = CommonUtil.maxOfAarray(highInt) + 5;
        int min = CommonUtil.minOfAarray(lowInt) - 5;
        renderer.setTextTypeface(CommonUtil.weatherIconFontFace(getContext()));
        renderer.setAxesColor(this.getResources().getColor(R.color.weather_chart_title, null));
        renderer.setZoomEnabled(true, true);
        renderer.setPanEnabled(false, false);
        renderer.setXLabels(0);
        renderer.setExternalZoomEnabled(true);
        renderer.setAxisTitleTextSize(20);
        renderer.setLegendTextSize(16);
        renderer.setChartTitleTextSize(18);
        renderer.setMarginsColor(this.getResources().getColor(R.color.weather_chart_card_bg, null));
        renderer.setLabelsColor(this.getResources().getColor(R.color.weather_chart_legend, null));
        renderer.setXLabelsColor(this.getResources().getColor(R.color.weather_chart_legend, null));
        renderer.setGridColor(this.getResources().getColor(R.color.weather_chart_grid, null));
        renderer.setYTitle("温度(℃)");
        renderer.setApplyBackgroundColor(true);
        renderer.setFitLegend(true);
        renderer.setLabelsTextSize(20);
        renderer.setMargins(new int[]{50, 50, 70, 50});//设置图表的外边框(上/左/下/右)
        renderer.setZoomRate(1.1f);
        renderer.setPointSize(10);
        renderer.setLabelsTextSize(25);
        renderer.setAxisTitleTextSize(25);
        renderer.setChartTitleTextSize(35);
        renderer.setShowGrid(true);
        renderer.setRange(new double[]{-0.5, 7.5, min, max});
        renderer.setYLabelsAlign(Paint.Align.LEFT);
        renderer.setYLabelsColor(0, this.getResources().getColor(R.color.tips, null));
        dataset.addSeries(seriesLow);
        dataset.addSeries(seriesHigh);
        // 3, 对点的绘制进行设置
        XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
        // 3.1设置颜色

        xyRenderer.setColor(this.getResources().getColor(R.color.weather_chart_low_temp, null));
        xyRenderer.setDisplayChartValues(true);
        xyRenderer.setFillPoints(true);
        xyRenderer.setLineWidth(5);
        xyRenderer.setChartValuesTextAlign(Paint.Align.CENTER);
        xyRenderer.setChartValuesTextSize(25);
        xyRenderer.setChartValuesSpacing(20);
        // 3.2设置点的样式
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        // 3.3, 将要绘制的点添加到坐标绘制中
        renderer.addSeriesRenderer(xyRenderer);
        // 3.4,重复 1~3的步骤绘制第二个系列点
        xyRenderer = new XYSeriesRenderer();
        xyRenderer.setColor(this.getResources().getColor(R.color.weather_chart_high_temp, null));
        xyRenderer.setDisplayChartValues(true);
        xyRenderer.setFillPoints(true);
        xyRenderer.setLineWidth(5);
        xyRenderer.setChartValuesTextAlign(Paint.Align.CENTER);
        xyRenderer.setChartValuesTextSize(25);
        xyRenderer.setChartValuesSpacing(20);
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        renderer.addSeriesRenderer(xyRenderer);
        View view = ChartFactory.getLineChartView(getContext(), dataset, renderer);
        view.setBackgroundColor(getResources().getColor(R.color.weather_chart_card_bg, null));

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        view.setLayoutParams(params1);
        recent.addView(view, params1);
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
}

package com.dddpeter.app.rainweather.view;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.IndexActivity;
import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.adapter.H24RecyclerAdapter;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class H24Fragment extends Fragment {

    @SuppressLint("NonConstantResourceId")
    RecyclerView recyclerView;
    private LineChart temperatureChart;

    private DatabaseManager databaseManager;
    private String cityId = "101010100";

    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH)) {

                try {
                    updateContent();

                } catch (Exception e) {
                    Log.w("RainWather", "Exception: ", e);
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_h24, viewGroup, false);
        recyclerView = view.findViewById(R.id.recycler_24h);
        temperatureChart = view.findViewById(R.id.temperature_chart);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 初始化温度图表
        initTemperatureChart();
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        boolean isFromHome = requireActivity().getIntent().getBooleanExtra("IS_FROM_HOME", false);
        databaseManager = DatabaseManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH);
        // Android 14+ 需要指定 RECEIVER_NOT_EXPORTED 标志
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        }
        FloatingActionButton fab = view.findViewById(R.id.home);
        try {
            updateContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isFromHome) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), IndexActivity.class);
                startActivity(intent);
            });
        } else {
            fab.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            updateContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateContent() {
        LocationVO locationVO = databaseManager.getLocationVO(CacheKey.CURRENT_LOCATION);
        
        // 检查locationVO是否为null
        if (locationVO == null) {
            Log.w("H24Fragment", "定位信息为空，使用默认位置");
            // 创建默认位置
            locationVO = new LocationVO();
            locationVO.setDistrict("东城区");
            locationVO.setCity("北京市");
            locationVO.setProvince("北京市");
        }
        
        try {
                String weatherJsonString = databaseManager.getCacheData(locationVO.getDistrict() + ":" + CacheKey.WEATHER_ALL);
                JSONObject wAllJson = new JSONObject(weatherJsonString);
                JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
                List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
                for (int i = 0; i < forecast24h.length(); i++) {
                    forcasts.add(forecast24h.getJSONObject(i));
                }
                // 设置RecyclerView适配器
                H24RecyclerAdapter recyclerAdapter = new H24RecyclerAdapter(requireContext(), forcasts, ParamApplication.databaseManager);
                recyclerView.setAdapter(recyclerAdapter);
                
                // 更新温度图表
                updateTemperatureChart(forcasts);

        }catch (Exception e){
            Toast.makeText(requireContext(), "数据正在更新", Toast.LENGTH_SHORT).show();
            getData(locationVO);
        }

    }
    private void getData( LocationVO location){
        String code = "";
        String district = location.getDistrict();
        if(district == null){
            district = "东城区";
        }
        final String finalDistrict = district;
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
            cityId = code;
        }
        if (code != null) {
            // 在后台线程中执行网络请求
            new Thread(() -> {
                try {
                    OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
                    Request request = new Request.Builder()
                            .url(CacheKey.DETAIL_API + cityId)//访问连接
                            .addHeader("Accept", "application/json")
                            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                            .get()
                            .build();
                    Call call = client.newCall(request);
                    //通过execute()方法获得请求响应的Response对象
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                        databaseManager.putCacheData(finalDistrict + ":" + CacheKey.WEATHER_ALL, weather.toString(), "JSONObject");
                        JSONObject wAllJson = weather;
                        JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
                        List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
                        for (int i = 0; i < forecast24h.length(); i++) {
                            forcasts.add(forecast24h.getJSONObject(i));
                        }
                        
                        // 在主线程中更新UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // 设置RecyclerView适配器
                                H24RecyclerAdapter recyclerAdapter = new H24RecyclerAdapter(requireContext(), forcasts, ParamApplication.databaseManager);
                                recyclerView.setAdapter(recyclerAdapter);
                                
                                // 更新温度图表
                                updateTemperatureChart(forcasts);
                            });
                        }
                    }
                } catch (IOException | JSONException e) {
                    Log.w("RainWather", "Exception: ", e);
                    // 在主线程中显示错误信息
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "数据获取失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            requireContext().unregisterReceiver(mRefreshBroadcastReceiver);
        } catch (Exception e) {
            Log.w("Destory", "onDestroy: ", e);
        }
    }

    /**
     * 初始化温度图表
     */
    private void initTemperatureChart() {
        if (temperatureChart == null) return;
        
        // 基本设置 - 禁用所有交互功能
        temperatureChart.setTouchEnabled(false);
        temperatureChart.setDragEnabled(false);
        temperatureChart.setScaleEnabled(false);
        temperatureChart.setPinchZoom(false);
        temperatureChart.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        // 隐藏图例
        temperatureChart.getLegend().setEnabled(false);
        
        // 隐藏描述
        temperatureChart.getDescription().setEnabled(false);
        
        // 设置X轴
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(android.graphics.Color.WHITE);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelCount(8, true); // 增加标签数量以显示更多小时
        
        // 设置Y轴
        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.setTextColor(android.graphics.Color.WHITE);
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(android.graphics.Color.parseColor("#33FFFFFF"));
        leftAxis.setDrawAxisLine(false);
        
        YAxis rightAxis = temperatureChart.getAxisRight();
        rightAxis.setEnabled(false);
        
        // 设置动画
        temperatureChart.animateX(1000);
    }

    /**
     * 更新温度图表数据
     */
    private void updateTemperatureChart(List<JSONObject> weatherList) {
        if (temperatureChart == null || weatherList == null || weatherList.isEmpty()) return;
        
        try {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            
            for (int i = 0; i < weatherList.size(); i++) {
                JSONObject item = weatherList.get(i);
                String temperature = item.getString("temperature").replace("℃", "");
                String forecastTime = item.getString("forecasttime");
                
                // 解析时间，只取小时部分的前两位
                String timeLabel = extractHourFromTime(forecastTime);
                
                float temp = Float.parseFloat(temperature);
                entries.add(new Entry(i, temp));
                labels.add(timeLabel);
            }
            
            // 创建数据集
            LineDataSet dataSet = new LineDataSet(entries, "温度");
            dataSet.setColor(android.graphics.Color.parseColor("#FF6B6B"));
            dataSet.setLineWidth(2f);
            dataSet.setCircleColor(android.graphics.Color.parseColor("#FF6B6B"));
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextColor(android.graphics.Color.WHITE);
            dataSet.setValueTextSize(10f);
            dataSet.setDrawValues(false); // 不显示数值标签
            
            // 设置填充
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(android.graphics.Color.parseColor("#33FF6B6B"));
            
            // 创建数据
            LineData lineData = new LineData(dataSet);
            temperatureChart.setData(lineData);
            
            // 设置X轴标签，确保只显示前两位
            temperatureChart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < labels.size()) {
                        String label = labels.get(index);
                        // 确保只显示前两位数字
                        if (label.length() > 2) {
                            return label.substring(0, 2);
                        }
                        return label;
                    }
                    return "";
                }
            });
            
            // 刷新图表
            temperatureChart.invalidate();
            
        } catch (Exception e) {
            Log.e("H24Fragment", "更新温度图表失败", e);
        }
    }

    /**
     * 从时间字符串中提取小时部分
     */
    private String extractHourFromTime(String forecastTime) {
        if (forecastTime == null || forecastTime.trim().isEmpty()) {
            return "00";
        }
        
        Log.d("H24Fragment", "原始forecastTime: " + forecastTime);
        
        try {
            // 处理不同的时间格式
            String[] timeParts = forecastTime.split(" ");
            
            Log.d("H24Fragment", "timeParts长度: " + timeParts.length);
            for (int j = 0; j < timeParts.length; j++) {
                Log.d("H24Fragment", "timeParts[" + j + "]: " + timeParts[j]);
            }
            
            // 情况1: "2024-01-15 14:00:00" 或 "2024-01-15 14:00"
            if (timeParts.length > 1) {
                String timePart = timeParts[1];
                if (timePart.contains(":")) {
                    String[] hourMinute = timePart.split(":");
                    if (hourMinute.length > 0 && hourMinute[0].length() >= 2) {
                        String hour = hourMinute[0].substring(0, 2);
                        Log.d("H24Fragment", "从timeParts[1]提取小时: " + hour);
                        return hour;
                    }
                }
            }
            
            // 情况2: "14:00:00" 或 "14:00"
            if (timeParts.length == 1 && timeParts[0].contains(":")) {
                String[] hourMinute = timeParts[0].split(":");
                if (hourMinute.length > 0 && hourMinute[0].length() >= 2) {
                    String hour = hourMinute[0].substring(0, 2);
                    Log.d("H24Fragment", "从timeParts[0]提取小时: " + hour);
                    return hour;
                }
            }
            
            // 情况3: 如果包含数字，尝试提取前两位数字
            String numbersOnly = forecastTime.replaceAll("[^0-9]", "");
            if (numbersOnly.length() >= 2) {
                String hour = numbersOnly.substring(0, 2);
                Log.d("H24Fragment", "从数字提取小时: " + hour);
                return hour;
            }
            
        } catch (Exception e) {
            Log.e("H24Fragment", "解析时间失败: " + forecastTime, e);
        }
        
        Log.d("H24Fragment", "使用默认值: 00");
        return "00";
    }
}

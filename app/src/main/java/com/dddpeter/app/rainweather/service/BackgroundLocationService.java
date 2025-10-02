package com.dddpeter.app.rainweather.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.common.LocationService;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 后台定位服务 - 每5分钟更新一次定位和天气数据
 */
public class BackgroundLocationService extends Service {
    private static final String TAG = "BackgroundLocationService";
    private static final long LOCATION_UPDATE_INTERVAL = 5 * 60 * 1000; // 5分钟
    
    private Handler serviceHandler;
    private Runnable locationUpdateRunnable;
    private ExecutorService executorService;
    private LocationService locationService;
    private DatabaseManager databaseManager;
    private boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "后台定位服务创建");
        
        serviceHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
        locationService = LocationService.getInstance(this);
        databaseManager = DatabaseManager.getInstance(this);
        
        // 创建定时任务
        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isServiceRunning) {
                    updateLocationAndWeather();
                    // 5分钟后再次执行
                    serviceHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "后台定位服务启动");
        isServiceRunning = true;
        
        // 立即执行一次定位更新
        updateLocationAndWeather();
        
        // 启动定时任务
        serviceHandler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL);
        
        return START_STICKY; // 服务被杀死后自动重启
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "后台定位服务销毁");
        isServiceRunning = false;
        
        // 清理资源
        if (serviceHandler != null) {
            serviceHandler.removeCallbacks(locationUpdateRunnable);
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        if (locationService != null) {
            locationService.cleanup();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 更新定位和天气数据
     */
    private void updateLocationAndWeather() {
        Log.d(TAG, "========== 开始后台定位更新 ==========");
        Log.d(TAG, "时间: " + System.currentTimeMillis());
        
        locationService.startLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationSuccess(LocationVO location) {
                Log.i(TAG, "========== 后台定位成功 ==========");
                Log.i(TAG, "区县: " + location.getDistrict());
                Log.i(TAG, "城市: " + location.getCity());
                Log.i(TAG, "省份: " + location.getProvince());
                Log.i(TAG, "经度: " + location.getLng());
                Log.i(TAG, "纬度: " + location.getLat());
                Log.i(TAG, "=============================");
                
                // 立即保存位置信息到数据库
                databaseManager.saveLocation(location);
                Log.d(TAG, "位置信息已保存到数据库");
                
                // 在后台线程中获取天气数据
                executorService.execute(() -> {
                    getWeatherData(location);
                    get7DayWeatherData(location);
                });
            }
            
            @Override
            public void onLocationFailed(String error) {
                Log.e(TAG, "========== 后台定位失败 ==========");
                Log.e(TAG, "错误信息: " + error);
                Log.e(TAG, "=============================");
                
                // 定位失败时，尝试使用缓存的位置信息
                LocationVO cachedLocation = locationService.getCachedLocation();
                if (cachedLocation != null) {
                    Log.i(TAG, "使用缓存的位置信息: " + cachedLocation.getDistrict());
                    // 保存缓存的位置信息到数据库
                    databaseManager.saveLocation(cachedLocation);
                    Log.d(TAG, "缓存位置信息已保存到数据库");
                    executorService.execute(() -> {
                        getWeatherData(cachedLocation);
                        get7DayWeatherData(cachedLocation);
                    });
                } else {
                    Log.w(TAG, "没有缓存的位置信息，使用默认位置");
                    // 创建默认位置
                    LocationVO defaultLocation = new LocationVO();
                    defaultLocation.setDistrict("东城区");
                    defaultLocation.setCity("北京市");
                    defaultLocation.setProvince("北京市");
                    // 保存默认位置信息到数据库
                    databaseManager.saveLocation(defaultLocation);
                    Log.d(TAG, "默认位置信息已保存到数据库");
                    executorService.execute(() -> {
                        getWeatherData(defaultLocation);
                        get7DayWeatherData(defaultLocation);
                    });
                }
            }
        });
    }

    /**
     * 获取天气数据
     */
    private void getWeatherData(LocationVO locationVO) {
        if (locationVO == null) {
            Log.w(TAG, "LocationVO为null，使用默认位置");
            locationVO = new LocationVO();
            locationVO.setDistrict("东城区");
            locationVO.setCity("北京市");
            locationVO.setProvince("北京市");
        }
        
        String district = locationVO.getDistrict();
        if (district == null) {
            district = "东城区";
        }
        
        String code = "";
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
        }

        if (code != null && !code.isEmpty()) {
            String finalDistrict = district;
            executeWeatherRequestWithRetry(CacheKey.DETAIL_API + code, finalDistrict, 3);
        } else {
            Log.w(TAG, "未找到城市代码: " + district);
        }
    }

    /**
     * 获取7天天气数据
     */
    private void get7DayWeatherData(LocationVO locationVO) {
        if (locationVO == null) {
            Log.w(TAG, "LocationVO为null，使用默认位置");
            locationVO = new LocationVO();
            locationVO.setDistrict("东城区");
            locationVO.setCity("北京市");
            locationVO.setProvince("北京市");
        }
        
        String district = locationVO.getDistrict();
        if (district == null) {
            district = "东城区";
        }
        
        String code;
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
        } else {
            code = "";
        }

        if (code != null && !code.isEmpty()) {
            String finalDistrict = district;
            execute7DayWeatherRequestWithRetry(CacheKey.DETAIL_API + code, finalDistrict, 3);
        }
    }

    /**
     * 带重试机制的天气数据请求
     */
    private void executeWeatherRequestWithRetry(String url, String district, int maxRetries) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                .get()
                .build();
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Call call = client.newCall(request);
                Response response = call.execute();
                
                if (response.isSuccessful()) {
                    JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                    databaseManager.putCacheData(district + ":" + CacheKey.WEATHER_ALL, weather.toString(), "JSONObject");
                    Log.i(TAG, "后台天气数据获取成功: " + district + ", 尝试: " + attempt);
                    
                    // 发送广播通知UI更新
                    Intent intent = new Intent();
                    intent.setAction(CacheKey.REFRESH);
                    sendBroadcast(intent);
                    return;
                } else {
                    Log.w(TAG, "后台天气数据获取失败，状态码: " + response.code() + ", 尝试: " + attempt);
                }
            } catch (IOException e) {
                Log.w(TAG, "后台天气数据请求异常，尝试: " + attempt + ", 错误: " + e.getMessage());
                if (attempt == maxRetries) {
                    Log.e(TAG, "后台天气数据请求最终失败: " + url, e);
                }
            } catch (JSONException e) {
                Log.e(TAG, "后台天气数据JSON解析失败: " + url, e);
                return;
            }
            
            // 如果不是最后一次尝试，等待一段时间后重试
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * 带重试机制的7天天气数据请求
     */
    private void execute7DayWeatherRequestWithRetry(String url, String district, int maxRetries) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                .get()
                .build();
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Call call = client.newCall(request);
                Response response = call.execute();
                
                if (response.isSuccessful()) {
                    JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                    
                    // 获取15天预报数据
                    if (weather.has("forecast15d")) {
                        org.json.JSONArray forecast15d = weather.getJSONArray("forecast15d");
                        
                        // 创建7天天气数据数组
                        org.json.JSONArray forecast7d = new org.json.JSONArray();
                        int daysToTake = Math.min(7, forecast15d.length());
                        
                        for (int i = 0; i < daysToTake; i++) {
                            forecast7d.put(forecast15d.getJSONObject(i));
                        }
                        
                        // 创建7天天气数据对象
                        JSONObject weather7d = new JSONObject();
                        weather7d.put("forecast7d", forecast7d);
                        weather7d.put("city", weather.optString("city", district));
                        weather7d.put("updateTime", System.currentTimeMillis());
                        
                        // 保存7天天气数据到数据库
                        databaseManager.putCacheData(district + ":" + CacheKey.WEATHER_7D, weather7d.toString(), "JSONObject");
                        
                        Log.i(TAG, "后台7天天气数据获取成功: " + district + ", 尝试: " + attempt);
                        
                        // 发送广播通知UI更新
                        Intent intent = new Intent();
                        intent.setAction(CacheKey.REFRESH_7D);
                        sendBroadcast(intent);
                    }
                    return;
                } else {
                    Log.w(TAG, "后台7天天气数据获取失败，状态码: " + response.code() + ", 尝试: " + attempt);
                }
            } catch (IOException e) {
                Log.w(TAG, "后台7天天气数据请求异常，尝试: " + attempt + ", 错误: " + e.getMessage());
                if (attempt == maxRetries) {
                    Log.e(TAG, "后台7天天气数据请求最终失败: " + url, e);
                }
            } catch (JSONException e) {
                Log.e(TAG, "后台7天天气数据JSON解析失败: " + url, e);
                return;
            }
            
            // 如果不是最后一次尝试，等待一段时间后重试
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

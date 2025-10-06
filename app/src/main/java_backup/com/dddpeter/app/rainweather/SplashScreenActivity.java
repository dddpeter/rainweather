package com.dddpeter.app.rainweather;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dddpeter.app.rainweather.common.LocationService;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.xuexiang.xui.XUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SplashScreenActivity extends Activity {
    private static final int SPLASH_TIMEOUT = 10000; // 10秒超时
    private static final int LOCATION_TIMEOUT = 8000; // 8秒定位超时
    
    private DatabaseManager databaseManager;
    private LocationService locationService;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsLocationReceived = false;
    private boolean mIsActivityFinished = false;
    
    // 权限请求码
    private static final int REQUEST_PERMISSIONS = 1001;
    private PermissionCallback mPermissionCallback;
    
    /**
     * 权限请求回调接口
     */
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
    Runnable runnableHistory = new Runnable() {
        @Override
        public void run() {
            executeWithRetry(CacheKey.HISTORY_API, "history", 3);
        }
    };
    
    /**
     * 带重试机制的网络请求
     */
    private void executeWithRetry(String url, String cacheKey, int maxRetries) {
        OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
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
                    JSONObject data = new JSONObject(Objects.requireNonNull(response.body()).string());
                    if ("history".equals(cacheKey)) {
                        databaseManager.putCacheData("history:" + CacheKey.HISTORY, data.toString(), "JSONObject");
                    }
                    Log.i("RainWather", "网络请求成功: " + url);
                    return; // 成功则退出重试循环
                } else {
                    Log.w("RainWather", "网络请求失败，状态码: " + response.code() + ", 尝试: " + attempt);
                }
            } catch (IOException e) {
                Log.w("RainWather", "网络请求异常，尝试: " + attempt + ", 错误: " + e.getMessage());
                if (attempt == maxRetries) {
                    Log.e("RainWather", "网络请求最终失败: " + url, e);
                }
            } catch (JSONException e) {
                Log.e("RainWather", "JSON解析失败: " + url, e);
                return; // JSON解析失败不需要重试
            }
            
            // 如果不是最后一次尝试，等待一段时间后重试
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000 * attempt); // 递增等待时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    
    /**
     * 带重试机制的天气数据请求
     */
    private boolean executeWeatherRequestWithRetry(String url, String district, int maxRetries) {
        OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
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
                    Log.i("知雨天气", "天气数据获取成功，尝试: " + attempt);
                    return true; // 成功则返回true
                } else {
                    Log.w("知雨天气", "天气数据获取失败，状态码: " + response.code() + ", 尝试: " + attempt);
                }
            } catch (IOException e) {
                Log.w("RainWather", "获取天气数据异常，尝试: " + attempt + ", 错误: " + e.getMessage());
                if (attempt == maxRetries) {
                    Log.e("RainWather", "天气数据请求最终失败: " + url, e);
                }
            } catch (JSONException e) {
                Log.e("RainWather", "天气数据JSON解析失败: " + url, e);
                return false; // JSON解析失败返回false
            }
            
            // 如果不是最后一次尝试，等待一段时间后重试
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000 * attempt); // 递增等待时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false; // 所有重试都失败
    }
    
    private String cityId = "101010100";

    private void getWeatherData(LocationVO locationVO) {
        if (locationVO == null) {
            Log.w("SplashScreenActivity", "LocationVO为null，使用默认位置");
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
            cityId = code;
        } else {
            code = "";
        }

        if (code != null && !code.isEmpty()) {
            String finalDistrict = district;
            Log.d("知雨天气", "开始获取天气数据，城市代码: " + code);
            new Thread(() -> {
                boolean success = executeWeatherRequestWithRetry(CacheKey.DETAIL_API + code, finalDistrict, 3);
                // 只有成功获取天气数据后才进入主页面
                mHandler.post(() -> {
                    if (success) {
                        Log.i("知雨天气", "天气数据获取成功，进入主页面");
                        proceedToMainActivity();
                    } else {
                        Log.w("知雨天气", "天气数据获取失败，使用默认数据进入主页面");
                        proceedToMainActivity();
                    }
                });
            }).start();
        } else {
            // 没有找到城市代码，使用默认数据进入主页面
            Log.w("知雨天气", "未找到城市代码，使用默认位置");
            proceedToMainActivity();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置系统栏
        setupSystemBars();
        
        setContentView(R.layout.activity_splash_screen);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        databaseManager = DatabaseManager.getInstance(this);
        // 设置超时机制
        setupTimeout();
        
        // 检查权限，权限通过后才能进入主页面
        if (checkPermissions()) {
            Log.d("知雨天气", "权限已授予，开始定位");
            startLocationAndProceed();
        } else {
            Log.d("知雨天气", "权限未授予，请求权限");
            requestPermissions();
        }
    }
    
    /**
     * 设置系统栏（状态栏和导航栏）
     */
    private void setupSystemBars() {
        // 启用边到边显示
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // 设置状态栏为浅色内容（深色背景）
            controller.setAppearanceLightStatusBars(false);
            // 设置导航栏为浅色内容（深色背景）
            controller.setAppearanceLightNavigationBars(false);
        }
        
        // 设置状态栏和导航栏颜色为透明
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        
        // 处理系统窗口插入，确保内容避开安全区
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            // 获取系统窗口插入
            int statusBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                }
            }
            int navigationBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                }
            }

            // 设置内容区域的padding，避开安全区
            View contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setPadding(0, statusBarHeight, 0, navigationBarHeight);
            }
            
            return insets;
        });
    }
    
    
    
    
    /**
     * 检查权限是否已授予
     */
    private boolean checkPermissions() {
        // 检查位置权限
        boolean locationGranted = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED &&
                                 ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED;
        
        // 检查网络权限
        boolean networkGranted = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_WIFI_STATE") == PackageManager.PERMISSION_GRANTED;
        
        // 检查存储权限（Android 12及以下）
        boolean storageGranted = true;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            storageGranted = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED;
        }
        
        Log.d("知雨天气", "========== 权限检查结果 ==========");
        Log.d("知雨天气", "位置权限: " + (locationGranted ? "已授予" : "未授予"));
        Log.d("知雨天气", "网络权限: " + (networkGranted ? "已授予" : "未授予"));
        Log.d("知雨天气", "存储权限: " + (storageGranted ? "已授予" : "未授予"));
        Log.d("知雨天气", "总体结果: " + (locationGranted && networkGranted && storageGranted ? "通过" : "未通过"));
        Log.d("知雨天气", "================================");
        return locationGranted && networkGranted && storageGranted;
    }
    
    /**
     * 请求权限
     */
    private void requestPermissions() {
        // 构建需要请求的权限列表
        List<String> permissionsToRequest = new ArrayList<>();
        
        // 位置权限
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.ACCESS_FINE_LOCATION");
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.ACCESS_COARSE_LOCATION");
        }
        
        // 网络权限
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_NETWORK_STATE") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.ACCESS_NETWORK_STATE");
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_WIFI_STATE") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.ACCESS_WIFI_STATE");
        }
        
        // 存储权限（Android 12及以下）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add("android.permission.WRITE_EXTERNAL_STORAGE");
            }
        }
        
        if (permissionsToRequest.isEmpty()) {
            // 所有权限都已授予
            Log.d("知雨天气", "所有权限已授予，跳转到主页面");
            proceedToMainActivity();
        } else {
            // 显示权限说明
            showPermissionRationale();
            
            // 请求权限
            Log.d("知雨天气", "========== 请求权限 ==========");
            for (String permission : permissionsToRequest) {
                Log.d("知雨天气", "请求权限: " + permission);
            }
            Log.d("知雨天气", "权限数量: " + permissionsToRequest.size());
            Log.d("知雨天气", "=============================");
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }
    
    /**
     * 显示权限说明
     */
    private void showPermissionRationale() {
        Toast.makeText(this, "知雨天气需要以下权限来为您提供准确的天气信息：\n" +
                "• 位置权限：获取您的位置以显示当地天气\n" +
                "• 网络权限：连接网络获取天气数据\n" +
                "• 存储权限：缓存天气数据以节省流量", Toast.LENGTH_LONG).show();
    }
    
    /**
     * 设置总超时机制
     */
    private void setupTimeout() {
        mHandler.postDelayed(this::onSplashTimeout, SPLASH_TIMEOUT);
    }
    
    /**
     * 定位超时处理
     */
    private void onLocationTimeout() {
        if (!mIsLocationReceived && !mIsActivityFinished) {
            Log.w("知雨天气", "定位超时，使用默认位置");
            Toast.makeText(this, "定位超时，使用默认位置", Toast.LENGTH_SHORT).show();
            proceedToMainActivity();
        }
    }
    
    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Log.d("知雨天气", "所有权限已授予，开始定位");
                startLocationAndProceed();
            } else {
                Log.w("知雨天气", "权限被拒绝，显示提示并退出应用");
                // 标记Activity已完成，防止其他方法强制跳转
                mIsActivityFinished = true;
                Toast.makeText(this, "需要相关权限才能正常使用应用，请重新启动应用并授予权限", Toast.LENGTH_LONG).show();
                // 延迟退出应用
                mHandler.postDelayed(() -> {
                    finish();
                }, 3000);
            }
        }
    }
    
    /**
     * 启动页超时处理
     */
    private void onSplashTimeout() {
        if (!mIsActivityFinished) {
            Log.w("知雨天气", "启动页超时，强制进入主页面");
            Toast.makeText(this, "启动超时，进入主页面", Toast.LENGTH_SHORT).show();
            proceedToMainActivity();
        }
    }
    
    /**
     * 开始定位并获取天气数据
     */
    private void startLocationAndProceed() {
        Log.d("知雨天气", "开始定位服务");
        locationService = LocationService.getInstance(this);
        
        locationService.startLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationSuccess(LocationVO location) {
                Log.i("知雨天气", "========== 定位成功 ==========");
                Log.i("知雨天气", "区县: " + location.getDistrict());
                Log.i("知雨天气", "城市: " + location.getCity());
                Log.i("知雨天气", "省份: " + location.getProvince());
                Log.i("知雨天气", "经度: " + location.getLng());
                Log.i("知雨天气", "纬度: " + location.getLat());
                Log.i("知雨天气", "=============================");
                
                mIsLocationReceived = true;
                getWeatherData(location);
            }
            
            @Override
            public void onLocationFailed(String error) {
                Log.e("知雨天气", "========== 定位失败 ==========");
                Log.e("知雨天气", "错误信息: " + error);
                Log.e("知雨天气", "=============================");
                
                // 定位失败时，尝试使用缓存的位置信息
                LocationVO cachedLocation = locationService.getCachedLocation();
                if (cachedLocation != null) {
                    Log.i("知雨天气", "使用缓存的位置信息: " + cachedLocation.getDistrict());
                    getWeatherData(cachedLocation);
                } else {
                    Log.w("知雨天气", "没有缓存的位置信息，使用默认位置");
                    // 创建默认位置
                    LocationVO defaultLocation = new LocationVO();
                    defaultLocation.setDistrict("东城区");
                    defaultLocation.setCity("北京市");
                    defaultLocation.setProvince("北京市");
                    getWeatherData(defaultLocation);
                }
            }
        });
    }
    
    
    /**
     * 进入主页面
     */
    private void proceedToMainActivity() {
        if (mIsActivityFinished) {
            return;
        }
        mIsActivityFinished = true;
        
        // 停止定位服务
        if (locationService != null) {
            locationService.stop();
        }
        
        // 跳转到主页面
        Intent indexIntent = new Intent(getApplicationContext(), IndexActivity.class);
        startActivity(indexIntent);
        finish();
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理Handler回调
        mHandler.removeCallbacksAndMessages(null);
        
        // 停止定位服务
        if (locationService != null) {

        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 如果Activity被暂停且未完成，且权限已授予，才强制进入主页面
        if (!mIsActivityFinished && checkPermissions()) {
            Log.w("知雨天气", "Activity被暂停，权限已授予，强制进入主页面");
            proceedToMainActivity();
        }
    }


}


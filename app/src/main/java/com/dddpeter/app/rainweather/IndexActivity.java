package com.dddpeter.app.rainweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.dddpeter.app.rainweather.common.LocationManager;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.common.PermissionManager;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.dddpeter.app.rainweather.view.CustomBotTabItem;
import com.dddpeter.app.rainweather.view.TabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.xuexiang.xui.XUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 主界面Activity - 升级到Android 15+现代写法
 */
@SuppressLint("NonConstantResourceId")
public class IndexActivity extends AppCompatActivity {
    // 现代Android 15+成员变量
    private DatabaseManager databaseManager;
    private List<Fragment> mFragmentList;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private String cityId = "101010100";
    private final static String PROJECT_PATH = "/media/lijinde/work/work/rainweather";
    
    // 现代权限管理
    private PermissionManager permissionManager;
    private OnBackPressedCallback onBackPressedCallback;
    
    // 线程池管理
    private ExecutorService executorService;
    
    // 定位管理器
    private LocationManager locationManager;

    private void getWeatherData(LocationVO locationVO) {
        if (locationVO == null) {
            Log.w("IndexActivity", "LocationVO为null，使用默认位置");
            locationVO = new LocationVO();
            locationVO.setDistrict("东城区");
            locationVO.setCity("北京市");
            locationVO.setProvince("北京市");
        }
        
        String district = locationVO.getDistrict();
        if (district == null) {
            district = "东城区";
        }
        final String finalDistrict = district;
        
        String code = "";
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
            cityId = code;
        }

        if (code != null) {
            if (ParamApplication.isStart) {
                ParamApplication.isStart = false;
            } else {
                new Thread(() -> {
                    OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
                    Request request = new Request.Builder()
                            .url(CacheKey.DETAIL_API + cityId)//访问连接
                            .addHeader("Accept", "application/json")
                            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                            .get()
                            .build();
                    Call call = client.newCall(request);
                    //通过execute()方法获得请求响应的Response对象
                    Response response;
                    try {
                        response = call.execute();
                        if (response.isSuccessful()) {
                            JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                            databaseManager.putCacheData(finalDistrict + ":" + CacheKey.WEATHER_ALL, weather.toString(), "JSONObject");
                            Intent intent = new Intent();
                            intent.setAction(CacheKey.REFRESH);
                            sendBroadcast(intent);
                        }
                    } catch (IOException | JSONException e) {
                        Log.w("RainWather", "Exception: ", e);
                    }
                }).start();
            }
        }
    }

    /**
     * 获取7日内天气数据
     */
    private void get7DayWeatherData(LocationVO locationVO) {
        if (locationVO == null) {
            Log.w("IndexActivity", "LocationVO为null，使用默认位置");
            locationVO = new LocationVO();
            locationVO.setDistrict("东城区");
            locationVO.setCity("北京市");
            locationVO.setProvince("北京市");
        }
        
        String district = locationVO.getDistrict();
        if (district == null) {
            district = "东城区";
        }
        final String finalDistrict = district;
        
        String code;
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
        } else {
            code = "";
        }

        if (code != null) {
            // 在后台线程中执行网络请求
            new Thread(() -> {
                try {
                    OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
                    Request request = new Request.Builder()
                            .url(CacheKey.DETAIL_API + code)
                            .addHeader("Accept", "application/json")
                            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                            .get()
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    
                    if (response.isSuccessful()) {
                        JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                        
                        // 获取15天预报数据
                        if (weather.has("forecast15d")) {
                            JSONArray forecast15d = weather.getJSONArray("forecast15d");
                            
                            // 创建7天天气数据数组
                            JSONArray forecast7d = new JSONArray();
                            int daysToTake = Math.min(7, forecast15d.length());
                            
                            for (int i = 0; i < daysToTake; i++) {
                                forecast7d.put(forecast15d.getJSONObject(i));
                            }
                            
                            // 创建7天天气数据对象
                            JSONObject weather7d = new JSONObject();
                            weather7d.put("forecast7d", forecast7d);
                            weather7d.put("city", weather.optString("city", finalDistrict));
                            weather7d.put("updateTime", System.currentTimeMillis());
                            
                            // 保存7天天气数据到数据库
                            databaseManager.putCacheData(finalDistrict + ":" + CacheKey.WEATHER_7D, weather7d.toString(), "JSONObject");
                            
                            Log.i("知雨天气", "7天天气数据获取成功: " + finalDistrict);
                            
                            // 发送广播通知UI更新
                            Intent intent = new Intent();
                            intent.setAction(CacheKey.REFRESH_7D);
                            sendBroadcast(intent);
                        }
                    }
                } catch (IOException | JSONException e) {
                    Log.w("知雨天气", "获取7天天气数据失败: " + finalDistrict, e);
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("IndexActivity", "onResume - 重新启动定位服务");
        
        // 重新启动定位服务，获取最新位置
        startLocationService();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 启用边到边显示
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // 初始化字体
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        
        super.onCreate(savedInstanceState);
        
        // 设置状态栏和导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setupSystemBars();
        }

        // 初始化数据库管理器
        databaseManager = DatabaseManager.getInstance(this);
        
        // 初始化定位管理器
        locationManager = LocationManager.getInstance(this);
        
        // 设置布局
        setContentView(R.layout.activity_index);
        
        // 初始化UI组件
        initViews();
        
        // 初始化权限管理
        initPermissionManager();
        
        // 设置返回按钮处理
        setupBackPressedCallback();
        
        // 初始化线程池
        executorService = Executors.newCachedThreadPool();
        
        // 设置底部导航
        setupBottomNavigation();
        
        // 启动定位服务
        startLocationService();
    }
    
    /**
     * 启动定位服务
     */
    private void startLocationService() {
        Log.d("IndexActivity", "启动定位服务");
        
        // 权限已在SplashScreenActivity中检查，这里直接启动定位
        locationManager.startLocation(new LocationManager.LocationCallback() {
            @Override
            public void onLocationSuccess(LocationVO location) {
                Log.i("IndexActivity", "========== 定位成功 ==========");
                Log.i("IndexActivity", "区县: " + location.getDistrict());
                Log.i("IndexActivity", "城市: " + location.getCity());
                Log.i("IndexActivity", "省份: " + location.getProvince());
                Log.i("IndexActivity", "国家: " + location.getCountry());
                Log.i("IndexActivity", "经度: " + location.getLng());
                Log.i("IndexActivity", "纬度: " + location.getLat());
                Log.i("IndexActivity", "街道: " + location.getStreet());
                Log.i("IndexActivity", "乡镇: " + location.getTown());
                Log.i("IndexActivity", "定位时间: " + System.currentTimeMillis());
                Log.i("IndexActivity", "=============================");
                getWeatherData(location);
                get7DayWeatherData(location);
            }
            
            @Override
            public void onLocationFailed(String error) {
                Log.e("IndexActivity", "========== 定位失败 ==========");
                Log.e("IndexActivity", "错误信息: " + error);
                Log.e("IndexActivity", "时间: " + System.currentTimeMillis());
                Log.e("IndexActivity", "线程: " + Thread.currentThread().getName());
                Log.e("IndexActivity", "=============================");
                
                // 定位失败时，尝试使用缓存的位置信息
                LocationVO cachedLocation = locationManager.getCachedLocation();
                if (cachedLocation != null) {
                    Log.i("IndexActivity", "使用缓存的位置信息: " + cachedLocation.getDistrict());
                    getWeatherData(cachedLocation);
                    get7DayWeatherData(cachedLocation);
                } else {
                    Log.w("IndexActivity", "没有缓存的位置信息，使用默认位置");
                    // 创建默认位置
                    LocationVO defaultLocation = new LocationVO();
                    defaultLocation.setDistrict("东城区");
                    defaultLocation.setCity("北京市");
                    defaultLocation.setProvince("北京市");
                    getWeatherData(defaultLocation);
                    get7DayWeatherData(defaultLocation);
                }
            }
        });
    }
    
    /**
     * 设置系统栏（状态栏和导航栏）
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
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
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            
            // 设置内容区域的padding，避开安全区
            View contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setPadding(0, statusBarHeight, 0, navigationBarHeight);
            }
            
            return insets;
        });
        
        // 保持屏幕常亮（如果需要）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        mTabLayout = findViewById(R.id.id_tab_layout);
        mViewPager = findViewById(R.id.id_vp);
    }
    
    /**
     * 初始化权限管理
     */
    private void initPermissionManager() {
        permissionManager = new PermissionManager(this);
        
        // 位置权限已通过LocationManager处理，这里不需要额外处理
    }
    
    /**
     * 设置返回按钮处理（Android 15+推荐方式）
     */
    private void setupBackPressedCallback() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 处理返回按钮逻辑
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // 如果没有Fragment在栈中，则关闭Activity
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
    
    /**
     * 设置底部导航
     */
    private void setupBottomNavigation() {
        CustomBotTabItem item = CustomBotTabItem.create();
        item.setContext(this)
                .setViewPager(mViewPager)
                .setTabLayout(mTabLayout)
                .build();
        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), getLifecycle(), 4));
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理定位资源
        if (locationManager != null) {
            locationManager.cleanup();
        }
        
        // 清理资源
        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager != null) {
            permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        int current = getIntent().getIntExtra("currentTab", 0);
        mViewPager.setCurrentItem(current);
        mViewPager.setUserInputEnabled(false);
        databaseManager = DatabaseManager.getInstance(this);
        Intent intent = new Intent();
        intent.setAction(CacheKey.REFRESH);
        sendBroadcast(intent);
        Intent intent1 = new Intent();
        intent1.setAction(CacheKey.HISTORY);
        sendBroadcast(intent1);
    }


}

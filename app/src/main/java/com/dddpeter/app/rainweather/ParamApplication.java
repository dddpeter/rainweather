package com.dddpeter.app.rainweather;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.dddpeter.app.rainweather.adapter.H24Adapter;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.common.Promise;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.xuexiang.xui.XUI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
// 注意：此项目使用的是百度定位SDK，不是地图SDK
// 百度定位SDK不需要在Application中初始化

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ParamApplication extends Application {
    public final static String[] MAIN_CITY = {"北京", "上海", "香港", "成都", "广州", "深圳", "天津", "杭州", "南京", "澳门"};
    public static boolean isStart = true;
    public static Map<String, String> cityIdMap = new ConcurrentHashMap<>();
    public static DatabaseManager databaseManager;
    String url = CacheKey.DETAIL_API;
    private int countActivity = 0;
    //是否进入后台
    private boolean isBackground = false;
    private String cityId = "101010100";


    public static CityInfo getCityInfo(String somewhere) {
        CityInfo cityInfo = null;
        String s0 = somewhere
                .replace("省", "")
                .replace("市", "")
                .replace("自治区", "")
                .replace("区", "");
        String s1 = s0
                .replace("县", "")
                .replace("自治县", "")
                .replace("特区", "")
                .replace("特别行政区", "");
        String s2 = s0
                .replace("自治县", "")
                .replace("特区", "")
                .replace("特别行政区", "");
        String r1 = cityIdMap.get(s1);
        String r2 = cityIdMap.get(s2);
        if (r1 != null) {
            cityInfo = new CityInfo(r1, somewhere);
        } else {
            if (r2 != null) {
                cityInfo = new CityInfo(null, somewhere);
            }
        }
        return cityInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化数据库管理器
        databaseManager = DatabaseManager.getInstance(this);
        
        XUI.init(this);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        // 数据库管理器已在onCreate中初始化
        
        // 清理可能存在的旧缓存数据以避免序列化冲突
        clearOldCacheData();
        
        // 清理过期数据库数据
        databaseManager.cleanExpiredData();
        
        try {
            initDayWeather();
            initNightWeather();
            initWeatherIcon();
            initCityIds();

        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }
        initBackgroundCallBack();
    }
    
    /**
     * 清理旧的缓存数据以避免序列化冲突
     */
    private void clearOldCacheData() {
        try {
            // 尝试获取旧的LocationVO数据，如果序列化失败则清理
            LocationVO oldLocation = databaseManager.getLocationVO(CacheKey.CURRENT_LOCATION);
            if (oldLocation == null) {
                Log.i("RainWather", "No old location data found, cache is clean");
            } else {
                Log.i("RainWather", "Old location data found and loaded successfully");
            }
        } catch (Exception e) {
            Log.w("RainWather", "Old cache data incompatible, clearing cache", e);
            // 清理可能不兼容的缓存数据
            databaseManager.deleteCacheData(CacheKey.CURRENT_LOCATION);
            // 可以添加更多需要清理的缓存键
            Toast.makeText(this, "缓存数据已更新", Toast.LENGTH_SHORT).show();
        }
    }
    private void getData( LocationVO location){
        // 检查location参数是否为null
        if (location == null) {
            Log.w("RainWather", "LocationVO is null in getData method");
            return;
        }
        
        String code = "";
        String district = location.getDistrict();
        if(district == null || district.isEmpty()){
            district = "东城区";
        }
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
            cityId = code;
        }
        if (code != null) {
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
                    databaseManager.putCacheData(district + ":" + CacheKey.WEATHER_ALL, weather.toString(), "JSONObject");
                    JSONObject wAllJson = weather;
                    JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
                    List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
                    for (int i = 0; i < forecast24h.length(); i++) {
                        forcasts.add(forecast24h.getJSONObject(i));
                    }
                    ArrayAdapter<JSONObject> adapter = new H24Adapter(getApplicationContext(), R.layout.listview_item,
                            forcasts, databaseManager);
                }
            } catch (IOException | JSONException e) {
                Log.w("RainWather", "Exception: ", e);
            }
        }
    }
    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                countActivity++;
                if (countActivity == 1 && isBackground) {
                    Log.i("MyApplication", "onActivityStarted: 应用进入前台");
                    isBackground = false;
                    //说明应用重新进入了前台
                    LocationVO locationVO = databaseManager.getLocationVO(CacheKey.CURRENT_LOCATION);
                    try {
                        // 检查locationVO是否为null
                        if (locationVO == null) {
                            Log.w("RainWather", "LocationVO is null, skipping data update");
                            Toast.makeText(getApplicationContext(), "位置信息不可用，请重新定位", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // 检查district是否为null
                        String district = locationVO.getDistrict();
                        if (district == null || district.isEmpty()) {
                            Log.w("RainWather", "District is null or empty, using default");
                            district = "东城区";
                        }
                        
                        try {
                            String weatherJsonString = databaseManager.getCacheData(district + ":" + CacheKey.WEATHER_ALL);
                            JSONObject wAllJson = new JSONObject(weatherJsonString);
                            JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
                            List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
                            for (int i = 0; i < forecast24h.length(); i++) {
                                forcasts.add(forecast24h.getJSONObject(i));
                            }
                            ArrayAdapter<JSONObject> adapter = new H24Adapter(getApplicationContext(), R.layout.listview_item,
                                    forcasts, databaseManager);

                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "数据正在更新", Toast.LENGTH_SHORT).show();
                            getData(locationVO);
                        }
                    } catch (Exception e) {
                        Log.w("RainWather", "Exception: ", e);
                    }
                    Toast.makeText(getApplicationContext() ,"数据正在更新", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                countActivity--;
                if (countActivity <= 0 && !isBackground) {
                  //  Log.e("MyApplication", "onActivityStarted: 应用进入后台");
                    isBackground = true;
                    //说明应用进入了后台
                   // Toast.makeText(getApplicationContext(), "应用进入后台", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void initCommonCities() {
        List<Callable<JSONObject>> callables = new ArrayList<>();
        OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
        for (String city : MAIN_CITY) {
            CityInfo cityInfo = ParamApplication.getCityInfo(city);
            if (cityInfo == null) {
                continue;
            }
            Callable<JSONObject> callable = () -> {
                JSONObject weather = new JSONObject();
                Request request = new Request.Builder()
                        .url(url + cityInfo.getCityid())//访问连接
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
                        assert response.body() != null;
                        weather = new JSONObject(Objects.requireNonNull(response.body()).string()).getJSONObject("data");
                        weather.put("city", city);
                    }
                } catch (IOException | JSONException e) {
                    Log.w("RainWather", "Exception: ", e);
                }
                return weather;
            };
            callables.add(callable);
        }
        Runnable runnable = () -> {
            try {
                List<JSONObject> results = Promise.all(callables);
                for (JSONObject r : results) {
                    databaseManager.putCacheData(r.getString("city") + ":" + CacheKey.WEATHER_ALL, r.toString(), "JSONObject");
                }
                Intent intent = new Intent();
                intent.setAction(CacheKey.REFRESH_CITY);
                sendBroadcast(intent);

            } catch (Exception e) {
                Log.w("RainWather", "Exception: ", e);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void initWeatherIcon() {
        // 使用数据库管理器替代SharedPreferences
        databaseManager.putString("weahter_icon:大雨", "\ue723");
        databaseManager.putString("weahter_icon:暴雨", "\ue725");
        databaseManager.putString("weahter_icon:冻雨", "\ue731");
        databaseManager.putString("weahter_icon:大雪", "\ue72c");
        databaseManager.putString("weahter_icon:暴雪", "\ue72b");
        databaseManager.putString("weahter_icon:多云", "\ue716");
        databaseManager.putString("weahter_icon:雷阵雨", "\ue726");
        databaseManager.putString("weahter_icon:沙尘暴", "\ue733");
        databaseManager.putString("weahter_icon:多云转晴", "\ue716");
        databaseManager.putString("weahter_icon:晴转多云", "\ue716");
        databaseManager.putString("weahter_icon:雾", "\ue72f");
        databaseManager.putString("weahter_icon:小雪", "\ue729");
        databaseManager.putString("weahter_icon:小雨", "\ue717");
        databaseManager.putString("weahter_icon:阴", "\ue721");
        databaseManager.putString("weahter_icon:晴", "\ue719");
        databaseManager.putString("weahter_icon:雨夹雪", "\ue727");
        databaseManager.putString("weahter_icon:中雨", "\ue720");
        databaseManager.putString("weahter_icon:中雪", "\ue72a");
        databaseManager.putString("weahter_icon:阵雨", "\ue71f");
        databaseManager.putString("weahter_icon:雷阵雨", "\ue726");
        databaseManager.putString("weahter_icon:霾", "\ue730");
        databaseManager.putString("weahter_icon:扬沙", "\ue72e");
        databaseManager.putString("weahter_icon:浮尘", "\ue732");
        databaseManager.putBoolean("weahter_icon:init", true);
        
        Log.d("ParamApplication", "天气图标数据已保存到数据库");
    }

    private void initNightWeather() {
        boolean isInited = databaseManager.getBoolean("night_picture:init", false);
        //isInited=false;
        if (!isInited) {
            // 使用数据库管理器替代SharedPreferences
            databaseManager.putString("night_picture:大雨", "dby.png");
            databaseManager.putString("night_picture:暴雨", "dby.png");
            databaseManager.putString("night_picture:冻雨", "dy0.png");
            databaseManager.putString("night_picture:大雪", "dx0.png");
            databaseManager.putString("night_picture:暴雪", "bx.png");
            databaseManager.putString("night_picture:多云", "dy0.png");
            databaseManager.putString("night_picture:雷阵雨", "lzy0.png");
            databaseManager.putString("night_picture:沙尘暴", "scb.png");
            databaseManager.putString("night_picture:多云转晴", "dyq0.png");
            databaseManager.putString("night_picture:晴转多云", "dyq0.png");
            databaseManager.putString("night_picture:雾", "w.png");
            databaseManager.putString("night_picture:小雪", "xx.png");
            databaseManager.putString("night_picture:小雨", "xy.png");
            databaseManager.putString("night_picture:阴", "y.png");
            databaseManager.putString("night_picture:晴", "q0.png");
            databaseManager.putString("night_picture:雨夹雪", "yjx.png");
            databaseManager.putString("night_picture:中雨", "zhy.png");
            databaseManager.putString("night_picture:中雪", "zx.png");
            databaseManager.putString("night_picture:阵雨", "zy0.png");
            databaseManager.putString("night_picture:霾", "scb.png");
            databaseManager.putString("night_picture:扬沙", "scb.png");
            databaseManager.putString("night_picture:浮尘", "scb.png");
            databaseManager.putBoolean("night_picture:init", true);
            
            Log.d("ParamApplication", "夜间天气图片数据已保存到数据库");
        }
    }

    private void initDayWeather() {
        boolean isInited = databaseManager.getBoolean("day_picture:init", false);
        //isInited=false;
        if (!isInited) {
            // 使用数据库管理器替代SharedPreferences
            databaseManager.putString("day_picture:大雨", "dby.png");
            databaseManager.putString("day_picture:暴雨", "dby.png");
            databaseManager.putString("day_picture:冻雨", "dy.png");
            databaseManager.putString("day_picture:大雪", "dx.png");
            databaseManager.putString("day_picture:暴雪", "bx.png");
            databaseManager.putString("day_picture:多云", "dy.png");
            databaseManager.putString("day_picture:多云转晴", "dyq.png");
            databaseManager.putString("day_picture:晴转多云", "dyq.png");
            databaseManager.putString("day_picture:雷阵雨", "lzy.png");
            databaseManager.putString("day_picture:沙尘暴", "scb.png");
            databaseManager.putString("day_picture:雾", "w.png");
            databaseManager.putString("day_picture:小雪", "xx.png");
            databaseManager.putString("day_picture:小雨", "xy.png");
            databaseManager.putString("day_picture:阴", "y.png");
            databaseManager.putString("day_picture:晴", "q.png");
            databaseManager.putString("day_picture:雨夹雪", "yjx.png");
            databaseManager.putString("day_picture:中雨", "zhy.png");
            databaseManager.putString("day_picture:中雪", "zx.png");
            databaseManager.putString("day_picture:阵雨", "zy.png");
            databaseManager.putString("day_picture:霾", "scb.png");
            databaseManager.putString("day_picture:扬沙", "scb.png");
            databaseManager.putString("day_picture:浮尘", "scb.png");
            databaseManager.putBoolean("day_picture:init", true);
            
            Log.d("ParamApplication", "白天天气图片数据已保存到数据库");
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 清理Glide缓存以释放内存
        Glide.get(this).clearMemory();
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 根据内存压力级别清理Glide缓存
        if (level >= TRIM_MEMORY_MODERATE) {
            Glide.get(this).clearMemory();
        }
    }

    public int getTAB_TAG_RECENT() {
        return 0;
    }

    public void initCityIds() throws IOException, JSONException {
        AssetManager manager = getAssets();
        InputStream is = manager.open("city.json");
        StringBuilder stringBuffer = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }
        JSONArray citys = new JSONArray(stringBuffer.toString());
        Log.i("cityinfo", "initCityIds: " + citys.length());
        for (int i = 0; i < citys.length(); i++) {
            JSONObject c = citys.getJSONObject(i);
            cityIdMap.put(c.getString("name"), c.getString("id"));
        }
        initCommonCities();
    }

}

package com.dddpeter.app.rainweather;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.common.Promise;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.xuexiang.xui.XUI;

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
    public boolean isRefreshed = false;
    String url = CacheKey.DETAIL_API;
    ACache mCache;

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
        XUI.init(this);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        super.onCreate();
        mCache = ACache.get(this);
        try {
            initDayWeather();
            initNightWeather();
            initWeatherIcon();
            initCityIds();

        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

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
            Callable<JSONObject> callable = (Callable<JSONObject>) () -> {
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
                        weather = new JSONObject(response.body().string()).getJSONObject("data");
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
                    mCache.put(r.getString("city") + ":" + CacheKey.WEATHER_ALL, r);
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
        SharedPreferences preferences = getSharedPreferences("weahter_icon", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("大雨", "\ue723");
        editor.putString("暴雨", "\ue725");
        editor.putString("冻雨", "\ue731");
        editor.putString("大雪", "\ue72c");
        editor.putString("暴雪", "\ue72b");
        editor.putString("多云", "\ue716");
        editor.putString("雷阵雨", "\ue726");
        editor.putString("沙尘暴", "\ue733");
        editor.putString("多云转晴", "\ue716");
        editor.putString("晴转多云", "\ue716");
        editor.putString("雾", "\ue72f");
        editor.putString("小雪", "\ue729");
        editor.putString("小雨", "\ue717");
        editor.putString("阴", "\ue721");
        editor.putString("晴", "\ue719");
        editor.putString("雨夹雪", "\ue727");
        editor.putString("中雨", "\ue720");
        editor.putString("中雪", "\ue72a");
        editor.putString("阵雨", "\ue71f");
        editor.putString("雷阵雨", "\ue726");
        editor.putString("霾", "\ue730");
        editor.putString("扬沙", "\ue72e");
        editor.putString("浮尘", "\ue732");
        editor.putBoolean("init", true);
        editor.apply();
    }

    private void initNightWeather() {
        SharedPreferences preferences = getSharedPreferences("night_picture", MODE_PRIVATE);
        boolean isInited = preferences.getBoolean("init", false);
        //isInited=false;
        if (!isInited) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("大雨", "dby.png");
            editor.putString("暴雨", "dby.png");
            editor.putString("冻雨", "dy0.png");
            editor.putString("大雪", "dx0.png");
            editor.putString("暴雪", "bx.png");
            editor.putString("多云", "dy0.png");
            editor.putString("雷阵雨", "lzy0.png");
            editor.putString("沙尘暴", "scb.png");
            editor.putString("多云转晴", "dyq0.png");
            editor.putString("晴转多云", "dyq0.png");
            editor.putString("雾", "w.png");
            editor.putString("小雪", "xx.png");
            editor.putString("小雨", "xy.png");
            editor.putString("阴", "y.png");
            editor.putString("晴", "q0.png");
            editor.putString("雨夹雪", "yjx.png");
            editor.putString("中雨", "zhy.png");
            editor.putString("中雪", "zx.png");
            editor.putString("阵雨", "zy0.png");
            editor.putString("霾", "scb.png");
            editor.putString("扬沙", "scb.png");
            editor.putString("浮尘", "scb.png");
            editor.putBoolean("init", true);
            editor.apply();
        }

    }

    private void initDayWeather() {
        SharedPreferences preferences = getSharedPreferences("day_picture", MODE_PRIVATE);
        boolean isInited = preferences.getBoolean("init", false);
        //isInited=false;
        if (!isInited) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("大雨", "dby.png");
            editor.putString("暴雨", "dby.png");
            editor.putString("冻雨", "dy.png");
            editor.putString("大雪", "dx.png");
            editor.putString("暴雪", "bx.png");
            editor.putString("多云", "dy.png");
            editor.putString("多云转晴", "dyq.png");
            editor.putString("晴转多云", "dyq.png");
            editor.putString("雷阵雨", "lzy.png");
            editor.putString("沙尘暴", "scb.png");
            editor.putString("雾", "w.png");
            editor.putString("小雪", "xx.png");
            editor.putString("小雨", "xy.png");
            editor.putString("阴", "y.png");
            editor.putString("晴", "q.png");
            editor.putString("雨夹雪", "yjx.png");
            editor.putString("中雨", "zhy.png");
            editor.putString("中雪", "zx.png");
            editor.putString("阵雨", "zy.png");
            editor.putString("霾", "scb.png");
            editor.putString("扬沙", "scb.png");
            editor.putString("浮尘", "scb.png");
            editor.putBoolean("init", true);
            editor.apply();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    public boolean isRefreshed() {
        return isRefreshed;
    }

    public String getTAB_TAG_TODAY() {
        return "tab_tag_today";
    }

    public String getTAB_TAG_RECENT() {
        return "tab_tag_recent";
    }

    public String getTAB_TAG_AIR() {
        return "tab_tag_air";
    }

    public String getTAB_TAG_ABOUT() {
        return "tab_tag_about";
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

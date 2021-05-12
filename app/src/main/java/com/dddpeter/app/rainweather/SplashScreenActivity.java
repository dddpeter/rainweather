package com.dddpeter.app.rainweather;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.wang.avi.AVLoadingIndicatorView;
import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import lombok.NonNull;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashScreenActivity extends Activity {
    private final int REQUEST_GPS = 1;
    public AMapLocationClient mLocationClient = null;
    String url = CacheKey.API_DOMAIN + CacheKey.API_CITY;
    ACache mCache;
    AVLoadingIndicatorView avi ;
    private void getWeatherData(AMapLocation amapLocation){
        String u = url + amapLocation.getDistrict();
        Log.i("Location", "onLocationChanged: " + amapLocation.toStr());
        mCache.put(CacheKey.CURRENT_LOCATION, amapLocation.toJson(1));
        String location = mCache.getAsString(CacheKey.CURRENT_LOCATION);
        String city = amapLocation.getDistrict();
       /* if (mCache.getAsJSONObject(city + ":" + CacheKey.WEATHER_DATA) == null) {
            OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
            Request request = new Request.Builder()
                    .url(u)//访问连接
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                    .get()
                    .build();
            Call call = client.newCall(request);
            //通过execute()方法获得请求响应的Response对象
            Response response = null;
            try {
                response = call.execute();
                if (response.isSuccessful()) {
                    JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                    mCache.put(city + ":" + CacheKey.WEATHER_DATA, weather);
                    Log.i("RainWather", "getWeatherData: 获取详细天气信息 ");
                    this.getWeatherDetail(city);
                }
            } catch (IOException | JSONException e) {
                Log.w("RainWather", "Exception: ", e);
            } finally {

            }

        }
        else{*/
            this.getWeatherDetail(city);
       /* }*/
        mLocationClient.disableBackgroundLocation(true);
        mLocationClient.unRegisterLocationListener(mAMapLocationListener);
        mLocationClient.onDestroy();
        mLocationClient = null;
    }
    private void getWeatherDetail(String city){
        if (mCache.getAsJSONObject(city + ":" + CacheKey.WEATHER_ALL) == null) {
            String shortLocation = city
                    .replace("省", "")
                    .replace("市", "")
                    .replace("自治区", "")
                    .replace("区", "")
                    .replace("县", "")
                    .replace("自治县", "")
                    .replace("特区", "")
                    .replace("特别行政区", "");
            FinalDb db = FinalDb.create(this, "my.db");
            List<CityInfo> list = db.findAllByWhere(CityInfo.class, " city ='" + city +
                    "' or  city ='" + shortLocation
                    + "' or city like '" + shortLocation + "%'");
            if (list.size() > 0) {
                OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
                Request request = new Request.Builder()
                        .url(CacheKey.DETAIL_API + list.get(0).getCityid())//访问连接
                        .addHeader("Accept", "application/json")
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                        .get()
                        .build();
                Call call = client.newCall(request);
                //通过execute()方法获得请求响应的Response对象
                Response response = null;
                try {
                    response = call.execute();
                    if (response.isSuccessful()) {
                        JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                        mCache.put(city + ":" + CacheKey.WEATHER_ALL, weather);
                        Intent intent = new Intent();
                        intent.setAction(CacheKey.REFRESH);
                        sendBroadcast(intent);
                        Intent indexIndent = new Intent(SplashScreenActivity.this, IndexActivity.class);  //从启动动画ui跳转到主ui
                        startActivity(indexIndent);
                       // SplashScreenActivity.this.finish();
                    }
                } catch (IOException | JSONException e) {
                    Log.w("RainWather", "Exception: ", e);
                } finally {
                    avi.smoothToHide();
                }
            }

        }
        else{
            Intent intent = new Intent();
            intent.setAction(CacheKey.REFRESH);
            sendBroadcast(intent);
            Intent indexIndent = new Intent(SplashScreenActivity.this, IndexActivity.class);  //从启动动画ui跳转到主ui
            startActivity(indexIndent);
            avi.smoothToHide();
        }
    }

    //异步获取定位结果
    public AMapLocationListener mAMapLocationListener = amapLocation -> {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                this.getWeatherData(amapLocation);
            }
        }
    };
    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        mCache = ACache.get(this);
        avi = findViewById(R.id.avi);
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_GPS);
        Log.d("知雨天气", "开始进行定位:");
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        avi.smoothToShow();
        mLocationClient.setLocationListener(this.mAMapLocationListener);
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setInterval(5000);
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(locationOption);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("权限", "onRequestPermissionsResult: " + grantResults.toString());
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission GET", Toast.LENGTH_SHORT).show();
            if (requestCode == REQUEST_GPS) {
                mLocationClient.startLocation();
            }

        } else {
            //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


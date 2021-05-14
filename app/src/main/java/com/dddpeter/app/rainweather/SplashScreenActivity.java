package com.dddpeter.app.rainweather;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.xuexiang.xui.XUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import lombok.NonNull;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SplashScreenActivity extends Activity {
    private final int REQUEST_GPS = 1;
    ACache mCache;
    public LocationClient mLocationClient = null;
    private   String  cityId = "101010100";



    private void getWeatherData(BDLocation location){
        String addr = location.getAddrStr();    //获取详细地址信息
        String country = location.getCountry();    //获取国家
        String province = location.getProvince();    //获取省份
        String city = location.getCity();    //获取城市
        String district = location.getDistrict();    //获取区县
        String street = location.getStreet();    //获取街道信息
        String adcode = location.getAdCode();    //获取adcode
        String town = location.getTown();    //获取乡镇信息
        Double lat = location.getLatitude(); //纬度坐标
        Double lng = location.getLongitude();
        LocationVO locationVO = new LocationVO();
        locationVO.setAdcode(adcode);
        locationVO.setAddress(addr);
        locationVO.setCity(city);
        locationVO.setCountry(country);
        locationVO.setDistrict(district);
        locationVO.setProvince(province);
        locationVO.setStreet(street);
        locationVO.setTown(town);
        locationVO.setLat(lat);
        locationVO.setLng(lng);
        Log.i("Location", "onLocationChanged: " + locationVO.toString());
        mCache.put(CacheKey.CURRENT_LOCATION, locationVO);
        String code= "";
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
            cityId = code;
        }

        if (code!=null) {
            new Thread(new Runnable(){
                @Override
                public void run() {
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
                            mCache.put(district + ":" + CacheKey.WEATHER_ALL, weather);
                            mLocationClient.stop();
                            Intent indexIndent = new Intent(SplashScreenActivity.this, IndexActivity.class);  //从启动动画ui跳转到主ui
                            startActivity(indexIndent);
                        }
                    } catch (IOException | JSONException e) {
                        Log.w("RainWather", "Exception: ", e);
                    }
                }
            }).start();


        }

    }

    BDAbstractLocationListener locationListener  = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            getWeatherData(location);
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
        ActivityCompat.requestPermissions(this, new String[]{
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_GPS);
        Log.d("知雨天气", "开始进行定位:");
        mLocationClient = new LocationClient(getApplicationContext());
        // 声明LocationClient类
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setNeedNewVersionRgc(true);
        option.setIsNeedLocationPoiList(true);
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(locationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("权限", "onRequestPermissionsResult: " + grantResults.toString());
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permission GET", Toast.LENGTH_SHORT).show();
            if (requestCode == REQUEST_GPS) {
                mLocationClient.start();
            }

        } else {
            //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}


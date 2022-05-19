package com.dddpeter.app.rainweather;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.dddpeter.app.rainweather.view.CustomBotTabItem;
import com.dddpeter.app.rainweather.view.TabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.xuexiang.xui.XUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*import android.os.StrictMode;*/

@SuppressLint("NonConstantResourceId")
public class IndexActivity extends AppCompatActivity {
    public LocationClient mLocationClient = null;
    ACache mCache;
    private List<Fragment> mFragmentList;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private String cityId = "101010100";
    BDAbstractLocationListener locationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            getWeatherData(location);
        }
    };

    private void getWeatherData(BDLocation location) {
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
                            mCache.put(district + ":" + CacheKey.WEATHER_ALL, weather);
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("知雨天气", "开始进行定位:");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        setContentView(R.layout.activity_index);
        mTabLayout = (TabLayout) findViewById(R.id.id_tab_layout);
        mViewPager = (ViewPager2) findViewById(R.id.id_vp);

        CustomBotTabItem item = CustomBotTabItem.create();
        item.setContext(this)
                .setViewPager(mViewPager)
                .setTabLayout(mTabLayout)
                .build();
        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), getLifecycle(), 4));
        int current = getIntent().getIntExtra("currentTab", 0);
        mViewPager.setCurrentItem(current);
        mViewPager.setUserInputEnabled(false);
        mCache = ACache.get(this);
        Intent intent = new Intent();
        intent.setAction(CacheKey.REFRESH);
        sendBroadcast(intent);
        Intent intent1 = new Intent();
        intent1.setAction(CacheKey.HISTORY);
        sendBroadcast(intent1);
        mLocationClient = new LocationClient(getApplicationContext());
        // 声明LocationClient类
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setNeedNewVersionRgc(true);
        option.setIsNeedLocationPoiList(true);
        option.setScanSpan(30 * 1000);
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(locationListener);
        mLocationClient.start();
    }


}

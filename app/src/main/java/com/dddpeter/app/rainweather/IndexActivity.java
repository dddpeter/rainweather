package com.dddpeter.app.rainweather;


import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalDb;
import net.tsz.afinal.annotation.view.ViewInject;

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

/*import android.os.StrictMode;*/


public class IndexActivity extends FinalActivity {
    public final static int TAB_ICON_SIZE = 100;
    private final int REQUEST_GPS = 1;
    public AMapLocationClient mLocationClient = null;
    @ViewInject(id = R.id.radioGroup1)
    RadioGroup rg;
    @ViewInject(id = R.id.home)
    RadioButton home;
    @ViewInject(id = R.id.main)
    RadioButton main;
    @ViewInject(id = R.id.h24)
    RadioButton air;
    @ViewInject(id = R.id.about)
    RadioButton about;
    @ViewInject(id = android.R.id.tabhost)
    TabHost tabHost;
    LocalActivityManager activityGroup;
    String url = CacheKey.API_DOMAIN + CacheKey.API_CITY;
    ACache mCache;
    //异步获取定位结果
    public AMapLocationListener mAMapLocationListener = amapLocation -> {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                String u = url + amapLocation.getDistrict();
                Log.i("Location", "onLocationChanged: " + amapLocation.toStr());
                mCache.put(CacheKey.CURRENT_LOCATION, amapLocation.toJson(1));
                String location = mCache.getAsString(CacheKey.CURRENT_LOCATION);
                String city = amapLocation.getDistrict();
                if (mCache.getAsJSONObject(city + ":" + CacheKey.WEATHER_DATA) == null) {
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
                            Intent intent = new Intent();
                            intent.setAction(CacheKey.REFRESH);
                            sendBroadcast(intent);
                        }
                    } catch (IOException | JSONException e) {
                        Log.w("RainWather", "Exception: ", e);
                    } finally {

                    }

                }
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
                                //Intent intent = new Intent();
                                // intent.setAction(CacheKey.REFRESH);
                                //sendBroadcast(intent);
                            }
                        } catch (IOException | JSONException e) {
                            Log.w("RainWather", "Exception: ", e);
                        } finally {

                        }
                    }

                }
            }
        }
    };
    // 内容Intent
    private Intent todayIntent;
    private Intent recentIntent;
    private Intent airIntent;
    private Intent aboutIntent;

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

    @Override
    protected void onResume() {
        super.onResume();
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
      /*  String strVer = android.os.Build.VERSION.RELEASE;
        float fv = Float.valueOf(strVer);
        if (fv > 2.3) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
                    .penaltyLog() //打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects() //探测SQLite数据库操作
                    .penaltyLog() //打印logcat
                    .penaltyDeath()
                    .build());
        }*/

        activityGroup = new LocalActivityManager(this,
                true);
        activityGroup.dispatchCreate(savedInstanceState);

        this.tabHost.setup(activityGroup);
        //初始化
        prepareIntent();
        initView();
        rg.setOnCheckedChangeListener((rg, id) -> {
            ParamApplication application = (ParamApplication) IndexActivity.this.getApplicationContext();
            if (id == home.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_TODAY());

            } else if (id == main.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_RECENT());
            } else if (id == air.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_AIR());
            } else if (id == about.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_ABOUT());
            }

        });
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_GPS);
        Log.d("知雨天气", "开始进行定位:");
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this.mAMapLocationListener);
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setInterval(5000);
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(locationOption);

    }


    private void prepareIntent() {
        todayIntent = new Intent(this, TodayActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        recentIntent = new Intent(this, MainActivty.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        airIntent = new Intent(this, H24Activity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        aboutIntent = new Intent(this, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ParamApplication application = (ParamApplication) IndexActivity.this.getApplicationContext();
        TabHost localTabHost = this.tabHost;
        localTabHost.addTab(buildTabSpec(application.getTAB_TAG_TODAY(), R.string.tab1, R.drawable.home, todayIntent));
        localTabHost.addTab(buildTabSpec(application.getTAB_TAG_RECENT(), R.string.tab2, R.drawable.main, recentIntent));
        localTabHost.addTab(buildTabSpec(application.getTAB_TAG_AIR(), R.string.tab3, R.drawable.air, airIntent));
        localTabHost.addTab(buildTabSpec(application.getTAB_TAG_ABOUT(), R.string.tab4, R.drawable.about, aboutIntent));

    }

    private TabSpec buildTabSpec(String tabTag, int titleResourceID, int iconResourceID,
                                 Intent intent) {
        TabHost.TabSpec spec = this.tabHost.newTabSpec(tabTag);
        spec.setContent(intent);
        spec.setIndicator(getResources().getString(titleResourceID),
                getResources().getDrawable(iconResourceID));
        return spec;
    }

    private void initView() {
        //定义底部标签图片大小和位置
        Drawable drawableHome = getResources().getDrawable(R.drawable.home);
        //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形
        drawableHome.setBounds(0, 0, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //设置图片在文字的哪个方向
        home.setCompoundDrawables(null, drawableHome, null, null);

        //定义底部标签图片大小和位置
        Drawable drawableMain = getResources().getDrawable(R.drawable.main);
        //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形
        drawableMain.setBounds(0, 0, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //设置图片在文字的哪个方向
        main.setCompoundDrawables(null, drawableMain, null, null);

        //定义底部标签图片大小和位置
        Drawable drawableAir = getResources().getDrawable(R.drawable.air);
        //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形
        drawableAir.setBounds(0, 0, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //设置图片在文字的哪个方向
        air.setCompoundDrawables(null, drawableAir, null, null);

        //定义底部标签图片大小和位置
        Drawable drawableAbout = getResources().getDrawable(R.drawable.about);
        //当这个图片被绘制时，给他绑定一个矩形 ltrb规定这个矩形
        drawableAbout.setBounds(0, 0, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //设置图片在文字的哪个方向
        about.setCompoundDrawables(null, drawableAbout, null, null);
    }


}

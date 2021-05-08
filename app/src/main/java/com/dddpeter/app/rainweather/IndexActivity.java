package com.dddpeter.app.rainweather;


import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;


import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;


import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lombok.NonNull;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class IndexActivity extends FinalActivity {
    @ViewInject(id = R.id.radioGroup1)
    RadioGroup rg;
    @ViewInject(id = R.id.radio0)
    RadioButton rb1;
    @ViewInject(id = R.id.radio1)
    RadioButton rb2;
    @ViewInject(id = R.id.radio2)
    RadioButton rb3;
    @ViewInject(id = R.id.radio3)
    RadioButton rb4;
    @ViewInject(id = android.R.id.tabhost)
    TabHost tabHost;

    LocalActivityManager activityGroup;

    // 内容Intent
    private Intent todayIntent;
    private Intent recentIntent;
    private Intent airIntent;
    private Intent aboutIntent;
    private final int REQUEST_GPS = 1;


    String url = CacheKey.API_DOMAIN + CacheKey.API_CITY;
    public AMapLocationClient mLocationClient = null;
    ACache mCache;

    //异步获取定位结果
    public AMapLocationListener mAMapLocationListener = amapLocation -> {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                String u = url + amapLocation.getCity();
                Log.i("Location", "onLocationChanged: " + amapLocation.toStr());
                if(mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION) == null){
                    mCache.put(CacheKey.CURRENT_LOCATION,amapLocation.toJson(1));
                }
                if(mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION+":"+CacheKey.WEATHER_DATA) == null){
                    OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
                    Request request = new Request.Builder()
                            .url(u)//访问连接
                            .addHeader("Accept", "application/json")
                            .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                            .get()
                            .build();
                    Call call = client.newCall(request);
                    //通过execute()方法获得请求响应的Response对象
                    Response response = null;
                    try {
                        response = call.execute();
                        if (response.isSuccessful()) {
                            JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                            mCache.put(CacheKey.CURRENT_LOCATION+":"+CacheKey.WEATHER_DATA, weather);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("权限", "onRequestPermissionsResult: "+  grantResults.toString());
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "Permission GET", Toast.LENGTH_SHORT).show();
            if(requestCode==REQUEST_GPS){
                mLocationClient.startLocation();
            }

        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        setContentView(R.layout.activity_index);
        String strVer = android.os.Build.VERSION.RELEASE;
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
        }

        activityGroup = new LocalActivityManager(this,
                true);
        activityGroup.dispatchCreate(savedInstanceState);

        this.tabHost.setup(activityGroup);
        //初始化
        prepareIntent();

        rg.setOnCheckedChangeListener((rg, id) -> {
            ParamApplication application = (ParamApplication) IndexActivity.this.getApplicationContext();
            if (id == rb1.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_TODAY());

            } else if (id == rb2.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_RECENT());
            } else if (id == rb3.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_AIR());
            } else if (id == rb4.getId()) {
                tabHost.setCurrentTabByTag(application.getTAB_TAG_ABOUT());
            }

        });
        ActivityCompat.requestPermissions(this,new String[]{ "android.permission.ACCESS_FINE_LOCATION","android.permission.WRITE_EXTERNAL_STORAGE" }, REQUEST_GPS);
        Log.d("知雨天气", "开始进行定位:");
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this.mAMapLocationListener);

    }




    private void prepareIntent() {
        todayIntent = new Intent(this, TodayActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        recentIntent = new Intent(this, RecentActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        airIntent = new Intent(this, AirActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        aboutIntent = new Intent(this, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ParamApplication application = (ParamApplication) IndexActivity.this.getApplicationContext();
        TabHost localTabHost = this.tabHost;
        localTabHost.addTab(buildTabSpec(application.getTAB_TAG_TODAY(), R.string.tab1, R.drawable.home, todayIntent));
        localTabHost.addTab(buildTabSpec(application.getTAB_TAG_RECENT(), R.string.tab2, R.drawable.recent, recentIntent));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.index, menu);


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh_menu:
               // messageSender(REFRESH_MSG);
                break;
            case R.id.reset_menu:
                // messageSender(RESET_MSG);
                break;


            case R.id.exit_menu:
                // messageSender(EXIT_MSG);
                break;
        }
        return false;
    }

}

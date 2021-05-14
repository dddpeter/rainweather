package com.dddpeter.app.rainweather;


import android.Manifest;
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
    public final static int TAB_ICON_SIZE = 90;
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


    // 内容Intent
    private Intent todayIntent;
    private Intent recentIntent;
    private Intent airIntent;
    private Intent aboutIntent;

    ACache mCache;



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
        String current = getIntent().getStringExtra("currentTab");
        if(current!=null && !current.trim().equals("")){
            tabHost.setCurrentTabByTag(current);
            main.setChecked(true);
        }
        mCache = ACache.get(this);
        Intent intent = new Intent();
        intent.setAction(CacheKey.REFRESH);
        sendBroadcast(intent);
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
                getResources().getDrawable(iconResourceID,null));
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

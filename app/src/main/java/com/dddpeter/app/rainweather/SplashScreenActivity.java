package com.dddpeter.app.rainweather;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.dddpeter.app.rainweather.common.ACache;
import com.xuexiang.xui.XUI;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import lombok.NonNull;


public class SplashScreenActivity extends Activity {
    private final int REQUEST_GPS = 1;

    ACache mCache;




    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");

        //启动动画持续1秒钟
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, IndexActivity.class);  //从启动动画ui跳转到主ui
            startActivity(intent);
            SplashScreenActivity.this.finish();
        }, 600);
    }

}


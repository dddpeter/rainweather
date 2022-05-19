package com.dddpeter.app.rainweather;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dddpeter.app.rainweather.common.ACache;
import com.xuexiang.xui.XUI;


public class MainActivity extends AppCompatActivity {
    ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        setContentView(R.layout.activity_main);
    }
}


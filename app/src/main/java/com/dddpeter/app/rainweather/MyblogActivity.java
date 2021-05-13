package com.dddpeter.app.rainweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.xui.XUI;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MyblogActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myblog);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        WebView blog = findViewById(R.id.blog);
        blog.loadUrl("https://blog.dddpeter.top");
        FloatingActionButton fab = findViewById(R.id.home);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(view -> {
                Intent intent=new Intent(getApplicationContext(),IndexActivity.class);
                startActivity(intent);
        });
    }
}
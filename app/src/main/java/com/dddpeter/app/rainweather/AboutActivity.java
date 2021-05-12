package com.dddpeter.app.rainweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class AboutActivity extends FinalActivity {

    @ViewInject(id = R.id.info)
    TextView textAbout;
    @ViewInject(id=R.id.blog_btn)
    Button blogBtn;
    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        String html =
                "<div style='line-height:1.5;'>&nbsp;&nbsp;&nbsp;&nbsp;本软件（知雨天气）为个人作品，主要功能是通过GPS或者A-GPS定位从网络获取天气信息，" +
                        "以及部分城市的PM2.5信息(有些城市暂未发布PM2.5)，个人交流使用，不用于商业用途。</div>" ;

        textAbout.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        blogBtn.setOnClickListener(v->{
            Intent intent = new Intent(this,MyblogActivity.class);
            startActivity(intent);
        });
    }

}

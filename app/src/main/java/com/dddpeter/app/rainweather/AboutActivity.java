package com.dddpeter.app.rainweather;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

public class AboutActivity extends FinalActivity {

    @ViewInject(id = R.id.info)
    TextView textAbout;
    @ViewInject(id = R.id.contact)
    TextView textContact;
    @ViewInject(id=R.id.blog_btn)
    Button blogBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String html =
                "<div style='line-height:1.5;'>&nbsp;&nbsp;&nbsp;&nbsp;本软件（知雨天气）为个人作品，主要功能是通过GPS或者A-GPS定位从网络获取天气信息，" +
                        "以及部分城市的PM2.5信息(有些城市暂未发布PM2.5)，个人交流使用，不用于商业用途。</div>" ;

        String contactHTml =
                        "软件作者：烈焰之雨\n\n"  +
                        "电子邮件：dddpeter@126.com" ;
        textContact.setText(contactHTml);
        textAbout.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        blogBtn.setOnClickListener(v->{
            Intent intent = new Intent(this,MyblogActivity.class);
            startActivity(intent);
        });
    }

}

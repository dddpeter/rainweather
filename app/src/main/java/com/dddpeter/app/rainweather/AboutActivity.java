package com.dddpeter.app.rainweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.text.Html;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dddpeter.app.rainweather.adapter.TraceListAdapter;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.Trace;
import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutActivity extends FinalActivity {

    @ViewInject(id = R.id.info)
    TextView textAbout;
    @ViewInject(id = R.id.historyinfo_title)
    TextView historyinfoTitle;

    @ViewInject(id=R.id.blog_btn)
    Button blogBtn;
    @ViewInject(id=R.id.lvTrace)
    private ListView lvTrace;
    private List<Trace> traceList = new ArrayList<>(10);
    private ArrayAdapter<Trace> adapter;
    ACache mCache;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            update();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void update() throws JSONException {
        JSONObject data = mCache.getAsJSONObject("history:" + CacheKey.HISTORY);
        historyinfoTitle.setText(" 历史上的今天:" +data.getString("today") );

        JSONArray array = data.getJSONArray("result");
        for(int j=array.length()-2;j>=0 ;j--){
            traceList.add(new Trace(array.getJSONObject(j).getString("year") +"年",array.getJSONObject(j).getString("title") ));
        }
        adapter = new TraceListAdapter(this, R.layout.history_list,
                R.id.tvAcceptTime,
                R.id.tvAcceptStation,
                R.id.tvTopLine,
                R.id.tvDot,
                R.drawable.timelline_dot_first,
                R.drawable.timelline_dot_normal,
                traceList);
        lvTrace.setAdapter(adapter);

    }
    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.HISTORY)) {

                try {
                    update();
                } catch (Exception e) {
                    Log.w("RainWather", "Exception: ", e);
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mCache = ACache.get(this);
        try {
            update();
        } catch (JSONException e) {
            e.printStackTrace();
        }
       /* IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.HISTORY);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);*/
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
       /* String html =
                "<div style='line-height:1.5;'>&nbsp;&nbsp;&nbsp;&nbsp;本软件（知雨天气）为个人作品，主要功能是通过定位从网络获取天气信息，"
                        +"个人交流使用，不用于商业用途。</div>" ;

        textAbout.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));*/
        blogBtn.setOnClickListener(v->{
            Intent intent = new Intent(this,MyblogActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }
}

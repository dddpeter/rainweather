package com.dddpeter.app.rainweather;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dddpeter.app.rainweather.adapter.MainAdapter;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivty extends FinalActivity {

    @SuppressLint("NonConstantResourceId")
    @ViewInject(id = R.id.main_list)
    ListView mainList;
    ACache mCache;
    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH_CITY)) {
                try {
                    update();
                } catch (JSONException e) {
                    Log.w("RainWather", "Exception: ", e);
                }
            }

        }
    };

    private void update() throws JSONException {
        String[] citys = ParamApplication.MAIN_CITY;
        List<JSONObject> items = new ArrayList<>();
        for (String city : citys) {
            JSONObject weatherJson = mCache.getAsJSONObject(city + ":" + CacheKey.WEATHER_ALL);
            JSONObject airJson = weatherJson.getJSONObject("current").getJSONObject("air");
            JSONObject current = weatherJson.getJSONObject("current").getJSONObject("current");
            current.put("city", city);
            current.put("air", airJson);
            items.add(current);
        }
        ArrayAdapter<JSONObject> adapter = new MainAdapter(this, R.layout.main_list_item, items, getSharedPreferences("weahter_icon", MODE_PRIVATE));
        mainList.setAdapter(adapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        mCache = ACache.get(this);
        this.setContentView(R.layout.activity_main);
        boolean isFromHome = getIntent().getBooleanExtra("IS_FROM_HOME", false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH_CITY);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        try {
            update();
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }
        FloatingActionButton fab = findViewById(R.id.home1);
        if (isFromHome) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), IndexActivity.class);
                startActivity(intent);
            });
        } else {
            fab.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }

}

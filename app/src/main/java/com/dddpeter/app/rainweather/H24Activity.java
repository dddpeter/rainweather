package com.dddpeter.app.rainweather;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class H24Activity extends FinalActivity {

    @ViewInject(id = R.id.list_24h)
    ListView content;

    @ViewInject(id = R.id.textViewtitle)
    TextView textViewtitle;

    ACache mCache;

    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH)) {

                try {
                    updateContent();

                } catch (Exception e) {
                    Log.w("RainWather", "Exception: ", e);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h24);
        boolean isFromHome = getIntent().getBooleanExtra("IS_FROM_HOME",false);
        mCache = ACache.get(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        try {
            updateContent();
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }
        FloatingActionButton fab = findViewById(R.id.home);
        if(isFromHome){
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(view -> {
                Intent intent=new Intent(getApplicationContext(),IndexActivity.class);
                startActivity(intent);
            });
        }
        else{
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateContent();
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }
    }

    private void updateContent() throws JSONException {

        String location = mCache.getAsString(CacheKey.CURRENT_LOCATION);
        JSONObject cityJson = new JSONObject(location);
        JSONObject wAllJson = mCache.getAsJSONObject(cityJson.getString("district") + ":" + CacheKey.WEATHER_ALL);
        JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
        List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
        for (int i = 0; i < forecast24h.length(); i++) {
            forcasts.add(forecast24h.getJSONObject(i));
        }
        ArrayAdapter<JSONObject> adapter = new H24Adapter(this, R.layout.listview_item, forcasts, getSharedPreferences("weahter_icon", MODE_PRIVATE));
        content.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }
}

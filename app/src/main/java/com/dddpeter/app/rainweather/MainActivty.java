package com.dddpeter.app.rainweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.componet.BorderBottomLinearLayout;
import com.dddpeter.app.rainweather.componet.BorderBottomTextView;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivty extends FinalActivity {

    @ViewInject(id = R.id.main)
    LinearLayout my;
    @ViewInject(id = R.id.main_list)
    ListView mainList;
    ACache mCache;
    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
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
            JSONObject weatherJson = mCache.getAsJSONObject(city + ":" + CacheKey.WEATHER_DATA);
            JSONObject today = ((JSONObject) weatherJson.getJSONArray("forecast").get(0));
            today.put("city",city);
            today.put("wendu",weatherJson.getString("wendu"));
            items.add(today);
        }
        ArrayAdapter<JSONObject> adapter = new MainAdapter(this, R.layout.main_list_item, items, getSharedPreferences("weahter_icon", MODE_PRIVATE));
        mainList.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        this.setContentView(R.layout.activity_main);
        boolean isFromHome = getIntent().getBooleanExtra("IS_FROM_HOME",false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH_CITY);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        try {
            update();
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }
        FloatingActionButton fab = findViewById(R.id.home1);
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }

}

package com.dddpeter.app.rainweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.componet.BorderBottomLinearLayout;
import com.dddpeter.app.rainweather.componet.BorderBottomTextView;
import com.dddpeter.app.rainweather.enums.CacheKey;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivty extends FinalActivity {

    @ViewInject(id = R.id.main)
    LinearLayout my;
    ACache mCache;
    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH_CITY)) {
                try {
                    update();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private void update() throws JSONException {
        String[] citys = ParamApplication.MAIN_CITY;
        for (String city : citys) {
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 9f);
            BorderBottomLinearLayout cityLinear = new BorderBottomLinearLayout(this);
            cityLinear.setOrientation(LinearLayout.HORIZONTAL);
            BorderBottomTextView child = new BorderBottomTextView(cityLinear.getContext());
            child.setTextSize(22);
            child.setPadding(25, 25, 25, 25);
            child.setTextColor(getResources().getColor(R.color.black_overlay, null));
            child.setText(city);
            child.setLayoutParams(params);

            JSONObject weatherJson = mCache.getAsJSONObject(city + ":" + CacheKey.WEATHER_DATA);
            SharedPreferences preferencesWI = getSharedPreferences("weahter_icon", MODE_PRIVATE);
            JSONObject today = ((JSONObject) weatherJson.getJSONArray("forecast").get(0));

            ViewGroup.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
            TextView child2 = new TextView(cityLinear.getContext());
            child2.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            child2.setTextSize(20);
            child2.setPadding(25, 25, 10, 25);
            child2.setTextColor(getResources().getColor(R.color.tips, null));
            child2.setTypeface(CommonUtil.weatherIconFontFace(this));
            child2.setText(preferencesWI.getString(today.getString("type"), "\ue73e")
                    + "\t" + today.getString("type") + "\t" + weatherJson.getString("wendu") + "°C");
            child2.setLayoutParams(params1);

            cityLinear.addView(child);
            cityLinear.addView(child2);
            my.addView(cityLinear);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        this.setContentView(R.layout.activity_main);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH_CITY);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        try {
            update();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }

}

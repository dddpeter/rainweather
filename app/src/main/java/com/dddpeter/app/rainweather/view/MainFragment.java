package com.dddpeter.app.rainweather.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.IndexActivity;
import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.adapter.MainAdapter;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.xui.XUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    @SuppressLint("NonConstantResourceId")
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
        getContext();
        ArrayAdapter<JSONObject> adapter = new MainAdapter(requireContext(), R.layout.main_list_item, items,
                requireContext().getSharedPreferences("weahter_icon", Context.MODE_PRIVATE));
        mainList.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, viewGroup, false);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        mCache = ACache.get(getContext());
        mainList = view.findViewById(R.id.main_list);
        boolean isFromHome = requireActivity().getIntent().getBooleanExtra("IS_FROM_HOME", false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH_CITY);
        requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        try {
            update();
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }

        FloatingActionButton fab = view.findViewById(R.id.home1);
        if (isFromHome) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), IndexActivity.class);
                startActivity(intent);
            });
        } else {
            fab.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            requireContext().unregisterReceiver(mRefreshBroadcastReceiver);
        } catch (Exception e) {

        }
    }

}

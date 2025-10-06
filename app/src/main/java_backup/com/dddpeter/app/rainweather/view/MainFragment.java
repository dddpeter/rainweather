package com.dddpeter.app.rainweather.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.IndexActivity;
import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.adapter.MainAdapter;
import com.dddpeter.app.rainweather.database.DatabaseManager;
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
    private DatabaseManager databaseManager;
    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH_CITY)) {
                update();
            }

        }
    };

    private void update() {
        try {
            String[] citys = ParamApplication.MAIN_CITY;
            List<JSONObject> items = new ArrayList<>();
            for (String city : citys) {
                String weatherJsonString = databaseManager.getCacheData(city + ":" + CacheKey.WEATHER_ALL);
                
                // 检查数据是否为null
                if (weatherJsonString == null) {
                    Log.w("MainFragment", "城市 " + city + " 的天气数据为空，跳过");
                    continue;
                }
                
                JSONObject weatherJson = new JSONObject(weatherJsonString);
            JSONObject airJson = weatherJson.getJSONObject("current").getJSONObject("air");
            JSONObject current = weatherJson.getJSONObject("current").getJSONObject("current");
            current.put("city", city);
            current.put("air", airJson);
            items.add(current);
        }
            getContext();
            ArrayAdapter<JSONObject> adapter = new MainAdapter(requireContext(), R.layout.main_list_item, items,
                    ParamApplication.databaseManager);
            mainList.setAdapter(adapter);
        } catch (JSONException e) {
            Log.e("MainFragment", "JSON解析异常", e);
        } catch (Exception e) {
            Log.e("MainFragment", "其他异常", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, viewGroup, false);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        databaseManager = DatabaseManager.getInstance(getContext());
        mainList = view.findViewById(R.id.main_list);
        boolean isFromHome = requireActivity().getIntent().getBooleanExtra("IS_FROM_HOME", false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH_CITY);
        // Android 14+ 需要指定 RECEIVER_NOT_EXPORTED 标志
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(requireContext(), mRefreshBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
        update();

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

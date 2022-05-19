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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.IndexActivity;
import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.adapter.H24Adapter;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.OKHttpClientBuilder;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.po.CityInfo;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.xui.XUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class H24Fragment extends Fragment {

    @SuppressLint("NonConstantResourceId")
    ListView content;

    ACache mCache;
    private String cityId = "101010100";

    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_h24, viewGroup, false);
        content = view.findViewById(R.id.list_24h);
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        boolean isFromHome = requireActivity().getIntent().getBooleanExtra("IS_FROM_HOME", false);
        mCache = ACache.get(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH);
        requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        FloatingActionButton fab = view.findViewById(R.id.home);
        try {
            updateContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void onResume() {
        super.onResume();
        try {
            updateContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateContent() {
        LocationVO locationVO = (LocationVO) mCache.getAsObject(CacheKey.CURRENT_LOCATION);
        try {
                JSONObject wAllJson = mCache.getAsJSONObject(locationVO.getDistrict() + ":" + CacheKey.WEATHER_ALL);
                JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
                List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
                for (int i = 0; i < forecast24h.length(); i++) {
                    forcasts.add(forecast24h.getJSONObject(i));
                }
                ArrayAdapter<JSONObject> adapter = new H24Adapter(requireContext(), R.layout.listview_item,
                        forcasts, requireContext().getSharedPreferences("weahter_icon", Context.MODE_PRIVATE));
                content.setAdapter(adapter);

        }catch (Exception e){
            Toast.makeText(requireContext(), "数据正在更新", Toast.LENGTH_SHORT).show();
            getData(locationVO);
        }

    }
    private void getData( LocationVO location){
        String code = "";
        String district = location.getDistrict();
        CityInfo cityInfo = ParamApplication.getCityInfo(district);
        if (cityInfo != null) {
            code = cityInfo.getCityid();
            cityId = code;
        }
        if (code != null) {
                OkHttpClient client = OKHttpClientBuilder.buildOKHttpClient().build();
                Request request = new Request.Builder()
                        .url(CacheKey.DETAIL_API + cityId)//访问连接
                        .addHeader("Accept", "application/json")
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                        .get()
                        .build();
                Call call = client.newCall(request);
                //通过execute()方法获得请求响应的Response对象
                Response response;
                try {
                    response = call.execute();
                    if (response.isSuccessful()) {
                        JSONObject weather = new JSONObject(response.body().string()).getJSONObject("data");
                        mCache.put(district + ":" + CacheKey.WEATHER_ALL, weather);
                        JSONObject wAllJson = weather;
                        JSONArray forecast24h = wAllJson.getJSONArray("forecast24h");
                        List<JSONObject> forcasts = new ArrayList<>(forecast24h.length());
                        for (int i = 0; i < forecast24h.length(); i++) {
                            forcasts.add(forecast24h.getJSONObject(i));
                        }
                        ArrayAdapter<JSONObject> adapter = new H24Adapter(requireContext(), R.layout.listview_item,
                                forcasts, requireContext().getSharedPreferences("weahter_icon", Context.MODE_PRIVATE));
                        content.setAdapter(adapter);
                    }
                } catch (IOException | JSONException e) {
                    Log.w("RainWather", "Exception: ", e);
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            requireContext().unregisterReceiver(mRefreshBroadcastReceiver);
        } catch (Exception e) {
            Log.w("Destory", "onDestroy: ", e);
        }
    }
}

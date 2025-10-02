package com.dddpeter.app.rainweather.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.MyBlogActivity;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.adapter.TraceListAdapter;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.Trace;
import com.xuexiang.xui.XUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NonConstantResourceId")
public class AboutFragment extends Fragment {

    private final List<Trace> traceList = new ArrayList<>(10);
    TextView historyinfoTitle;
    Button blogBtn;
    private DatabaseManager databaseManager;
    private ListView lvTrace;
    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
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
    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        try {
            String historyJsonString = databaseManager.getCacheData("history:" + CacheKey.HISTORY);
            
            // 检查数据是否为null
            if (historyJsonString == null) {
                Log.w("AboutFragment", "历史数据为空，显示默认信息");
                historyinfoTitle.setText(" 历史上的今天: 暂无数据");
                showDefaultHistoryInfo();
                return;
            }
            
            JSONObject data = new JSONObject(historyJsonString);
        
            // 检查数据是否为null
            if (data == null) {
                Log.w("AboutFragment", "历史数据为空，显示默认信息");
                historyinfoTitle.setText(" 历史上的今天: 暂无数据");
                // 显示默认的历史信息或提示用户
                showDefaultHistoryInfo();
                return;
            }
            String today = data.optString("today", "未知日期");
            String title = " 历史上的今天:" + today;
            historyinfoTitle.setText(title);

            JSONArray array = data.optJSONArray("result");
            if (array != null && array.length() > 0) {
                for (int j = array.length() - 2; j >= 0; j--) {
                    JSONObject item = array.optJSONObject(j);
                    if (item != null) {
                        String year = item.optString("year", "未知");
                        String titleText = item.optString("title", "无标题");
                        traceList.add(new Trace(year + "年", titleText));
                    }
                }
            } else {
                Log.w("AboutFragment", "历史结果数组为空");
                showDefaultHistoryInfo();
                return;
            }
            
            ArrayAdapter<Trace> adapter = new TraceListAdapter(getActivity(), R.layout.history_list,
                    R.id.tvAcceptTime,
                    R.id.tvAcceptStation,
                    R.id.tvTopLine,
                    R.id.tvDot,
                    R.drawable.timelline_dot_first,
                    R.drawable.timelline_dot_normal,
                    traceList);
            lvTrace.setAdapter(adapter);
            
        } catch (JSONException e) {
            Log.e("AboutFragment", "JSON解析异常", e);
            historyinfoTitle.setText(" 历史上的今天: 数据解析错误");
            showDefaultHistoryInfo();
        } catch (Exception e) {
            Log.e("AboutFragment", "其他异常", e);
            historyinfoTitle.setText(" 历史上的今天: 数据解析错误");
            showDefaultHistoryInfo();
        }
    }
    
    /**
     * 显示默认的历史信息
     */
    private void showDefaultHistoryInfo() {
        // 添加一些默认的历史信息
        traceList.add(new Trace("2024年", "知雨天气应用升级到Android 15+"));
        traceList.add(new Trace("2023年", "知雨天气应用发布"));
        
        ArrayAdapter<Trace> adapter = new TraceListAdapter(getActivity(), R.layout.history_list,
                R.id.tvAcceptTime,
                R.id.tvAcceptStation,
                R.id.tvTopLine,
                R.id.tvDot,
                R.drawable.timelline_dot_first,
                R.drawable.timelline_dot_normal,
                traceList);
        lvTrace.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, viewGroup, false);
        historyinfoTitle = view.findViewById(R.id.historyinfo_title);
        blogBtn = view.findViewById(R.id.blog_btn);
        lvTrace = view.findViewById(R.id.lvTrace);
        databaseManager = DatabaseManager.getInstance(getContext());
        try {
            update();
        } catch (Exception e) {
            Log.e("AboutFragment", "更新历史数据时发生未知异常", e);
            // 显示错误信息
            historyinfoTitle.setText(" 历史上的今天: 数据加载失败");
            showDefaultHistoryInfo();
        }
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        blogBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyBlogActivity.class);
            startActivity(intent);
        });
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

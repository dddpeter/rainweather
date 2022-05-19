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
import com.dddpeter.app.rainweather.common.ACache;
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
    ACache mCache;
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
        try {
            update();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void update() throws JSONException {
        JSONObject data = mCache.getAsJSONObject("history:" + CacheKey.HISTORY);
        String title = " 历史上的今天:" + data.getString("today");
        historyinfoTitle.setText(title);

        JSONArray array = data.getJSONArray("result");
        for (int j = array.length() - 2; j >= 0; j--) {
            traceList.add(new Trace(array.getJSONObject(j).getString("year") + "年", array.getJSONObject(j).getString("title")));
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, viewGroup, false);
        historyinfoTitle = view.findViewById(R.id.historyinfo_title);
        blogBtn = view.findViewById(R.id.blog_btn);
        lvTrace = view.findViewById(R.id.lvTrace);
        mCache = ACache.get(getContext());
        try {
            update();
        } catch (JSONException e) {
            e.printStackTrace();
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

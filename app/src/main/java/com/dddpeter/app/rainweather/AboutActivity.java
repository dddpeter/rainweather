package com.dddpeter.app.rainweather;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dddpeter.app.rainweather.adapter.TraceListAdapter;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.Trace;
import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

@SuppressLint("NonConstantResourceId")
public class AboutActivity extends FinalActivity {

    private final List<Trace> traceList = new ArrayList<>(10);
    @ViewInject(id = R.id.historyinfo_title)
    TextView historyinfoTitle;
    @ViewInject(id = R.id.blog_btn)
    Button blogBtn;
    ACache mCache;
    @ViewInject(id = R.id.lvTrace)
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
        String title = " 历史上的今天:" + data.getString("today");
        historyinfoTitle.setText(title);

        JSONArray array = data.getJSONArray("result");
        for (int j = array.length() - 2; j >= 0; j--) {
            traceList.add(new Trace(array.getJSONObject(j).getString("year") + "年", array.getJSONObject(j).getString("title")));
        }
        ArrayAdapter<Trace> adapter = new TraceListAdapter(this, R.layout.history_list,
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mCache = ACache.get(this);
        try {
            update();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        blogBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyblogActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }
}

package com.dddpeter.app.rainweather.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.H24Activity;
import com.dddpeter.app.rainweather.IndexActivity;
import com.dddpeter.app.rainweather.MainActivity;
import com.dddpeter.app.rainweather.ParamApplication;
import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.common.DataUtil;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.LocationVO;
import com.xuexiang.xui.XUI;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint({"DefaultLocale", "NonConstantResourceId", "UseCompatLoadingForDrawables"})
public class TodayFragment extends Fragment {

    LinearLayout topInfo;
    ImageView image;
    TextView city;
    TextView type;
    TextView wendu;
    TextView hpa;
    TextView wind;
    TextView ganmao;
    TextView shidu;
    RelativeLayout recent;
    TextView airText;
    TextView airq1;
    TextView airq;
    LinearLayout topPic;
    Button h24Btn;
    Button mainBtn;
    Button backBtn;

    ACache mCache;
    String cityName;
    private final BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH)) {

                try {
                    renderContent();
                    renderRecent();
                } catch (Exception e) {
                    Log.w("RainWather", "Exception: ", e);
                }
            }
        }
    };
    View.OnClickListener l1 = v -> {
        Intent intent = new Intent(getContext(), H24Activity.class);
        intent.putExtra("IS_FROM_HOME", true);
        startActivity(intent);
    };
    View.OnClickListener l2 = v -> {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("IS_FROM_HOME", true);
        startActivity(intent);
    };
    View.OnClickListener l3 = v -> {
        Intent intent = new Intent(getContext(), IndexActivity.class);
        ParamApplication application = (ParamApplication) requireActivity().getApplication();
        intent.putExtra("currentTab", Objects.requireNonNull(application).getTAB_TAG_RECENT());
        startActivity(intent);
    };

    private void findViwes(View me) {
        topInfo = me.findViewById(R.id.top_info);
        image = me.findViewById(R.id.imageView1);
        city = me.findViewById(R.id.city);
        type = me.findViewById(R.id.type);
        wendu = me.findViewById(R.id.wendu);
        hpa = me.findViewById(R.id.hpa);
        wind = me.findViewById(R.id.wind);
        ganmao = me.findViewById(R.id.ganmao);
        shidu = me.findViewById(R.id.shidu);
        recent = me.findViewById(R.id.recent_today);
        airText = me.findViewById(R.id.air_text);
        airq1 = me.findViewById(R.id.airq);
        airq = me.findViewById(R.id.airq1);
        topPic = me.findViewById(R.id.top_pic);
        h24Btn = me.findViewById(R.id.h24_btn);
        mainBtn = me.findViewById(R.id.main_btn);
        backBtn = me.findViewById(R.id.back_btn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, viewGroup, false);
        findViwes(view);
        LinearLayout infoB = view.findViewById(R.id.info_b);
        mCache = ACache.get(getContext());
        cityName = requireActivity().getIntent().getStringExtra("city");
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        if (cityName == null || cityName.trim().equals("")) {
            h24Btn.setOnClickListener(l1);
            mainBtn.setOnClickListener(l2);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CacheKey.REFRESH);
            requireContext().registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        } else {
            h24Btn.setVisibility(View.GONE);
            mainBtn.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(l3);
        }

        int[] size = CommonUtil.getScreenSize(requireActivity());
        int y = size[1];
        if (y >= 1500) {
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    (int) (y * 0.95 / 2.0)
            );
            topPic.setLayoutParams(params1);
            infoB.setPadding(0, (int) (y * 0.7 / 20), 0, 0);
        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        try {
            renderContent();
            renderRecent();
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

    }

    public void renderContent() {
        JSONObject wAllJson;
        String cityCurrent;
        if (cityName == null || cityName.trim().equals("")) {
            //  JSONObject cityJson  = mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION);
            LocationVO locationVO = (LocationVO) mCache.getAsObject(CacheKey.CURRENT_LOCATION);
            cityCurrent = locationVO.getDistrict();
        } else {
            cityCurrent = cityName;
        }
        wAllJson = mCache.getAsJSONObject(cityCurrent + ":" + CacheKey.WEATHER_ALL);

        try {
            //JSONObject today = ((JSONObject) weatherJson.getJSONArray("forecast").get(0));
            JSONObject airJson = wAllJson.getJSONObject("current").getJSONObject("air");
            JSONObject current = wAllJson.getJSONObject("current").getJSONObject("current");
            String tempratureStr = current.getString("temperature") + "°";
            String pressureStr = "压强:" + current.getString("airpressure") + "hpa";
            String humidityStr = "湿度:" + current.getString("humidity") + "%";

            String wtype = current.getString("weather");
            city.setTypeface(CommonUtil.weatherIconFontFace(requireContext()));
            type.setTypeface(CommonUtil.weatherIconFontFace(requireContext()));
            city.setText(Html.fromHtml((cityName == null || cityName.trim().equals("") ? "<font>\ue71b</font> " : "") +
                    cityCurrent, Html.FROM_HTML_MODE_LEGACY));
            type.setText(Html.fromHtml(wtype, Html.FROM_HTML_MODE_LEGACY));

            wendu.setText(tempratureStr);
            hpa.setText(pressureStr);
            wind.setText(Html.fromHtml(current.getString("winddir") + current.getString("windpower"),
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            shidu.setText(humidityStr);
            ganmao.setText(wAllJson.getJSONObject("current").getString("tips"));
            setAirColor(airJson.getInt("AQI"), airJson.getString("levelIndex"));
            airq.setText(airJson.getString("levelIndex"));
            airq1.setText(airJson.getString("AQI"));
            SharedPreferences preferences;
            if (DataUtil.isDay()) {
                getContext();
                preferences = requireContext().getSharedPreferences("day_picture", Context.MODE_PRIVATE);
            } else {
                getContext();
                preferences = requireContext().getSharedPreferences("night_picture", Context.MODE_PRIVATE);
                topPic.setBackgroundColor(getResources().getColor(R.color.skyblue_night, null));
                topInfo.setBackground(getResources().getDrawable(R.drawable.nbackground, null));
            }
            if (wtype.equals("阴天") || wtype.equals("多云") || wtype.equals("阴")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skygrey, null));
            }
            if (wtype.contains("雨") && !wtype.contains("转")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skyrain, null));
            }
            if (wtype.contains("雪") && !wtype.contains("转")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skysnow, null));
            }
            if (wtype.equals("沙尘暴") || wtype.equals("扬沙") || wtype.equals("浮尘")) {
                topPic.setBackgroundColor(getResources().getColor(R.color.skydust, null));
            }
            String weatherImg = preferences.getString(current.getString("weather"), "notclear.png");
            image.setImageDrawable(CommonUtil.drawableFromAssets(requireContext(), weatherImg));
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

    }

    private void setAirColor(Integer aqi, String index) {
        String text = "空气指数：" + aqi + "（" + index + "）";
        airText.setText(text);
        if (aqi <= 50) {

            airText.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            airq.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            airq1.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        } else if (aqi <= 100) {
            airText.setTextColor(getResources().getColor(R.color.color51_, null));
            airq.setBackgroundColor(getResources().getColor(R.color.color51_, null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color51_, null));
            airq.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
            airq1.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        } else if (aqi <= 150) {
            airText.setTextColor(getResources().getColor(R.color.color100_, null));
            airq.setBackgroundColor(getResources().getColor(R.color.color100_, null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color100_, null));
            airq.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
            airq1.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        } else if (aqi <= 200) {
            airText.setTextColor(getResources().getColor(R.color.color150_, null));
            airq.setBackgroundColor(getResources().getColor(R.color.color150_, null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color150_, null));
        } else if (aqi <= 300) {
            airText.setTextColor(getResources().getColor(R.color.color200_, null));
            airq.setBackgroundColor(getResources().getColor(R.color.color200_, null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color200_, null));
        } else {
            airText.setTextColor(getResources().getColor(R.color.color300_, null));
            airq.setBackgroundColor(getResources().getColor(R.color.color300_, null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color300_, null));
        }
    }

    public void renderRecent() throws Exception {
        String cityCurrent;
        if (cityName == null || cityName.trim().equals("")) {
            LocationVO locationVO = (LocationVO) mCache.getAsObject(CacheKey.CURRENT_LOCATION);
            cityCurrent = locationVO.getDistrict();
        } else {
            cityCurrent = cityName;
        }
        JSONObject weatherJson = mCache.getAsJSONObject(cityCurrent + ":" + CacheKey.WEATHER_ALL);
        JSONArray recentArray = weatherJson.getJSONArray("forecast15d");
        getContext();
        SharedPreferences preferences = requireContext().getSharedPreferences("weahter_icon", Context.MODE_PRIVATE);
        int len = recentArray.length();
        int l = len - 1;
        String[] high = new String[l];
        String[] low = new String[l];
        int[] highInt = new int[l];
        int[] lowInt = new int[l];
        String[] weathers = new String[l];
        String[] days = new String[l];
        // 1, 构造显示用渲染图
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        // 2,进行显示
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // 2.1, 构建数据
        XYSeries seriesHigh = new XYSeries("最高温度");
        XYSeries seriesLow = new XYSeries("最低温度");
        for (int i = 1; i < len; i++) {
            int j = i - 1;
            JSONObject day = recentArray.getJSONObject(i);
            days[j] = day.getString("forecasttime");
            weathers[j] = day.getString("weather_am");
            low[j] = day.getString("temperature_pm")
                    .replace("低温", "")
                    .replace(" ", "")
                    .replace("℃", "");
            high[j] = day.getString("temperature_am")
                    .replace("高温", "")
                    .replace(" ", "")
                    .replace("℃", "");
            lowInt[j] = Integer.parseInt(low[j]);
            highInt[j] = Integer.parseInt(high[j]);
            String weather = preferences.getString(weathers[j], "\ue73e");
            renderer.addXTextLabel(j, days[j] + "\n" + weather);
            seriesHigh.add(j, highInt[j]);
            seriesLow.add(j, lowInt[j]);

            //System.out.println(Integer.parseInt(temps[0].trim()));
        }
        int max = CommonUtil.maxOfAarray(highInt) + 5;
        int min = CommonUtil.minOfAarray(lowInt) - 5;
        renderer.setTextTypeface(CommonUtil.weatherIconFontFace(getContext()));
        renderer.setAxesColor(this.getResources().getColor(R.color.myorange, null));
        renderer.setZoomEnabled(true, true);
        renderer.setPanEnabled(false, false);
        renderer.setXLabels(0);
        renderer.setExternalZoomEnabled(true);
        renderer.setAxisTitleTextSize(25);
        renderer.setLegendTextSize(25);
        renderer.setChartTitleTextSize(30);
        renderer.setMarginsColor(this.getResources().getColor(R.color.cardview_light_background, null));
        renderer.setLabelsColor(this.getResources().getColor(R.color.tips, null));
        renderer.setXLabelsColor(this.getResources().getColor(R.color.tips, null));
        renderer.setGridColor(this.getResources().getColor(R.color.mybord, null));
        renderer.setYTitle("温度(℃)");
        renderer.setApplyBackgroundColor(true);
        renderer.setFitLegend(true);
        renderer.setLabelsTextSize(20);
        renderer.setMargins(new int[]{50, 50, 70, 50});//设置图表的外边框(上/左/下/右)
        renderer.setZoomRate(1.1f);
        renderer.setPointSize(10);
        renderer.setLabelsTextSize(25);
        renderer.setAxisTitleTextSize(25);
        renderer.setChartTitleTextSize(35);
        renderer.setShowGrid(true);
        renderer.setRange(new double[]{-0.5, 7.5, min, max});
        renderer.setYLabelsAlign(Paint.Align.LEFT);
        renderer.setYLabelsColor(0, this.getResources().getColor(R.color.tips, null));
        dataset.addSeries(seriesLow);
        dataset.addSeries(seriesHigh);
        // 3, 对点的绘制进行设置
        XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
        // 3.1设置颜色

        xyRenderer.setColor(this.getResources().getColor(R.color.myblue, null));
        xyRenderer.setDisplayChartValues(true);
        xyRenderer.setFillPoints(true);
        xyRenderer.setLineWidth(5);
        xyRenderer.setChartValuesTextAlign(Paint.Align.CENTER);
        xyRenderer.setChartValuesTextSize(25);
        xyRenderer.setChartValuesSpacing(20);
        // 3.2设置点的样式
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        // 3.3, 将要绘制的点添加到坐标绘制中
        renderer.addSeriesRenderer(xyRenderer);
        // 3.4,重复 1~3的步骤绘制第二个系列点
        xyRenderer = new XYSeriesRenderer();
        xyRenderer.setColor(this.getResources().getColor(R.color.myorange, null));
        xyRenderer.setDisplayChartValues(true);
        xyRenderer.setFillPoints(true);
        xyRenderer.setLineWidth(5);
        xyRenderer.setChartValuesTextAlign(Paint.Align.CENTER);
        xyRenderer.setChartValuesTextSize(25);
        xyRenderer.setChartValuesSpacing(20);
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        renderer.addSeriesRenderer(xyRenderer);
        View view = ChartFactory.getLineChartView(getContext(), dataset, renderer);
        view.setBackgroundColor(Color.WHITE);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        view.setLayoutParams(params1);
        recent.addView(view, params1);
        // 通过Activity类中的getWindowManager()方法获取窗口管理，再调用getDefaultDisplay()方法获取获取Display对象

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

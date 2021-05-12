package com.dddpeter.app.rainweather;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dddpeter.app.rainweather.common.ACache;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.common.DataUtil;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.xuexiang.xui.XUI;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;

import java.io.InputStream;
import java.time.LocalDateTime;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("DefaultLocale")
public class TodayActivity extends FinalActivity {
    @ViewInject(id=R.id.top_info)
    LinearLayout topInfo;
    @ViewInject(id = R.id.imageView1)
    ImageView image;
    @ViewInject(id = R.id.city)
    TextView city;
    @ViewInject(id = R.id.type)
    TextView type;
    @ViewInject(id = R.id.wendu)
    TextView wendu;
    @ViewInject(id = R.id.hpa)
    TextView hpa;

    @ViewInject(id = R.id.wind)
    TextView wind;
    @ViewInject(id = R.id.ganmao)
    TextView ganmao;
    @ViewInject(id = R.id.shidu)
    TextView  shidu;
    @ViewInject(id = R.id.recent_today)
    RelativeLayout recent;
    @ViewInject(id = R.id.air_text)
    TextView airText;
    @ViewInject(id = R.id.airq)
    TextView airq1;
    @ViewInject(id = R.id.airq1)
    TextView airq;
    @ViewInject(id=R.id.top_pic)
    LinearLayout topPic;
    @ViewInject(id = R.id.h24_btn)
    Button h24Btn;
    @ViewInject(id = R.id.main_btn)
    Button mainBtn;
    @ViewInject(id = R.id.back_btn)
    Button backBtn;
    ACache mCache;
    String cityName;


    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
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
        Intent intent=new Intent(getApplicationContext(),H24Activity.class);
        intent.putExtra("IS_FROM_HOME",true);
        startActivity(intent);
    };
    View.OnClickListener l2 = v -> {
        Intent intent=new Intent(getApplicationContext(),MainActivty.class);
        intent.putExtra("IS_FROM_HOME",true);
        startActivity(intent);
    };
    View.OnClickListener l3 = v -> {
        Intent intent=new Intent(getApplicationContext(),IndexActivity.class);
        ParamApplication application = (ParamApplication) getApplicationContext();
        intent.putExtra("currentTab",application.getTAB_TAG_RECENT());
        startActivity(intent);
    };
    @Override
    protected void attachBaseContext(Context newBase) {
        //注入字体
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        setContentView(R.layout.activity_today);
        cityName = getIntent().getStringExtra("city");
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        if(StringUtil.isBlank(cityName)){
            h24Btn.setOnClickListener(l1);
            mainBtn.setOnClickListener(l2);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CacheKey.REFRESH);
            registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        }
        else{
            h24Btn.setVisibility(View.GONE);
            mainBtn.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(l3);
        }

        try {
            renderContent();
            renderRecent();
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

    }

    @Override
    protected void onResume() {
        ParamApplication application = (ParamApplication) TodayActivity.this.getApplicationContext();
        if (application.isRefreshed()) {
            Toast.makeText(TodayActivity.this, "正在刷新天气", Toast.LENGTH_SHORT).show();
        }
        super.onResume();

        try {
            renderContent();
            renderRecent();
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

    }

    public void renderContent() throws JSONException {
        JSONObject wAllJson;
        String cityCurrent;
        if(StringUtil.isBlank(cityName)){
            JSONObject cityJson  = mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION);
            cityCurrent = cityJson.getString("district");
        }
        else{
            cityCurrent = cityName;
        }
        wAllJson = mCache.getAsJSONObject(cityCurrent + ":" + CacheKey.WEATHER_ALL);

        try {
            //JSONObject today = ((JSONObject) weatherJson.getJSONArray("forecast").get(0));
            JSONObject airJson = wAllJson.getJSONObject("current").getJSONObject("air");
            JSONObject current = wAllJson.getJSONObject("current").getJSONObject("current");
            String wtype = current.getString("weather");
            city.setTypeface(CommonUtil.weatherIconFontFace(this));
            type.setTypeface(CommonUtil.weatherIconFontFace(this));
            city.setText(Html.fromHtml(  (StringUtil.isBlank(cityName)?"<font>\ue71b</font> " : "") +
                    cityCurrent,Html.FROM_HTML_MODE_LEGACY));
            type.setText(Html.fromHtml(wtype,Html.FROM_HTML_MODE_LEGACY));
            wendu.setText(current.getString("temperature") + "°");
            hpa.setText("压强:" +  current.getString("airpressure")+"hpa");
            wind.setText(Html.fromHtml(current.getString("winddir") + current.getString("windpower"),
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            shidu.setText("湿度:"+ current.getString("humidity")+"%");
            ganmao.setText( wAllJson.getJSONObject("current").getString("tips"));
            setAirColor(new Integer(airJson.getInt("AQI")), airJson.getString("levelIndex"));
            airq.setText(airJson.getString("levelIndex"));
            airq1.setText(airJson.getString("AQI"));
            SharedPreferences preferences;
            if (DataUtil.isDay()) {
                preferences = getSharedPreferences("day_picture", MODE_PRIVATE);
            } else {
                preferences = getSharedPreferences("night_picture", MODE_PRIVATE);
                topPic.setBackgroundColor(getResources().getColor(R.color.skyblue_night,null));
                topInfo.setBackground(getResources().getDrawable(R.drawable.nbackground,null));
            }
            if(wtype == "阴天" || wtype == "多云" ){
                topPic.setBackgroundColor(getResources().getColor(R.color.skygrey,null));
            }
            if(wtype.indexOf("雨") !=-1 && wtype.indexOf("转") < 0){
                topPic.setBackgroundColor(getResources().getColor(R.color.skyrain,null));
            }
            if(wtype.indexOf("雪") !=-1 && wtype.indexOf("转") < 0){
                topPic.setBackgroundColor(getResources().getColor(R.color.skysnow,null));
            }
            if(wtype == "沙尘暴" || wtype == "扬沙"  || wtype == "浮尘"){
                topPic.setBackgroundColor(getResources().getColor(R.color.skydust,null));
            }
            String weatherImg = preferences.getString(current.getString("weather"), "notclear.png");
            AssetManager manager = getAssets();
            InputStream is = manager.open(weatherImg);
            image.setImageDrawable(Drawable.createFromStream(is, weatherImg));
            is.close();
        } catch (Exception e) {
            Log.w("RainWather", "Exception: ", e);
        }

    }

    private void setAirColor(Integer aqi,String index) {
        String text ="空气指数：" + aqi + "（" + index+ "）";
        airText.setText(text);
        if(aqi<=50){

            airText.setTextColor(getResources().getColor(R.color.colorPrimary,null));
            airq.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
            airq1.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
        }
        else if(aqi<=100){
            airText.setTextColor(getResources().getColor(R.color.color51_,null));
            airq.setBackgroundColor(getResources().getColor(R.color.color51_,null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color51_,null));
            airq.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
            airq1.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
        }
        else if(aqi<=150){
            airText.setTextColor(getResources().getColor(R.color.color100_,null));
            airq.setBackgroundColor(getResources().getColor(R.color.color100_,null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color100_,null));
            airq.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
            airq1.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
        }
        else if(aqi<=200){
            airText.setTextColor(getResources().getColor(R.color.color150_,null));
            airq.setBackgroundColor(getResources().getColor(R.color.color150_,null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color150_,null));
        }
        else if(aqi<=300){
            airText.setTextColor(getResources().getColor(R.color.color200_,null));
            airq.setBackgroundColor(getResources().getColor(R.color.color200_,null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color200_,null));
        }
        else{
            airText.setTextColor(getResources().getColor(R.color.color300_,null));
            airq.setBackgroundColor(getResources().getColor(R.color.color300_,null));
            airq1.setBackgroundColor(getResources().getColor(R.color.color300_,null));
        }
    }

    protected void renderRecent() throws Exception {
        String cityCurrent;
        if(StringUtil.isBlank(cityName)){
            JSONObject cityJson  = mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION);
            cityCurrent = cityJson.getString("district");
        }
        else{
            cityCurrent = cityName;
        }
        JSONObject weatherJson = mCache.getAsJSONObject(cityCurrent+ ":" + CacheKey.WEATHER_ALL);
        JSONArray recentArray = weatherJson.getJSONArray("forecast15d");
        SharedPreferences preferences = getSharedPreferences("weahter_icon", MODE_PRIVATE);
        int len = recentArray.length();
        int l = len -4;
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
        for (int i = 4; i < len; i++) {
            int j = i-4;
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
        int max = CommonUtil.maxOfAarray(highInt) + 2;
        int min = CommonUtil.minOfAarray(lowInt) - 5;
        renderer.setTextTypeface(CommonUtil.weatherIconFontFace(this));
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
        renderer.setRange(new double[]{-0.36, 4.36, min, max});
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
        View view = ChartFactory.getLineChartView(this, dataset, renderer);
        view.setBackgroundColor(Color.WHITE);


        @SuppressWarnings("deprecation")
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT
        );
        view.setLayoutParams(params1);
        recent.addView(view, params1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }
}

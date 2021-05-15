package com.dddpeter.app.rainweather;

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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import com.dddpeter.app.rainweather.pojo.LocationVO;
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


import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

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
    @ViewInject(id= R.id.recent_today_scroll)
    LinearLayout rtScoll;
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
        if(cityName==null  || cityName.trim().equals("")){
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
        LinearLayout infoB = findViewById(R.id.info_b);
/*        int[]  size = CommonUtil.getScreenSize(this);
        int x = size[0];
        int y = size[1];
        if(y>=1500){
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    (int) (y*0.95/2.0)
            );
            topPic.setLayoutParams(params1);
            infoB.setPadding(0,   (int) (y*0.7/20),0,0);
        }*/
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
        if(cityName==null  || cityName.trim().equals("")){
          //  JSONObject cityJson  = mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION);
            LocationVO locationVO = (LocationVO) mCache.getAsObject(CacheKey.CURRENT_LOCATION);
            cityCurrent = locationVO.getDistrict();
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
            city.setText(Html.fromHtml(  (cityName==null  || cityName.trim().equals("")?"<font>\ue71b</font> " : "") +
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
            if(wtype.equals("阴天")  || wtype.equals("多云")|| wtype.equals("阴")){
                topPic.setBackgroundColor(getResources().getColor(R.color.skygrey,null));
            }
            if(wtype.indexOf("雨") !=-1 && wtype.indexOf("转") < 0){
                topPic.setBackgroundColor(getResources().getColor(R.color.skyrain,null));
            }
            if(wtype.indexOf("雪") !=-1 && wtype.indexOf("转") < 0){
                topPic.setBackgroundColor(getResources().getColor(R.color.skysnow,null));
            }
            if(wtype.equals("沙尘暴") || wtype.equals("扬沙") || wtype.equals("浮尘")){
                topPic.setBackgroundColor(getResources().getColor(R.color.skydust,null));
            }
            String weatherImg = preferences.getString(current.getString("weather"), "notclear.png");
            image.setImageDrawable(CommonUtil.drawableFromAssets(this,weatherImg));
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
        if(cityName==null  || cityName.trim().equals("")){
            LocationVO locationVO   = (LocationVO) mCache.getAsObject(CacheKey.CURRENT_LOCATION);
            cityCurrent = locationVO.getDistrict();
        }
        else{
            cityCurrent = cityName;
        }
        JSONObject weatherJson = mCache.getAsJSONObject(cityCurrent+ ":" + CacheKey.WEATHER_ALL);
        JSONArray recentArray = weatherJson.getJSONArray("forecast15d");
        SharedPreferences preferences = getSharedPreferences("weahter_icon", MODE_PRIVATE);
        int len = recentArray.length();
        int l = len -1;
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
            int j = i-1;
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
            SharedPreferences preferences1 = getSharedPreferences("day_picture", MODE_PRIVATE);
            String iconw = preferences1.getString(day.getString("weather_am"),"notclear.png");
            LinearLayout linearLayout = new LinearLayout(this);
            TextView textView = new TextView(this);
            TextView textView1 = new TextView(this);
            TextView textView2 = new TextView(this);
            ImageView imageView = new ImageView(this);
            float factor =  getResources().getDisplayMetrics().density;
            LinearLayout.LayoutParams paramOuter =  new LinearLayout.LayoutParams(
                    (int)(50 * factor + 0.5),
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            ViewGroup.LayoutParams param1 =  new ViewGroup.LayoutParams(
                    (int)(50 * factor + 0.5),
                    (int)(14 * factor + 0.5)
            );
            ViewGroup.LayoutParams param2 =  new ViewGroup.LayoutParams(
                    (int)(50 * factor + 0.5),
                    (int)(32 * factor + 0.5)
            );

            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(paramOuter);
            linearLayout.setPadding(0,  (int)(5 * factor + 0.5),0,0);
            imageView.setLayoutParams(param2);
            imageView.setImageDrawable(CommonUtil.drawableFromAssets(this,iconw));
            imageView.setPadding(0,  (int)(5 * factor + 0.5),0,(int)(5 * factor + 0.5));
            textView.setText(days[j]);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            textView.setTextColor(getResources().getColor(R.color.greyfont,null));
            textView.setGravity(Gravity.CENTER);

            textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            textView1.setTextColor(getResources().getColor(R.color.myblue,null));
            textView1.setText(weathers[j]);
            textView1.setGravity(Gravity.CENTER);

            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            textView2.setTextColor(getResources().getColor(R.color.myblue,null));
            textView2.setText(high[j]+"℃");
            textView2.setGravity(Gravity.CENTER);

            textView.setLayoutParams(param1);
            textView1.setLayoutParams(param1);
            textView2.setLayoutParams(param1);

            linearLayout.addView(textView);
            linearLayout.addView(imageView);
            linearLayout.addView(textView1);
            linearLayout.addView(textView2);
            rtScoll.addView(linearLayout);
        }
        int max = CommonUtil.maxOfAarray(highInt) + 2;
        int min = CommonUtil.minOfAarray(lowInt) - 5;
        renderer.setTextTypeface(CommonUtil.weatherIconFontFace(this));
        renderer.setAxesColor(this.getResources().getColor(R.color.colorPrimary, null));
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
        renderer.setShowAxes(false);
        renderer.setGridColor(this.getResources().getColor(R.color.mybord, null));
        //renderer.setYTitle("温度(℃)");
        renderer.setYTitle("");
        renderer.setApplyBackgroundColor(true);
        renderer.setFitLegend(true);
        renderer.setLabelsTextSize(20);
        renderer.setMargins(new int[]{5, 30, 55, 30});//设置图表的外边框(上/左/下/右)
        renderer.setFitLegend(true);
        renderer.setZoomRate(0.8f);
        renderer.setPointSize(8);
        renderer.setLabelsTextSize(25);
        renderer.setAxisTitleTextSize(25);
        renderer.setChartTitleTextSize(35);
        renderer.setShowGrid(true);
        renderer.setRange(new double[]{-0.36, 10.36, min, max});
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
        xyRenderer.setChartValuesSpacing(-35);
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
        xyRenderer.setChartValuesSpacing(-35);
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        renderer.addSeriesRenderer(xyRenderer);
        View view = ChartFactory.getLineChartView(this, dataset, renderer);
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshBroadcastReceiver);
    }
}

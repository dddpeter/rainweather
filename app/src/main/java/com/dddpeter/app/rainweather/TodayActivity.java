package com.dddpeter.app.rainweather;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.common.DataUtil;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.common.ACache;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
@SuppressLint("DefaultLocale")
public class TodayActivity extends FinalActivity {

    @ViewInject(id = R.id.imageView1)
    ImageView image;
    @ViewInject(id = R.id.city)
    TextView city;
    @ViewInject(id = R.id.type)
    TextView type;
    @ViewInject(id = R.id.wendu)
    TextView wendu;
    @ViewInject(id = R.id.wendugd)
    TextView wendugd;
    @ViewInject(id = R.id.wind)
    TextView wind;
    @ViewInject(id = R.id.ganmao)
    TextView ganmao;
    @ViewInject(id = R.id.recent_today)
    RelativeLayout recent;
    @ViewInject(id=R.id.air)
    com.xuexiang.xui.widget.progress.HorizontalProgressView air;
    @ViewInject(id=R.id.air_text)
    TextView airText;

    ACache mCache;


    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CacheKey.REFRESH))
            {

                try {
                    renderContent();
                    renderRecent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        setContentView(R.layout.activity_today);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheKey.REFRESH);
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);

        try {
            renderContent();
            renderRecent();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }

    }

    public void renderContent() throws JSONException {
        String location = mCache.getAsString(CacheKey.CURRENT_LOCATION);
        JSONObject cityJson = new JSONObject(location);
        JSONObject weatherJson = mCache.getAsJSONObject(cityJson.getString("district")+":"+CacheKey.WEATHER_DATA);
        JSONObject wAllJson = mCache.getAsJSONObject(cityJson.getString("district")+":"+CacheKey.WEATHER_ALL);
        StringBuilder htmlStrBuilder = new StringBuilder();
        SharedPreferences preferencesWI = getSharedPreferences("weahter_icon", MODE_PRIVATE);
        htmlStrBuilder.append("<p>");
        try {
            JSONObject today =  ((JSONObject)weatherJson.getJSONArray("forecast").get(0));
            city.setText( weatherJson.getString("city"));
            type.setTypeface(CommonUtil.weatherIconFontFace(this));
            type.setText( preferencesWI.getString(today.getString("type"),"\ue73e")
                    + "\t" + today.getString("type") );
            wendu.setText( weatherJson.getString("wendu")+"°C");
            wendugd.setText( today.getString("low") + "  ~  " + today.getString("high"));
            wind.setText( Html.fromHtml(today.getString("fengxiang") + "   " + today.getString("fengli"),
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            ganmao.setText(weatherJson.getString("ganmao"));
            JSONObject airJson = wAllJson.getJSONObject("current").getJSONObject("air");
            air.setProgress(airJson.getInt("AQI")*1.0f/5.0f);
            air.setProgressTextVisibility(false);
            airText.setText("空气指数："+ airJson.getInt("AQI") +"（" +airJson.getString("levelIndex")+"）");
            SharedPreferences preferences;
            if(DataUtil.isDay()){
                preferences = getSharedPreferences("day_picture", MODE_PRIVATE);
            }
            else{
                preferences = getSharedPreferences("night_picture", MODE_PRIVATE);
            }
            String weatherImg = preferences.getString(today.getString("type"),"notclear.png");
            AssetManager manager = getAssets();
            InputStream is = manager.open(weatherImg);
            image.setImageDrawable(Drawable.createFromStream(is,weatherImg));
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    protected void renderRecent() throws Exception {
        String location = mCache.getAsString(CacheKey.CURRENT_LOCATION);
        JSONObject cityJson = new JSONObject(location);
        JSONObject weatherJson = mCache.getAsJSONObject(cityJson.getString("district") + ":"+CacheKey.WEATHER_DATA);
        JSONArray recentArray = weatherJson.getJSONArray("forecast");
        SharedPreferences preferences = getSharedPreferences("weahter_icon", MODE_PRIVATE);
        int len = recentArray.length();
        String[] high = new String[len];
        String[] low = new String[len];
        int[] highInt = new int[len];
        int[] lowInt = new int[len];
        String[] weathers = new String[len];
        String[] days = new String[len];
        // 1, 构造显示用渲染图
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        // 2,进行显示
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // 2.1, 构建数据
        XYSeries seriesHigh = new XYSeries("最高温度");
        XYSeries seriesLow = new XYSeries("最低温度");
        for (int i=0;i<len;i++) {
            JSONObject day = recentArray.getJSONObject(i);
            days[i] = LocalDateTime.now().getMonthValue() +"/"+ day.getString("date").split("日")[0];
            weathers[i] = day.getString("type");
            low[i] = day.getString("low")
                    .replace("低温","")
                    .replace(" ","")
                    .replace("℃","");
            high[i] = day.getString("high")
                    .replace("高温","")
                    .replace(" ","")
                    .replace("℃","");
            lowInt[i] = Integer.parseInt(low[i]);
            highInt[i] = Integer.parseInt(high[i]);
            String weather = preferences.getString(weathers[i] ,"\ue73e");
            renderer.addXTextLabel(i, days[i] + "\n" + weather);
            seriesHigh.add(i , highInt[i]);
            seriesLow.add(i , lowInt[i]);

            //System.out.println(Integer.parseInt(temps[0].trim()));
        }
        int max = CommonUtil.maxOfAarray(highInt) + 3;
        int min = CommonUtil.minOfAarray(lowInt) - 2 ;
        renderer.setTextTypeface(CommonUtil.weatherIconFontFace(this));
        renderer.setAxesColor(this.getResources().getColor(R.color.myorange,null));
        renderer.setZoomEnabled(true, true);
        renderer.setPanEnabled(false, false);
        renderer.setXLabels(0);
        renderer.setExternalZoomEnabled(true);
        renderer.setAxisTitleTextSize(25);
        renderer.setLegendTextSize(25);
        renderer.setChartTitleTextSize(30);
        renderer.setMarginsColor(this.getResources().getColor(R.color.myblue,null));
        renderer.setLabelsColor(this.getResources().getColor(R.color.tips,null));
        renderer.setXLabelsColor(this.getResources().getColor(R.color.tips,null));
        renderer.setGridColor(this.getResources().getColor(R.color.mygrey,null));
        renderer.setYTitle("温度(℃)");
        renderer.setApplyBackgroundColor(true);
        renderer.setFitLegend(true);
        renderer.setLabelsTextSize(20);
        renderer.setMargins(new int[]{50, 50, 70, 50});//设置图表的外边框(上/左/下/右)
        renderer.setZoomRate(1.1f);
        renderer.setPointSize(10);
        renderer.setBackgroundColor(this.getResources().getColor(R.color.myblue,null));
        renderer.setLabelsTextSize(25);
        renderer.setAxisTitleTextSize(25);
        //renderer.setChartTitle("最近天气");
        renderer.setChartTitleTextSize(35);

        renderer.setShowGrid(true);
       renderer.setRange(new double[] { -0.36,4.36, min, max });
       renderer.setYLabelsAlign(Paint.Align.LEFT);
       renderer.setYLabelsColor(0,this.getResources().getColor(R.color.tips,null));
        dataset.addSeries(seriesLow);
        dataset.addSeries(seriesHigh);
        // 3, 对点的绘制进行设置
        XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
        // 3.1设置颜色

        xyRenderer.setColor(this.getResources().getColor(R.color.tips,null));
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
        xyRenderer.setColor(this.getResources().getColor(R.color.myorange,null));
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

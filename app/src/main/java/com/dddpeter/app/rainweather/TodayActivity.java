package com.dddpeter.app.rainweather;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dddpeter.app.rainweather.common.DataUtil;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.common.ACache;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.json.JSONObject;

import java.io.InputStream;

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

    ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCache = ACache.get(this);
        setContentView(R.layout.activity_today);
        renderContent();

    }

    @Override
    protected void onResume() {
        ParamApplication application = (ParamApplication) TodayActivity.this.getApplicationContext();
        if (application.isRefreshed()) {
            Toast.makeText(TodayActivity.this, "正在刷新天气", Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    public void renderContent(){
        JSONObject weatherJson = mCache.getAsJSONObject(CacheKey.CURRENT_LOCATION+":"+CacheKey.WEATHER_DATA);
        StringBuilder htmlStrBuilder = new StringBuilder();
        htmlStrBuilder.append("<p>");
        try {
            JSONObject today =  ((JSONObject)weatherJson.getJSONArray("forecast").get(0));
            city.setText( weatherJson.getString("city"));
            type.setText( today.getString("type") );
            wendu.setText( weatherJson.getString("wendu")+"°C");
            wendugd.setText( today.getString("low") + "  ~  " + today.getString("high"));
            wind.setText( Html.fromHtml(today.getString("fengxiang") + "   " + today.getString("fengli"),
                    Html.FROM_HTML_OPTION_USE_CSS_COLORS));
            ganmao.setText(weatherJson.getString("ganmao"));
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


}

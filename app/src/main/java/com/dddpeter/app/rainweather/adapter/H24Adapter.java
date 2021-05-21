package com.dddpeter.app.rainweather.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.common.DataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class H24Adapter extends ArrayAdapter<JSONObject> {
    private int resourceId;
    private SharedPreferences preferences;

    public H24Adapter(@NonNull Context context, int resource, @NonNull List<JSONObject> objects, SharedPreferences preferencesWI) {
        super(context, resource, objects);
        resourceId = resource;
        preferences = preferencesWI;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject item = getItem(position);   //获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView h24Date = view.findViewById(R.id.h24_date);
        LinearLayout h24info = view.findViewById(R.id.h24_info);
        TextView infoTemperature = view.findViewById(R.id.info_temprature);
        ImageView imageView  = view.findViewById(R.id.info_typeimg);
        TextView infoW = view.findViewById(R.id.info_w);
        TextView infoWind = view.findViewById(R.id.info_wind);
        try {
            String time = item.getString("forecasttime");
            int h = Integer.valueOf(time.substring(0,time.indexOf(":")));
            h24Date.setText(item.getString("forecasttime"));
            /*
            "windPower": "3级",
            "temperature": "17℃",
            "weather": "多云",
            "windDir": "东北风"
             */
            infoTemperature.setText(item.getString("temperature"));
            infoW.setTypeface(CommonUtil.weatherIconFontFace(getContext()));
            infoW.setText(item.getString("weather"));
            SharedPreferences p = view.getContext().getSharedPreferences("day_picture", MODE_PRIVATE);
            if (h<6 || h>=18) {
                p =   view.getContext().getSharedPreferences("night_picture", MODE_PRIVATE);
            }
            else{
                p =   view.getContext().getSharedPreferences("day_picture", MODE_PRIVATE);
            }
            String weatherImg = p.getString(item.getString("weather"), "notclear.png");
            imageView.setImageDrawable(CommonUtil.drawableFromAssets(view.getContext(),weatherImg));
            infoWind.setText(item.getString("windDir") + "(" + item.getString("windPower") + ")");
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }
        return view;
    }
}

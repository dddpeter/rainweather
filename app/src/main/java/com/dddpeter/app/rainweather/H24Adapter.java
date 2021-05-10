package com.dddpeter.app.rainweather;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dddpeter.app.rainweather.common.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class H24Adapter extends ArrayAdapter<JSONObject> {
    private int resourceId;
    private SharedPreferences preferences;

    public H24Adapter(@NonNull Context context, int resource, @NonNull List<JSONObject> objects,SharedPreferences preferencesWI) {
        super(context, resource, objects);
        resourceId = resource;
        preferences = preferencesWI;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
       JSONObject item =getItem(position);   //获取当前项的实例
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView h24Date = view.findViewById(R.id.h24_date);
        LinearLayout h24info = view.findViewById(R.id.h24_info);
        TextView infoTemperature = view.findViewById(R.id.info_temprature);

        TextView infoW = view.findViewById(R.id.info_w);
        TextView infoWind = view.findViewById(R.id.info_wind);
        try {
            h24Date.setText(item.getString("forecasttime"));
            /*
            "windPower": "3级",
            "temperature": "17℃",
            "weather": "多云",
            "windDir": "东北风"
             */
            infoTemperature.setText(item.getString("temperature"));
            infoW.setTypeface(CommonUtil.weatherIconFontFace(getContext()));
            infoW.setText(preferences.getString(item.getString("weather"),"\ue73e")+ "\t" + item.getString("weather"));
            infoWind.setText(item.getString("windDir")+"(" + item.getString("windPower")+")" );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}

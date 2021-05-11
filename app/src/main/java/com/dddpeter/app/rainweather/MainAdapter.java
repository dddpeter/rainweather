package com.dddpeter.app.rainweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dddpeter.app.rainweather.common.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainAdapter extends ArrayAdapter<JSONObject> {
    private int resourceId;
    private SharedPreferences preferences;

    public MainAdapter(@NonNull Context context, int resource, @NonNull List<JSONObject> objects, SharedPreferences preferencesWI) {
        super(context, resource, objects);
        resourceId = resource;
        preferences = preferencesWI;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject item = getItem(position);   //获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView mainCity = view.findViewById(R.id.main_city);
        TextView mainWeather = view.findViewById(R.id.main_weather);
        TextView mainTemprature = view.findViewById(R.id.main_tempratuare);
        TextView mainfengli = view.findViewById(R.id.main_fengli);

        try {
            mainCity.setText(item.getString("city"));
            mainTemprature.setText(item.getString("wendu") + "°C");
            mainWeather.setTypeface(CommonUtil.weatherIconFontFace(getContext()));
            mainWeather.setText(preferences.getString(item.getString("type"), "\ue73e") +
                                 "\t" + item.getString("type") );
            mainfengli.setText(Html.fromHtml(item.getString("fengxiang") + "("+item.getString("fengli") +")",Html.FROM_HTML_MODE_LEGACY));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}

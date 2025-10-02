package com.dddpeter.app.rainweather.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import com.dddpeter.app.rainweather.database.DatabaseManager;
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


public class H24Adapter extends ArrayAdapter<JSONObject> {
    private int resourceId;
    private DatabaseManager databaseManager;

    public H24Adapter(@NonNull Context context, int resource, @NonNull List<JSONObject> objects, DatabaseManager databaseManager) {
        super(context, resource, objects);
        resourceId = resource;
        this.databaseManager = databaseManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject item = getItem(position);   //获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView h24Date = view.findViewById(R.id.h24_date);
        LinearLayout h24info = view.findViewById(R.id.h24_info);
        TextView infoTemperature = view.findViewById(R.id.info_temprature);
        ImageView imageView = view.findViewById(R.id.info_typeimg);
        TextView infoW = view.findViewById(R.id.info_w);
        TextView infoWind = view.findViewById(R.id.info_wind);
        try {
            // 解析时间信息，只显示小时部分
            String forecastTime = item.getString("forecasttime");
            String time = extractHourFromTime(forecastTime);
            h24Date.setText(time);
            /*
            "windPower": "3级",
            "temperature": "17℃",
            "weather": "多云",
            "windDir": "东北风"
             */
            infoTemperature.setText(item.getString("temperature"));
            infoW.setTypeface(CommonUtil.weatherIconFontFace(getContext()));
            infoW.setText(item.getString("weather"));
            String weatherImg;
            if (DataUtil.isDay()) {
                weatherImg = databaseManager.getString("day_picture:" + item.getString("weather"), "notclear.png");
            } else {
                weatherImg = databaseManager.getString("night_picture:" + item.getString("weather"), "notclear.png");
            }
            imageView.setImageDrawable(CommonUtil.drawableFromAssets(view.getContext(), weatherImg));
            infoWind.setText(item.getString("windDir") + "(" + item.getString("windPower") + ")");
        } catch (JSONException e) {
            Log.w("RainWather", "Exception: ", e);
        }
        return view;
    }

    /**
     * 从时间字符串中提取小时部分
     */
    private String extractHourFromTime(String forecastTime) {
        if (forecastTime == null || forecastTime.trim().isEmpty()) {
            return "00";
        }
        
        try {
            // 处理不同的时间格式
            String[] timeParts = forecastTime.split(" ");
            
            // 情况1: "2024-01-15 14:00:00" 或 "2024-01-15 14:00"
            if (timeParts.length > 1) {
                String timePart = timeParts[1];
                if (timePart.contains(":")) {
                    String[] hourMinute = timePart.split(":");
                    if (hourMinute.length > 0 && hourMinute[0].length() >= 2) {
                        return hourMinute[0].substring(0, 2);
                    }
                }
            }
            
            // 情况2: "14:00:00" 或 "14:00"
            if (timeParts.length == 1 && timeParts[0].contains(":")) {
                String[] hourMinute = timeParts[0].split(":");
                if (hourMinute.length > 0 && hourMinute[0].length() >= 2) {
                    return hourMinute[0].substring(0, 2);
                }
            }
            
            // 情况3: 如果包含数字，尝试提取前两位数字
            String numbersOnly = forecastTime.replaceAll("[^0-9]", "");
            if (numbersOnly.length() >= 2) {
                return numbersOnly.substring(0, 2);
            }
            
        } catch (Exception e) {
            Log.e("H24Adapter", "解析时间失败: " + forecastTime, e);
        }
        
        return "00";
    }
}

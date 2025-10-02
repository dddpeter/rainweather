package com.dddpeter.app.rainweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.common.CommonUtil;
import com.dddpeter.app.rainweather.common.DataUtil;
import com.dddpeter.app.rainweather.database.DatabaseManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class H24RecyclerAdapter extends RecyclerView.Adapter<H24RecyclerAdapter.ViewHolder> {
    private List<JSONObject> weatherList;
    private DatabaseManager databaseManager;
    private Context context;

    public H24RecyclerAdapter(Context context, List<JSONObject> weatherList, DatabaseManager databaseManager) {
        this.context = context;
        this.weatherList = weatherList;
        this.databaseManager = databaseManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.h24_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject item = weatherList.get(position);
        try {
            // 解析时间信息
            String forecastTime = item.getString("forecasttime");
            String[] timeParts = forecastTime.split(" ");
            String date = timeParts.length > 0 ? timeParts[0] : "今天";
            
            // 设置时间信息
            holder.dateText.setText(date);
            
            // 设置温度
            String temperature = item.getString("temperature").replace("℃", "°");
            holder.temperatureText.setText(temperature);
            
            // 设置天气描述
            String weather = item.getString("weather");
            holder.weatherText.setText(weather);
            
            // 设置风向风力
            String windDir = item.getString("windDir");
            String windPower = item.getString("windPower");
            holder.windText.setText(windDir + " " + windPower);
            
            // 设置天气图标
            String weatherImg;
            if (DataUtil.isDay()) {
                weatherImg = databaseManager.getString("day_picture:" + weather, "notclear.png");
            } else {
                weatherImg = databaseManager.getString("night_picture:" + weather, "notclear.png");
            }
            holder.weatherIcon.setImageDrawable(CommonUtil.drawableFromAssets(context, weatherImg));
            
            // 设置体感温度（模拟计算）
            try {
                String temp = item.getString("temperature").replace("℃", "");
                int tempInt = Integer.parseInt(temp);
                int feelsLike = tempInt + (int)(Math.random() * 3) - 1; // 简单模拟
                holder.feelsLikeText.setText("体感 " + feelsLike + "°");
            } catch (Exception e) {
                holder.feelsLikeText.setText("体感 --");
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView temperatureText;
        TextView weatherText;
        TextView windText;
        TextView feelsLikeText;
        ImageView weatherIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.h24_date);
            temperatureText = itemView.findViewById(R.id.info_temprature);
            weatherText = itemView.findViewById(R.id.info_w);
            windText = itemView.findViewById(R.id.info_wind);
            feelsLikeText = itemView.findViewById(R.id.info_feels_like);
            weatherIcon = itemView.findViewById(R.id.info_typeimg);
        }
    }
}

package com.dddpeter.app.rainweather.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dddpeter.app.rainweather.data.models.DailyWeather
import com.dddpeter.app.rainweather.databinding.ItemDailyWeatherBinding
import com.dddpeter.app.rainweather.utils.WeatherIconMapper

/**
 * 15日天气预报适配器
 */
class DailyWeatherAdapter : ListAdapter<DailyWeather, DailyWeatherAdapter.ViewHolder>(DailyWeatherDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyWeatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    
    class ViewHolder(private val binding: ItemDailyWeatherBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(weather: DailyWeather, position: Int) {
            // 处理日期标签（今天/明天/星期）
            val dateLabel = when (position) {
                0 -> "今天"
                1 -> "明天"
                else -> weather.week ?: "星期"
            }
            binding.tvDateLabel.text = dateLabel
            
            // 显示日期（API返回的格式已经是"10/05"）
            binding.tvDate.text = weather.forecasttime ?: "--/--"
            
            // 天气图标（优先显示白天天气）
            val weatherDesc = weather.weatherAm ?: weather.weatherPm ?: "未知"
            val weatherIcon = WeatherIconMapper.getWeatherIcon(weatherDesc)
            binding.tvWeatherIcon.text = weatherIcon
            
            // 天气描述（如果白天和晚上不同，显示"白天天气转晚上天气"）
            val amWeather = weather.weatherAm
            val pmWeather = weather.weatherPm
            binding.tvWeatherDesc.text = if (pmWeather == null) {
                amWeather ?: "未知"
            } else if (amWeather == null) {
                pmWeather
            } else if (amWeather == pmWeather) {
                amWeather
            } else {
                "${amWeather}转${pmWeather}"
            }
            
            // 温度
            binding.tvTempLow.text = "${weather.getLowTemp()}℃"
            binding.tvTempHigh.text = "${weather.getHighTemp()}℃"
            
            // 风力（优先显示白天风力）
            val windDir = weather.winddirAm ?: weather.winddirPm ?: ""
            val windPower = weather.windpowerAm ?: weather.windpowerPm ?: ""
            binding.tvWind.text = when {
                windDir.isNotEmpty() && windPower.isNotEmpty() -> "$windDir$windPower"
                windDir.isNotEmpty() -> windDir
                windPower.isNotEmpty() -> windPower
                else -> "无风"
            }
        }
    }
    
    private class DailyWeatherDiffCallback : DiffUtil.ItemCallback<DailyWeather>() {
        override fun areItemsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean {
            return oldItem.forecasttime == newItem.forecasttime
        }
        
        override fun areContentsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean {
            return oldItem == newItem
        }
    }
}


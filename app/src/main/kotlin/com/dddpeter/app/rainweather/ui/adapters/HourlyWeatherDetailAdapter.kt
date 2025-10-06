package com.dddpeter.app.rainweather.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dddpeter.app.rainweather.data.models.HourlyWeather
import com.dddpeter.app.rainweather.databinding.ItemHourlyWeatherDetailBinding
import com.dddpeter.app.rainweather.utils.WeatherIconMapper
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 24小时天气详情Adapter（用于列表）
 */
class HourlyWeatherDetailAdapter : ListAdapter<HourlyWeather, HourlyWeatherDetailAdapter.ViewHolder>(DiffCallback) {
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyWeatherDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemHourlyWeatherDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(hourlyWeather: HourlyWeather) {
            with(binding) {
                // 时间
                tvTime.text = hourlyWeather.getFormattedTime()
                
                // 天气图标
                tvWeatherIcon.text = WeatherIconMapper.getWeatherIcon(hourlyWeather.weather)
                
                // 天气描述
                tvWeatherDesc.text = hourlyWeather.weather ?: "未知"
                
                // 温度
                tvTemperature.text = WeatherIconMapper.formatTemperature(hourlyWeather.temperature)
                
                // 风力
                tvWind.text = "${hourlyWeather.windDir ?: ""}${hourlyWeather.windPower ?: ""}"
            }
        }
    }
    
    object DiffCallback : DiffUtil.ItemCallback<HourlyWeather>() {
        override fun areItemsTheSame(oldItem: HourlyWeather, newItem: HourlyWeather): Boolean {
            return oldItem.forecasttime == newItem.forecasttime
        }
        
        override fun areContentsTheSame(oldItem: HourlyWeather, newItem: HourlyWeather): Boolean {
            return oldItem == newItem
        }
    }
}


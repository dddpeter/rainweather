package com.dddpeter.app.rainweather.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dddpeter.app.rainweather.data.models.HourlyWeather
import com.dddpeter.app.rainweather.databinding.ItemHourlyWeatherBinding
import com.dddpeter.app.rainweather.utils.WeatherIconMapper

class HourlyWeatherAdapter : ListAdapter<HourlyWeather, HourlyWeatherAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyWeatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(private val binding: ItemHourlyWeatherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weather: HourlyWeather) {
            binding.apply {
                tvTime.text = weather.getFormattedTime()
                tvIcon.text = WeatherIconMapper.getWeatherIcon(weather.weather)
                tvTemp.text = WeatherIconMapper.formatTemperature(weather.temperature)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<HourlyWeather>() {
        override fun areItemsTheSame(oldItem: HourlyWeather, newItem: HourlyWeather) =
            oldItem.forecasttime == newItem.forecasttime
        
        override fun areContentsTheSame(oldItem: HourlyWeather, newItem: HourlyWeather) =
            oldItem == newItem
    }
}


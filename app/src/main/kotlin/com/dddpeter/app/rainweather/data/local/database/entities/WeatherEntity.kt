package com.dddpeter.app.rainweather.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dddpeter.app.rainweather.data.models.AppConstants
import com.dddpeter.app.rainweather.data.models.WeatherModel
import com.google.gson.Gson

/**
 * 天气缓存实体
 */
@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey
    val cityId: String,
    val weatherDataJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (AppConstants.CACHE_EXPIRY_MINUTES * 60 * 1000)
) {
    /**
     * 判断缓存是否过期
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }

    /**
     * 转换为WeatherModel
     */
    fun toWeatherModel(): WeatherModel {
        return Gson().fromJson(weatherDataJson, WeatherModel::class.java)
    }

    companion object {
        /**
         * 从WeatherModel创建实体
         */
        fun from(cityId: String, weatherModel: WeatherModel): WeatherEntity {
            return WeatherEntity(
                cityId = cityId,
                weatherDataJson = Gson().toJson(weatherModel)
            )
        }
    }
}


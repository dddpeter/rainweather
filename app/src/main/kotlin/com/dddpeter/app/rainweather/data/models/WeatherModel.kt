package com.dddpeter.app.rainweather.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 天气数据模型（对应Flutter版本的WeatherModel）
 */
@Parcelize
data class WeatherModel(
    val current: CurrentWeatherData? = null,
    @SerializedName("forecast24h")
    val forecast24h: List<HourlyWeather>? = null,
    @SerializedName("forecast15d")
    val forecast15d: List<DailyWeather>? = null,
    val air: AirQuality? = null,
    val tips: String? = null
) : Parcelable

/**
 * 当前天气数据
 */
@Parcelize
data class CurrentWeatherData(
    val alerts: List<WeatherAlert>? = null,
    val current: CurrentWeather? = null,
    val nongLi: String? = null,
    val air: AirQuality? = null,
    val tips: String? = null
) : Parcelable

/**
 * 当前天气
 */
@Parcelize
data class CurrentWeather(
    val airpressure: String? = null,
    val weatherPic: String? = null,
    val visibility: String? = null,
    val windpower: String? = null,
    val feelstemperature: String? = null,
    val temperature: String? = null,
    val weather: String? = null,
    val humidity: String? = null,
    val weatherIndex: String? = null,
    val winddir: String? = null,
    val reporttime: String? = null
) : Parcelable

/**
 * 小时天气
 */
@Parcelize
data class HourlyWeather(
    val windDirectionDegree: String? = null,
    val weatherPic: String? = null,
    val forecasttime: String? = null,
    val windPower: String? = null,
    val weatherCode: String? = null,
    val temperature: String? = null,
    val weather: String? = null,
    val windDir: String? = null
) : Parcelable {
    /**
     * 获取格式化的时间（HH:mm）
     */
    fun getFormattedTime(): String {
        return try {
            // API返回的forecasttime格式是"10:00"或"2025-10-06 10:00"
            val time = forecasttime ?: return "--:--"
            if (time.length == 5 && time.contains(":")) {
                // 已经是"HH:mm"格式
                time
            } else if (time.length > 10) {
                // 完整日期时间格式，提取时间部分
                time.substring(11, 16)
            } else {
                "--:--"
            }
        } catch (e: Exception) {
            "--:--"
        }
    }
    
    
    /**
     * 获取温度（整数）
     */
    fun getTemperature(): Int {
        return try {
            // API返回的temperature格式是"15℃"，需要去除℃符号
            temperature?.replace("℃", "")?.trim()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取降水概率（API未提供，返回0）
     */
    fun getPrecipitation(): Int {
        return 0
    }
}

/**
 * 日天气
 */
@Parcelize
data class DailyWeather(
    @SerializedName("temperature_am")
    val temperatureAm: String? = null,
    @SerializedName("weather_pm_pic")
    val weatherPmPic: String? = null,
    @SerializedName("winddir_am")
    val winddirAm: String? = null,
    val week: String? = null,
    val forecasttime: String? = null,
    @SerializedName("windpower_pm")
    val windpowerPm: String? = null,
    @SerializedName("weather_pm")
    val weatherPm: String? = null,
    val reporttime: String? = null,
    @SerializedName("weather_index_pm")
    val weatherIndexPm: String? = null,
    @SerializedName("winddir_pm")
    val winddirPm: String? = null,
    @SerializedName("weather_am")
    val weatherAm: String? = null,
    @SerializedName("sunrise_sunset")
    val sunriseSunset: String? = null,
    @SerializedName("windpower_am")
    val windpowerAm: String? = null,
    @SerializedName("weather_am_pic")
    val weatherAmPic: String? = null,
    @SerializedName("temperature_pm")
    val temperaturePm: String? = null,
    @SerializedName("weather_index_am")
    val weatherIndexAm: String? = null
) : Parcelable {
    /**
     * 获取最高温度
     */
    fun getHighTemp(): Int {
        return try {
            val amTemp = temperatureAm?.toIntOrNull() ?: 0
            val pmTemp = temperaturePm?.toIntOrNull() ?: 0
            maxOf(amTemp, pmTemp)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 获取最低温度
     */
    fun getLowTemp(): Int {
        return try {
            val amTemp = temperatureAm?.toIntOrNull() ?: 0
            val pmTemp = temperaturePm?.toIntOrNull() ?: 0
            minOf(amTemp, pmTemp)
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * 空气质量
 */
@Parcelize
data class AirQuality(
    val levelIndex: String? = null,
    @SerializedName("AQI")
    val aqi: String? = null
) : Parcelable {
    /**
     * 获取AQI数值
     */
    fun getAqiValue(): Int? {
        return aqi?.toIntOrNull()
    }

    /**
     * 获取AQI等级描述
     */
    fun getAqiLevel(): String {
        val value = getAqiValue() ?: return "未知"
        return when {
            value <= 50 -> "优"
            value <= 100 -> "良"
            value <= 150 -> "轻度污染"
            value <= 200 -> "中度污染"
            value <= 300 -> "重度污染"
            else -> "严重污染"
        }
    }
}

/**
 * 天气预警
 */
@Parcelize
data class WeatherAlert(
    val publishTime: String? = null,
    val city: String? = null,
    val level: String? = null,
    val typeNumber: String? = null,
    val alertPic: String? = null,
    val provice: String? = null,
    val levelNumber: String? = null,
    val alertid: String? = null,
    val type: String? = null,
    val content: String? = null
) : Parcelable

/**
 * 天气API响应
 */
data class WeatherResponse(
    val code: Int? = null,
    val msg: String? = null,
    val data: WeatherModel? = null
)


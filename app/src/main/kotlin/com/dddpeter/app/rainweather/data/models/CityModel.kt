package com.dddpeter.app.rainweather.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 城市数据模型（对应Flutter版本的CityModel）
 */
@Parcelize
data class CityModel(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
    val isCurrentLocation: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    /**
     * 判断是否是虚拟当前位置城市
     */
    fun isVirtualCurrentLocation(): Boolean {
        return id == VIRTUAL_CURRENT_LOCATION_ID
    }

    companion object {
        const val VIRTUAL_CURRENT_LOCATION_ID = "virtual_current_location"

        /**
         * 创建虚拟当前位置城市
         */
        fun createVirtualCurrentLocation(cityName: String): CityModel {
            return CityModel(
                id = VIRTUAL_CURRENT_LOCATION_ID,
                name = cityName,
                sortOrder = 0,
                isCurrentLocation = true
            )
        }

        /**
         * 从城市ID和名称创建
         */
        fun from(id: String, name: String, sortOrder: Int = 0): CityModel {
            return CityModel(
                id = id,
                name = name,
                sortOrder = sortOrder
            )
        }
    }
}

/**
 * 城市信息（用于搜索）
 */
@Parcelize
data class CityInfo(
    val id: String,
    val name: String,
    val province: String? = null,
    val pinyin: String? = null
) : Parcelable {
    /**
     * 获取显示名称
     */
    fun getDisplayName(): String {
        return if (province.isNullOrEmpty() || province == name) {
            name
        } else {
            "$name ($province)"
        }
    }

    /**
     * 转换为CityModel
     */
    fun toCityModel(sortOrder: Int = 0): CityModel {
        return CityModel(
            id = id,
            name = name,
            sortOrder = sortOrder
        )
    }
}

/**
 * 带天气信息的城市模型（用于主要城市列表显示）
 */
@Parcelize
data class CityWithWeather(
    val city: CityModel,
    val temperature: String? = null,
    val weather: String? = null,
    val weatherIcon: String? = null
) : Parcelable {
    /**
     * 获取温度显示文本
     */
    fun getTemperatureText(): String {
        return temperature ?: "--℃"
    }
    
    /**
     * 获取天气描述
     */
    fun getWeatherText(): String {
        return weather ?: "未知"
    }
    
    /**
     * 获取天气图标
     */
    fun getWeatherIconText(): String {
        return weatherIcon ?: "☀️"
    }
}


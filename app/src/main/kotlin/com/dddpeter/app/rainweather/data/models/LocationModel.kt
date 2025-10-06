package com.dddpeter.app.rainweather.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 位置数据模型（对应Flutter版本的LocationModel）
 */
@Parcelize
data class LocationModel(
    val lat: Double,
    val lng: Double,
    val address: String = "",
    val country: String = "",
    val province: String = "",
    val city: String = "",
    val district: String = "",
    val street: String = "",
    val adcode: String = "",
    val town: String = "",
    val isProxyDetected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    /**
     * 获取完整地址描述
     */
    fun getFullAddress(): String {
        return buildString {
            if (province.isNotEmpty()) append(province)
            if (city.isNotEmpty() && city != province) {
                if (isNotEmpty()) append(" ")
                append(city)
            }
            if (district.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(district)
            }
            if (street.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(street)
            }
        }.ifEmpty { address }
    }

    /**
     * 获取简短地址（区县级别）
     */
    fun getShortAddress(): String {
        return when {
            district.isNotEmpty() -> district
            city.isNotEmpty() -> city
            province.isNotEmpty() -> province
            else -> "未知位置"
        }
    }

    /**
     * 获取城市名称（用于查询天气）
     */
    fun getCityName(): String {
        return when {
            district.isNotEmpty() -> district
            city.isNotEmpty() -> city
            province.isNotEmpty() -> province
            else -> "北京"
        }
    }

    /**
     * 判断是否是有效位置
     */
    fun isValid(): Boolean {
        return lat != 0.0 && lng != 0.0
    }

    companion object {
        /**
         * 创建默认位置（北京）
         */
        fun createDefault(): LocationModel {
            return LocationModel(
                lat = 39.9042,
                lng = 116.4074,
                address = "北京市东城区",
                country = "中国",
                province = "北京市",
                city = "北京市",
                district = "东城区",
                street = "天安门广场",
                adcode = "110101",
                town = "",
                isProxyDetected = false
            )
        }

        /**
         * 从经纬度创建位置
         */
        fun fromLatLng(lat: Double, lng: Double): LocationModel {
            return LocationModel(
                lat = lat,
                lng = lng
            )
        }
    }
}

/**
 * 定位结果封装类
 */
sealed class LocationResult {
    data class Success(val location: LocationModel) : LocationResult()
    data class Error(val message: String, val exception: Exception? = null) : LocationResult()
    object Loading : LocationResult()
    object PermissionDenied : LocationResult()
}


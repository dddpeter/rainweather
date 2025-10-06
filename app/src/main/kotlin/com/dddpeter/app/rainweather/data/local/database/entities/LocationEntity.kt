package com.dddpeter.app.rainweather.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dddpeter.app.rainweather.data.models.LocationModel

/**
 * 位置缓存实体
 */
@Entity(tableName = "location_cache")
data class LocationEntity(
    @PrimaryKey
    val key: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val country: String,
    val province: String,
    val city: String,
    val district: String,
    val street: String,
    val adcode: String,
    val town: String,
    val isProxyDetected: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 转换为LocationModel
     */
    fun toLocationModel(): LocationModel {
        return LocationModel(
            lat = latitude,
            lng = longitude,
            address = address,
            country = country,
            province = province,
            city = city,
            district = district,
            street = street,
            adcode = adcode,
            town = town,
            isProxyDetected = isProxyDetected,
            timestamp = timestamp
        )
    }

    companion object {
        /**
         * 从LocationModel创建实体
         */
        fun from(key: String, location: LocationModel): LocationEntity {
            return LocationEntity(
                key = key,
                latitude = location.lat,
                longitude = location.lng,
                address = location.address,
                country = location.country,
                province = location.province,
                city = location.city,
                district = location.district,
                street = location.street,
                adcode = location.adcode,
                town = location.town,
                isProxyDetected = location.isProxyDetected,
                timestamp = location.timestamp
            )
        }
    }
}


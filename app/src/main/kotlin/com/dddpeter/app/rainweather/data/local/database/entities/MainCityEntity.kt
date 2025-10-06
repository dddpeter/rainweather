package com.dddpeter.app.rainweather.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dddpeter.app.rainweather.data.models.CityModel

/**
 * 主要城市实体
 */
@Entity(tableName = "main_cities")
data class MainCityEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sortOrder: Int,
    val isCurrentLocation: Boolean,
    val addedAt: Long = System.currentTimeMillis()
) {
    /**
     * 转换为CityModel
     */
    fun toCityModel(): CityModel {
        return CityModel(
            id = id,
            name = name,
            sortOrder = sortOrder,
            isCurrentLocation = isCurrentLocation,
            addedAt = addedAt
        )
    }

    companion object {
        /**
         * 从CityModel创建实体
         */
        fun from(city: CityModel): MainCityEntity {
            return MainCityEntity(
                id = city.id,
                name = city.name,
                sortOrder = city.sortOrder,
                isCurrentLocation = city.isCurrentLocation,
                addedAt = city.addedAt
            )
        }
    }
}


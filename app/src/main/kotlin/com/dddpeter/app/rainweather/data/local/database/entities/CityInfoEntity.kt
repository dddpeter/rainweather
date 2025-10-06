package com.dddpeter.app.rainweather.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dddpeter.app.rainweather.data.models.CityInfo

/**
 * 城市信息实体（用于搜索）
 */
@Entity(tableName = "city_info")
data class CityInfoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val province: String?,
    val pinyin: String?
) {
    /**
     * 转换为CityInfo
     */
    fun toCityInfo(): CityInfo {
        return CityInfo(
            id = id,
            name = name,
            province = province,
            pinyin = pinyin
        )
    }

    companion object {
        /**
         * 从CityInfo创建实体
         */
        fun from(cityInfo: CityInfo): CityInfoEntity {
            return CityInfoEntity(
                id = cityInfo.id,
                name = cityInfo.name,
                province = cityInfo.province,
                pinyin = cityInfo.pinyin
            )
        }
    }
}


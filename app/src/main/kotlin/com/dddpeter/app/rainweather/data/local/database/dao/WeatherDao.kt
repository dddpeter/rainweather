package com.dddpeter.app.rainweather.data.local.database.dao

import androidx.room.*
import com.dddpeter.app.rainweather.data.local.database.entities.WeatherEntity
import com.dddpeter.app.rainweather.data.models.WeatherModel

/**
 * 天气数据DAO
 */
@Dao
interface WeatherDao {
    
    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    suspend fun getWeatherData(cityId: String): WeatherEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherEntity(entity: WeatherEntity)
    
    /**
     * 插入天气数据
     */
    suspend fun insertWeatherData(cityId: String, weatherData: WeatherModel) {
        val entity = WeatherEntity.from(cityId, weatherData)
        insertWeatherEntity(entity)
    }
    
    @Query("DELETE FROM weather_cache WHERE cityId = :cityId")
    suspend fun deleteWeatherData(cityId: String)
    
    @Query("DELETE FROM weather_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredWeatherData(currentTime: Long = System.currentTimeMillis()): Int
    
    @Query("DELETE FROM weather_cache")
    suspend fun deleteAllWeatherData()
    
    @Query("SELECT COUNT(*) FROM weather_cache")
    suspend fun getWeatherCount(): Int
}


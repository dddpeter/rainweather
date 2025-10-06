package com.dddpeter.app.rainweather.data.local.database.dao

import androidx.room.*
import com.dddpeter.app.rainweather.data.local.database.entities.LocationEntity
import com.dddpeter.app.rainweather.data.models.LocationModel

/**
 * 位置数据DAO
 */
@Dao
interface LocationDao {
    
    @Query("SELECT * FROM location_cache WHERE key = :key")
    suspend fun getLocationData(key: String): LocationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationEntity(entity: LocationEntity)
    
    /**
     * 插入位置数据
     */
    suspend fun insertLocationData(key: String, location: LocationModel) {
        val entity = LocationEntity.from(key, location)
        insertLocationEntity(entity)
    }
    
    @Query("DELETE FROM location_cache WHERE key = :key")
    suspend fun deleteLocationData(key: String)
    
    @Query("DELETE FROM location_cache")
    suspend fun deleteAllLocationData()
    
    @Query("SELECT COUNT(*) FROM location_cache")
    suspend fun getLocationCount(): Int
}


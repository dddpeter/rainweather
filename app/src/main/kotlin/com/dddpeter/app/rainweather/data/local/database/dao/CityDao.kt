package com.dddpeter.app.rainweather.data.local.database.dao

import androidx.room.*
import com.dddpeter.app.rainweather.data.local.database.entities.CityInfoEntity
import com.dddpeter.app.rainweather.data.models.CityInfo

/**
 * 城市信息DAO
 */
@Dao
interface CityDao {
    
    @Query("SELECT * FROM city_info WHERE name LIKE :keyword LIMIT 50")
    suspend fun searchCities(keyword: String): List<CityInfoEntity>
    
    @Query("SELECT * FROM city_info WHERE name LIKE :keyword LIMIT 50")
    suspend fun searchCitiesByName(keyword: String): List<CityInfo> {
        return searchCities(keyword).map { it.toCityInfo() }
    }
    
    @Query("SELECT id FROM city_info WHERE name = :cityName LIMIT 1")
    suspend fun findCityIdByName(cityName: String): String?
    
    @Query("SELECT * FROM city_info WHERE id = :cityId")
    suspend fun getCityById(cityId: String): CityInfo? {
        return getCityByIdInternal(cityId)?.toCityInfo()
    }
    
    @Query("SELECT * FROM city_info WHERE id = :cityId")
    suspend fun getCityByIdInternal(cityId: String): CityInfoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(entity: CityInfoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CityInfoEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(entities: List<CityInfoEntity>)
    
    @Query("DELETE FROM city_info")
    suspend fun deleteAll()
    
    @Query("DELETE FROM city_info")
    suspend fun deleteAllCities()
    
    @Query("SELECT COUNT(*) FROM city_info")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM city_info")
    suspend fun getCityCount(): Int
}


package com.dddpeter.app.rainweather.data.local.database.dao

import androidx.room.*
import com.dddpeter.app.rainweather.data.local.database.entities.MainCityEntity
import com.dddpeter.app.rainweather.data.models.CityModel

/**
 * 主要城市DAO
 */
@Dao
interface MainCityDao {
    
    @Query("SELECT * FROM main_cities ORDER BY sortOrder ASC")
    suspend fun getAllMainCities(): List<CityModel> {
        return getAllMainCitiesInternal().map { it.toCityModel() }
    }
    
    @Query("SELECT * FROM main_cities ORDER BY sortOrder ASC")
    suspend fun getAllMainCitiesInternal(): List<MainCityEntity>
    
    @Query("SELECT * FROM main_cities WHERE id = :cityId")
    suspend fun getMainCityById(cityId: String): CityModel? {
        return getMainCityByIdInternal(cityId)?.toCityModel()
    }
    
    @Query("SELECT * FROM main_cities WHERE id = :cityId")
    suspend fun getMainCityByIdInternal(cityId: String): MainCityEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMainCityEntity(entity: MainCityEntity)
    
    /**
     * 插入主要城市
     */
    suspend fun insertMainCity(city: CityModel) {
        val entity = MainCityEntity.from(city)
        insertMainCityEntity(entity)
    }
    
    @Query("DELETE FROM main_cities WHERE id = :cityId")
    suspend fun deleteMainCity(cityId: String)
    
    @Query("DELETE FROM main_cities")
    suspend fun deleteAllMainCities()
    
    @Query("UPDATE main_cities SET sortOrder = :sortOrder WHERE id = :cityId")
    suspend fun updateSortOrder(cityId: String, sortOrder: Int)
    
    @Query("SELECT MAX(sortOrder) FROM main_cities")
    suspend fun getMaxSortOrder(): Int?
    
    @Query("SELECT COUNT(*) FROM main_cities")
    suspend fun getMainCityCount(): Int
}


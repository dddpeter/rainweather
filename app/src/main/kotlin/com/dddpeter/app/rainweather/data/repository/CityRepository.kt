package com.dddpeter.app.rainweather.data.repository

import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.data.models.CityInfo
import com.dddpeter.app.rainweather.data.models.CityModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 城市数据仓库（对应Flutter版本的CityService）
 */
class CityRepository(
    private val database: AppDatabase
) {
    private val cityDao = database.cityDao()
    private val mainCityDao = database.mainCityDao()
    
    /**
     * 搜索城市
     */
    suspend fun searchCities(keyword: String): List<CityInfo> {
        return withContext(Dispatchers.IO) {
            try {
                if (keyword.isEmpty()) {
                    // 返回默认城市列表（省会和直辖市）
                    getDefaultCities()
                } else {
                    cityDao.searchCitiesByName("%$keyword%")
                }
            } catch (e: Exception) {
                Timber.e(e, "搜索城市失败")
                emptyList()
            }
        }
    }
    
    /**
     * 根据城市名称查找城市ID
     */
    suspend fun findCityIdByName(cityName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                cityDao.findCityIdByName(cityName)
            } catch (e: Exception) {
                Timber.e(e, "查找城市ID失败")
                null
            }
        }
    }
    
    /**
     * 获取主要城市列表
     */
    suspend fun getMainCities(): List<CityModel> {
        return withContext(Dispatchers.IO) {
            try {
                mainCityDao.getAllMainCities()
            } catch (e: Exception) {
                Timber.e(e, "获取主要城市失败")
                emptyList()
            }
        }
    }
    
    /**
     * 添加主要城市
     */
    suspend fun addMainCity(city: CityModel): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 检查是否已存在
                val existing = mainCityDao.getMainCityById(city.id)
                if (existing != null) {
                    Timber.w("城市已存在: ${city.name}")
                    return@withContext false
                }
                
                // 获取最大sortOrder
                val maxSortOrder = mainCityDao.getMaxSortOrder() ?: 0
                val newCity = city.copy(sortOrder = maxSortOrder + 1)
                
                mainCityDao.insertMainCity(newCity)
                Timber.d("添加主要城市: ${city.name}")
                true
            } catch (e: Exception) {
                Timber.e(e, "添加主要城市失败")
                false
            }
        }
    }
    
    /**
     * 删除主要城市
     */
    suspend fun removeMainCity(cityId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                mainCityDao.deleteMainCity(cityId)
                Timber.d("删除主要城市: $cityId")
                true
            } catch (e: Exception) {
                Timber.e(e, "删除主要城市失败")
                false
            }
        }
    }
    
    /**
     * 更新城市排序
     */
    suspend fun updateCitiesSortOrder(cities: List<CityModel>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                cities.forEachIndexed { index, city ->
                    mainCityDao.updateSortOrder(city.id, index)
                }
                Timber.d("更新城市排序")
                true
            } catch (e: Exception) {
                Timber.e(e, "更新城市排序失败")
                false
            }
        }
    }
    
    /**
     * 获取默认城市列表（省会和直辖市）
     */
    private suspend fun getDefaultCities(): List<CityInfo> {
        val defaultCityNames = listOf(
            "北京", "上海", "天津", "重庆", // 直辖市
            "哈尔滨", "长春", "沈阳", "呼和浩特", "石家庄", "太原", "西安", // 北方省会
            "济南", "郑州", "南京", "武汉", "杭州", "合肥", "福州", "南昌", // 中部省会
            "长沙", "贵阳", "成都", "广州", "昆明", "南宁", "海口", // 南方省会
            "兰州", "西宁", "银川", "乌鲁木齐", "拉萨" // 西部省会
        )
        
        return defaultCityNames.mapNotNull { name ->
            try {
                val cityId = cityDao.findCityIdByName(name)
                if (cityId != null) {
                    CityInfo(id = cityId, name = name)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    companion object {
        @Volatile
        private var instance: CityRepository? = null
        
        fun getInstance(database: AppDatabase): CityRepository {
            return instance ?: synchronized(this) {
                instance ?: CityRepository(database).also { instance = it }
            }
        }
    }
}


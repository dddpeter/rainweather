package com.dddpeter.app.rainweather.data.repository

import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.data.models.LocationModel
import com.dddpeter.app.rainweather.data.models.SunMoonIndexData
import com.dddpeter.app.rainweather.data.models.WeatherModel
import com.dddpeter.app.rainweather.data.remote.RetrofitClient
import timber.log.Timber

/**
 * 天气数据仓库（对应Flutter版本的WeatherProvider部分逻辑）
 */
class WeatherRepository(
    private val database: AppDatabase
) {
    private val weatherApiService = RetrofitClient.getWeatherApiService()
    private val weatherDao = database.weatherDao()
    private val cityDao = database.cityDao()
    
    /**
     * 获取天气数据（优先缓存，缓存过期则从网络获取）
     */
    suspend fun getWeatherData(
        cityId: String,
        forceRefresh: Boolean = false
    ): Result<WeatherModel> {
        return try {
            // 如果不强制刷新，先检查缓存
            if (!forceRefresh) {
                val cached = weatherDao.getWeatherData(cityId)
                if (cached != null && !cached.isExpired()) {
                    Timber.d("使用缓存的天气数据: $cityId")
                    return Result.success(cached.toWeatherModel())
                }
            }
            
            // 从网络获取
            Timber.d("从网络获取天气数据: $cityId")
            val response = weatherApiService.getWeatherData(cityId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                val weatherModel = response.body()!!.data!!
                
                
                // 保存到缓存
                weatherDao.insertWeatherData(
                    cityId = cityId,
                    weatherData = weatherModel
                )
                
                Result.success(weatherModel)
            } else {
                Timber.e("获取天气数据失败: ${response.code()} ${response.message()}")
                Result.failure(Exception("获取天气数据失败: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "获取天气数据异常")
            Result.failure(e)
        }
    }
    
    /**
     * 获取位置的天气数据
     */
    suspend fun getWeatherDataForLocation(
        location: LocationModel,
        forceRefresh: Boolean = false
    ): Result<WeatherModel> {
        val cityId = getCityIdFromLocation(location)
        Timber.d("位置: ${location.district}, 城市ID: $cityId")
        
        if (cityId.isEmpty()) {
            return Result.failure(Exception("无法找到城市ID"))
        }
        
        return getWeatherData(cityId, forceRefresh)
    }
    
    /**
     * 获取日出日落和生活指数数据
     */
    suspend fun getSunMoonIndexData(cityId: String): Result<SunMoonIndexData> {
        return try {
            val response = weatherApiService.getSunMoonIndexData(cityId)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("获取日出日落数据失败: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "获取日出日落数据异常")
            Result.failure(e)
        }
    }
    
    /**
     * 从位置获取城市ID
     */
    private suspend fun getCityIdFromLocation(location: LocationModel): String {
        // 优先按区县查找
        var cityId = cityDao.findCityIdByName(location.district)
        
        // 如果没找到，按城市查找
        if (cityId == null && location.city.isNotEmpty()) {
            cityId = cityDao.findCityIdByName(location.city)
        }
        
        // 如果还没找到，按省份查找
        if (cityId == null && location.province.isNotEmpty()) {
            cityId = cityDao.findCityIdByName(location.province)
        }
        
        // 返回找到的城市ID，如果没找到则返回默认值（北京）
        return cityId ?: "101010100"
    }
    
    /**
     * 清理过期缓存
     */
    suspend fun cleanExpiredCache() {
        try {
            val deletedCount = weatherDao.deleteExpiredWeatherData()
            Timber.d("清理过期缓存: $deletedCount 条")
        } catch (e: Exception) {
            Timber.e(e, "清理缓存失败")
        }
    }
    
    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache() {
        try {
            weatherDao.deleteAllWeatherData()
            Timber.d("清除所有缓存")
        } catch (e: Exception) {
            Timber.e(e, "清除缓存失败")
        }
    }
    
    companion object {
        @Volatile
        private var instance: WeatherRepository? = null
        
        fun getInstance(database: AppDatabase): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository(database).also { instance = it }
            }
        }
    }
}


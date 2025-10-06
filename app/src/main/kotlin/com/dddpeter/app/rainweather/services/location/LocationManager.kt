package com.dddpeter.app.rainweather.services.location

import android.content.Context
import com.dddpeter.app.rainweather.data.models.AppConstants
import com.dddpeter.app.rainweather.data.models.LocationModel
import com.dddpeter.app.rainweather.data.models.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * 定位管理器（对应Flutter版本的LocationService）
 * 实现三层降级策略：百度定位 > GPS定位 > IP定位
 */
class LocationManager private constructor(private val context: Context) {
    
    private val baiduLocationService = BaiduLocationService.getInstance(context)
    private val gpsLocationService = GpsLocationService.getInstance(context)
    private val ipLocationService = IpLocationService.getInstance()
    
    private val _locationState = MutableStateFlow<LocationResult>(LocationResult.Loading)
    val locationState: StateFlow<LocationResult> = _locationState.asStateFlow()
    
    private var cachedLocation: LocationModel? = null
    
    /**
     * 获取当前位置（三层降级策略）
     */
    suspend fun getCurrentLocation(forceRefresh: Boolean = false): LocationModel? {
        // 如果不强制刷新，先返回缓存
        if (!forceRefresh && cachedLocation != null) {
            Timber.d("📍 使用缓存位置: ${cachedLocation?.district}")
            return cachedLocation
        }
        
        _locationState.value = LocationResult.Loading
        
        try {
            // 1. 尝试百度定位（高精度，8秒超时）
            Timber.d("📍 第一层：尝试百度定位...")
            val baiduLocation = baiduLocationService.getCurrentLocation(
                AppConstants.BAIDU_LOCATION_TIMEOUT
            )
            
            if (baiduLocation != null && baiduLocation.isValid()) {
                Timber.d("✅ 百度定位成功: ${baiduLocation.district}")
                cachedLocation = baiduLocation
                _locationState.value = LocationResult.Success(baiduLocation)
                return baiduLocation
            }
            
            Timber.w("⚠️ 百度定位失败，切换到GPS定位")
            
            // 2. 尝试GPS定位（10秒超时）
            Timber.d("📍 第二层：尝试GPS定位...")
            val gpsLocation = gpsLocationService.getCurrentLocation(
                AppConstants.GPS_LOCATION_TIMEOUT
            )
            
            if (gpsLocation != null && gpsLocation.isValid()) {
                Timber.d("✅ GPS定位成功: ${gpsLocation.district}")
                cachedLocation = gpsLocation
                _locationState.value = LocationResult.Success(gpsLocation)
                return gpsLocation
            }
            
            Timber.w("⚠️ GPS定位失败，切换到IP定位")
            
            // 3. 最后降级到IP定位（5秒超时）
            Timber.d("📍 第三层：尝试IP定位...")
            val ipLocation = ipLocationService.getCurrentLocation(
                AppConstants.IP_LOCATION_TIMEOUT
            )
            
            if (ipLocation != null && ipLocation.isValid()) {
                Timber.d("✅ IP定位成功: ${ipLocation.city}")
                cachedLocation = ipLocation
                _locationState.value = LocationResult.Success(ipLocation)
                return ipLocation
            }
            
            // 所有定位方式都失败，返回默认位置（北京）
            Timber.e("❌ 所有定位方式都失败，使用默认位置（北京）")
            val defaultLocation = LocationModel.createDefault()
            cachedLocation = defaultLocation
            _locationState.value = LocationResult.Success(defaultLocation)
            return defaultLocation
            
        } catch (e: Exception) {
            Timber.e(e, "❌ 定位过程异常")
            _locationState.value = LocationResult.Error("定位失败: ${e.message}", e)
            
            // 异常情况也返回默认位置
            val defaultLocation = LocationModel.createDefault()
            cachedLocation = defaultLocation
            return defaultLocation
        }
    }
    
    /**
     * 获取缓存的位置
     */
    fun getCachedLocation(): LocationModel? {
        return cachedLocation
    }
    
    /**
     * 设置缓存的位置
     */
    fun setCachedLocation(location: LocationModel) {
        cachedLocation = location
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        cachedLocation = null
    }
    
    /**
     * 初始化定位服务
     */
    fun initialize() {
        try {
            baiduLocationService.initialize()
            Timber.d("✅ 定位管理器初始化完成")
        } catch (e: Exception) {
            Timber.e(e, "❌ 定位管理器初始化失败")
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        baiduLocationService.release()
        clearCache()
        Timber.d("🗑️ 定位管理器资源已释放")
    }
    
    companion object {
        @Volatile
        private var instance: LocationManager? = null
        
        fun getInstance(context: Context): LocationManager {
            return instance ?: synchronized(this) {
                instance ?: LocationManager(context.applicationContext).also { 
                    instance = it
                }
            }
        }
    }
}


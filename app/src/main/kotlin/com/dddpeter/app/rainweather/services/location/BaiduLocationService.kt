package com.dddpeter.app.rainweather.services.location

import android.content.Context
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.dddpeter.app.rainweather.data.models.LocationModel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * 百度定位服务（对应Flutter版本的BaiduLocationService）
 * 提供高精度定位功能
 */
class BaiduLocationService private constructor(private val context: Context) {
    
    private var locationClient: LocationClient? = null
    private var isInitialized = false
    
    /**
     * 初始化百度定位服务
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            Timber.d("🔧 初始化百度定位服务")
            
            // 创建LocationClient
            locationClient = LocationClient(context.applicationContext)
            
            // 配置定位参数
            val option = LocationClientOption().apply {
                // 定位模式：高精度（GPS + 网络定位）
                locationMode = LocationClientOption.LocationMode.Hight_Accuracy
                
                // 坐标类型：BD09LL（百度经纬度坐标）
                setCoorType("bd09ll")
                
                // 设置发起定位请求的间隔（0表示单次定位）
                scanSpan = 0
                
                // 是否需要地址信息
                setIsNeedAddress(true)
                
                // 是否需要位置描述信息
                setIsNeedLocationDescribe(true)
                
                // 是否需要POI信息
                setIsNeedLocationPoiList(true)
                
                // 是否使用GPS
                isOpenGps = true
                
                // 是否需要设备方向信息
                setIsNeedAltitude(false)
            }
            
            locationClient?.locOption = option
            
            isInitialized = true
            Timber.d("✅ 百度定位服务初始化成功")
        } catch (e: Exception) {
            Timber.e(e, "❌ 百度定位服务初始化失败")
            throw LocationException("百度定位服务初始化失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取当前位置（单次定位，带超时）
     */
    suspend fun getCurrentLocation(timeoutMillis: Long = 8000): LocationModel? {
        return withTimeoutOrNull(timeoutMillis) {
            getCurrentLocationInternal()
        }
    }
    
    /**
     * 获取当前位置（内部实现）
     */
    private suspend fun getCurrentLocationInternal(): LocationModel? = suspendCancellableCoroutine { continuation ->
        try {
            if (!isInitialized) {
                initialize()
            }
            
            if (locationClient == null) {
                Timber.e("❌ LocationClient未初始化")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            Timber.d("🚀 开始百度定位...")
            
            // 创建定位监听器
            val listener = object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation?) {
                    Timber.d("📍 收到百度定位回调")
                    
                    if (location == null) {
                        Timber.e("❌ 定位结果为空")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                        return
                    }
                    
                    // 检查定位类型和错误码
                    val locType = location.locType
                    Timber.d("📍 定位类型: $locType")
                    
                    // 定位成功的类型码
                    // 61: GPS定位成功
                    // 161: 网络定位成功
                    // 66: 离线定位成功
                    if (locType == 61 || locType == 161 || locType == 66) {
                        val locationModel = parseLocation(location)
                        if (continuation.isActive) {
                            continuation.resume(locationModel)
                        }
                    } else {
                        Timber.e("❌ 定位失败，错误码: $locType, 错误信息: ${location.locTypeDescription}")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                    
                    // 停止定位
                    locationClient?.stop()
                }
            }
            
            // 注册监听器
            locationClient?.registerLocationListener(listener)
            
            // 启动定位
            locationClient?.start()
            
            // 取消时停止定位
            continuation.invokeOnCancellation {
                Timber.d("⏰ 定位被取消，停止定位")
                locationClient?.unRegisterLocationListener(listener)
                locationClient?.stop()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ 百度定位异常")
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
    
    /**
     * 解析百度定位结果
     */
    private fun parseLocation(location: BDLocation): LocationModel? {
        return try {
            val lat = location.latitude
            val lng = location.longitude
            
            if (lat == 0.0 || lng == 0.0 || lat == 4.9E-324 || lng == 4.9E-324) {
                Timber.e("❌ 定位坐标无效: ($lat, $lng)")
                return null
            }
            
            Timber.d("✅ 百度定位成功: ($lat, $lng)")
            
            LocationModel(
                lat = lat,
                lng = lng,
                address = location.addrStr ?: "",
                country = location.country ?: "",
                province = location.province ?: "",
                city = location.city ?: "",
                district = location.district ?: "",
                street = location.street ?: "",
                adcode = location.adCode ?: "",
                town = location.town ?: "",
                isProxyDetected = false
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ 解析百度定位结果失败")
            null
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        try {
            locationClient?.stop()
            locationClient = null
            isInitialized = false
            Timber.d("🗑️ 百度定位服务资源已释放")
        } catch (e: Exception) {
            Timber.e(e, "❌ 释放百度定位服务资源失败")
        }
    }
    
    companion object {
        @Volatile
        private var instance: BaiduLocationService? = null
        
        fun getInstance(context: Context): BaiduLocationService {
            return instance ?: synchronized(this) {
                instance ?: BaiduLocationService(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}

/**
 * 定位异常类
 */
class LocationException(message: String, cause: Throwable? = null) : Exception(message, cause)


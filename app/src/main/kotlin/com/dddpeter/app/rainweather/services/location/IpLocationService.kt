package com.dddpeter.app.rainweather.services.location

import com.dddpeter.app.rainweather.data.models.LocationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import timber.log.Timber
import java.net.URL

/**
 * IP定位服务（对应Flutter版本的IpLocationService）
 * 作为最后的降级方案
 */
class IpLocationService private constructor() {
    
    /**
     * 获取当前位置（通过IP定位，带超时）
     */
    suspend fun getCurrentLocation(timeoutMillis: Long = 5000): LocationModel? {
        return withTimeoutOrNull(timeoutMillis) {
            getCurrentLocationInternal()
        }
    }
    
    /**
     * 获取当前位置（内部实现）
     */
    private suspend fun getCurrentLocationInternal(): LocationModel? = withContext(Dispatchers.IO) {
        try {
            Timber.d("🚀 开始IP定位...")
            
            // 使用免费的IP定位API（示例：ip-api.com）
            val url = "http://ip-api.com/json/?lang=zh-CN"
            val response = URL(url).readText()
            
            val json = JSONObject(response)
            
            // 检查状态
            val status = json.optString("status", "")
            if (status != "success") {
                Timber.e("❌ IP定位失败: ${json.optString("message", "未知错误")}")
                return@withContext null
            }
            
            val lat = json.optDouble("lat", 0.0)
            val lng = json.optDouble("lon", 0.0)
            
            if (lat == 0.0 || lng == 0.0) {
                Timber.e("❌ IP定位坐标无效")
                return@withContext null
            }
            
            Timber.d("✅ IP定位成功: ($lat, $lng)")
            
            LocationModel(
                lat = lat,
                lng = lng,
                address = json.optString("country", "") + json.optString("regionName", "") + json.optString("city", ""),
                country = json.optString("country", "中国"),
                province = json.optString("regionName", ""),
                city = json.optString("city", ""),
                district = "",
                street = "",
                adcode = "",
                town = "",
                isProxyDetected = json.optBoolean("proxy", false)
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ IP定位异常")
            null
        }
    }
    
    companion object {
        @Volatile
        private var instance: IpLocationService? = null
        
        fun getInstance(): IpLocationService {
            return instance ?: synchronized(this) {
                instance ?: IpLocationService().also { instance = it }
            }
        }
    }
}


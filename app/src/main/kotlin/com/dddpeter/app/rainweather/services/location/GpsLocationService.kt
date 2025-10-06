package com.dddpeter.app.rainweather.services.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.dddpeter.app.rainweather.data.models.LocationModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.Locale
import kotlin.coroutines.resume

/**
 * GPS定位服务（对应Flutter版本的GpsLocationService）
 * 使用Google Play Services的FusedLocationProviderClient
 */
class GpsLocationService private constructor(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.CHINA)
    
    /**
     * 获取当前位置（单次定位，带超时）
     */
    suspend fun getCurrentLocation(timeoutMillis: Long = 10000): LocationModel? {
        return withTimeoutOrNull(timeoutMillis) {
            getCurrentLocationInternal()
        }
    }
    
    /**
     * 获取当前位置（内部实现）
     */
    private suspend fun getCurrentLocationInternal(): LocationModel? = suspendCancellableCoroutine { continuation ->
        try {
            // 检查权限
            if (!hasLocationPermission()) {
                Timber.e("❌ 没有定位权限")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            Timber.d("🚀 开始GPS定位...")
            
            // 获取最后已知位置（快速）
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Timber.d("✅ 使用最后已知位置: (${location.latitude}, ${location.longitude})")
                    
                    // 反向地理编码
                    val locationModel = reverseGeocode(location.latitude, location.longitude)
                    
                    if (continuation.isActive) {
                        continuation.resume(locationModel)
                    }
                } else {
                    // 如果没有最后位置，请求当前位置
                    Timber.d("⚠️ 没有最后已知位置，请求当前位置")
                    getCurrentPositionWithPriority(continuation)
                }
            }.addOnFailureListener { e ->
                Timber.e(e, "❌ 获取最后位置失败")
                // 失败时也尝试获取当前位置
                getCurrentPositionWithPriority(continuation)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ GPS定位异常")
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
    
    /**
     * 使用高精度优先级获取当前位置
     */
    private fun getCurrentPositionWithPriority(
        continuation: kotlinx.coroutines.CancellableContinuation<LocationModel?>
    ) {
        try {
            if (!hasLocationPermission()) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
                return
            }
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    Timber.d("✅ GPS定位成功: (${location.latitude}, ${location.longitude})")
                    
                    val locationModel = reverseGeocode(location.latitude, location.longitude)
                    
                    if (continuation.isActive) {
                        continuation.resume(locationModel)
                    }
                } else {
                    Timber.e("❌ GPS定位结果为空")
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }.addOnFailureListener { e ->
                Timber.e(e, "❌ GPS定位失败")
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ 获取当前位置异常")
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
    
    /**
     * 反向地理编码（坐标转地址）
     */
    private fun reverseGeocode(lat: Double, lng: Double): LocationModel {
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                
                LocationModel(
                    lat = lat,
                    lng = lng,
                    address = address.getAddressLine(0) ?: "",
                    country = address.countryName ?: "中国",
                    province = address.adminArea ?: "",
                    city = address.locality ?: address.subAdminArea ?: "",
                    district = address.subLocality ?: "",
                    street = address.thoroughfare ?: "",
                    adcode = "",
                    town = "",
                    isProxyDetected = false
                )
            } else {
                // 如果反向地理编码失败，只返回坐标
                Timber.w("⚠️ 反向地理编码无结果，只返回坐标")
                LocationModel.fromLatLng(lat, lng)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ 反向地理编码失败")
            LocationModel.fromLatLng(lat, lng)
        }
    }
    
    /**
     * 检查是否有定位权限
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    companion object {
        @Volatile
        private var instance: GpsLocationService? = null
        
        fun getInstance(context: Context): GpsLocationService {
            return instance ?: synchronized(this) {
                instance ?: GpsLocationService(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}


package com.dddpeter.app.rainweather.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * 权限管理器
 * 负责处理应用所需的各种运行时权限
 */
object PermissionManager {
    
    // 权限请求码
    const val REQUEST_LOCATION_PERMISSION = 1001
    const val REQUEST_NOTIFICATION_PERMISSION = 1002
    
    /**
     * 检查定位权限是否已授予
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return fineLocation && coarseLocation
    }
    
    /**
     * 检查通知权限是否已授予（Android 13+）
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13以下不需要通知权限
        }
    }
    
    /**
     * 请求定位权限
     */
    fun requestLocationPermission(activity: Activity) {
        Timber.d("🔐 请求定位权限")
        
        // 记录权限请求历史
        markLocationPermissionRequested(activity)
        
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            REQUEST_LOCATION_PERMISSION
        )
    }
    
    /**
     * 请求通知权限（Android 13+）
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Timber.d("🔐 请求通知权限")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }
    
    /**
     * 检查是否应该显示权限说明
     * （用户之前拒绝过权限，但没有选择"不再询问"）
     */
    fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    /**
     * 检查权限是否被永久拒绝
     * （用户选择了"不再询问"并拒绝了权限）
     */
    fun isLocationPermissionPermanentlyDenied(activity: Activity): Boolean {
        val isGranted = isLocationPermissionGranted(activity)
        val shouldShowRationale = shouldShowLocationPermissionRationale(activity)
        val hasRequestedBefore = hasRequestedLocationPermissionBefore(activity)
        
        Timber.d("🔐 永久拒绝检查: isGranted=$isGranted, shouldShowRationale=$shouldShowRationale, hasRequestedBefore=$hasRequestedBefore")
        
        // 如果权限未授予，并且不应该显示说明（意味着用户选择了"不再询问"）
        // 但是要注意：首次安装时shouldShowRationale也是false，所以需要额外检查
        val isPermanentlyDenied = !isGranted && !shouldShowRationale && hasRequestedBefore
        Timber.d("🔐 是否永久拒绝: $isPermanentlyDenied")
        
        return isPermanentlyDenied
    }
    
    /**
     * 检查是否之前请求过定位权限
     * 通过检查SharedPreferences来记录权限请求历史
     */
    private fun hasRequestedLocationPermissionBefore(activity: Activity): Boolean {
        val prefs = activity.getSharedPreferences("permission_history", Context.MODE_PRIVATE)
        return prefs.getBoolean("location_permission_requested", false)
    }
    
    /**
     * 记录定位权限请求历史
     */
    fun markLocationPermissionRequested(activity: Activity) {
        val prefs = activity.getSharedPreferences("permission_history", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("location_permission_requested", true).apply()
    }
    
    /**
     * 打开应用设置页面
     */
    fun openAppSettings(context: Context) {
        Timber.d("📱 打开应用设置页面")
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "❌ 无法打开应用设置页面")
            
            // 降级方案：打开系统设置
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Timber.e(e2, "❌ 无法打开系统设置")
            }
        }
    }
    
    /**
     * 打开位置设置页面
     */
    fun openLocationSettings(context: Context) {
        Timber.d("📍 打开位置设置页面")
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "❌ 无法打开位置设置页面")
        }
    }
    
    /**
     * 获取定位权限状态描述
     */
    fun getLocationPermissionStatus(context: Context): PermissionStatus {
        val isGranted = isLocationPermissionGranted(context)
        val isPermanentlyDenied = context is Activity && isLocationPermissionPermanentlyDenied(context)
        
        Timber.d("🔐 权限状态检查: isGranted=$isGranted, isPermanentlyDenied=$isPermanentlyDenied")
        
        return when {
            isGranted -> {
                Timber.d("✅ 权限状态: GRANTED")
                PermissionStatus.GRANTED
            }
            isPermanentlyDenied -> {
                Timber.d("❌ 权限状态: PERMANENTLY_DENIED")
                PermissionStatus.PERMANENTLY_DENIED
            }
            else -> {
                Timber.d("⚠️ 权限状态: DENIED")
                PermissionStatus.DENIED
            }
        }
    }
    
    /**
     * 权限状态枚举
     */
    enum class PermissionStatus {
        GRANTED,           // 已授予
        DENIED,            // 被拒绝（可以再次请求）
        PERMANENTLY_DENIED // 永久拒绝（需要去设置中手动开启）
    }
    
    /**
     * 处理权限请求结果
     */
    fun handleLocationPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
        onPermanentlyDenied: () -> Unit
    ) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) return
        
        if (grantResults.isNotEmpty() && 
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Timber.d("✅ 定位权限已授予")
            onGranted()
        } else {
            Timber.w("❌ 定位权限被拒绝")
            
            // 检查是否是永久拒绝
            val isPermanentlyDenied = permissions.any { permission ->
                grantResults[permissions.indexOf(permission)] == PackageManager.PERMISSION_DENIED
            }
            
            if (isPermanentlyDenied) {
                Timber.w("⚠️ 定位权限被永久拒绝")
                onPermanentlyDenied()
            } else {
                onDenied()
            }
        }
    }
}


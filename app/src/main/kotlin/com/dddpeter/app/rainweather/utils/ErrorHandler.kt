package com.dddpeter.app.rainweather.utils

import android.content.Context
import android.widget.Toast
import timber.log.Timber

/**
 * 错误处理工具类
 */
object ErrorHandler {
    
    /**
     * 处理网络错误
     */
    fun handleNetworkError(context: Context, throwable: Throwable) {
        Timber.e(throwable, "网络错误")
        
        val message = when {
            throwable.message?.contains("timeout") == true -> "网络请求超时，请检查网络连接"
            throwable.message?.contains("Unable to resolve host") == true -> "网络连接失败，请检查网络设置"
            throwable.message?.contains("Connection refused") == true -> "服务器连接被拒绝"
            else -> "网络请求失败，请稍后重试"
        }
        
        showErrorToast(context, message)
    }
    
    /**
     * 处理定位错误
     */
    fun handleLocationError(context: Context, throwable: Throwable) {
        Timber.e(throwable, "定位错误")
        
        val message = when {
            throwable.message?.contains("permission") == true -> "定位权限被拒绝，请在设置中开启"
            throwable.message?.contains("timeout") == true -> "定位超时，请检查GPS设置"
            throwable.message?.contains("no location provider") == true -> "无可用定位服务，请检查GPS设置"
            else -> "定位失败，将使用默认城市"
        }
        
        showErrorToast(context, message)
    }
    
    /**
     * 处理天气数据错误
     */
    fun handleWeatherDataError(context: Context, throwable: Throwable) {
        Timber.e(throwable, "天气数据错误")
        
        val message = when {
            throwable.message?.contains("404") == true -> "城市数据不存在"
            throwable.message?.contains("500") == true -> "服务器内部错误，请稍后重试"
            throwable.message?.contains("timeout") == true -> "数据加载超时，请检查网络"
            else -> "天气数据加载失败，请稍后重试"
        }
        
        showErrorToast(context, message)
    }
    
    /**
     * 处理数据库错误
     */
    fun handleDatabaseError(context: Context, throwable: Throwable) {
        Timber.e(throwable, "数据库错误")
        
        val message = when {
            throwable.message?.contains("database is locked") == true -> "数据库被占用，请稍后重试"
            throwable.message?.contains("no such table") == true -> "数据表不存在，请重新安装应用"
            else -> "数据存储失败，请稍后重试"
        }
        
        showErrorToast(context, message)
    }
    
    /**
     * 处理权限错误
     */
    fun handlePermissionError(context: Context, permission: String) {
        Timber.w("权限被拒绝: $permission")
        
        val message = when (permission) {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION -> "定位权限被拒绝，无法获取当前位置"
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存储权限被拒绝，无法保存数据"
            else -> "权限被拒绝，部分功能可能无法使用"
        }
        
        showErrorToast(context, message)
    }
    
    /**
     * 显示错误提示
     */
    private fun showErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 获取用户友好的错误信息
     */
    fun getUserFriendlyMessage(throwable: Throwable): String {
        return when {
            throwable.message?.contains("timeout") == true -> "请求超时"
            throwable.message?.contains("network") == true -> "网络异常"
            throwable.message?.contains("permission") == true -> "权限不足"
            throwable.message?.contains("location") == true -> "定位失败"
            else -> "操作失败"
        }
    }
    
    /**
     * 记录错误日志
     */
    fun logError(tag: String, throwable: Throwable, context: String = "") {
        Timber.e(throwable, "❌ $tag: $context")
    }
    
    /**
     * 记录警告日志
     */
    fun logWarning(tag: String, message: String) {
        Timber.w("⚠️ $tag: $message")
    }
    
    /**
     * 记录信息日志
     */
    fun logInfo(tag: String, message: String) {
        Timber.d("ℹ️ $tag: $message")
    }
}

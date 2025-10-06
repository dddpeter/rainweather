package com.dddpeter.app.rainweather.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * 网络状态监听器
 */
class NetworkMonitor(private val context: Context) {
    
    private val _isNetworkAvailable = MutableStateFlow(isNetworkAvailable())
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startNetworkMonitoring()
        }
    }
    
    /**
     * 检查网络是否可用
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                } else {
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                connectivityManager.activeNetworkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            Timber.e(e, "检查网络状态失败")
            false
        }
    }
    
    /**
     * 开始监听网络状态变化
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _isNetworkAvailable.value = true
                Timber.d("🌐 网络已连接")
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                _isNetworkAvailable.value = false
                Timber.w("🌐 网络已断开")
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val isAvailable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                _isNetworkAvailable.value = isAvailable
                Timber.d("🌐 网络能力变化: $isAvailable")
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }
    
    /**
     * 停止监听网络状态
     */
    fun stopMonitoring() {
        networkCallback?.let { callback ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(callback)
            }
            networkCallback = null
        }
    }
    
    /**
     * 手动刷新网络状态
     */
    fun refreshNetworkStatus() {
        _isNetworkAvailable.value = isNetworkAvailable()
    }
    
    /**
     * 获取网络类型描述
     */
    fun getNetworkType(): String {
        return try {
            val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.activeNetwork
            } else {
                null
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && network != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                when {
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "移动数据"
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "以太网"
                    else -> "未知"
                }
            } else {
                @Suppress("DEPRECATION")
                when (connectivityManager.activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> "WiFi"
                    ConnectivityManager.TYPE_MOBILE -> "移动数据"
                    ConnectivityManager.TYPE_ETHERNET -> "以太网"
                    else -> "未知"
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "获取网络类型失败")
            "未知"
        }
    }
}

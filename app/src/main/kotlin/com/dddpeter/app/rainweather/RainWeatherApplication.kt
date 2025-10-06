package com.dddpeter.app.rainweather

import android.app.Application
import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.ui.theme.ThemeManager
import com.dddpeter.app.rainweather.utils.CityDataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 应用Application类（对应Flutter版本的main.dart）
 */
class RainWeatherApplication : Application() {
    
    // 全局单例
    lateinit var database: AppDatabase
        private set
    
    lateinit var themeManager: ThemeManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Timber日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("🚀 RainWeather Application 启动")
        
        // 初始化数据库
        database = AppDatabase.getInstance(this)
        Timber.d("✅ 数据库初始化完成")
        
        // 初始化城市数据
        initializeCityData()
        
        // 初始化主题管理器
        themeManager = ThemeManager.getInstance(this)
        Timber.d("✅ 主题管理器初始化完成")
        
        // 这里可以初始化其他全局服务
        // 例如：百度定位SDK的全局配置
        initializeBaiduLocation()
    }
    
    /**
     * 初始化城市数据
     */
    private fun initializeCityData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val initializer = CityDataInitializer(
                    context = this@RainWeatherApplication,
                    database = database
                )
                
                if (!initializer.isCitiesInitialized()) {
                    Timber.d("🏙️ 开始加载城市数据...")
                    val success = initializer.initializeCities()
                    
                    if (success) {
                        val count = initializer.getCityCount()
                        Timber.d("✅ 城市数据加载完成，共 $count 个城市")
                    } else {
                        Timber.e("❌ 城市数据加载失败")
                    }
                } else {
                    val count = initializer.getCityCount()
                    Timber.d("✅ 城市数据已存在，共 $count 个城市")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ 城市数据初始化异常")
            }
        }
    }
    
    /**
     * 初始化百度定位SDK
     */
    private fun initializeBaiduLocation() {
        try {
            Timber.d("🔧 百度定位SDK全局配置")
            
            // 设置隐私政策同意（百度定位SDK要求）
            // 注意：在实际应用中，应该在用户同意隐私政策后调用
            try {
                val locationClientClass = Class.forName("com.baidu.location.LocationClient")
                val method = locationClientClass.getMethod("setAgreePrivacy", Boolean::class.java)
                method.invoke(null, true)
                Timber.d("✅ 百度定位隐私政策同意设置成功")
            } catch (e: Exception) {
                Timber.e(e, "⚠️ 百度定位隐私政策设置失败（可能SDK未集成）")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ 百度定位SDK配置失败")
        }
    }
    
    companion object {
        @Volatile
        private var instance: RainWeatherApplication? = null
        
        fun getInstance(): RainWeatherApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
    
    init {
        instance = this
    }
}


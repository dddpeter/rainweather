package com.dddpeter.app.rainweather.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.dddpeter.app.rainweather.data.models.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * 主题管理器（对应Flutter版本的ThemeProvider）
 */
class ThemeManager private constructor(private val context: Context) {
    
    private val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _themeMode = MutableStateFlow(getCurrentThemeMode())
    val themeMode: StateFlow<AppConstants.ThemeMode> = _themeMode.asStateFlow()
    
    /**
     * 获取当前主题模式
     */
    private fun getCurrentThemeMode(): AppConstants.ThemeMode {
        val savedMode = prefs.getString(AppConstants.PREF_THEME_MODE, AppConstants.ThemeMode.SYSTEM.name)
        return try {
            AppConstants.ThemeMode.valueOf(savedMode ?: AppConstants.ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            Timber.e(e, "解析主题模式失败，使用默认值")
            AppConstants.ThemeMode.SYSTEM
        }
    }
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(mode: AppConstants.ThemeMode) {
        Timber.d("设置主题模式: $mode")
        
        // 保存到SharedPreferences
        prefs.edit().putString(AppConstants.PREF_THEME_MODE, mode.name).apply()
        
        // 更新StateFlow
        _themeMode.value = mode
        
        // 应用主题
        applyTheme(mode)
    }
    
    /**
     * 应用主题
     */
    private fun applyTheme(mode: AppConstants.ThemeMode) {
        val nightMode = when (mode) {
            AppConstants.ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppConstants.ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppConstants.ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        AppCompatDelegate.setDefaultNightMode(nightMode)
        Timber.d("应用主题模式: $mode -> $nightMode")
    }
    
    /**
     * 初始化主题
     */
    fun initialize() {
        val mode = getCurrentThemeMode()
        Timber.d("初始化主题: $mode")
        applyTheme(mode)
    }
    
    /**
     * 判断当前是否是暗色主题
     */
    fun isDarkTheme(): Boolean {
        return when (_themeMode.value) {
            AppConstants.ThemeMode.LIGHT -> false
            AppConstants.ThemeMode.DARK -> true
            AppConstants.ThemeMode.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    /**
     * 判断当前是否是亮色主题
     */
    fun isLightTheme(): Boolean {
        return !isDarkTheme()
    }
    
    /**
     * 切换主题（亮色 <-> 暗色）
     */
    fun toggleTheme() {
        val newMode = when (_themeMode.value) {
            AppConstants.ThemeMode.LIGHT -> AppConstants.ThemeMode.DARK
            AppConstants.ThemeMode.DARK -> AppConstants.ThemeMode.LIGHT
            AppConstants.ThemeMode.SYSTEM -> {
                if (isDarkTheme()) {
                    AppConstants.ThemeMode.LIGHT
                } else {
                    AppConstants.ThemeMode.DARK
                }
            }
        }
        setThemeMode(newMode)
    }
    
    companion object {
        @Volatile
        private var instance: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { 
                    instance = it
                    it.initialize()
                }
            }
        }
    }
}


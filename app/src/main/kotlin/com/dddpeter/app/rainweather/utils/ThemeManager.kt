package com.dddpeter.app.rainweather.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import timber.log.Timber

/**
 * 主题管理器
 * 支持动态切换日间/夜间主题
 */
object ThemeManager {
    
    private const val PREF_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    
    // 主题模式
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"
    
    private var preferences: SharedPreferences? = null
    
    /**
     * 初始化主题管理器
     */
    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        applyTheme(getCurrentTheme())
    }
    
    /**
     * 获取当前主题
     */
    fun getCurrentTheme(): String {
        return preferences?.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    /**
     * 设置主题
     */
    fun setTheme(theme: String) {
        preferences?.edit()?.putString(KEY_THEME_MODE, theme)?.apply()
        applyTheme(theme)
        Timber.d("🎨 主题已切换到: $theme")
    }
    
    /**
     * 应用主题
     */
    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            THEME_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * 检查是否为深色主题
     */
    fun isDarkTheme(): Boolean {
        return getCurrentTheme() == THEME_DARK
    }
    
    /**
     * 切换主题（在日间和夜间之间切换）
     */
    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            THEME_LIGHT -> THEME_DARK
            THEME_DARK -> THEME_LIGHT
            else -> THEME_DARK // 系统主题时切换到夜间
        }
        setTheme(newTheme)
    }
    
    /**
     * 获取主题显示名称
     */
    fun getThemeDisplayName(theme: String): String {
        return when (theme) {
            THEME_LIGHT -> "浅色主题"
            THEME_DARK -> "深色主题"
            THEME_SYSTEM -> "跟随系统"
            else -> "跟随系统"
        }
    }
    
    /**
     * 获取所有可用主题
     */
    fun getAvailableThemes(): List<String> {
        return listOf(THEME_LIGHT, THEME_DARK, THEME_SYSTEM)
    }
    
    /**
     * 获取主题图标
     */
    fun getThemeIcon(theme: String): String {
        return when (theme) {
            THEME_LIGHT -> "☀️"
            THEME_DARK -> "🌙"
            THEME_SYSTEM -> "⚙️"
            else -> "⚙️"
        }
    }
}

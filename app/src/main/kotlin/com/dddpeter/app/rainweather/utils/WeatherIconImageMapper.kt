package com.dddpeter.app.rainweather.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.dddpeter.app.rainweather.R
import timber.log.Timber

/**
 * 天气图标图片映射工具类
 * 根据天气描述和时间（白天/夜间）返回对应的图片资源
 */
object WeatherIconImageMapper {
    
    // 白天天气图标映射
    private val dayWeatherIcons = mapOf(
        "大雨" to "dby.png",
        "暴雨" to "dby.png",
        "冻雨" to "dy.png",
        "大雪" to "dx.png",
        "暴雪" to "bx.png",
        "多云" to "dy.png",
        "多云转晴" to "dyq.png",
        "晴转多云" to "dyq.png",
        "雷阵雨" to "lzy.png",
        "沙尘暴" to "scb.png",
        "雾" to "w.png",
        "小雪" to "xx.png",
        "小雨" to "xy.png",
        "阴" to "y.png",
        "晴" to "q.png",
        "雨夹雪" to "yjx.png",
        "中雨" to "zhy.png",
        "中雪" to "zx.png",
        "阵雨" to "zy.png",
        "霾" to "scb.png",
        "扬沙" to "scb.png",
        "浮尘" to "scb.png"
    )
    
    // 夜间天气图标映射
    private val nightWeatherIcons = mapOf(
        "大雨" to "dby.png",
        "暴雨" to "dby.png",
        "冻雨" to "dy0.png",
        "大雪" to "dx0.png",
        "暴雪" to "bx.png",
        "多云" to "dy0.png",
        "多云转晴" to "dyq0.png",
        "晴转多云" to "dyq0.png",
        "雷阵雨" to "lzy0.png",
        "沙尘暴" to "scb.png",
        "雾" to "w.png",
        "小雪" to "xx.png",
        "小雨" to "xy.png",
        "阴" to "y.png",
        "晴" to "q0.png",
        "雨夹雪" to "yjx.png",
        "中雨" to "zhy.png",
        "中雪" to "zx.png",
        "阵雨" to "zy0.png",
        "霾" to "scb.png",
        "扬沙" to "scb.png",
        "浮尘" to "scb.png"
    )
    
    /**
     * 获取天气图标Drawable
     * @param context 上下文
     * @param weather 天气描述
     * @param isNight 是否为夜间
     * @return 天气图标Drawable，如果找不到则返回默认图标
     */
    fun getWeatherIconDrawable(context: Context, weather: String?, isNight: Boolean = false): Drawable? {
        val weatherDesc = weather ?: "晴"
        val iconMap = if (isNight) nightWeatherIcons else dayWeatherIcons
        val iconName = iconMap[weatherDesc] ?: "notclear.png"
        
        Timber.d("🖼️ 加载天气图标: weather=$weatherDesc, isNight=$isNight, iconName=$iconName")
        
        return try {
            // 从assets根目录加载图片
            val inputStream = context.assets.open(iconName)
            val drawable = Drawable.createFromStream(inputStream, null)
            inputStream.close()
            
            // 设置图片尺寸
            drawable?.setBounds(0, 0, 80, 80)
            
            Timber.d("✅ 天气图标加载成功: $iconName, size=${drawable?.intrinsicWidth}x${drawable?.intrinsicHeight}")
            drawable
        } catch (e: Exception) {
            Timber.e(e, "❌ 加载天气图标失败: $iconName")
            // 如果加载失败，返回默认图标
            try {
                val inputStream = context.assets.open("notclear.png")
                val drawable = Drawable.createFromStream(inputStream, null)
                inputStream.close()
                drawable?.setBounds(0, 0, 80, 80)
                Timber.d("✅ 默认天气图标加载成功")
                drawable
            } catch (ex: Exception) {
                Timber.e(ex, "❌ 加载默认天气图标失败")
                null
            }
        }
    }
    
    /**
     * 判断当前是否为夜间
     * 简单实现：根据当前小时判断（18:00-06:00为夜间）
     * @return true为夜间，false为白天
     */
    fun isNightTime(): Boolean {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return currentHour >= 18 || currentHour < 6
    }
    
    /**
     * 获取天气图标资源ID（用于ImageView的setImageResource）
     * @param context 上下文
     * @param weather 天气描述
     * @param isNight 是否为夜间
     * @return 天气图标资源ID
     */
    fun getWeatherIconResourceId(context: Context, weather: String?, isNight: Boolean = false): Int {
        val weatherDesc = weather ?: "晴"
        val iconMap = if (isNight) nightWeatherIcons else dayWeatherIcons
        val iconName = iconMap[weatherDesc] ?: "notclear"
        
        // 尝试从drawable资源中获取
        val resourceId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        return if (resourceId != 0) {
            resourceId
        } else {
            // 如果drawable中没有，返回默认图标
            R.drawable.ic_weather_default
        }
    }
}

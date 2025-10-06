package com.dddpeter.app.rainweather.utils

import com.dddpeter.app.rainweather.data.models.AppConstants

/**
 * 天气图标映射工具类（对应Flutter版本的天气图标系统）
 * 支持45种天气类型的emoji映射
 */
object WeatherIconMapper {
    
    /**
     * 获取天气图标emoji
     */
    fun getWeatherIcon(weatherType: String?): String {
        if (weatherType.isNullOrEmpty()) return "☀️"
        return AppConstants.WEATHER_ICONS[weatherType] ?: "☀️"
    }
    
    /**
     * 获取AQI等级描述
     */
    fun getAqiLevel(aqi: Int?): String {
        if (aqi == null) return "未知"
        return when {
            aqi <= 50 -> "优"
            aqi <= 100 -> "良"
            aqi <= 150 -> "轻度污染"
            aqi <= 200 -> "中度污染"
            aqi <= 300 -> "重度污染"
            else -> "严重污染"
        }
    }
    
    /**
     * 获取AQI等级颜色
     */
    fun getAqiLevelColor(aqi: Int?): Int {
        if (aqi == null) return 0xFF6B7280.toInt()
        return when {
            aqi <= 50 -> 0xFF4CAF50.toInt() // 优 - 绿色
            aqi <= 100 -> 0xFF8edafc.toInt() // 良 - 蓝色
            aqi <= 150 -> 0xFFFF9800.toInt() // 轻度污染 - 橙色
            aqi <= 200 -> 0xFFD32F2F.toInt() // 中度污染 - 红色
            aqi <= 300 -> 0xFF9C27B0.toInt() // 重度污染 - 紫色
            else -> 0xFF5D4037.toInt() // 严重污染 - 褐色
        }
    }
    
    /**
     * 获取月相emoji
     */
    fun getMoonPhaseEmoji(moonPhase: String?): String {
        return when (moonPhase) {
            "新月" -> "🌑"
            "峨眉月" -> "🌒"
            "上弦月" -> "🌓"
            "盈凸月" -> "🌔"
            "满月" -> "🌕"
            "亏凸月" -> "🌖"
            "下弦月" -> "🌗"
            "残月" -> "🌘"
            else -> "🌙"
        }
    }
    
    /**
     * 获取生活指数图标
     */
    fun getLifeIndexIcon(indexName: String?): String {
        return when (indexName) {
            "穿衣指数" -> "👕"
            "感冒指数" -> "🤧"
            "运动指数" -> "🏃"
            "洗车指数" -> "🚗"
            "紫外线指数" -> "☀️"
            "空气污染扩散指数" -> "💨"
            "旅游指数" -> "🧳"
            "舒适度指数" -> "😊"
            else -> "📋"
        }
    }
    
    /**
     * 获取风力等级描述
     */
    fun getWindPowerDescription(windPower: String?): String {
        if (windPower.isNullOrEmpty()) return "微风"
        
        // 提取风力等级数字
        val powerLevel = windPower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        
        return when {
            powerLevel == 0 -> "平静"
            powerLevel <= 2 -> "微风"
            powerLevel <= 4 -> "和风"
            powerLevel <= 6 -> "清风"
            powerLevel <= 8 -> "强风"
            powerLevel <= 10 -> "疾风"
            powerLevel <= 12 -> "狂风"
            else -> "飓风"
        }
    }
    
    /**
     * 格式化温度显示
     */
    fun formatTemperature(temp: String?): String {
        if (temp.isNullOrEmpty()) return "--"
        // 检查是否已经包含℃符号，避免重复添加
        return if (temp.endsWith("℃")) {
            temp
        } else {
            "${temp}℃"
        }
    }
    
    /**
     * 格式化湿度显示
     */
    fun formatHumidity(humidity: String?): String {
        if (humidity.isNullOrEmpty()) return "--"
        return "${humidity}%"
    }
    
    /**
     * 格式化气压显示
     */
    fun formatAirPressure(pressure: String?): String {
        if (pressure.isNullOrEmpty()) return "--"
        return "${pressure}hPa"
    }
    
    /**
     * 格式化能见度显示
     */
    fun formatVisibility(visibility: String?): String {
        if (visibility.isNullOrEmpty()) return "--"
        return "${visibility}km"
    }
    
    /**
     * 格式化时间（HH:mm）
     */
    fun formatTime(time: String?): String {
        if (time.isNullOrEmpty()) return "--:--"
        return try {
            if (time.contains(":")) {
                time.substring(0, 5) // 取前5位 HH:mm
            } else {
                time
            }
        } catch (e: Exception) {
            time
        }
    }
    
    /**
     * 格式化日期（MM-DD）
     */
    fun formatDate(date: String?): String {
        if (date.isNullOrEmpty()) return "--"
        return try {
            // 假设输入格式为 yyyy-MM-dd
            if (date.length >= 10) {
                date.substring(5, 10) // MM-DD
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }
    
    /**
     * 获取星期几
     */
    fun getWeekday(week: String?): String {
        if (week.isNullOrEmpty()) return ""
        
        // 如果已经是中文星期，直接返回
        if (week.startsWith("星期") || week.startsWith("周")) {
            return week
        }
        
        // 英文转中文
        return when (week.lowercase()) {
            "monday", "mon" -> "星期一"
            "tuesday", "tue" -> "星期二"
            "wednesday", "wed" -> "星期三"
            "thursday", "thu" -> "星期四"
            "friday", "fri" -> "星期五"
            "saturday", "sat" -> "星期六"
            "sunday", "sun" -> "星期日"
            else -> week
        }
    }
}


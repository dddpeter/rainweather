package com.dddpeter.app.rainweather.data.models

/**
 * 应用常量（对应Flutter版本的AppConstants）
 */
object AppConstants {
    // API配置
    const val WEATHER_API_BASE_URL = "https://www.weatherol.cn/"
    const val WEATHER_API_PATH = "api/home/getCurrAnd15dAnd24h"
    const val DEFAULT_CITY_ID = "101010100" // 北京

    // 缓存配置
    const val CACHE_EXPIRY_MINUTES = 30
    const val CURRENT_LOCATION_KEY = "current_location"
    const val WEATHER_ALL_KEY = "weather_all"

    // 屏幕配置
    const val SCREEN_HORIZONTAL_PADDING = 16f

    // 天气图标映射（45种天气类型）
    val WEATHER_ICONS = mapOf(
        "晴" to "☀️",
        "少云" to "🌤️",
        "晴间多云" to "⛅",
        "多云" to "☁️",
        "阴" to "☁️",
        "有风" to "💨",
        "平静" to "😌",
        "微风" to "🍃",
        "和风" to "🌬️",
        "清风" to "🌬️",
        "强风/劲风" to "💨",
        "疾风" to "🌪️",
        "大风" to "🌬️",
        "烈风" to "🌪️",
        "风暴" to "⛈️",
        "狂爆风" to "🌪️",
        "飓风" to "🌀",
        "热带风暴" to "🌀",
        "霾" to "😷",
        "中度霾" to "😷",
        "重度霾" to "😷",
        "严重霾" to "😷",
        "阵雨" to "🌦️",
        "雷阵雨" to "⛈️",
        "雷阵雨并伴有冰雹" to "⛈️",
        "小雨" to "🌧️",
        "中雨" to "🌧️",
        "大雨" to "🌧️",
        "暴雨" to "⛈️",
        "大暴雨" to "⛈️",
        "特大暴雨" to "⛈️",
        "强阵雨" to "🌧️",
        "强雷阵雨" to "⛈️",
        "极端降雨" to "⛈️",
        "毛毛雨/细雨" to "🌦️",
        "雨" to "🌧️",
        "小雨-中雨" to "🌧️",
        "中雨-大雨" to "🌧️",
        "大雨-暴雨" to "⛈️",
        "暴雨-大暴雨" to "⛈️",
        "大暴雨-特大暴雨" to "⛈️",
        "雨雪天气" to "🌨️",
        "雨夹雪" to "🌨️",
        "阵雨夹雪" to "🌨️",
        "冻雨" to "🧊",
        "雪" to "❄️",
        "阵雪" to "🌨️",
        "小雪" to "🌨️",
        "中雪" to "❄️",
        "大雪" to "❄️",
        "暴雪" to "❄️",
        "小雪-中雪" to "🌨️",
        "中雪-大雪" to "❄️",
        "大雪-暴雪" to "❄️",
        "浮尘" to "🌫️",
        "扬沙" to "🌫️",
        "沙尘暴" to "🌪️",
        "强沙尘暴" to "🌪️",
        "龙卷风" to "🌪️",
        "雾" to "🌫️",
        "浓雾" to "🌫️",
        "强浓雾" to "🌫️",
        "轻雾" to "🌁",
        "大雾" to "🌫️",
        "特强浓雾" to "🌫️"
    )

    // 主题模式
    enum class ThemeMode {
        LIGHT,
        DARK,
        SYSTEM
    }

    // 定位超时时间
    const val BAIDU_LOCATION_TIMEOUT = 8000L // 8秒
    const val GPS_LOCATION_TIMEOUT = 10000L // 10秒
    const val IP_LOCATION_TIMEOUT = 5000L // 5秒

    // 数据库配置
    const val DATABASE_NAME = "rainweather.db"
    const val DATABASE_VERSION = 1

    // SharedPreferences配置
    const val PREFS_NAME = "rainweather_prefs"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_LAST_LOCATION = "last_location"
    const val PREF_PRIVACY_AGREED = "privacy_agreed"

    // 权限请求码
    const val REQUEST_LOCATION_PERMISSION = 1001
    const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 1002

    // 通知配置
    const val NOTIFICATION_CHANNEL_ID = "weather_notification"
    const val NOTIFICATION_CHANNEL_NAME = "天气通知"
    const val NOTIFICATION_ID = 1001
}


package com.dddpeter.app.rainweather.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 日出日落和生活指数数据模型（对应Flutter版本的SunMoonIndexModel）
 */
@Parcelize
data class SunMoonIndexData(
    val sunMoonData: SunMoonData? = null,
    val lifeIndex: List<LifeIndex>? = null
) : Parcelable

/**
 * 日出日落月相数据
 */
@Parcelize
data class SunMoonData(
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    val moonPhase: String? = null,
    val moonPhaseEmoji: String? = null,
    val moonAge: String? = null
) : Parcelable {
    /**
     * 获取月相emoji
     */
    fun getMoonEmoji(): String {
        return when (moonPhase) {
            "新月" -> "🌑"
            "峨眉月" -> "🌒"
            "上弦月" -> "🌓"
            "盈凸月" -> "🌔"
            "满月" -> "🌕"
            "亏凸月" -> "🌖"
            "下弦月" -> "🌗"
            "残月" -> "🌘"
            else -> moonPhaseEmoji ?: "🌙"
        }
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
}

/**
 * 生活指数
 */
@Parcelize
data class LifeIndex(
    val name: String? = null,
    val level: String? = null,
    val detail: String? = null,
    val description: String? = null
) : Parcelable {
    /**
     * 获取生活指数图标
     */
    fun getIcon(): String {
        return when (name) {
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
     * 获取等级颜色（Material Design 3）
     */
    fun getLevelColor(): Int {
        return when (level) {
            "较适宜", "适宜", "优" -> 0xFF4CAF50.toInt() // Green
            "一般", "良" -> 0xFF8edafc.toInt() // Blue
            "较不宜", "不宜", "中等" -> 0xFFFF9800.toInt() // Orange
            "不宜", "差" -> 0xFFD32F2F.toInt() // Red
            else -> 0xFF6B7280.toInt() // Gray
        }
    }
}

/**
 * 日出日落和生活指数API响应
 */
data class SunMoonIndexResponse(
    val code: Int? = null,
    val msg: String? = null,
    val data: SunMoonIndexData? = null
)


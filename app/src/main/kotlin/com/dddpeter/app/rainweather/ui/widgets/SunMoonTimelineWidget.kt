package com.dddpeter.app.rainweather.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.dddpeter.app.rainweather.data.models.SunMoonData
import com.dddpeter.app.rainweather.databinding.WidgetSunMoonTimelineBinding

/**
 * 日出日落时间线组件
 */
class SunMoonTimelineWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val binding: WidgetSunMoonTimelineBinding
    
    init {
        binding = WidgetSunMoonTimelineBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }
    
    /**
     * 设置日出日落数据
     */
    fun setSunMoonData(data: SunMoonData?) {
        data?.let { sunMoonData ->
            // 设置日出时间
            binding.tvSunriseTime.text = sunMoonData.formatTime(sunMoonData.sunrise)
            
            // 设置日落时间
            binding.tvSunsetTime.text = sunMoonData.formatTime(sunMoonData.sunset)
            
            // 设置月相信息（如果有的话）
            val moonPhase = sunMoonData.moonPhase
            val moonAge = sunMoonData.moonAge
            
            if (!moonPhase.isNullOrEmpty() || !moonAge.isNullOrEmpty()) {
                binding.layoutMoonInfo.visibility = VISIBLE
                
                // 设置月相 emoji
                binding.tvMoonEmoji.text = sunMoonData.getMoonEmoji()
                
                // 设置月相文字
                if (!moonPhase.isNullOrEmpty()) {
                    binding.tvMoonPhase.text = moonPhase
                }
                
                // 设置月龄
                if (!moonAge.isNullOrEmpty()) {
                    binding.tvMoonAge.text = moonAge
                }
            } else {
                binding.layoutMoonInfo.visibility = GONE
            }
        } ?: run {
            // 数据为空时显示默认值
            binding.tvSunriseTime.text = "--:--"
            binding.tvSunsetTime.text = "--:--"
            binding.layoutMoonInfo.visibility = GONE
        }
    }
    
    /**
     * 设置动画效果
     */
    fun setAnimated(isAnimated: Boolean = true) {
        if (isAnimated) {
            // 可以添加日出日落的动画效果
            // 例如：太阳/月亮的旋转动画
            binding.tvSunriseIcon.animate()
                .rotation(360f)
                .setDuration(1000)
                .start()
                
            binding.tvSunsetIcon.animate()
                .rotation(-360f)
                .setDuration(1000)
                .start()
        }
    }
}

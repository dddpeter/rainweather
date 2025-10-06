package com.dddpeter.app.rainweather.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dddpeter.app.rainweather.databinding.FragmentTodayBinding
import com.dddpeter.app.rainweather.ui.MainActivity
import com.dddpeter.app.rainweather.ui.adapters.HourlyWeatherAdapter
import com.dddpeter.app.rainweather.utils.WeatherIconMapper
import com.dddpeter.app.rainweather.utils.WeatherIconImageMapper
import com.dddpeter.app.rainweather.viewmodels.WeatherViewModel
import com.dddpeter.app.rainweather.viewmodels.WeatherViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 今日天气Fragment
 */
class TodayFragment : Fragment() {
    
    companion object {
        private const val ARG_CITY_ID = "arg_city_id"
        
        /**
         * 创建指定城市的 Fragment
         */
        fun newInstance(cityId: String? = null): TodayFragment {
            return TodayFragment().apply {
                arguments = Bundle().apply {
                    cityId?.let { putString(ARG_CITY_ID, it) }
                }
            }
        }
    }
    
    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    
    // 城市ID（如果是查看特定城市）
    private var cityId: String? = null
    
    // ViewModel - 统一使用 Activity 作用域
    // 在 CityWeatherActivity 中，三个 Fragment 共享同一个 ViewModel
    // 在 MainActivity 中，每个 Activity 实例也有独立的 ViewModel
    private val viewModel: WeatherViewModel by activityViewModels {
        val app = requireActivity().application as com.dddpeter.app.rainweather.RainWeatherApplication
        WeatherViewModelFactory(
            app.database, 
            com.dddpeter.app.rainweather.services.location.LocationManager.getInstance(requireContext())
        )
    }
    
    private lateinit var hourlyAdapter: HourlyWeatherAdapter
    private lateinit var lifeIndexAdapter: com.dddpeter.app.rainweather.ui.adapters.LifeIndexAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 获取城市ID参数
        cityId = arguments?.getString(ARG_CITY_ID)
        
        Timber.d("📱 TodayFragment onViewCreated: cityId=$cityId, Activity=${requireActivity()::class.simpleName}")
        
        // 如果是查看特定城市，隐藏城市名称（Activity toolbar 会显示）
        if (cityId != null) {
            // 隐藏城市名称显示，由Activity toolbar显示
            binding.tvCityName.visibility = View.GONE
            Timber.d("🏙️ TodayFragment: 城市模式，隐藏城市名称，cityId=$cityId")
        }
        
        setupViews()
        observeData()
        
        // 仅在 MainActivity 中主动加载数据
        // 在 CityWeatherActivity 中，Activity 会统一加载数据
        if (cityId == null) {
            Timber.d("📍 TodayFragment: 主页模式，等待权限检查完成后加载数据")
            // 不再主动调用refreshWithLocation，等待MainActivity权限检查完成后调用
        } else {
            Timber.d("🏙️ TodayFragment: 城市模式，等待 Activity 加载数据")
        }
    }
    
    private fun setupViews() {
        // 设置默认天气图标
        binding.ivWeatherIcon.setImageResource(com.dddpeter.app.rainweather.R.drawable.ic_weather_default)
        
        // 农历日期将从天气数据中获取，这里设置默认值
        binding.tvLunarDate.text = "农历信息加载中..."
        binding.tvLunarDate.visibility = View.GONE
        
        // 设置下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            cityId?.let {
                viewModel.loadWeatherForCity(it)
            } ?: run {
                viewModel.refreshWithLocation()
            }
        }
        
        // 设置24小时预报RecyclerView
        hourlyAdapter = HourlyWeatherAdapter()
        binding.rvHourlyPreview.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
        
        // 设置生活指数RecyclerView
        lifeIndexAdapter = com.dddpeter.app.rainweather.ui.adapters.LifeIndexAdapter()
        binding.rvLifeIndex.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
            adapter = lifeIndexAdapter
        }
        
        // 配置温度图表
        setupTemperatureChart()
    }
    
    private fun observeData() {
        // 观察天气数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentWeather.collect { weather ->
                if (weather != null) {
                    val temp = weather.current?.current?.temperature
                    Timber.d("📊 TodayFragment 收到天气数据更新: cityId=$cityId, temperature=$temp")
                    updateWeatherUI(weather)
                } else {
                    Timber.d("📊 TodayFragment 收到天气数据更新: weather=null，显示默认状态")
                    // 显示默认状态
                    binding.ivWeatherIcon.setImageResource(com.dddpeter.app.rainweather.R.drawable.ic_weather_default)
                    binding.tvCurrentTemp.text = "--℃"
                    binding.tvWeatherDesc.text = "晴"
                }
            }
        }
        
        // 观察位置（仅在主页模式下更新城市名称）
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentLocation.collect { location ->
                if (cityId == null) {
                    // 只在主页模式下更新城市名称
                    if (location != null) {
                        Timber.d("📍 TodayFragment 收到位置更新: district=${location.district}, cityId=$cityId")
                        // 更新城市名称显示
                        updateCityName(location)
                    } else {
                        Timber.d("📍 TodayFragment 收到位置更新: location=null")
                        // 显示默认城市名称
                        binding.tvCityName.text = "未知位置"
                    }
                } else {
                    Timber.d("📍 TodayFragment 城市模式，跳过位置更新")
                }
            }
        }
        
        // 观察加载状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }
        
        // 观察错误
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Timber.e("错误: $it")
                    // TODO: 显示错误提示
                }
            }
        }
    }
    
    /**
     * 更新城市名称显示
     */
    private fun updateCityName(location: com.dddpeter.app.rainweather.data.models.LocationModel) {
        val cityName = when {
            !location.district.isNullOrEmpty() -> location.district
            !location.city.isNullOrEmpty() -> location.city
            !location.province.isNullOrEmpty() -> location.province
            else -> "未知位置"
        }
        binding.tvCityName.text = cityName
        Timber.d("🏙️ 更新城市名称: $cityName")
    }
    
    private fun updateWeatherUI(weather: com.dddpeter.app.rainweather.data.models.WeatherModel) {
        val current = weather.current?.current
        
        // 更新当前天气
        if (current != null) {
            // 更新温度和天气描述（参考Flutter设计）
            binding.tvCurrentTemp.text = WeatherIconMapper.formatTemperature(current.temperature)
            
            // 更新天气图标（使用图片版本）
            val isNight = WeatherIconImageMapper.isNightTime()
            Timber.d("🌤️ 更新天气图标: weather=${current.weather}, isNight=$isNight")
            val weatherIconDrawable = WeatherIconImageMapper.getWeatherIconDrawable(requireContext(), current.weather, isNight)
            if (weatherIconDrawable != null) {
                binding.ivWeatherIcon.setImageDrawable(weatherIconDrawable)
                Timber.d("✅ 天气图标设置成功")
            } else {
                Timber.e("❌ 天气图标设置失败，使用默认图标")
                binding.ivWeatherIcon.setImageResource(com.dddpeter.app.rainweather.R.drawable.ic_weather_default)
            }
            
            binding.tvWeatherDesc.text = current.weather ?: "晴"
        } else {
            // 如果天气数据为空，显示默认图标
            Timber.d("🌤️ 天气数据为空，显示默认图标")
            binding.ivWeatherIcon.setImageResource(com.dddpeter.app.rainweather.R.drawable.ic_weather_default)
            binding.tvCurrentTemp.text = "--℃"
            binding.tvWeatherDesc.text = "晴"
        }
        
        // 更新农历日期（如果有）
        weather.current?.nongLi?.let { lunarDate ->
            binding.tvLunarDate.text = lunarDate
            binding.tvLunarDate.visibility = View.VISIBLE
        }
        
        // 更新详细信息卡片
        current?.let {
            binding.tvFeelsLike.text = WeatherIconMapper.formatTemperature(it.feelstemperature)
            binding.tvHumidity.text = WeatherIconMapper.formatHumidity(it.humidity)
            binding.tvWind.text = "${it.winddir ?: ""}${it.windpower ?: ""}"
        }
        
        // 更新AQI
        weather.current?.air?.let {
            binding.tvAqi.text = WeatherIconMapper.getAqiLevel(it.getAqiValue())
        }
        
        // 更新24小时预报（显示前12小时）
        weather.forecast24h?.take(12)?.let { hourlyList ->
            hourlyAdapter.submitList(hourlyList)
        }
        
        // 更新7日温度图表
        weather.forecast15d?.take(7)?.let { forecast7d ->
            updateTemperatureChart(forecast7d)
        }
        
        // 更新日出日落时间线
        val sunrise = weather.forecast15d?.firstOrNull()?.sunriseSunset?.split("|")?.getOrNull(0)
        val sunset = weather.forecast15d?.firstOrNull()?.sunriseSunset?.split("|")?.getOrNull(1)
        if (sunrise != null && sunset != null) {
            // 解析月相信息
            val nongLi = weather.current?.nongLi
            val moonPhase = parseMoonPhase(nongLi)
            val moonAge = parseMoonAge(nongLi)
            
            val sunMoonData = com.dddpeter.app.rainweather.data.models.SunMoonData(
                sunrise = sunrise,
                sunset = sunset,
                moonPhase = moonPhase,
                moonAge = moonAge
            )
            binding.sunMoonTimeline.setSunMoonData(sunMoonData)
        }
        
        // 模拟生活指数数据（实际应该从API获取）
        val mockLifeIndex = listOf(
            com.dddpeter.app.rainweather.data.models.LifeIndex(
                name = "运动指数",
                level = "适宜",
                detail = "适宜户外运动"
            ),
            com.dddpeter.app.rainweather.data.models.LifeIndex(
                name = "穿衣指数",
                level = "舒适",
                detail = "建议穿薄外套"
            ),
            com.dddpeter.app.rainweather.data.models.LifeIndex(
                name = "紫外线指数",
                level = "中等",
                detail = "注意防晒"
            ),
            com.dddpeter.app.rainweather.data.models.LifeIndex(
                name = "洗车指数",
                level = "适宜",
                detail = "适宜洗车"
            ),
            com.dddpeter.app.rainweather.data.models.LifeIndex(
                name = "旅游指数",
                level = "适宜",
                detail = "天气较好"
            ),
            com.dddpeter.app.rainweather.data.models.LifeIndex(
                name = "感冒指数",
                level = "较不宜",
                detail = "注意保暖"
            )
        )
        lifeIndexAdapter.submitList(mockLifeIndex)
    }
    
    private fun setupTemperatureChart() {
        binding.chartTemperature.apply {
            // 基础设置
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            legend.isEnabled = true
            
            // 设置透明背景
            setBackgroundColor(Color.TRANSPARENT)
            
            // 禁用缩放，只允许滑动
            setScaleEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            
            // 启用拖拽（水平滑动）
            isDragEnabled = true
            setDragDecelerationEnabled(true)
            dragDecelerationFrictionCoef = 0.9f
            
            // 图例样式
            legend.apply {
                textColor = requireContext().getColor(android.R.color.darker_gray)
                textSize = 10f // 缩小字体
            }
            
            // X轴设置
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = requireContext().getColor(android.R.color.darker_gray)
                textSize = 10f // 缩小字体
            }
            
            // Y轴左侧设置
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#40808080") // 半透明灰色
                axisMinimum = 0f
                textColor = requireContext().getColor(android.R.color.darker_gray)
                textSize = 10f // 缩小字体
            }
            
            // 禁用右侧Y轴
            axisRight.isEnabled = false
            
            // 设置可见数据范围（一次显示7天）
            setVisibleXRangeMaximum(7f)
        }
    }
    
    private fun updateTemperatureChart(forecast: List<com.dddpeter.app.rainweather.data.models.DailyWeather>) {
        val highTempEntries = ArrayList<Entry>()
        val lowTempEntries = ArrayList<Entry>()
        
        forecast.forEachIndexed { index, weather ->
            highTempEntries.add(Entry(index.toFloat(), weather.getHighTemp().toFloat()))
            lowTempEntries.add(Entry(index.toFloat(), weather.getLowTemp().toFloat()))
        }
        
        val highTempDataSet = LineDataSet(highTempEntries, "最高温度").apply {
            color = Color.parseColor("#FF5722") // 鲜艳的橙红色
            setCircleColor(Color.parseColor("#FF5722"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 9f // 缩小字体
            valueTextColor = Color.parseColor("#D32F2F") // 数值文字颜色
            mode = LineDataSet.Mode.CUBIC_BEZIER
            // 设置数值格式化器，显示整数
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        
        val lowTempDataSet = LineDataSet(lowTempEntries, "最低温度").apply {
            color = Color.parseColor("#42A5F5") // 鲜艳的蓝色
            setCircleColor(Color.parseColor("#42A5F5"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 9f // 缩小字体
            valueTextColor = Color.parseColor("#1976D2") // 数值文字颜色
            mode = LineDataSet.Mode.CUBIC_BEZIER
            // 设置数值格式化器，显示整数
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        
        val lineData = LineData(highTempDataSet, lowTempDataSet)
        binding.chartTemperature.data = lineData
        binding.chartTemperature.invalidate()
    }
    
    fun refresh() {
        cityId?.let {
            viewModel.loadWeatherForCity(it)
        } ?: run {
            viewModel.refreshWithLocation()
        }
    }
    
    /**
     * 解析月相信息
     */
    private fun parseMoonPhase(nongLi: String?): String? {
        if (nongLi.isNullOrBlank()) return null
        
        // 从农历信息中提取月相，例如："2025-10-06  星期一  八月十五"
        val regex = "\\s+([一二三四五六七八九十]+)月([一二三四五六七八九十]+)".toRegex()
        val matchResult = regex.find(nongLi)
        
        if (matchResult != null) {
            val month = matchResult.groupValues[1]
            val day = matchResult.groupValues[2]
            
            // 根据农历日期判断月相
            return when (day) {
                "初一" -> "新月"
                "初二", "初三" -> "峨眉月"
                "初四", "初五", "初六" -> "上弦月"
                "初七", "初八", "初九" -> "上凸月"
                "初十", "十一", "十二" -> "盈凸月"
                "十三", "十四" -> "渐盈凸月"
                "十五", "十六" -> "满月"
                "十七", "十八" -> "渐亏凸月"
                "十九", "二十" -> "亏凸月"
                "二十一", "二十二" -> "下凸月"
                "二十三", "二十四", "二十五" -> "下弦月"
                "二十六", "二十七", "二十八" -> "残月"
                "二十九", "三十" -> "晦月"
                else -> "未知月相"
            }
        }
        
        return null
    }
    
    /**
     * 解析月龄信息
     */
    private fun parseMoonAge(nongLi: String?): String? {
        if (nongLi.isNullOrBlank()) return null
        
        // 从农历信息中提取月龄，例如："2025-10-06  星期一  八月十五"
        val regex = "\\s+([一二三四五六七八九十]+)月([一二三四五六七八九十]+)".toRegex()
        val matchResult = regex.find(nongLi)
        
        if (matchResult != null) {
            val month = matchResult.groupValues[1]
            val day = matchResult.groupValues[2]
            
            // 将中文数字转换为阿拉伯数字
            val dayNumber = chineseToNumber(day)
            if (dayNumber != null) {
                return "${dayNumber}天"
            }
        }
        
        return null
    }
    
    /**
     * 中文数字转阿拉伯数字
     */
    private fun chineseToNumber(chinese: String): Int? {
        val chineseNumbers = mapOf(
            "一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5,
            "六" to 6, "七" to 7, "八" to 8, "九" to 9, "十" to 10,
            "十一" to 11, "十二" to 12, "十三" to 13, "十四" to 14, "十五" to 15,
            "十六" to 16, "十七" to 17, "十八" to 18, "十九" to 19, "二十" to 20,
            "二十一" to 21, "二十二" to 22, "二十三" to 23, "二十四" to 24, "二十五" to 25,
            "二十六" to 26, "二十七" to 27, "二十八" to 28, "二十九" to 29, "三十" to 30
        )
        return chineseNumbers[chinese]
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


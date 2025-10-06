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
import com.dddpeter.app.rainweather.data.models.DailyWeather
import com.dddpeter.app.rainweather.databinding.FragmentForecast15dBinding
import com.dddpeter.app.rainweather.ui.MainActivity
import com.dddpeter.app.rainweather.ui.adapters.DailyWeatherAdapter
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
 * 15日预报Fragment（对应Flutter版本的Forecast15dScreen）
 */
class Forecast15dFragment : Fragment() {
    
    companion object {
        private const val ARG_CITY_ID = "arg_city_id"
        
        fun newInstance(cityId: String? = null): Forecast15dFragment {
            return Forecast15dFragment().apply {
                arguments = Bundle().apply {
                    cityId?.let { putString(ARG_CITY_ID, it) }
                }
            }
        }
    }
    
    private var _binding: FragmentForecast15dBinding? = null
    private val binding get() = _binding!!
    
    private var cityId: String? = null
    
    // ViewModel - 统一使用 Activity 作用域
    private val viewModel: WeatherViewModel by activityViewModels {
        val app = requireActivity().application as com.dddpeter.app.rainweather.RainWeatherApplication
        WeatherViewModelFactory(
            app.database, 
            com.dddpeter.app.rainweather.services.location.LocationManager.getInstance(requireContext())
        )
    }
    
    private lateinit var dailyAdapter: DailyWeatherAdapter
    private var currentForecast: List<DailyWeather> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecast15dBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("📱 Forecast15dFragment: onViewCreated")
        
        cityId = arguments?.getString(ARG_CITY_ID)
        
        setupViews()
        observeData()
        
        // 仅在 MainActivity 中主动加载数据
        // 在 CityWeatherActivity 中，Activity 会统一加载数据
        if (cityId == null) {
            viewModel.initializeWeather()
        }
    }
    
    private fun setupViews() {
        // 设置下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            cityId?.let {
                viewModel.loadWeatherForCity(it)
            } ?: run {
                viewModel.refreshWithLocation()
            }
        }
        
        // 设置图表
        setupTemperatureChart()
        
        // 设置列表RecyclerView
        dailyAdapter = DailyWeatherAdapter()
        binding.rvDailyList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dailyAdapter
        }
    }
    
    private fun observeData() {
        // 观察天气数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentWeather.collect { weather ->
                weather?.let { updateUI(it) }
            }
        }
        
        // 观察位置
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentLocation.collect { location ->
                location?.let {
                    binding.tvCityName.text = it.getCityName()
                }
            }
        }
        
        // 观察加载状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }
    }
    
    private fun updateUI(weather: com.dddpeter.app.rainweather.data.models.WeatherModel) {
        // 更新15日预报
        weather.forecast15d?.let { dailyList ->
            currentForecast = dailyList
            
            // 更新图表
            updateTemperatureChart(dailyList)
            
            // 更新列表
            dailyAdapter.submitList(dailyList)
        }
    }
    
    private fun setupTemperatureChart() {
        binding.chartForecast.apply {
            // 基础设置
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            legend.isEnabled = false
            
            // 设置透明背景
            setBackgroundColor(Color.TRANSPARENT)
            
            // 禁用Y轴缩放，启用X轴拖拽
            setScaleEnabled(false)
            setScaleXEnabled(false)
            setScaleYEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            
            // 启用拖拽（水平滑动）
            isDragEnabled = true
            isDragXEnabled = true
            isDragYEnabled = false
            setDragDecelerationEnabled(true)
            dragDecelerationFrictionCoef = 0.92f
            
            // X轴设置
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textSize = 10f
                granularity = 1f
                textColor = requireContext().getColor(android.R.color.darker_gray)
                setAvoidFirstLastClipping(true)
            }
            
            // 左Y轴设置
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#40808080")
                gridLineWidth = 0.5f
                textSize = 10f
                textColor = requireContext().getColor(android.R.color.darker_gray)
            }
            
            // 右Y轴禁用
            axisRight.isEnabled = false
            
            // 设置边距
            setExtraOffsets(8f, 16f, 8f, 8f)
        }
    }
    
    private fun updateTemperatureChart(dailyList: List<DailyWeather>) {
        if (dailyList.isEmpty()) {
            binding.chartForecast.clear()
            return
        }
        
        currentForecast = dailyList
        
        // 准备数据
        val highTempEntries = mutableListOf<Entry>()
        val lowTempEntries = mutableListOf<Entry>()
        
        dailyList.forEachIndexed { index, weather ->
            val x = index.toFloat()
            highTempEntries.add(Entry(x, weather.getHighTemp().toFloat()))
            lowTempEntries.add(Entry(x, weather.getLowTemp().toFloat()))
        }
        
        // 创建数据集 - 最高温度
        val highTempDataSet = LineDataSet(highTempEntries, "最高温度").apply {
            color = Color.parseColor("#FF5722")
            lineWidth = 2.5f
            setCircleColor(Color.parseColor("#FF5722"))
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = Color.parseColor("#D32F2F")
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            setDrawFilled(true)
            fillColor = Color.parseColor("#FF5722")
            fillAlpha = 60
            // 设置数值格式化器，显示整数
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        
        // 创建数据集 - 最低温度
        val lowTempDataSet = LineDataSet(lowTempEntries, "最低温度").apply {
            color = Color.parseColor("#8edafc")
            lineWidth = 2.5f
            setCircleColor(Color.parseColor("#8edafc"))
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = Color.parseColor("#2196F3")
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            setDrawFilled(true)
            fillColor = Color.parseColor("#8edafc")
            fillAlpha = 60
            // 设置数值格式化器，显示整数
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        
        // 设置数据
        val lineData = LineData(highTempDataSet, lowTempDataSet)
        binding.chartForecast.data = lineData
        
        // 设置X轴标签
        binding.chartForecast.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < currentForecast.size) {
                    // 标签：前3天显示"今天/明天/后天"，之后显示日期
                    when (index) {
                        0 -> "今天"
                        1 -> "明天"
                        2 -> "后天"
                        else -> currentForecast[index].forecasttime ?: "$index"
                    }
                } else {
                    ""
                }
            }
        }
        
        // 设置可见数据范围（在设置数据后）
        binding.chartForecast.apply {
            setVisibleXRangeMaximum(7f)
            setVisibleXRangeMinimum(5f)
            
            // 移动到开始位置
            moveViewToX(0f)
            
            // 刷新图表
            invalidate()
        }
        
        Timber.d("📊 Updated 15-day forecast chart with ${dailyList.size} days")
    }
    
    fun refresh() {
        cityId?.let {
            viewModel.loadWeatherForCity(it)
        } ?: run {
            viewModel.refreshWithLocation()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



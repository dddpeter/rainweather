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
import com.dddpeter.app.rainweather.databinding.FragmentHourlyBinding
import com.dddpeter.app.rainweather.ui.MainActivity
import com.dddpeter.app.rainweather.ui.adapters.HourlyWeatherAdapter
import com.dddpeter.app.rainweather.ui.adapters.HourlyWeatherDetailAdapter
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
 * 24小时预报Fragment（对应Flutter版本的HourlyScreen）
 */
class HourlyFragment : Fragment() {
    
    companion object {
        private const val ARG_CITY_ID = "arg_city_id"
        
        fun newInstance(cityId: String? = null): HourlyFragment {
            return HourlyFragment().apply {
                arguments = Bundle().apply {
                    cityId?.let { putString(ARG_CITY_ID, it) }
                }
            }
        }
    }
    
    private var _binding: FragmentHourlyBinding? = null
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
    
    private lateinit var hourlyAdapter: HourlyWeatherDetailAdapter
    private var currentForecast: List<com.dddpeter.app.rainweather.data.models.HourlyWeather> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHourlyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("📱 HourlyFragment: onViewCreated")
        
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
        
        // 设置列表RecyclerView（逐小时详情）
        hourlyAdapter = HourlyWeatherDetailAdapter()
        binding.rvHourlyList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hourlyAdapter
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
        // 更新24小时预报
        weather.forecast24h?.let { hourlyList ->
            // 更新图表
            updateTemperatureChart(hourlyList)
            
            // 更新逐小时详情列表
            hourlyAdapter.submitList(hourlyList)
        }
    }
    
    private fun setupTemperatureChart() {
        binding.chartHourlyTemperature.apply {
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
            isDragXEnabled = true  // 明确启用X轴拖拽
            isDragYEnabled = false // 禁用Y轴拖拽
            setDragDecelerationEnabled(true)
            dragDecelerationFrictionCoef = 0.92f
            
            // 设置拖拽约束
            setDragOffsetX(0f)
            
            // X轴设置
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = requireContext().getColor(android.R.color.darker_gray)
                textSize = 10f
                setAvoidFirstLastClipping(true)
                // 设置自定义的时间格式化器
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < currentForecast.size) {
                            // 每3小时显示一次时间标签
                            if (index % 3 == 0) {
                                currentForecast[index].getFormattedTime()
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }
                    }
                }
            }
            
            // Y轴左侧设置
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#40808080")
                textColor = requireContext().getColor(android.R.color.darker_gray)
                textSize = 10f
            }
            
            // 禁用右侧Y轴
            axisRight.isEnabled = false
        }
    }
    
    private fun updateTemperatureChart(forecast: List<com.dddpeter.app.rainweather.data.models.HourlyWeather>) {
        // 保存当前数据供X轴格式化器使用
        currentForecast = forecast.take(24)
        
        val entries = ArrayList<Entry>()
        currentForecast.forEachIndexed { index, weather ->
            entries.add(Entry(index.toFloat(), weather.getTemperature().toFloat()))
        }
        
        val dataSet = LineDataSet(entries, "温度").apply {
            color = Color.parseColor("#FF5722")
            setCircleColor(Color.parseColor("#FF5722"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 9f
            valueTextColor = Color.parseColor("#D32F2F")
            mode = LineDataSet.Mode.CUBIC_BEZIER
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
        
        val lineData = LineData(dataSet)
        binding.chartHourlyTemperature.apply {
            data = lineData
            
            // 设置可见数据范围（在设置数据后）
            setVisibleXRangeMaximum(8f)
            setVisibleXRangeMinimum(6f)
            
            // 移动到开始位置
            moveViewToX(0f)
            
            // 刷新图表
            invalidate()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


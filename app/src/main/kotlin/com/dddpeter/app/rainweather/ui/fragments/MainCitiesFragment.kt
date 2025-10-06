package com.dddpeter.app.rainweather.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.data.models.CityInfo
import com.dddpeter.app.rainweather.data.models.CityModel
import com.dddpeter.app.rainweather.data.models.CityWithWeather
import com.dddpeter.app.rainweather.databinding.DialogAddCityBinding
import com.dddpeter.app.rainweather.databinding.FragmentMainCitiesBinding
import com.dddpeter.app.rainweather.ui.CityWeatherActivity
import com.dddpeter.app.rainweather.ui.MainActivity
import com.dddpeter.app.rainweather.ui.adapters.CitySearchAdapter
import com.dddpeter.app.rainweather.ui.adapters.MainCityAdapter
import com.dddpeter.app.rainweather.viewmodels.WeatherViewModel
import com.dddpeter.app.rainweather.viewmodels.WeatherViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 主要城市Fragment（对应Flutter版本的MainCitiesScreen）
 */
class MainCitiesFragment : Fragment() {
    
    private var _binding: FragmentMainCitiesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private lateinit var cityAdapter: MainCityAdapter
    private var currentCities = listOf<CityWithWeather>()
    
    // ViewModel - 统一使用 Activity 作用域
    private val viewModel: WeatherViewModel by activityViewModels {
        val app = requireActivity().application as com.dddpeter.app.rainweather.RainWeatherApplication
        WeatherViewModelFactory(
            app.database, 
            com.dddpeter.app.rainweather.services.location.LocationManager.getInstance(requireContext())
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainCitiesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("📱 MainCitiesFragment: onViewCreated")
        
        database = (requireActivity() as MainActivity).getApp().database
        
        setupViews()
        loadCities()
        
        // 监听天气数据变化，实时更新城市卡片
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentWeather.collect { weather ->
                if (weather != null) {
                    updateCitiesWeather(weather)
                }
            }
        }
    }
    
    private fun setupViews() {
        // 设置城市列表
        cityAdapter = MainCityAdapter(
            onCityClick = { city ->
                // 点击城市项，跳转到该城市的天气详情页面
                CityWeatherActivity.start(requireContext(), city)
            },
            onDeleteClick = { city ->
                showDeleteConfirmDialog(city)
            },
            onOrderChanged = { newOrder ->
                updateCityOrder(newOrder)
            }
        )
        
        binding.rvCities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cityAdapter
            cityAdapter.attachToRecyclerView(this)
        }
    }
    
    /**
     * 添加城市（从菜单调用）
     */
    fun onAddCityClick() {
        showAddCityDialog()
    }
    
    /**
     * 刷新城市数据
     */
    fun refresh() {
        Timber.d("🔄 MainCitiesFragment: 刷新城市数据")
        loadCities()
    }
    
    private fun loadCities() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 获取当前定位城市
                val currentLocation = getCurrentLocationCity()
                
                // 获取已添加的城市
                val addedCities = database.mainCityDao().getAllMainCities()
                
                // 构建默认城市列表：当前定位城市、北京、上海
                val defaultCities = buildDefaultCities(currentLocation)
                
                // 合并并去重
                val allCities = mergeAndDeduplicateCities(defaultCities, addedCities)
                
                // 获取当前天气数据（从ViewModel获取，与今日天气页面共享）
                val currentWeather = viewModel.currentWeather.value
                
                // 创建CityWithWeather对象，为每个城市获取对应的天气数据
                val citiesWithWeather = allCities.map { city ->
                    val weatherData = if (city.isCurrentLocation) {
                        // 当前位置使用当前天气数据
                        currentWeather
                    } else {
                        // 其他城市获取各自的天气数据
                        getWeatherDataForCity(city.id)
                    }
                    
                    CityWithWeather(
                        city = city,
                        temperature = weatherData?.current?.current?.temperature,
                        weather = weatherData?.current?.current?.weather,
                        weatherIcon = weatherData?.current?.current?.weather?.let { 
                            com.dddpeter.app.rainweather.data.models.AppConstants.WEATHER_ICONS[it] ?: "☀️"
                        }
                    )
                }
                
                currentCities = citiesWithWeather
                cityAdapter.submitList(citiesWithWeather)
                
                // 更新UI状态
                updateUIState(citiesWithWeather)
                
                Timber.d("✅ 加载了 ${citiesWithWeather.size} 个城市")
            } catch (e: Exception) {
                Timber.e(e, "❌ 加载城市失败")
                Toast.makeText(requireContext(), "加载城市失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 更新城市卡片的天气信息（只更新当前定位城市）
     */
    private fun updateCitiesWeather(weather: com.dddpeter.app.rainweather.data.models.WeatherModel) {
        if (currentCities.isNotEmpty()) {
            val updatedCities = currentCities.map { cityWithWeather ->
                if (cityWithWeather.city.isCurrentLocation) {
                    // 只更新当前定位城市的天气信息
                    cityWithWeather.copy(
                        temperature = weather.current?.current?.temperature,
                        weather = weather.current?.current?.weather,
                        weatherIcon = weather.current?.current?.weather?.let { 
                            com.dddpeter.app.rainweather.data.models.AppConstants.WEATHER_ICONS[it] ?: "☀️"
                        }
                    )
                } else {
                    // 其他城市保持不变
                    cityWithWeather
                }
            }
            currentCities = updatedCities
            cityAdapter.submitList(updatedCities)
            Timber.d("🔄 更新了当前定位城市的天气信息")
        }
    }
    
    /**
     * 获取指定城市的天气数据
     */
    private suspend fun getWeatherDataForCity(cityId: String): com.dddpeter.app.rainweather.data.models.WeatherModel? {
        return try {
            val result = com.dddpeter.app.rainweather.data.repository.WeatherRepository.getInstance(database)
                .getWeatherData(cityId, forceRefresh = false)
            result.getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "❌ 获取城市 $cityId 天气数据失败")
            null
        }
    }
    
    /**
     * 获取当前定位城市
     */
    private suspend fun getCurrentLocationCity(): CityModel? {
        return try {
            // 从ViewModel获取当前位置
            val location = viewModel.currentLocation.value
            if (location != null) {
                CityModel(
                    id = location.adcode.ifEmpty { "unknown" },
                    name = location.district.ifEmpty { location.city.ifEmpty { location.province } },
                    sortOrder = 0, // 默认排序，可以拖拽
                    isCurrentLocation = true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ 获取当前定位城市失败")
            null
        }
    }
    
    /**
     * 构建默认城市列表
     */
    private fun buildDefaultCities(currentLocation: CityModel?): List<CityModel> {
        val cities = mutableListOf<CityModel>()
        
        // 1. 当前定位城市（可以拖拽排序）
        currentLocation?.let { 
            cities.add(it.copy(sortOrder = 0))
        }
        
        // 2. 北京
        val beijing = CityModel(
            id = "101010100",
            name = "北京",
            sortOrder = 1,
            isCurrentLocation = false
        )
        cities.add(beijing)
        
        // 3. 上海
        val shanghai = CityModel(
            id = "101020100", 
            name = "上海",
            sortOrder = 2,
            isCurrentLocation = false
        )
        cities.add(shanghai)
        
        return cities
    }
    
    /**
     * 合并并去重城市列表
     */
    private fun mergeAndDeduplicateCities(
        defaultCities: List<CityModel>,
        addedCities: List<CityModel>
    ): List<CityModel> {
        val result = mutableListOf<CityModel>()
        val addedCityIds = mutableSetOf<String>()
        
        // 1. 添加默认城市
        defaultCities.forEach { city ->
            result.add(city)
            addedCityIds.add(city.id)
        }
        
        // 2. 添加用户添加的城市（排除重复的）
        addedCities.forEach { city ->
            if (!addedCityIds.contains(city.id)) {
                // 调整排序顺序
                val adjustedCity = city.copy(sortOrder = result.size)
                result.add(adjustedCity)
                addedCityIds.add(city.id)
            }
        }
        
        // 3. 按排序顺序排序
        return result.sortedBy { it.sortOrder }
    }
    
    private fun updateUIState(cities: List<CityWithWeather>) {
        if (cities.isEmpty()) {
            binding.rvCities.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.tvCityCount.text = "0个城市"
        } else {
            binding.rvCities.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            binding.tvCityCount.text = "${cities.size}个城市"
        }
    }
    
    private fun showAddCityDialog() {
        val dialogBinding = DialogAddCityBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        // 设置搜索适配器
        val searchAdapter = CitySearchAdapter { cityInfo ->
            addCity(cityInfo)
            dialog.dismiss()
        }
        
        dialogBinding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
        
        // 搜索功能
        dialogBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchCities(query, searchAdapter, dialogBinding)
            }
        })
        
        // 取消按钮
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun searchCities(query: String, adapter: CitySearchAdapter, dialogBinding: DialogAddCityBinding) {
        if (query.isEmpty()) {
            adapter.submitList(emptyList())
            dialogBinding.rvSearchResults.visibility = View.GONE
            dialogBinding.layoutEmpty.visibility = View.VISIBLE
            dialogBinding.tvEmptyMessage.text = "输入城市名称搜索"
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = database.cityDao().searchCitiesByName("%$query%")
                if (results.isEmpty()) {
                    adapter.submitList(emptyList())
                    dialogBinding.rvSearchResults.visibility = View.GONE
                    dialogBinding.layoutEmpty.visibility = View.VISIBLE
                    dialogBinding.tvEmptyMessage.text = "未找到匹配的城市"
                } else {
                    adapter.submitList(results)
                    dialogBinding.rvSearchResults.visibility = View.VISIBLE
                    dialogBinding.layoutEmpty.visibility = View.GONE
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ 搜索城市失败")
            }
        }
    }
    
    private fun addCity(cityInfo: CityInfo) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 检查是否已存在
                val existingCity = database.mainCityDao().getMainCityById(cityInfo.id)
                if (existingCity != null) {
                    Toast.makeText(requireContext(), "${cityInfo.name}已在列表中", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // 获取当前最大排序值
                val maxOrder = database.mainCityDao().getMaxSortOrder() ?: -1
                
                // 创建新城市
                val newCity = cityInfo.toCityModel(sortOrder = maxOrder + 1)
                database.mainCityDao().insertMainCity(newCity)
                
                Toast.makeText(requireContext(), "已添加${cityInfo.name}", Toast.LENGTH_SHORT).show()
                
                // 重新加载列表
                loadCities()
                
                Timber.d("✅ 添加城市: ${cityInfo.name}")
            } catch (e: Exception) {
                Timber.e(e, "❌ 添加城市失败")
                Toast.makeText(requireContext(), "添加城市失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmDialog(city: CityModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除城市")
            .setMessage("确定要删除${city.name}吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteCity(city)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteCity(city: CityModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                database.mainCityDao().deleteMainCity(city.id)
                
                Toast.makeText(requireContext(), "已删除${city.name}", Toast.LENGTH_SHORT).show()
                
                // 重新加载列表
                loadCities()
                
                Timber.d("✅ 删除城市: ${city.name}")
            } catch (e: Exception) {
                Timber.e(e, "❌ 删除城市失败")
                Toast.makeText(requireContext(), "删除城市失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateCityOrder(newOrder: List<CityWithWeather>) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 更新每个城市的sortOrder
                newOrder.forEachIndexed { index, cityWithWeather ->
                    database.mainCityDao().updateSortOrder(cityWithWeather.city.id, index)
                }
                
                Timber.d("✅ 更新城市排序")
            } catch (e: Exception) {
                Timber.e(e, "❌ 更新城市排序失败")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


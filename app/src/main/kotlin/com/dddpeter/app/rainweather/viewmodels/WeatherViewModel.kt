package com.dddpeter.app.rainweather.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.data.models.LocationModel
import com.dddpeter.app.rainweather.data.models.WeatherModel
import com.dddpeter.app.rainweather.data.repository.WeatherRepository
import com.dddpeter.app.rainweather.services.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 天气ViewModel（对应Flutter版本的WeatherProvider）
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationManager: LocationManager,
    private val database: AppDatabase
) : ViewModel() {
    
    // 当前天气数据
    private val _currentWeather = MutableStateFlow<WeatherModel?>(null)
    val currentWeather: StateFlow<WeatherModel?> = _currentWeather.asStateFlow()
    
    // 当前位置
    private val _currentLocation = MutableStateFlow<LocationModel?>(null)
    val currentLocation: StateFlow<LocationModel?> = _currentLocation.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        Timber.d("🎬 WeatherViewModel: 初始化")
        // 不在初始化时自动加载数据，由调用方决定何时加载
        // initializeWeather()
    }
    
    /**
     * 初始化天气数据（仅在主页需要时调用）
     */
    fun initializeWeather() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 初始化定位服务
                locationManager.initialize()
                
                // 尝试获取缓存位置
                var location = locationManager.getCachedLocation()
                
                if (location == null) {
                    // 如果没有缓存，使用默认位置
                    Timber.d("📍 没有缓存位置，使用默认位置")
                    location = LocationModel.createDefault()
                    locationManager.setCachedLocation(location)
                }
                
                _currentLocation.value = location
                
                // 加载天气数据
                loadWeatherData(location, forceRefresh = false)
                
            } catch (e: Exception) {
                Timber.e(e, "❌ 初始化天气数据失败")
                _error.value = "初始化失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 加载天气数据
     */
    private suspend fun loadWeatherData(location: LocationModel, forceRefresh: Boolean = false) {
        try {
            val result = weatherRepository.getWeatherDataForLocation(location, forceRefresh)
            
            result.onSuccess { weatherModel ->
                _currentWeather.value = weatherModel
                Timber.d("✅ 天气数据加载成功: ${location.district}")
            }.onFailure { exception ->
                _error.value = "加载天气数据失败: ${exception.message}"
                Timber.e(exception, "❌ 加载天气数据失败")
            }
            
        } catch (e: Exception) {
            _error.value = "加载天气数据异常: ${e.message}"
            Timber.e(e, "❌ 加载天气数据异常")
        }
    }
    
    /**
     * 刷新天气数据（包含定位）
     */
    fun refreshWithLocation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Timber.d("🔄 刷新天气数据（含定位）")
                
                // 获取当前位置
                val location = locationManager.getCurrentLocation(forceRefresh = true)
                
                if (location != null) {
                    _currentLocation.value = location
                    loadWeatherData(location, forceRefresh = true)
                } else {
                    _error.value = "定位失败"
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ 刷新失败")
                _error.value = "刷新失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 仅刷新天气数据（不重新定位）
     */
    fun refreshWeatherOnly() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val location = _currentLocation.value
                if (location != null) {
                    loadWeatherData(location, forceRefresh = true)
                } else {
                    _error.value = "位置信息不可用"
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ 刷新天气失败")
                _error.value = "刷新失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 按城市ID获取天气数据（用于城市详情页面）
     */
    fun loadWeatherForCity(cityId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Timber.d("🏙️ WeatherViewModel: 开始加载城市天气数据, cityId=$cityId, forceRefresh=$forceRefresh")
                
                // 从数据库获取城市信息
                val city = database.mainCityDao().getMainCityById(cityId)
                Timber.d("📍 从数据库获取城市信息: city=${city?.name}, id=${city?.id}")
                
                if (city != null) {
                    // 创建位置信息（使用城市名称作为 district）
                    val location = LocationModel(
                        lat = 0.0,  // 城市模式不需要精确经纬度
                        lng = 0.0,
                        district = city.name
                    )
                    _currentLocation.value = location
                    Timber.d("📍 更新位置信息: district=${location.district}")
                } else {
                    Timber.e("❌ 数据库中未找到城市信息: cityId=$cityId")
                }
                
                Timber.d("🌐 开始从 API 加载天气数据: cityId=$cityId")
                val result = weatherRepository.getWeatherData(cityId, forceRefresh)
                
                result.onSuccess { weatherModel ->
                    _currentWeather.value = weatherModel
                    val temp = weatherModel.current?.current?.temperature
                    Timber.d("✅ 城市天气数据加载成功: cityId=$cityId, temperature=$temp")
                }.onFailure { exception ->
                    _error.value = "加载城市天气数据失败: ${exception.message}"
                    Timber.e(exception, "❌ 加载城市天气数据失败: cityId=$cityId")
                }
                
            } catch (e: Exception) {
                _error.value = "加载城市天气数据异常: ${e.message}"
                Timber.e(e, "❌ 加载城市天气数据异常: cityId=$cityId")
            } finally {
                _isLoading.value = false
                Timber.d("🏁 城市天气数据加载完成: cityId=$cityId")
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        Timber.d("🗑️ WeatherViewModel: 清理资源")
    }
}

/**
 * ViewModel Factory
 */
class WeatherViewModelFactory(
    private val database: AppDatabase,
    private val locationManager: LocationManager
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            val weatherRepository = WeatherRepository.getInstance(database)
            return WeatherViewModel(weatherRepository, locationManager, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


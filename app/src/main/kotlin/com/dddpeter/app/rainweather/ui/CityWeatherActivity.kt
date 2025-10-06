package com.dddpeter.app.rainweather.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dddpeter.app.rainweather.RainWeatherApplication
import com.dddpeter.app.rainweather.data.models.CityModel
import com.dddpeter.app.rainweather.databinding.ActivityCityWeatherBinding
import com.dddpeter.app.rainweather.services.location.LocationManager
import com.dddpeter.app.rainweather.ui.fragments.Forecast15dFragment
import com.dddpeter.app.rainweather.ui.fragments.HourlyFragment
import com.dddpeter.app.rainweather.ui.fragments.TodayFragment
import com.dddpeter.app.rainweather.viewmodels.WeatherViewModel
import com.dddpeter.app.rainweather.viewmodels.WeatherViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

/**
 * 城市天气详情Activity
 * 显示选中城市的完整天气信息，包括今日、24小时、15日预报
 */
class CityWeatherActivity : AppCompatActivity() {
    
    companion object {
        private const val EXTRA_CITY = "extra_city"
        
        /**
         * 启动城市天气详情页
         */
        fun start(context: Context, city: CityModel) {
            val intent = Intent(context, CityWeatherActivity::class.java).apply {
                putExtra(EXTRA_CITY, city)
            }
            context.startActivity(intent)
        }
    }
    
    private lateinit var binding: ActivityCityWeatherBinding
    private lateinit var city: CityModel
    
    // 共享的 ViewModel，供所有 Fragment 使用
    private val viewModel: WeatherViewModel by viewModels {
        Timber.d("🎬 CityWeatherActivity: 创建 ViewModel")
        WeatherViewModelFactory(
            (application as RainWeatherApplication).database,
            LocationManager.getInstance(this)
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("🎬 CityWeatherActivity: onCreate 开始")
        
        binding = ActivityCityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传递的城市信息
        city = intent.getParcelableExtra<CityModel>(EXTRA_CITY) ?: run {
            Timber.e("❌ 未传递城市信息")
            finish()
            return
        }
        
        Timber.d("🏙️ CityWeatherActivity: 接收到城市信息 - id=${city.id}, name=${city.name}")
        
        setupViews()
        setupViewPager()
        
        Timber.d("🎬 CityWeatherActivity: onCreate 完成")
    }
    
    private fun setupViews() {
        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = city.name
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        
        // 返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupViewPager() {
        // 创建适配器
        val adapter = CityWeatherPagerAdapter(this, city)
        binding.viewPager.adapter = adapter
        
        // 设置TabLayout
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "今日"
                1 -> "24小时"
                2 -> "15日"
                else -> "未知"
            }
        }.attach()
        
        // 设置ViewPager预加载Fragment数量
        binding.viewPager.offscreenPageLimit = 2
        
        // 在 Activity 中统一加载城市天气数据，Fragment 只负责观察和显示
        Timber.d("🌤️ CityWeatherActivity 开始加载城市天气数据: cityId=${city.id}, cityName=${city.name}")
        viewModel.loadWeatherForCity(city.id, forceRefresh = true)
    }
    
    /**
     * ViewPager适配器
     */
    private class CityWeatherPagerAdapter(
        private val activity: CityWeatherActivity,
        private val city: CityModel
    ) : FragmentStateAdapter(activity) {
        
        override fun getItemCount(): Int = 3
        
        override fun createFragment(position: Int) = when (position) {
            0 -> TodayFragment.newInstance(city.id)
            1 -> HourlyFragment.newInstance(city.id)
            2 -> Forecast15dFragment.newInstance(city.id)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

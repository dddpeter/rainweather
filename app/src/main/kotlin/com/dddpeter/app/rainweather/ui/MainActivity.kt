package com.dddpeter.app.rainweather.ui

import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dddpeter.app.rainweather.R
import com.dddpeter.app.rainweather.RainWeatherApplication
import com.dddpeter.app.rainweather.databinding.ActivityMainBinding
import com.dddpeter.app.rainweather.ui.dialogs.PermissionDialogs
import com.dddpeter.app.rainweather.ui.fragments.Forecast15dFragment
import com.dddpeter.app.rainweather.ui.fragments.HourlyFragment
import com.dddpeter.app.rainweather.ui.fragments.MainCitiesFragment
import com.dddpeter.app.rainweather.ui.fragments.TodayFragment
import com.dddpeter.app.rainweather.ui.dialogs.ThemeDialog
import com.dddpeter.app.rainweather.utils.PermissionManager
import com.dddpeter.app.rainweather.utils.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 主Activity（对应Flutter版本的MainScreen）
 * 包含4个Tab：今日天气、24小时、15日预报、主要城市
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null
    private var currentTabId: Int = R.id.navigation_today
    
    // Fragment缓存
    private val fragmentCache = mutableMapOf<Int, Fragment>()
    
    // 权限检查完成标志
    private var isPermissionCheckComplete = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装SplashScreen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // 初始化主题管理器
        ThemeManager.init(this)
        
        // ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Timber.d("🎬 MainActivity: onCreate")
        
        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // 设置Toolbar菜单点击事件
        setupToolbarMenu()
        
        // 初始化底部导航
        setupBottomNavigation()
        
        // 恢复状态
        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt(KEY_CURRENT_TAB, R.id.navigation_today)
            Timber.d("📦 恢复状态: tabId=$currentTabId")
            // 恢复Fragment
            currentFragment = supportFragmentManager.findFragmentByTag(getFragmentTag(currentTabId))
        }
        
        // 设置菜单（无论是否首次启动）
        // 延迟一点时间确保Toolbar完全初始化
        binding.toolbar.post {
            updateToolbarMenu(currentTabId)
        }
        
        // 在SplashScreen期间检查权限
        setupSplashScreenWithPermission(splashScreen)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB, currentTabId)
    }
    
    /**
     * 设置SplashScreen期间的权限申请
     */
    private fun setupSplashScreenWithPermission(splashScreen: androidx.core.splashscreen.SplashScreen) {
        // 设置SplashScreen保持显示直到权限检查完成
        splashScreen.setKeepOnScreenCondition {
            // 如果权限检查还没有完成，保持SplashScreen显示
            !isPermissionCheckComplete
        }
        
        // 在SplashScreen期间检查权限
        lifecycleScope.launch {
            // 稍微延迟一下，让SplashScreen动画开始
            delay(200)
            checkAndRequestPermissions()
            
            // 备用机制：如果3秒后权限检查还没有完成，强制完成
            delay(3000)
            if (!isPermissionCheckComplete) {
                Timber.w("⚠️ 权限检查超时，强制完成")
                isPermissionCheckComplete = true
                loadFragmentAndHideSplash(R.id.navigation_today)
            }
        }
    }
    
    /**
     * 设置Toolbar菜单
     */
    private fun setupToolbarMenu() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_refresh -> {
                    // 刷新当前Fragment的数据
                    val fragment = currentFragment
                    when (fragment) {
                        is TodayFragment -> fragment.refresh()
                        is MainCitiesFragment -> fragment.refresh()
                        else -> {
                            // 其他Fragment的刷新逻辑
                            Toast.makeText(this, "刷新功能开发中", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                R.id.action_add_city -> {
                    // 通知MainCitiesFragment添加城市
                    (currentFragment as? MainCitiesFragment)?.onAddCityClick()
                    true
                }
                R.id.action_theme -> {
                    // 显示主题选择对话框
                    ThemeDialog().show(supportFragmentManager, "ThemeDialog")
                    true
                }
                R.id.action_settings -> {
                    // TODO: 打开设置页面
                    Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_about -> {
                    showAbout()
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * 更新Toolbar菜单
     */
    private fun updateToolbarMenu(tabId: Int) {
        Timber.d("🔄 更新Toolbar菜单: tabId=$tabId")
        binding.toolbar.menu.clear()
        val menuRes = when (tabId) {
            R.id.navigation_today -> R.menu.menu_today
            R.id.navigation_main_cities -> R.menu.menu_main_cities
            else -> R.menu.menu_common
        }
        Timber.d("📋 加载菜单资源: menuRes=$menuRes")
        binding.toolbar.inflateMenu(menuRes)
        Timber.d("✅ 菜单加载完成，菜单项数量: ${binding.toolbar.menu.size()}")
    }
    
    /**
     * 设置底部导航
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_today,
                R.id.navigation_hourly,
                R.id.navigation_forecast15d,
                R.id.navigation_main_cities -> {
                    if (currentTabId != item.itemId) {
                        loadFragment(item.itemId)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
        
        // 设置默认选中项
        binding.bottomNavigation.selectedItemId = currentTabId
    }
    
    
    /**
     * 加载Fragment
     */
    private fun loadFragment(tabId: Int) {
        Timber.d("🔄 加载Fragment: tabId=$tabId")
        
        // 获取或创建Fragment
        val fragment = fragmentCache[tabId] ?: createFragment(tabId).also {
            fragmentCache[tabId] = it
        }
        
        // 切换Fragment
        val transaction = supportFragmentManager.beginTransaction()
        
        // 隐藏当前Fragment
        currentFragment?.let { transaction.hide(it) }
        
        // 显示新Fragment
        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.nav_host_fragment, fragment, getFragmentTag(tabId))
        }
        
        transaction.commitNowAllowingStateLoss()
        
        currentFragment = fragment
        currentTabId = tabId
        
        // 更新Toolbar菜单
        updateToolbarMenu(tabId)
        
        // 根据页面类型刷新数据
        when (tabId) {
            R.id.navigation_today -> {
                (fragment as? TodayFragment)?.refresh()
                Timber.d("🔄 切换到今日天气页面，刷新定位和天气数据")
            }
            R.id.navigation_main_cities -> {
                (fragment as? MainCitiesFragment)?.refresh()
                Timber.d("🔄 切换到主要城市页面，刷新数据")
            }
            R.id.navigation_hourly -> {
                (fragment as? HourlyFragment)?.refresh()
                Timber.d("🔄 切换到24小时页面，刷新数据")
            }
            R.id.navigation_forecast15d -> {
                (fragment as? Forecast15dFragment)?.refresh()
                Timber.d("🔄 切换到15日预报页面，刷新数据")
            }
        }
    }
    
    /**
     * 创建Fragment
     */
    private fun createFragment(tabId: Int): Fragment {
        return when (tabId) {
            R.id.navigation_today -> TodayFragment()
            R.id.navigation_hourly -> HourlyFragment()
            R.id.navigation_forecast15d -> Forecast15dFragment()
            R.id.navigation_main_cities -> MainCitiesFragment()
            else -> throw IllegalArgumentException("Unknown tab id: $tabId")
        }
    }
    
    /**
     * 获取Fragment Tag
     */
    private fun getFragmentTag(tabId: Int): String {
        return "fragment_$tabId"
    }
    
    
    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermissions() {
        Timber.d("🔐 检查权限状态")
        
        // 检查定位权限
        val permissionStatus = PermissionManager.getLocationPermissionStatus(this)
        Timber.d("🔐 权限状态: $permissionStatus")
        
        when (permissionStatus) {
            PermissionManager.PermissionStatus.GRANTED -> {
                Timber.d("✅ 定位权限已授予")
                checkLocationService()
                // 权限已授予，可以加载Fragment
                loadFragmentAndHideSplash(R.id.navigation_today)
                isPermissionCheckComplete = true
            }
            PermissionManager.PermissionStatus.DENIED -> {
                Timber.d("⚠️ 定位权限未授予，直接请求权限")
                // 直接请求权限，不显示说明对话框
                PermissionManager.requestLocationPermission(this)
                // 权限请求后，立即完成检查（权限结果会在onRequestPermissionsResult中处理）
                isPermissionCheckComplete = true
                loadFragmentAndHideSplash(R.id.navigation_today)
            }
            PermissionManager.PermissionStatus.PERMANENTLY_DENIED -> {
                Timber.w("❌ 定位权限被永久拒绝，使用默认位置")
                // 权限被永久拒绝，直接使用默认位置
                loadFragmentAndHideSplash(R.id.navigation_today)
                isPermissionCheckComplete = true
            }
        }
    }
    
    /**
     * 加载Fragment并隐藏SplashScreen
     */
    private fun loadFragmentAndHideSplash(tabId: Int) {
        loadFragment(tabId)
        // Fragment加载完成后，SplashScreen会自动隐藏（因为currentFragment不再为null）
        
        // 不在这里刷新主要城市数据，等到用户切换到主要城市页面时再刷新
        // refreshMainCitiesData()
    }
    
    /**
     * 刷新主要城市数据
     */
    private fun refreshMainCitiesData() {
        // 确保MainCitiesFragment被创建
        val mainCitiesFragment = fragmentCache[R.id.navigation_main_cities] as? MainCitiesFragment
            ?: (createFragment(R.id.navigation_main_cities) as MainCitiesFragment).also {
                fragmentCache[R.id.navigation_main_cities] = it
            }
        
        // 检查Fragment是否已经创建View
        if (mainCitiesFragment.isAdded && mainCitiesFragment.view != null) {
            mainCitiesFragment.refresh()
            Timber.d("🔄 刷新主要城市数据")
        } else {
            // 如果Fragment还没有创建View，延迟刷新
            Timber.d("🔄 MainCitiesFragment还未创建View，延迟刷新")
            mainCitiesFragment.view?.post {
                if (mainCitiesFragment.isAdded && mainCitiesFragment.view != null) {
                    mainCitiesFragment.refresh()
                    Timber.d("🔄 延迟刷新主要城市数据")
                }
            }
        }
    }
    
    /**
     * 显示定位权限说明对话框
     */
    private fun showLocationPermissionRationale() {
        Timber.d("🔐 准备显示权限说明对话框")
        
        // 确保在UI线程中显示对话框
        runOnUiThread {
            try {
                PermissionDialogs.showLocationPermissionRationale(
                    context = this,
                    onAllow = {
                        Timber.d("👤 用户同意授予定位权限")
                        PermissionManager.requestLocationPermission(this)
                    },
                    onDeny = {
                        Timber.d("👤 用户拒绝授予定位权限")
                        Toast.makeText(
                            this,
                            R.string.permission_location_denied_message,
                            Toast.LENGTH_SHORT
                        ).show()
                        // 即使拒绝权限，也加载Fragment（使用默认位置）
                        loadFragmentAndHideSplash(R.id.navigation_today)
                        isPermissionCheckComplete = true
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "❌ 显示权限对话框失败")
                // 如果对话框显示失败，直接加载Fragment
                loadFragmentAndHideSplash(R.id.navigation_today)
                isPermissionCheckComplete = true
            }
        }
    }
    
    /**
     * 显示定位权限被永久拒绝的对话框
     */
    private fun showLocationPermissionPermanentlyDeniedDialog() {
        Timber.d("🔐 准备显示永久拒绝权限对话框")
        
        // 确保在UI线程中显示对话框
        runOnUiThread {
            try {
                PermissionDialogs.showLocationPermissionPermanentlyDeniedDialog(
                    context = this,
                    onOpenSettings = {
                        Timber.d("👤 用户选择去设置页面")
                        PermissionManager.openAppSettings(this)
                    },
                    onCancel = {
                        Timber.d("👤 用户取消，使用默认位置")
                        Toast.makeText(
                            this,
                            "将使用默认位置（北京）",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 使用默认位置，加载Fragment
                        loadFragmentAndHideSplash(R.id.navigation_today)
                        isPermissionCheckComplete = true
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "❌ 显示永久拒绝权限对话框失败")
                // 如果对话框显示失败，直接加载Fragment
                loadFragmentAndHideSplash(R.id.navigation_today)
                isPermissionCheckComplete = true
            }
        }
    }
    
    /**
     * 检查位置服务是否开启
     */
    private fun checkLocationService() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        if (!isGpsEnabled && !isNetworkEnabled) {
            Timber.w("⚠️ 位置服务未开启")
            // 可以选择显示对话框提示用户开启位置服务
            // PermissionDialogs.showLocationServiceDisabledDialog(...)
        } else {
            Timber.d("✅ 位置服务已开启")
        }
    }
    
    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        PermissionManager.handleLocationPermissionResult(
            requestCode = requestCode,
            permissions = permissions,
            grantResults = grantResults,
            onGranted = {
                Timber.d("✅ 权限授予成功")
                Toast.makeText(this, R.string.permission_toast_granted, Toast.LENGTH_SHORT).show()
                checkLocationService()
                
                // 权限授予后，触发数据刷新
                (currentFragment as? TodayFragment)?.refresh()
            },
            onDenied = {
                Timber.w("❌ 权限被拒绝")
                Toast.makeText(this, R.string.permission_toast_denied, Toast.LENGTH_SHORT).show()
                // 权限被拒绝，使用默认位置，不需要额外处理
            },
            onPermanentlyDenied = {
                Timber.w("⚠️ 权限被永久拒绝")
                Toast.makeText(this, "权限被永久拒绝，将使用默认位置", Toast.LENGTH_SHORT).show()
                // 权限被永久拒绝，使用默认位置，不需要额外处理
            }
        )
    }
    
    /**
     * 从设置返回时重新检查权限
     */
    override fun onResume() {
        super.onResume()
        
        // 检查权限状态是否有变化
        if (PermissionManager.isLocationPermissionGranted(this)) {
            Timber.d("✅ 检测到定位权限已授予")
        }
    }
    
    /**
     * 显示关于页面
     */
    private fun showAbout() {
        Toast.makeText(this, "RainWeather v2.0.0\n基于 Flutter 版本重构", Toast.LENGTH_LONG).show()
    }
    
    /**
     * 获取Application实例
     */
    fun getApp(): RainWeatherApplication {
        return application as RainWeatherApplication
    }
    
    companion object {
        private const val KEY_CURRENT_TAB = "current_tab"
    }
}


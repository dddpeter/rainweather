# Android原生版本重新设计方案

基于 Flutter 版本（rainweather_flutter v1.5.0）的完整重新设计

## 📋 设计目标

根据 Flutter 版本完全重新设计 Android 原生应用，确保：
- ✅ 页面风格一致（Material Design 3）
- ✅ 页面逻辑一致（4个Tab页面 + 详情页）
- ✅ 定位逻辑一致（百度定位 > GPS > IP定位）
- ✅ 功能特性一致（主题切换、城市管理、图表展示等）

## 🏗️ 技术架构

### 架构模式
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** (数据仓库模式)
- **Single Activity** (单Activity多Fragment)

### 核心技术栈
- **Language**: Kotlin (100%)
- **UI**: Material Design 3 (Material3库)
- **Network**: Retrofit2 + OkHttp4
- **Database**: Room (SQLite抽象层)
- **Image Loading**: Glide
- **Charts**: MPAndroidChart
- **Dependency Injection**: Hilt (可选)
- **Coroutines**: Kotlin Coroutines
- **Location**: Baidu Location SDK 9.7+
- **JSON**: Gson / Moshi

## 📦 项目结构

```
app/src/main/java/com/dddpeter/app/rainweather/
├── RainWeatherApplication.kt          # Application类
├── ui/
│   ├── MainActivity.kt                 # 主Activity（包含BottomNavigation）
│   ├── theme/                          # 主题系统
│   │   ├── AppTheme.kt                # 主题配置
│   │   ├── Color.kt                   # 颜色定义
│   │   ├── Theme.kt                   # Material3主题
│   │   └── ThemeManager.kt            # 主题管理器
│   ├── fragments/
│   │   ├── TodayFragment.kt           # 今日天气
│   │   ├── HourlyFragment.kt          # 24小时预报
│   │   ├── Forecast15dFragment.kt     # 15日预报
│   │   └── MainCitiesFragment.kt      # 主要城市
│   ├── activities/
│   │   ├── CityWeatherActivity.kt     # 城市天气详情
│   │   └── SplashActivity.kt          # 启动页
│   └── widgets/                       # 自定义组件
│       ├── WeatherChartView.kt        # 温度图表
│       ├── LifeIndexCard.kt           # 生活指数卡片
│       └── SunMoonCard.kt             # 日出日落卡片
├── data/
│   ├── models/                         # 数据模型
│   │   ├── WeatherModel.kt
│   │   ├── LocationModel.kt
│   │   ├── CityModel.kt
│   │   └── SunMoonIndexModel.kt
│   ├── repository/                     # 数据仓库
│   │   ├── WeatherRepository.kt
│   │   ├── LocationRepository.kt
│   │   └── CityRepository.kt
│   ├── local/                          # 本地数据
│   │   ├── database/
│   │   │   ├── AppDatabase.kt         # Room数据库
│   │   │   ├── WeatherDao.kt
│   │   │   ├── LocationDao.kt
│   │   │   └── CityDao.kt
│   │   └── prefs/
│   │       └── PreferenceManager.kt   # SharedPreferences
│   └── remote/                         # 网络数据
│       ├── api/
│       │   └── WeatherApiService.kt   # Retrofit接口
│       └── dto/                        # 网络数据传输对象
│           └── WeatherResponse.kt
├── domain/                             # 业务逻辑层
│   ├── usecases/
│   │   ├── GetWeatherUseCase.kt
│   │   ├── GetLocationUseCase.kt
│   │   └── ManageCitiesUseCase.kt
│   └── utils/
│       ├── WeatherIconMapper.kt       # 天气图标映射
│       └── DateTimeUtils.kt
└── services/                           # 服务层
    ├── location/
    │   ├── BaiduLocationService.kt    # 百度定位
    │   ├── GpsLocationService.kt      # GPS定位
    │   └── IpLocationService.kt       # IP定位
    └── notification/
        └── WeatherNotificationService.kt

app/src/main/res/
├── layout/
│   ├── activity_main.xml              # 主Activity布局
│   ├── fragment_today.xml             # 今日天气布局
│   ├── fragment_hourly.xml            # 24小时预报布局
│   ├── fragment_forecast15d.xml       # 15日预报布局
│   ├── fragment_main_cities.xml       # 主要城市布局
│   └── activity_city_weather.xml      # 城市天气详情布局
├── values/
│   ├── colors.xml                     # 颜色资源（亮色主题）
│   ├── themes.xml                     # 主题资源（亮色）
│   └── strings.xml                    # 字符串资源
├── values-night/
│   ├── colors.xml                     # 颜色资源（暗色主题）
│   └── themes.xml                     # 主题资源（暗色）
└── drawable/
    └── (各种图标和背景)
```

## 🎨 页面设计（完全对应Flutter版本）

### 1. MainActivity (对应 MainScreen)
- BottomNavigationView（4个Tab）
- FloatingActionButton（刷新按钮，仅在今日天气页显示）
- Fragment容器

### 2. TodayFragment (对应 TodayScreen)
- 头部：深蓝色背景 + 城市名 + 定位图标
- 当前天气卡片：温度、天气状况、图标
- 详细信息卡片：体感温度、湿度、风力、气压等
- 24小时预报预览
- 7日温度趋势图表
- 日出日落月相卡片
- 生活指数卡片
- 下拉刷新

### 3. HourlyFragment (对应 HourlyScreen)
- 头部：深蓝色背景
- 24小时温度趋势图表（交互式）
- 逐小时天气列表（时间、图标、温度、天气描述、风力）

### 4. Forecast15dFragment (对应 Forecast15dScreen)
- 头部：深蓝色背景
- 15日温度趋势图表（最高/最低温度双曲线）
- 逐日天气列表（日期、上午/下午天气、温度、日出日落）

### 5. MainCitiesFragment (对应 MainCitiesScreen)
- 头部：深蓝色背景 + "添加城市"按钮 + 刷新按钮
- 提示文字：长按拖拽排序、左滑删除
- 城市卡片列表（可拖拽排序、可左滑删除）
- 当前位置标记（绿色徽章）
- 预警图标（如有）
- 点击进入城市详情

### 6. CityWeatherActivity (对应 CityWeatherScreen)
- 与TodayFragment相同的布局
- 添加返回按钮
- 下拉刷新

## 🎨 主题系统（完全对应Flutter版本）

### 亮色主题
```kotlin
// 主色调
val PrimaryBlue = Color(0xFF012d78)        // 深蓝色
val AccentBlue = Color(0xFF8edafc)         // 亮蓝色
val Background = Color(0xFFC0D8EC)         // 浅蓝背景 (192,216,236)
val Surface = Color(0xFFFFFFFF)            // 白色卡片

// 文字颜色
val TextPrimary = Color(0xFF001A4D)        // 深蓝色文字
val TextSecondary = Color(0xFF003366)      // 次要文字

// 其他颜色
val HighTemp = Color(0xFFD32F2F)           // 高温红色
val LowTemp = Color(0xFF8edafc)            // 低温蓝色
val Success = Color(0xFF2E7D32)            // 成功绿色
val Error = Color(0xFFD32F2F)              // 错误红色
```

### 暗色主题
```kotlin
// 主色调
val PrimaryBlueDark = Color(0xFF4A90E2)    // 亮蓝色
val AccentBlueDark = Color(0xFF8edafc)     // 亮蓝色
val BackgroundDark = Color(0xFF0A1B3D)     // 深蓝背景
val SurfaceDark = Color(0xFF1A2F5D)        // 深蓝卡片

// 文字颜色
val TextPrimaryDark = Color(0xFFFFFFFF)    // 白色文字
val TextSecondaryDark = Color(0xFFE8F4FD)  // 浅色文字

// 其他颜色（同亮色主题）
```

## 📡 API集成（weatherol.cn）

### 接口定义
```kotlin
interface WeatherApiService {
    @GET("api/home/getCurrAnd15dAnd24h")
    suspend fun getWeatherData(
        @Query("cityid") cityId: String
    ): Response<WeatherResponse>
}
```

### 响应数据结构（对应Flutter的WeatherModel）
```kotlin
data class WeatherResponse(
    val data: WeatherData
)

data class WeatherData(
    val current: CurrentWeatherData?,
    val forecast24h: List<HourlyWeather>?,
    val forecast15d: List<DailyWeather>?,
    val sunMoonData: SunMoonData?,
    val lifeIndex: List<LifeIndex>?
)
```

## 📍 定位逻辑（三层降级策略）

### 优先级顺序
1. **百度定位** (BaiduLocationService)
   - 超时时间：8秒
   - 高精度模式（GPS + 网络定位）
   - 返回：经纬度、地址、区县、城市等

2. **GPS定位** (GpsLocationService)
   - 使用 FusedLocationProviderClient
   - 单次定位
   - 通过 Geocoder 反向地理编码获取地址

3. **IP定位** (IpLocationService)
   - 通过 IP 定位 API 获取大致位置
   - 作为最后的降级方案

### 实现流程
```kotlin
suspend fun getCurrentLocation(): LocationModel? {
    // 1. 尝试百度定位（8秒超时）
    try {
        val location = withTimeout(8000) {
            baiduLocationService.getLocation()
        }
        if (location != null) return location
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "百度定位超时，切换到GPS定位")
    }
    
    // 2. 尝试GPS定位
    try {
        val location = gpsLocationService.getLocation()
        if (location != null) return location
    } catch (e: Exception) {
        Log.e(TAG, "GPS定位失败: ${e.message}")
    }
    
    // 3. 最后降级到IP定位
    return ipLocationService.getLocation()
}
```

## 🗄️ 数据库设计（Room）

### 实体定义
```kotlin
@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val key: String,
    val data: String,              // JSON序列化的天气数据
    val timestamp: Long,
    val expiresAt: Long
)

@Entity(tableName = "location_cache")
data class LocationEntity(
    @PrimaryKey val key: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val district: String,
    val timestamp: Long
)

@Entity(tableName = "main_cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int,
    val isCurrentLocation: Boolean,
    val addedAt: Long
)
```

## 📊 图表实现（MPAndroidChart）

### 温度趋势图
- 使用 LineChart
- 双Y轴（最高/最低温度）
- 平滑曲线
- 数据点标记
- 温度标签

### 配置示例
```kotlin
fun setupTemperatureChart(chart: LineChart, data: List<DailyWeather>) {
    val highTempEntries = ArrayList<Entry>()
    val lowTempEntries = ArrayList<Entry>()
    
    data.forEachIndexed { index, weather ->
        highTempEntries.add(Entry(index.toFloat(), weather.tempHigh.toFloat()))
        lowTempEntries.add(Entry(index.toFloat(), weather.tempLow.toFloat()))
    }
    
    val highTempDataSet = LineDataSet(highTempEntries, "最高温度").apply {
        color = Color(0xFFD32F2F)
        setCircleColor(Color(0xFFD32F2F))
        lineWidth = 2f
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(true)
    }
    
    // ... 配置lowTempDataSet
    
    chart.data = LineData(highTempDataSet, lowTempDataSet)
    chart.invalidate()
}
```

## 🎯 核心功能清单

### ✅ 必须实现的功能
- [x] 三层定位策略（百度>GPS>IP）
- [ ] 天气数据获取和缓存
- [ ] 今日天气详细信息展示
- [ ] 24小时逐时预报
- [ ] 15日每日预报
- [ ] 温度趋势图表（7日/24小时/15日）
- [ ] 主要城市管理（增删改查、拖拽排序）
- [ ] 当前位置标记
- [ ] 下拉刷新
- [ ] 亮色/暗色主题切换
- [ ] 日出日落月相信息
- [ ] 生活指数卡片
- [ ] 空气质量指数
- [ ] 天气预警（如有）
- [ ] 启动页面
- [ ] 权限管理

## 📝 实施计划

### Phase 1: 基础架构（1-2天）
- [x] 创建项目结构
- [ ] 配置 Gradle 依赖
- [ ] 设置 Room 数据库
- [ ] 配置 Retrofit 网络层
- [ ] 创建数据模型

### Phase 2: 服务层（2-3天）
- [ ] 实现 BaiduLocationService
- [ ] 实现 GpsLocationService
- [ ] 实现 IpLocationService
- [ ] 实现 WeatherRepository
- [ ] 实现 CityRepository

### Phase 3: 主题系统（1天）
- [ ] 定义亮色/暗色主题颜色
- [ ] 实现主题切换逻辑
- [ ] 创建主题管理器
- [ ] 适配所有页面

### Phase 4: UI实现（4-5天）
- [ ] MainActivity + BottomNavigation
- [ ] TodayFragment
- [ ] HourlyFragment
- [ ] Forecast15dFragment
- [ ] MainCitiesFragment
- [ ] CityWeatherActivity

### Phase 5: 自定义组件（2-3天）
- [ ] WeatherChartView（温度图表）
- [ ] LifeIndexCard（生活指数）
- [ ] SunMoonCard（日出日落月相）
- [ ] 城市卡片拖拽排序
- [ ] 左滑删除

### Phase 6: 功能完善（2-3天）
- [ ] 下拉刷新
- [ ] 缓存机制
- [ ] 错误处理
- [ ] 加载状态
- [ ] 空状态显示
- [ ] 权限请求流程

### Phase 7: 测试优化（2-3天）
- [ ] 单元测试
- [ ] UI测试
- [ ] 性能优化
- [ ] Bug修复
- [ ] 文档完善

## 📚 参考文档

- [Flutter版本 README](../rainweather_flutter/README.md)
- [Flutter版本源码](../rainweather_flutter/lib/)
- [Material Design 3](https://m3.material.io/)
- [百度定位SDK文档](https://lbsyun.baidu.com/index.php?title=android-locsdk)
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- [Room数据库](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)

## 🔧 开发注意事项

1. **完全对标Flutter版本**：每个页面的布局、交互、逻辑都要与Flutter版本保持一致
2. **Material Design 3**：严格遵循Material Design 3设计规范
3. **性能优化**：列表使用RecyclerView、图片使用Glide缓存、数据使用Room缓存
4. **权限处理**：Android 13+权限适配
5. **Kotlin优先**：所有新代码使用Kotlin编写
6. **代码质量**：遵循Kotlin编码规范，添加必要注释
7. **错误处理**：完善的异常捕获和用户友好提示
8. **响应式设计**：适配不同屏幕尺寸
9. **可维护性**：清晰的代码结构，便于后续维护

---

**最后更新**: 2025-01-06
**版本**: v1.0
**状态**: 设计中


# 构建和运行指南

## ✅ 配置已完成

- ✅ 百度地图API Key已配置
- ✅ TodayFragment完整实现已集成
- ✅ 所有必要的XML资源文件已创建
- ✅ 百度定位SDK初始化代码已添加

## 🚀 构建和运行

### 方法1：使用Android Studio

1. 打开项目：File → Open → 选择 `rainweather` 目录
2. 等待Gradle同步完成
3. 连接Android设备或启动模拟器
4. 点击运行按钮（绿色三角形）或按 `Shift + F10`

### 方法2：使用命令行

```bash
# 进入项目目录
cd /Users/dddpeter/myworks/workspace/personal/rainweather

# 清理构建
./gradlew clean

# 构建Debug版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 或者一步完成（清理、构建、安装）
./gradlew clean assembleDebug installDebug
```

### 方法3：生成APK并手动安装

```bash
# 生成APK
./gradlew assembleDebug

# APK位置
# app/build/outputs/apk/debug/app-debug.apk

# 使用adb安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📱 首次运行注意事项

### 1. 权限请求
应用首次启动时会请求以下权限：
- ✅ 位置权限（必需）
- ✅ 网络权限（自动授予）

**重要**：请在设备上允许位置权限，否则定位功能无法正常工作。

### 2. 网络连接
- 确保设备已连接到互联网
- 天气数据来自 weatherol.cn API

### 3. 定位服务
- 确保设备的GPS已开启
- 应用使用三层定位策略：
  1. 百度定位（高精度）
  2. GPS定位（FusedLocationProvider）
  3. IP定位（降级方案）

## 🐛 常见问题

### 问题1：Gradle同步失败

**解决方案**：
```bash
# 清理Gradle缓存
./gradlew clean

# 重新下载依赖
./gradlew build --refresh-dependencies
```

### 问题2：百度定位不工作

**可能原因**：
1. API Key未正确配置 ✅ 已解决
2. 定位权限未授予 → 请在设备上允许权限
3. 百度定位SDK未正确集成 → 检查libs目录是否有BaiduLBS_Android.jar

**检查方法**：
- 查看Logcat日志，搜索 "百度定位" 或 "BaiduLocation"
- 正常情况应该看到：
  ```
  ✅ 百度定位服务初始化成功
  ✅ 百度定位成功: (lat, lng)
  ```

### 问题3：应用闪退

**查看日志**：
```bash
# 查看应用日志
adb logcat | grep RainWeather

# 或者查看所有崩溃日志
adb logcat | grep AndroidRuntime
```

### 问题4：网络请求失败

**检查**：
1. 设备是否联网
2. 是否在AndroidManifest.xml中添加了网络权限（已添加✅）
3. API地址是否可访问：https://www.weatherol.cn

## 🎯 测试功能

### 启动后应该看到：

1. **启动页面**
   - 显示应用图标
   - 深蓝色背景

2. **主界面**
   - 底部导航栏（4个标签）
   - 默认显示"今日天气"页面

3. **今日天气页面**
   - 城市名称（如果定位成功，显示当前城市）
   - 当前温度和天气图标
   - 详细信息（体感温度、湿度、风力、AQI）
   - 24小时预报预览（横向滚动）
   - 7日温度趋势图表

4. **下拉刷新**
   - 在今日天气页面下拉可刷新数据

5. **右下角刷新按钮**
   - 点击可强制刷新（包含重新定位）

## 📊 预期日志输出

正常启动时的关键日志：

```
🚀 RainWeather Application 启动
✅ 数据库初始化完成
✅ 主题管理器初始化完成
🔧 百度定位SDK全局配置
✅ 百度定位隐私政策同意设置成功
🎬 WeatherViewModel: 初始化
📍 第一层：尝试百度定位...
✅ 百度定位成功: 北京市朝阳区
✅ 天气数据加载成功: 朝阳区
```

## 🔄 开发调试

### 启用详细日志

应用已集成 Timber 日志库，Debug版本会自动输出详细日志。

### 查看特定标签的日志

```bash
# 查看定位相关日志
adb logcat | grep "定位"

# 查看天气数据日志
adb logcat | grep "天气"

# 查看ViewModel日志
adb logcat | grep "ViewModel"
```

### 使用Android Studio Logcat

1. 打开 Android Studio
2. 底部工具栏 → Logcat
3. 选择设备和应用进程
4. 使用过滤器搜索关键词

## 📦 构建Release版本

```bash
# 构建Release APK
./gradlew assembleRelease

# APK位置
# app/build/outputs/apk/release/app-release.apk
```

**注意**：Release版本需要配置签名，当前使用的是rainweather.p12密钥库。

## 🎨 主题测试

应用支持三种主题模式：
- 亮色主题
- 暗色主题
- 跟随系统

可以在系统设置中切换暗色模式来测试主题切换效果。

## 📝 性能监控

### 使用Android Studio Profiler

1. 运行应用（Debug模式）
2. Android Studio → View → Tool Windows → Profiler
3. 选择应用进程
4. 监控 CPU、内存、网络使用情况

### 检查网络请求

```bash
# 使用adb监控网络请求
adb shell tcpdump -i any -w /sdcard/capture.pcap
```

---

**准备就绪！** 现在您可以构建和运行应用了。

如有任何问题，请查看日志输出或参考上述常见问题解决方案。


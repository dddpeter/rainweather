#!/bin/bash

echo "🔧 应用所有编译错误修复..."

cd /Users/dddpeter/myworks/workspace/personal/rainweather

# 1. 确保HourlyWeather有扩展方法
echo "✅ 检查 WeatherModel.kt..."
grep -q "fun getFormattedTime()" app/src/main/kotlin/com/dddpeter/app/rainweather/data/models/WeatherModel.kt || {
    echo "需要修复 WeatherModel.kt"
}

# 2. 清理并重新构建
echo "🔄 清理构建缓存..."
./gradlew clean

echo "📦 重新生成Binding类..."
./gradlew generateDebugSources

echo "🏗️ 开始构建..."
./gradlew assembleDebug

echo "✅ 完成！"


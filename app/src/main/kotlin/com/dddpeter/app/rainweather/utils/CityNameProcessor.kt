package com.dddpeter.app.rainweather.utils

import android.content.Context
import com.dddpeter.app.rainweather.R
import com.dddpeter.app.rainweather.data.models.CityModel
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/**
 * 城市名字处理工具类
 * 处理城市名字中的行政区划后缀，用于匹配城市ID
 */
object CityNameProcessor {
    
    private var cityIdMap: Map<String, String> = emptyMap()
    private var isInitialized = false
    
    /**
     * 初始化城市ID映射表
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            val jsonString = context.assets.open("cities.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val map = mutableMapOf<String, String>()
            
            for (i in 0 until jsonArray.length()) {
                val city = jsonArray.getJSONObject(i)
                val id = city.getString("id")
                val name = city.getString("name")
                
                // 存储原始名字
                map[name] = id
                
                // 存储处理后的名字
                val processedName = processCityName(name)
                if (processedName != name) {
                    map[processedName] = id
                }
            }
            
            cityIdMap = map
            isInitialized = true
            Timber.d("🏙️ 城市ID映射表初始化完成，共${cityIdMap.size}个城市")
        } catch (e: Exception) {
            Timber.e(e, "❌ 初始化城市ID映射表失败")
            // 使用默认映射表作为后备
            cityIdMap = getDefaultCityIdMap()
            isInitialized = true
        }
    }
    
    /**
     * 默认城市ID映射表（作为后备）
     */
    private fun getDefaultCityIdMap() = mapOf(
        "北京" to "101010100",
        "上海" to "101020100", 
        "广州" to "101280101",
        "深圳" to "101280601",
        "杭州" to "101210101",
        "南京" to "101190101",
        "武汉" to "101200101",
        "成都" to "101270101",
        "西安" to "101110101",
        "重庆" to "101040100",
        "天津" to "101030100"
    )
    
    /**
     * 处理城市名字，移除行政区划后缀
     */
    fun processCityName(cityName: String): String {
        if (cityName.isBlank()) return cityName
        
        var processedName = cityName
        
        // 第一轮：移除省级行政区划后缀
        processedName = processedName
            .replace("省", "")
            .replace("市", "")
            .replace("自治区", "")
            .replace("区", "")
        
        // 第二轮：移除县级行政区划后缀
        processedName = processedName
            .replace("县", "")
            .replace("自治县", "")
            .replace("特区", "")
            .replace("特别行政区", "")
        
        Timber.d("🏙️ 城市名字处理: '$cityName' -> '$processedName'")
        return processedName
    }
    
    /**
     * 根据城市名字获取城市ID
     */
    fun getCityIdByCityName(cityName: String): String? {
        val processedName = processCityName(cityName)
        val cityId = cityIdMap[processedName]
        
        Timber.d("🏙️ 查找城市ID: '$cityName' -> '$processedName' -> '$cityId'")
        return cityId
    }
    
    /**
     * 创建城市信息，处理城市名字和ID
     */
    fun createCityInfo(originalName: String, adcode: String? = null): CityModel {
        val processedName = processCityName(originalName)
        val cityId = getCityIdByCityName(processedName) ?: adcode ?: "unknown"
        
        return CityModel(
            id = cityId,
            name = originalName, // 保持原始名字用于显示
            sortOrder = 0,
            isCurrentLocation = true
        )
    }
    
    /**
     * 检查城市ID是否有效
     */
    fun isValidCityId(cityId: String): Boolean {
        return cityId != "unknown" && cityId.isNotBlank()
    }
}

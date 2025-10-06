package com.dddpeter.app.rainweather.utils

import android.content.Context
import com.dddpeter.app.rainweather.data.local.database.AppDatabase
import com.dddpeter.app.rainweather.data.local.database.entities.CityInfoEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 城市数据初始化器
 * 负责将cities.json中的城市数据加载到Room数据库中
 */
class CityDataInitializer(
    private val context: Context,
    private val database: AppDatabase
) {
    
    companion object {
        private const val CITIES_JSON_FILE = "cities.json"
        private const val PREFS_NAME = "city_data_initializer"
        private const val KEY_CITIES_INITIALIZED = "cities_initialized"
        private const val BATCH_SIZE = 100 // 批量插入大小
    }
    
    /**
     * 城市数据模型（JSON解析用）
     */
    data class CityJson(
        val id: String,
        val name: String
    )
    
    /**
     * 检查城市数据是否已经初始化
     */
    fun isCitiesInitialized(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CITIES_INITIALIZED, false)
    }
    
    /**
     * 标记城市数据已初始化
     */
    private fun markCitiesInitialized() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CITIES_INITIALIZED, true).apply()
    }
    
    /**
     * 重置初始化状态（测试用）
     */
    fun resetInitializationStatus() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CITIES_INITIALIZED, false).apply()
        Timber.d("🔄 城市数据初始化状态已重置")
    }
    
    /**
     * 初始化城市数据
     */
    suspend fun initializeCities(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 检查是否已经初始化
            if (isCitiesInitialized()) {
                Timber.d("✅ 城市数据已经初始化过，跳过")
                return@withContext true
            }
            
            Timber.d("🏙️ 开始初始化城市数据...")
            
            // 读取JSON文件
            val citiesJson = readCitiesJson()
            if (citiesJson.isEmpty()) {
                Timber.e("❌ 未能读取城市数据")
                return@withContext false
            }
            
            Timber.d("📊 读取到 ${citiesJson.size} 个城市")
            
            // 转换为Entity
            val entities = citiesJson.map { city ->
                CityInfoEntity(
                    id = city.id,
                    name = city.name,
                    province = null, // JSON中没有省份信息
                    pinyin = null    // JSON中没有拼音信息
                )
            }
            
            // 批量插入数据库
            insertCitiesBatch(entities)
            
            // 标记为已初始化
            markCitiesInitialized()
            
            Timber.d("✅ 城市数据初始化完成，共 ${entities.size} 个城市")
            true
            
        } catch (e: Exception) {
            Timber.e(e, "❌ 城市数据初始化失败")
            false
        }
    }
    
    /**
     * 读取cities.json文件
     */
    private fun readCitiesJson(): List<CityJson> {
        return try {
            val inputStream = context.assets.open(CITIES_JSON_FILE)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.use { it.readText() }
            
            val gson = Gson()
            val type = object : TypeToken<List<CityJson>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Timber.e(e, "❌ 读取cities.json失败")
            emptyList()
        }
    }
    
    /**
     * 批量插入城市数据
     */
    private suspend fun insertCitiesBatch(entities: List<CityInfoEntity>) {
        try {
            // 清空现有数据（如果需要重新初始化）
            database.cityDao().deleteAll()
            
            // 分批插入数据，避免一次插入太多导致性能问题
            entities.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                database.cityDao().insertAll(batch)
                Timber.d("📝 已插入第 ${index + 1} 批，共 ${batch.size} 个城市")
            }
            
            Timber.d("✅ 所有城市数据已插入数据库")
        } catch (e: Exception) {
            Timber.e(e, "❌ 插入城市数据失败")
            throw e
        }
    }
    
    /**
     * 获取城市总数（用于验证）
     */
    suspend fun getCityCount(): Int = withContext(Dispatchers.IO) {
        try {
            database.cityDao().getCount()
        } catch (e: Exception) {
            Timber.e(e, "❌ 获取城市总数失败")
            0
        }
    }
    
    /**
     * 搜索城市（用于测试）
     */
    suspend fun searchCities(query: String): List<CityInfoEntity> = withContext(Dispatchers.IO) {
        try {
            database.cityDao().searchCities("%$query%")
        } catch (e: Exception) {
            Timber.e(e, "❌ 搜索城市失败")
            emptyList()
        }
    }
}


package com.dddpeter.app.rainweather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dddpeter.app.rainweather.pojo.LocationVO;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 天气数据访问对象
 * 提供对SQLite数据库的CRUD操作
 */
public class WeatherDao {
    
    private static final String TAG = "WeatherDao";
    private WeatherDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    
    public WeatherDao(Context context) {
        dbHelper = new WeatherDatabaseHelper(context);
    }
    
    /**
     * 打开数据库连接
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
    
    // ==================== 简单数据类型操作 ====================
    
    /**
     * 保存String类型数据
     * @param key 键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean putString(String key, String value) {
        return saveCacheData(key, value, "String");
    }
    
    /**
     * 获取String类型数据
     * @param key 键
     * @return 值，如果不存在返回null
     */
    public String getString(String key) {
        return getCacheDataInternal(key);
    }
    
    /**
     * 保存Boolean类型数据
     * @param key 键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean putBoolean(String key, boolean value) {
        return saveCacheData(key, String.valueOf(value), "Boolean");
    }
    
    /**
     * 获取Boolean类型数据
     * @param key 键
     * @return 值，如果不存在返回false
     */
    public boolean getBoolean(String key) {
        String value = getCacheDataInternal(key);
        return value != null ? Boolean.parseBoolean(value) : false;
    }
    
    /**
     * 保存Integer类型数据
     * @param key 键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean putInt(String key, int value) {
        return saveCacheData(key, String.valueOf(value), "Integer");
    }
    
    /**
     * 获取Integer类型数据
     * @param key 键
     * @return 值，如果不存在返回0
     */
    public int getInt(String key) {
        String value = getCacheDataInternal(key);
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 保存Long类型数据
     * @param key 键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean putLong(String key, long value) {
        return saveCacheData(key, String.valueOf(value), "Long");
    }
    
    /**
     * 获取Long类型数据
     * @param key 键
     * @return 值，如果不存在返回0
     */
    public long getLong(String key) {
        String value = getCacheDataInternal(key);
        try {
            return value != null ? Long.parseLong(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 保存Float类型数据
     * @param key 键
     * @param value 值
     * @return 是否保存成功
     */
    public boolean putFloat(String key, float value) {
        return saveCacheData(key, String.valueOf(value), "Float");
    }
    
    /**
     * 获取Float类型数据
     * @param key 键
     * @return 值，如果不存在返回0.0f
     */
    public float getFloat(String key) {
        String value = getCacheDataInternal(key);
        try {
            return value != null ? Float.parseFloat(value) : 0.0f;
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }
    
    // ==================== 缓存数据操作 ====================
    
    /**
     * 保存缓存数据
     * @param key 缓存键
     * @param data 数据（JSON字符串）
     * @param type 数据类型
     * @return 是否保存成功
     */
    public boolean saveCacheData(String key, String data, String type) {
        try {
            open();
            
            ContentValues values = new ContentValues();
            values.put(WeatherDatabaseHelper.COLUMN_KEY, key);
            values.put(WeatherDatabaseHelper.COLUMN_DATA, data);
            values.put(WeatherDatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
            values.put(WeatherDatabaseHelper.COLUMN_TYPE, type);
            
            long result = database.insertWithOnConflict(
                WeatherDatabaseHelper.TABLE_CACHE_DATA,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            );
            
            Log.d(TAG, "保存缓存数据: " + key + ", 结果: " + (result != -1));
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "保存缓存数据失败: " + key, e);
            return false;
        } finally {
            close();
        }
    }
    
    /**
     * 获取缓存数据（内部方法）
     * @param key 缓存键
     * @return 数据（JSON字符串），如果不存在返回null
     */
    private String getCacheDataInternal(String key) {
        try {
            open();
            
            String[] columns = {WeatherDatabaseHelper.COLUMN_DATA};
            String selection = WeatherDatabaseHelper.COLUMN_KEY + " = ?";
            String[] selectionArgs = {key};
            
            Cursor cursor = database.query(
                WeatherDatabaseHelper.TABLE_CACHE_DATA,
                columns,
                selection,
                selectionArgs,
                null, null, null
            );
            
            String result = null;
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            
            cursor.close();
            Log.d(TAG, "获取缓存数据: " + key + ", 结果: " + (result != null));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "获取缓存数据失败: " + key, e);
            return null;
        } finally {
            close();
        }
    }
    
    /**
     * 删除缓存数据
     * @param key 缓存键
     * @return 是否删除成功
     */
    public boolean deleteCacheData(String key) {
        try {
            open();
            
            int result = database.delete(
                WeatherDatabaseHelper.TABLE_CACHE_DATA,
                WeatherDatabaseHelper.COLUMN_KEY + " = ?",
                new String[]{key}
            );
            
            Log.d(TAG, "删除缓存数据: " + key + ", 结果: " + (result > 0));
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "删除缓存数据失败: " + key, e);
            return false;
        } finally {
            close();
        }
    }
    
    /**
     * 清理过期缓存数据
     * @param expireTime 过期时间（毫秒）
     * @return 清理的数据条数
     */
    public int cleanExpiredCacheData(long expireTime) {
        try {
            open();
            
            long currentTime = System.currentTimeMillis();
            long cutoffTime = currentTime - expireTime;
            
            int result = database.delete(
                WeatherDatabaseHelper.TABLE_CACHE_DATA,
                WeatherDatabaseHelper.COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(cutoffTime)}
            );
            
            Log.d(TAG, "清理过期缓存数据: " + result + " 条");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "清理过期缓存数据失败", e);
            return 0;
        } finally {
            close();
        }
    }
    
    // ==================== 位置数据操作 ====================
    
    /**
     * 保存位置数据
     * @param locationVO 位置信息对象
     * @return 是否保存成功
     */
    public boolean saveLocationData(LocationVO locationVO) {
        try {
            open();
            
            ContentValues values = new ContentValues();
            values.put(WeatherDatabaseHelper.COLUMN_ADDRESS, locationVO.getAddress());
            values.put(WeatherDatabaseHelper.COLUMN_COUNTRY, locationVO.getCountry());
            values.put(WeatherDatabaseHelper.COLUMN_PROVINCE, locationVO.getProvince());
            values.put(WeatherDatabaseHelper.COLUMN_CITY, locationVO.getCity());
            values.put(WeatherDatabaseHelper.COLUMN_DISTRICT, locationVO.getDistrict());
            values.put(WeatherDatabaseHelper.COLUMN_STREET, locationVO.getStreet());
            values.put(WeatherDatabaseHelper.COLUMN_ADCODE, locationVO.getAdcode());
            values.put(WeatherDatabaseHelper.COLUMN_TOWN, locationVO.getTown());
            values.put(WeatherDatabaseHelper.COLUMN_LAT, locationVO.getLat());
            values.put(WeatherDatabaseHelper.COLUMN_LNG, locationVO.getLng());
            values.put(WeatherDatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
            
            long result = database.insertWithOnConflict(
                WeatherDatabaseHelper.TABLE_LOCATION_DATA,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            );
            
            Log.d(TAG, "保存位置数据: " + locationVO.getDistrict() + ", 结果: " + (result != -1));
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "保存位置数据失败", e);
            return false;
        } finally {
            close();
        }
    }
    
    /**
     * 获取最新位置数据
     * @return 位置信息对象，如果不存在返回null
     */
    public LocationVO getLatestLocationData() {
        try {
            open();
            
            String[] columns = {
                WeatherDatabaseHelper.COLUMN_ADDRESS,
                WeatherDatabaseHelper.COLUMN_COUNTRY,
                WeatherDatabaseHelper.COLUMN_PROVINCE,
                WeatherDatabaseHelper.COLUMN_CITY,
                WeatherDatabaseHelper.COLUMN_DISTRICT,
                WeatherDatabaseHelper.COLUMN_STREET,
                WeatherDatabaseHelper.COLUMN_ADCODE,
                WeatherDatabaseHelper.COLUMN_TOWN,
                WeatherDatabaseHelper.COLUMN_LAT,
                WeatherDatabaseHelper.COLUMN_LNG
            };
            
            String orderBy = WeatherDatabaseHelper.COLUMN_TIMESTAMP + " DESC";
            
            Cursor cursor = database.query(
                WeatherDatabaseHelper.TABLE_LOCATION_DATA,
                columns,
                null, null, null, null, orderBy, "1"
            );
            
            LocationVO result = null;
            if (cursor.moveToFirst()) {
                result = new LocationVO();
                result.setAddress(cursor.getString(0));
                result.setCountry(cursor.getString(1));
                result.setProvince(cursor.getString(2));
                result.setCity(cursor.getString(3));
                result.setDistrict(cursor.getString(4));
                result.setStreet(cursor.getString(5));
                result.setAdcode(cursor.getString(6));
                result.setTown(cursor.getString(7));
                result.setLat(cursor.getDouble(8));
                result.setLng(cursor.getDouble(9));
            }
            
            cursor.close();
            Log.d(TAG, "获取最新位置数据: " + (result != null));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "获取最新位置数据失败", e);
            return null;
        } finally {
            close();
        }
    }
    
    // ==================== 历史数据操作 ====================
    
    /**
     * 保存历史数据
     * @param key 历史数据键
     * @param data 历史数据（JSON字符串）
     * @return 是否保存成功
     */
    public boolean saveHistoryData(String key, String data) {
        try {
            open();
            
            ContentValues values = new ContentValues();
            values.put(WeatherDatabaseHelper.COLUMN_KEY, key);
            values.put(WeatherDatabaseHelper.COLUMN_DATA, data);
            values.put(WeatherDatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
            
            long result = database.insertWithOnConflict(
                WeatherDatabaseHelper.TABLE_HISTORY_DATA,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            );
            
            Log.d(TAG, "保存历史数据: " + key + ", 结果: " + (result != -1));
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "保存历史数据失败: " + key, e);
            return false;
        } finally {
            close();
        }
    }
    
    /**
     * 获取历史数据
     * @param key 历史数据键
     * @return 历史数据（JSON字符串），如果不存在返回null
     */
    public String getHistoryData(String key) {
        try {
            open();
            
            String[] columns = {WeatherDatabaseHelper.COLUMN_DATA};
            String selection = WeatherDatabaseHelper.COLUMN_KEY + " = ?";
            String[] selectionArgs = {key};
            
            Cursor cursor = database.query(
                WeatherDatabaseHelper.TABLE_HISTORY_DATA,
                columns,
                selection,
                selectionArgs,
                null, null, null
            );
            
            String result = null;
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            
            cursor.close();
            Log.d(TAG, "获取历史数据: " + key + ", 结果: " + (result != null));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "获取历史数据失败: " + key, e);
            return null;
        } finally {
            close();
        }
    }
    
    // ==================== 天气数据操作 ====================
    
    /**
     * 保存天气数据
     * @param key 天气数据键
     * @param data 天气数据（JSON字符串）
     * @param type 数据类型
     * @return 是否保存成功
     */
    public boolean saveWeatherData(String key, String data, String type) {
        try {
            open();
            
            ContentValues values = new ContentValues();
            values.put(WeatherDatabaseHelper.COLUMN_KEY, key);
            values.put(WeatherDatabaseHelper.COLUMN_DATA, data);
            values.put(WeatherDatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
            values.put(WeatherDatabaseHelper.COLUMN_TYPE, type);
            
            long result = database.insertWithOnConflict(
                WeatherDatabaseHelper.TABLE_WEATHER_DATA,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            );
            
            Log.d(TAG, "保存天气数据: " + key + ", 结果: " + (result != -1));
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "保存天气数据失败: " + key, e);
            return false;
        } finally {
            close();
        }
    }
    
    /**
     * 获取天气数据
     * @param key 天气数据键
     * @return 天气数据（JSON字符串），如果不存在返回null
     */
    public String getWeatherData(String key) {
        try {
            open();
            
            String[] columns = {WeatherDatabaseHelper.COLUMN_DATA};
            String selection = WeatherDatabaseHelper.COLUMN_KEY + " = ?";
            String[] selectionArgs = {key};
            
            Cursor cursor = database.query(
                WeatherDatabaseHelper.TABLE_WEATHER_DATA,
                columns,
                selection,
                selectionArgs,
                null, null, null
            );
            
            String result = null;
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            
            cursor.close();
            Log.d(TAG, "获取天气数据: " + key + ", 结果: " + (result != null));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "获取天气数据失败: " + key, e);
            return null;
        } finally {
            close();
        }
    }
    
    // ==================== 数据库维护操作 ====================
    
    /**
     * 清理所有过期数据
     * @param expireTime 过期时间（毫秒）
     * @return 清理的数据条数
     */
    public int cleanAllExpiredData(long expireTime) {
        int totalCleaned = 0;
        
        try {
            open();
            
            long currentTime = System.currentTimeMillis();
            long cutoffTime = currentTime - expireTime;
            
            // 清理缓存数据
            totalCleaned += database.delete(
                WeatherDatabaseHelper.TABLE_CACHE_DATA,
                WeatherDatabaseHelper.COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(cutoffTime)}
            );
            
            // 清理天气数据
            totalCleaned += database.delete(
                WeatherDatabaseHelper.TABLE_WEATHER_DATA,
                WeatherDatabaseHelper.COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(cutoffTime)}
            );
            
            // 清理历史数据
            totalCleaned += database.delete(
                WeatherDatabaseHelper.TABLE_HISTORY_DATA,
                WeatherDatabaseHelper.COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(cutoffTime)}
            );
            
            Log.d(TAG, "清理所有过期数据: " + totalCleaned + " 条");
            return totalCleaned;
        } catch (Exception e) {
            Log.e(TAG, "清理所有过期数据失败", e);
            return totalCleaned;
        } finally {
            close();
        }
    }
    
    /**
     * 清理所有数据
     * @return 是否清理成功
     */
    public boolean clearAllData() {
        try {
            open();
            
            database.execSQL("DELETE FROM " + WeatherDatabaseHelper.TABLE_CACHE_DATA);
            database.execSQL("DELETE FROM " + WeatherDatabaseHelper.TABLE_WEATHER_DATA);
            database.execSQL("DELETE FROM " + WeatherDatabaseHelper.TABLE_LOCATION_DATA);
            database.execSQL("DELETE FROM " + WeatherDatabaseHelper.TABLE_HISTORY_DATA);
            
            Log.d(TAG, "清理所有数据完成");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "清理所有数据失败", e);
            return false;
        } finally {
            close();
        }
    }
    
    /**
     * 获取缓存数据（公共方法）
     * @param key 缓存键
     * @return 数据（JSON字符串），如果不存在返回null
     */
    public String getCacheData(String key) {
        return getCacheDataInternal(key);
    }
    
    /**
     * 保存缓存数据（别名方法）
     * @param key 缓存键
     * @param data 数据（JSON字符串）
     * @param type 数据类型
     * @return 是否保存成功
     */
    public boolean putCacheData(String key, String data, String type) {
        return saveCacheData(key, data, type);
    }
    
    /**
     * 清理过期缓存数据（别名方法）
     * @param expireTime 过期时间（毫秒）
     * @return 清理的数据条数
     */
    public int cleanExpiredCacheData() {
        // 默认7天过期
        long expireTime = 7 * 24 * 60 * 60 * 1000L;
        return cleanExpiredCacheData(expireTime);
    }
    
    /**
     * 获取数据库统计信息
     * @return 数据库统计信息字符串
     */
    public String getDatabaseStats() {
        return "数据库统计信息暂不可用";
    }
}

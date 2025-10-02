package com.dddpeter.app.rainweather.database;

import android.content.Context;
import android.util.Log;

import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.LocationVO;

import org.json.JSONObject;

/**
 * 数据库管理器
 * 提供统一的数据库操作接口，替代SharedPreferences
 */
public class DatabaseManager {
    
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private final WeatherDao weatherDao;
    private final Context context;
    
    private DatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        this.weatherDao = new WeatherDao(context);
    }
    
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 存储String类型数据
     */
    public void putString(String key, String value) {
        weatherDao.putString(key, value);
    }
    
    /**
     * 获取String类型数据
     */
    public String getString(String key, String defaultValue) {
        String value = weatherDao.getString(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 存储Boolean类型数据
     */
    public void putBoolean(String key, boolean value) {
        weatherDao.putBoolean(key, value);
    }
    
    /**
     * 获取Boolean类型数据
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = weatherDao.getString(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    /**
     * 存储Integer类型数据
     */
    public void putInt(String key, int value) {
        weatherDao.putInt(key, value);
    }
    
    /**
     * 获取Integer类型数据
     */
    public int getInt(String key, int defaultValue) {
        String value = weatherDao.getString(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing Integer for key: " + key, e);
            }
        }
        return defaultValue;
    }
    
    /**
     * 存储Long类型数据
     */
    public void putLong(String key, long value) {
        weatherDao.putLong(key, value);
    }
    
    /**
     * 获取Long类型数据
     */
    public long getLong(String key, long defaultValue) {
        String value = weatherDao.getString(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing Long for key: " + key, e);
            }
        }
        return defaultValue;
    }
    
    /**
     * 存储Float类型数据
     */
    public void putFloat(String key, float value) {
        weatherDao.putFloat(key, value);
    }
    
    /**
     * 获取Float类型数据
     */
    public float getFloat(String key, float defaultValue) {
        String value = weatherDao.getString(key);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing Float for key: " + key, e);
            }
        }
        return defaultValue;
    }
    
    /**
     * 存储缓存数据（通用方法）
     */
    public void putCacheData(String key, String data, String type) {
        weatherDao.putCacheData(key, data, type);
    }
    
    /**
     * 获取缓存数据（通用方法）
     */
    public String getCacheData(String key) {
        return weatherDao.getCacheData(key);
    }
    
    /**
     * 删除缓存数据
     */
    public void deleteCacheData(String key) {
        weatherDao.deleteCacheData(key);
    }
    
    /**
     * 存储JSONObject
     */
    public void putJSONObject(String key, JSONObject jsonObject) {
        if (jsonObject != null) {
            weatherDao.putCacheData(key, jsonObject.toString(), "JSONObject");
        } else {
            Log.w(TAG, "Attempted to put null JSONObject for key: " + key);
        }
    }
    
    /**
     * 获取JSONObject
     */
    public JSONObject getJSONObject(String key) {
        String jsonString = weatherDao.getCacheData(key);
        if (jsonString != null) {
            try {
                return new JSONObject(jsonString);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSONObject for key: " + key, e);
            }
        }
        return null;
    }
    
    /**
     * 清理过期数据
     */
    public int cleanExpiredData() {
        return weatherDao.cleanExpiredCacheData();
    }
    
    /**
     * 获取数据库统计信息
     * @return 数据库统计信息字符串
     */
    public String getDatabaseStats() {
        return weatherDao.getDatabaseStats();
    }
    
    /**
     * 清理所有数据
     * @return 是否清理成功
     */
    public boolean clearAllData() {
        Log.d(TAG, "清理所有数据");
        boolean result = weatherDao.clearAllData();
        Log.d(TAG, "清理所有数据完成: " + (result ? "成功" : "失败"));
        return result;
    }
    
    /**
     * 保存定位信息
     */
    public void saveLocation(LocationVO location) {
        if (location != null) {
            putJSONObject(CacheKey.CURRENT_LOCATION, locationToJSON(location));
            Log.d(TAG, "定位信息已保存: " + location.getDistrict());
        }
    }
    
    /**
     * 获取定位信息
     */
    public LocationVO getLocationVO(String key) {
        JSONObject json = getJSONObject(key);
        if (json != null) {
            return jsonToLocation(json);
        }
        return null;
    }
    
    /**
     * 获取上下文（用于LocationManager）
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * 将LocationVO转换为JSONObject
     */
    private JSONObject locationToJSON(LocationVO location) {
        try {
            JSONObject json = new JSONObject();
            json.put("address", location.getAddress());
            json.put("country", location.getCountry());
            json.put("province", location.getProvince());
            json.put("city", location.getCity());
            json.put("district", location.getDistrict());
            json.put("street", location.getStreet());
            json.put("adcode", location.getAdcode());
            json.put("town", location.getTown());
            json.put("lat", location.getLat());
            json.put("lng", location.getLng());
            return json;
        } catch (Exception e) {
            Log.e(TAG, "转换LocationVO为JSON失败", e);
            return null;
        }
    }
    
    /**
     * 将JSONObject转换为LocationVO
     */
    private LocationVO jsonToLocation(JSONObject json) {
        try {
            LocationVO location = new LocationVO();
            location.setAddress(json.optString("address"));
            location.setCountry(json.optString("country"));
            location.setProvince(json.optString("province"));
            location.setCity(json.optString("city"));
            location.setDistrict(json.optString("district"));
            location.setStreet(json.optString("street"));
            location.setAdcode(json.optString("adcode"));
            location.setTown(json.optString("town"));
            location.setLat(json.optDouble("lat"));
            location.setLng(json.optDouble("lng"));
            return location;
        } catch (Exception e) {
            Log.e(TAG, "转换JSON为LocationVO失败", e);
            return null;
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        if (weatherDao != null) {
            weatherDao.close();
        }
    }
}
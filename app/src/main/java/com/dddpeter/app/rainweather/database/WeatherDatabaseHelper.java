package com.dddpeter.app.rainweather.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 天气数据库帮助类
 * 用于管理SQLite数据库的创建、升级和版本控制
 */
public class WeatherDatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = "WeatherDatabaseHelper";
    
    // 数据库信息
    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;
    
    // 表名常量
    public static final String TABLE_WEATHER_DATA = "weather_data";
    public static final String TABLE_LOCATION_DATA = "location_data";
    public static final String TABLE_HISTORY_DATA = "history_data";
    public static final String TABLE_CACHE_DATA = "cache_data";
    
    // 天气数据表字段
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_KEY = "cache_key";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TYPE = "data_type";
    
    // 位置数据表字段
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_PROVINCE = "province";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_DISTRICT = "district";
    public static final String COLUMN_STREET = "street";
    public static final String COLUMN_ADCODE = "adcode";
    public static final String COLUMN_TOWN = "town";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LNG = "longitude";
    
    // 创建天气数据表SQL
    private static final String CREATE_WEATHER_DATA_TABLE = 
        "CREATE TABLE " + TABLE_WEATHER_DATA + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
        COLUMN_DATA + " TEXT NOT NULL, " +
        COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
        COLUMN_TYPE + " TEXT NOT NULL" +
        ");";
    
    // 创建位置数据表SQL
    private static final String CREATE_LOCATION_DATA_TABLE = 
        "CREATE TABLE " + TABLE_LOCATION_DATA + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_ADDRESS + " TEXT, " +
        COLUMN_COUNTRY + " TEXT, " +
        COLUMN_PROVINCE + " TEXT, " +
        COLUMN_CITY + " TEXT, " +
        COLUMN_DISTRICT + " TEXT, " +
        COLUMN_STREET + " TEXT, " +
        COLUMN_ADCODE + " TEXT, " +
        COLUMN_TOWN + " TEXT, " +
        COLUMN_LAT + " REAL, " +
        COLUMN_LNG + " REAL, " +
        COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
        ");";
    
    // 创建历史数据表SQL
    private static final String CREATE_HISTORY_DATA_TABLE = 
        "CREATE TABLE " + TABLE_HISTORY_DATA + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
        COLUMN_DATA + " TEXT NOT NULL, " +
        COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
        ");";
    
    // 创建缓存数据表SQL
    private static final String CREATE_CACHE_DATA_TABLE = 
        "CREATE TABLE " + TABLE_CACHE_DATA + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
        COLUMN_DATA + " TEXT NOT NULL, " +
        COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
        COLUMN_TYPE + " TEXT NOT NULL" +
        ");";
    
    // 创建索引SQL
    private static final String CREATE_WEATHER_KEY_INDEX = 
        "CREATE INDEX idx_weather_key ON " + TABLE_WEATHER_DATA + " (" + COLUMN_KEY + ");";
    
    private static final String CREATE_CACHE_KEY_INDEX = 
        "CREATE INDEX idx_cache_key ON " + TABLE_CACHE_DATA + " (" + COLUMN_KEY + ");";
    
    private static final String CREATE_TIMESTAMP_INDEX = 
        "CREATE INDEX idx_timestamp ON " + TABLE_CACHE_DATA + " (" + COLUMN_TIMESTAMP + ");";
    
    public WeatherDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "创建数据库表");
        
        // 创建所有表
        db.execSQL(CREATE_WEATHER_DATA_TABLE);
        db.execSQL(CREATE_LOCATION_DATA_TABLE);
        db.execSQL(CREATE_HISTORY_DATA_TABLE);
        db.execSQL(CREATE_CACHE_DATA_TABLE);
        
        // 创建索引
        db.execSQL(CREATE_WEATHER_KEY_INDEX);
        db.execSQL(CREATE_CACHE_KEY_INDEX);
        db.execSQL(CREATE_TIMESTAMP_INDEX);
        
        Log.d(TAG, "数据库表创建完成");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "升级数据库从版本 " + oldVersion + " 到 " + newVersion);
        
        // 删除旧表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHE_DATA);
        
        // 重新创建表
        onCreate(db);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "降级数据库从版本 " + oldVersion + " 到 " + newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }
    
    /**
     * 清理过期数据
     * @param db 数据库实例
     * @param expireTime 过期时间（毫秒）
     */
    public void cleanExpiredData(SQLiteDatabase db, long expireTime) {
        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - expireTime;
        
        int deletedRows = db.delete(TABLE_CACHE_DATA, 
            COLUMN_TIMESTAMP + " < ?", 
            new String[]{String.valueOf(cutoffTime)});
        
        Log.d(TAG, "清理了 " + deletedRows + " 条过期数据");
    }
    
    /**
     * 获取数据库大小（字节）
     * @return 数据库文件大小
     */
    public long getDatabaseSize() {
        // 简化实现，返回0表示无法获取大小
        return 0;
    }
}

package com.dddpeter.app.rainweather.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dddpeter.app.rainweather.data.local.database.dao.*
import com.dddpeter.app.rainweather.data.local.database.entities.*
import com.dddpeter.app.rainweather.data.models.AppConstants

/**
 * App数据库（对应Flutter版本的DatabaseService）
 */
@Database(
    entities = [
        WeatherEntity::class,
        LocationEntity::class,
        MainCityEntity::class,
        CityInfoEntity::class
    ],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun weatherDao(): WeatherDao
    abstract fun locationDao(): LocationDao
    abstract fun cityDao(): CityDao
    abstract fun mainCityDao(): MainCityDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppConstants.DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // 简化版：直接删除旧数据库
                .build()
        }
    }
}


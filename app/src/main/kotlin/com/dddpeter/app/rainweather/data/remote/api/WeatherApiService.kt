package com.dddpeter.app.rainweather.data.remote.api

import com.dddpeter.app.rainweather.data.models.SunMoonIndexResponse
import com.dddpeter.app.rainweather.data.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 天气API接口（对应Flutter版本的WeatherService）
 */
interface WeatherApiService {
    
    /**
     * 获取天气数据（当前天气+24小时+15日预报）
     * 对应API: https://www.weatherol.cn/api/home/getCurrAnd15dAnd24h?cityid={cityId}
     */
    @GET("api/home/getCurrAnd15dAnd24h")
    suspend fun getWeatherData(
        @Query("cityid") cityId: String
    ): Response<WeatherResponse>
    
    /**
     * 获取日出日落和生活指数数据
     * 注：实际API可能需要根据weatherol.cn的实际接口调整
     */
    @GET("api/home/getSunMoonAndLifeIndex")
    suspend fun getSunMoonIndexData(
        @Query("cityid") cityId: String
    ): Response<SunMoonIndexResponse>
}


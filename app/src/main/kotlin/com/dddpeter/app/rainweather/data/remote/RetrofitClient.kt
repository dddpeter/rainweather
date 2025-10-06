package com.dddpeter.app.rainweather.data.remote

import com.dddpeter.app.rainweather.data.models.AppConstants
import com.dddpeter.app.rainweather.data.remote.api.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例
 */
object RetrofitClient {
    
    private var retrofit: Retrofit? = null
    
    /**
     * 获取Retrofit实例
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(AppConstants.WEATHER_API_BASE_URL)
                        .client(getOkHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return retrofit!!
    }
    
    /**
     * 获取OkHttpClient
     */
    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.49")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    
    /**
     * 获取WeatherApiService
     */
    fun getWeatherApiService(): WeatherApiService {
        return getRetrofit().create(WeatherApiService::class.java)
    }
}


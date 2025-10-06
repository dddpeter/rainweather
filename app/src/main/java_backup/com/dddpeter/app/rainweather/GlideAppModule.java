package com.dddpeter.app.rainweather;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.xuexiang.xui.BuildConfig;

/**
 * Glide配置模块
 * 优化内存使用，避免使用已弃用的功能
 */
@GlideModule
public final class GlideAppModule extends AppGlideModule {
    
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // 设置内存缓存大小（24MB）
        int memoryCacheSizeBytes = 1024 * 1024 * 24; // 24 MB
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        
        // 设置磁盘缓存大小（100MB）
        int diskCacheSizeBytes = 1024 * 1024 * 100; // 100 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        
        // 启用日志（仅在debug模式下）
        if (BuildConfig.DEBUG) {
            builder.setLogLevel(android.util.Log.DEBUG);
        }
    }
    
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // 可以在这里注册自定义组件
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        // 禁用清单解析以提高性能
        return false;
    }
}

package com.dddpeter.app.rainweather.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.LocationVO;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 定位服务 - 使用Android系统自带的定位API
 * 支持GPS和网络定位，无需第三方SDK
 */
public class LocationService {
    private static final String TAG = "LocationService";
    
    private Context context;
    private android.location.LocationManager locationManager;
    private DatabaseManager databaseManager;
    private LocationCallback callback;
    private boolean isLocationReceived = false;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long LOCATION_TIMEOUT = 30000; // 30秒超时
    private Handler timeoutHandler;
    private Geocoder geocoder;

    /**
     * 定位回调接口
     */
    public interface LocationCallback {
        void onLocationSuccess(LocationVO location);
        void onLocationFailed(String error);
    }

    /**
     * 初始化 LocationService
     * @param context
     */
    public LocationService(Context context) {
        this.context = context;
        this.databaseManager = DatabaseManager.getInstance(context);
        this.locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        
        Log.d(TAG, "LocationService初始化完成");
    }
    
    /**
     * 获取LocationService实例
     * @param context
     * @return
     */
    public static LocationService getInstance(Context context) {
        return new LocationService(context);
    }

    /**
     * 检查定位权限
     * @return
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查定位服务是否开启
     * @return
     */
    public boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    /**
     * 开始定位
     * @param callback
     */
    public void startLocation(LocationCallback callback) {
        this.callback = callback;
        this.isLocationReceived = false;
        this.retryCount = 0;
        
        Log.d(TAG, "========== 开始定位 ==========");
        Log.d(TAG, "重试次数: " + retryCount);
        Log.d(TAG, "是否已收到定位: " + isLocationReceived);
        Log.d(TAG, "权限状态: " + (hasLocationPermission() ? "已授予" : "未授予"));
        Log.d(TAG, "定位服务状态: " + (isLocationEnabled() ? "已开启" : "未开启"));
        Log.d(TAG, "=============================");
        
        // 检查权限
        if (!hasLocationPermission()) {
            Log.e(TAG, "定位权限未授予");
            if (callback != null) {
                callback.onLocationFailed("定位权限未授予，请在设置中开启位置权限");
            }
            return;
        }
        
        // 检查定位服务
        if (!isLocationEnabled()) {
            Log.e(TAG, "定位服务未开启");
            if (callback != null) {
                callback.onLocationFailed("定位服务未开启，请在设置中开启位置服务");
            }
            return;
        }
        
        // 设置超时处理
        timeoutHandler.postDelayed(() -> {
            if (!isLocationReceived) {
                Log.e(TAG, "========== 定位超时 ==========");
                Log.e(TAG, "定位超时时间: " + LOCATION_TIMEOUT + "ms");
                Log.e(TAG, "当前时间: " + System.currentTimeMillis());
                Log.e(TAG, "=============================");
                
                if (callback != null) {
                    callback.onLocationFailed("定位超时，请检查网络连接和位置权限");
                }
            }
        }, LOCATION_TIMEOUT);
        
        // 开始定位
        requestLocation();
    }

    /**
     * 请求定位
     */
    private void requestLocation() {
        try {
            // 先尝试获取最后已知位置
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                Log.d(TAG, "使用最后已知位置");
                handleLocationResult(lastKnownLocation);
                return;
            }
            
            // 注册位置监听器
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "注册GPS定位监听器");
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    1000, // 最小时间间隔1秒
                    1, // 最小距离间隔1米
                    locationListener
                );
            }
            
            if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                Log.d(TAG, "注册网络定位监听器");
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    1000, // 最小时间间隔1秒
                    1, // 最小距离间隔1米
                    locationListener
                );
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "定位权限不足", e);
            if (callback != null) {
                callback.onLocationFailed("定位权限不足: " + e.getMessage());
            }
        }
    }

    /**
     * 获取最后已知位置
     * @return
     */
    private Location getLastKnownLocation() {
        try {
            Location gpsLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            
            if (gpsLocation != null && networkLocation != null) {
                // 选择较新的位置
                return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            } else if (gpsLocation != null) {
                return gpsLocation;
            } else if (networkLocation != null) {
                return networkLocation;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "获取最后已知位置失败", e);
        }
        return null;
    }

    /**
     * 位置监听器
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "========== 收到位置更新 ==========");
            Log.d(TAG, "isLocationReceived: " + isLocationReceived);
            Log.d(TAG, "经度: " + location.getLongitude());
            Log.d(TAG, "纬度: " + location.getLatitude());
            Log.d(TAG, "精度: " + location.getAccuracy() + "米");
            Log.d(TAG, "时间: " + location.getTime());
            Log.d(TAG, "================================");
            
            if (!isLocationReceived) {
                isLocationReceived = true;
                Log.i(TAG, "收到位置更新，处理定位结果");
                
                // 停止位置监听
                stopLocationUpdates();
                
                // 处理定位结果
                handleLocationResult(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "定位状态变化: " + provider + " -> " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "定位提供者启用: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "定位提供者禁用: " + provider);
        }
    };

    /**
     * 处理定位结果
     * @param location
     */
    private void handleLocationResult(Location location) {
        // 清除超时处理
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
        
        if (location != null) {
            Log.i(TAG, "========== 定位成功 ==========");
            Log.i(TAG, "经度: " + location.getLongitude());
            Log.i(TAG, "纬度: " + location.getLatitude());
            Log.i(TAG, "精度: " + location.getAccuracy() + "米");
            Log.i(TAG, "开始解析地址信息");
            Log.i(TAG, "=============================");
            
            // 解析地址信息
            parseLocationWithGeocoder(location);
        } else {
            Log.e(TAG, "========== 定位失败 ==========");
            Log.e(TAG, "位置信息为空");
            Log.e(TAG, "=============================");
            
            // 尝试重试
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                Log.i(TAG, "定位失败，尝试重试 " + retryCount + "/" + MAX_RETRY_COUNT);
                
                // 延迟2秒后重试
                timeoutHandler.postDelayed(() -> {
                    isLocationReceived = false;
                    requestLocation();
                }, 2000);
                return;
            }
            
            // 回调失败
            if (callback != null) {
                callback.onLocationFailed("定位失败，无法获取位置信息");
            }
        }
    }

    /**
     * 使用Geocoder解析地址信息
     * @param location
     */
    private void parseLocationWithGeocoder(Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LocationVO locationVO = new LocationVO();
                
                // 设置坐标
                locationVO.setLng(location.getLongitude());
                locationVO.setLat(location.getLatitude());
                
                // 设置地址信息
                locationVO.setCountry(address.getCountryName());
                locationVO.setProvince(address.getAdminArea());
                locationVO.setCity(address.getLocality());
                locationVO.setDistrict(address.getSubLocality());
                locationVO.setStreet(address.getThoroughfare());
                locationVO.setTown(address.getSubLocality());
                
                // 构建详细地址
                StringBuilder addrBuilder = new StringBuilder();
                if (address.getCountryName() != null) addrBuilder.append(address.getCountryName());
                if (address.getAdminArea() != null) addrBuilder.append(address.getAdminArea());
                if (address.getLocality() != null) addrBuilder.append(address.getLocality());
                if (address.getSubLocality() != null) addrBuilder.append(address.getSubLocality());
                if (address.getThoroughfare() != null) addrBuilder.append(address.getThoroughfare());
                
                String fullAddress = addrBuilder.toString();
                locationVO.setAddress(fullAddress);
                locationVO.setAddr(fullAddress);
                locationVO.setTime(System.currentTimeMillis());
                
                Log.d(TAG, "地址解析成功: " + locationVO.getDistrict());
                
                // 保存到数据库
                saveLocation(locationVO);
                
                // 回调成功
                if (callback != null) {
                    callback.onLocationSuccess(locationVO);
                }
            } else {
                Log.w(TAG, "地址解析失败，使用坐标信息");
                
                // 地址解析失败，使用坐标信息
                LocationVO locationVO = new LocationVO();
                locationVO.setLng(location.getLongitude());
                locationVO.setLat(location.getLatitude());
                locationVO.setAddress("经度: " + location.getLongitude() + ", 纬度: " + location.getLatitude());
                locationVO.setAddr(locationVO.getAddress());
                locationVO.setTime(System.currentTimeMillis());
                
                // 保存到数据库
                saveLocation(locationVO);
                
                // 回调成功
                if (callback != null) {
                    callback.onLocationSuccess(locationVO);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "地址解析失败", e);
            
            // 地址解析失败，使用坐标信息
            LocationVO locationVO = new LocationVO();
            locationVO.setLng(location.getLongitude());
            locationVO.setLat(location.getLatitude());
            locationVO.setAddress("经度: " + location.getLongitude() + ", 纬度: " + location.getLatitude());
            locationVO.setAddr(locationVO.getAddress());
            locationVO.setTime(System.currentTimeMillis());
            
            // 保存到数据库
            saveLocation(locationVO);
            
            // 回调成功
            if (callback != null) {
                callback.onLocationSuccess(locationVO);
            }
        }
    }

    /**
     * 停止位置监听
     */
    public void stopLocationUpdates() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
                Log.d(TAG, "停止位置监听");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "停止位置监听失败", e);
        }
    }

    /**
     * 停止定位
     */
    public void stop() {
        stopLocationUpdates();
        Log.d(TAG, "停止定位服务");
    }

    /**
     * 检查是否已启动
     * @return
     */
    public boolean isStart() {
        return isLocationReceived;
    }

    /**
     * 获取缓存的定位信息
     */
    public LocationVO getCachedLocation() {
        return databaseManager.getLocationVO(CacheKey.CURRENT_LOCATION);
    }
    
    /**
     * 保存定位信息到数据库
     */
    public void saveLocation(LocationVO location) {
        databaseManager.saveLocation(location);
        Log.d(TAG, "定位信息已保存到数据库: " + location.getDistrict());
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        // 停止位置监听
        stopLocationUpdates();
        
        // 清理回调
        callback = null;
        
        // 清理超时处理
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
            timeoutHandler = null;
        }
        
        Log.d(TAG, "LocationService资源已清理");
    }
}
package com.dddpeter.app.rainweather.common;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.dddpeter.app.rainweather.enums.CacheKey;
import com.dddpeter.app.rainweather.pojo.LocationVO;

/**
 * 定位管理器 - 统一管理定位逻辑
 * 只在SplashScreenActivity中使用，其他页面直接读取数据库中的定位信息
 */
public class LocationManager {
    
    private static final String TAG = "LocationManager";
    private static LocationManager instance;
    private LocationClient mLocationClient;
    private final DatabaseManager databaseManager;
    private LocationCallback callback;
    private boolean isLocationReceived = false;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 2;
    private static final int LOCATION_TIMEOUT = 15000; // 15秒超时
    private android.os.Handler timeoutHandler;

    /**
     * 定位回调接口
     */
    public interface LocationCallback {
        void onLocationSuccess(LocationVO location);
        void onLocationFailed(String error);
    }
    
    private LocationManager(Context context) {
        LocationClient.setAgreePrivacy(true);
        databaseManager = DatabaseManager.getInstance(context);
    }
    
    public static synchronized LocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 开始定位
     */
    public void startLocation(LocationCallback callback) {
        this.callback = callback;
        this.isLocationReceived = false;
        this.retryCount = 0; // 重置重试计数
        
        // 打印所有可能的定位结果码
        printAllLocationCodes();
        
        if (mLocationClient != null) {
            mLocationClient.stop();
        }

        try {
            mLocationClient = new LocationClient(databaseManager.getContext());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setNeedNewVersionRgc(false); // 关闭新版地址解析，减少服务器压力
        option.setIsNeedLocationPoiList(false); // 关闭POI结果，减少网络请求
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000); // 增加定位间隔，减少请求频率
        option.setIsNeedLocationDescribe(false); // 关闭地址描述，减少服务器压力
        option.setLocationNotify(false); // 关闭持续定位通知
        option.setIgnoreKillProcess(false); // 是否在stop的时候杀死这个进程
        option.setEnableSimulateGps(false); // 是否开启GPS
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving); // 使用省电模式
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(locationListener);
        
        Log.d(TAG, "========== 开始定位 ==========");
        Log.d(TAG, "LocationClient: " + (mLocationClient != null ? "已初始化" : "未初始化"));
        Log.d(TAG, "定位选项: " + option.toString());
        Log.d(TAG, "监听器: " + (locationListener != null ? "已注册" : "未注册"));
        Log.d(TAG, "=============================");
        
        mLocationClient.start();
        
        // 设置超时处理
        if (timeoutHandler == null) {
            timeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
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
    }
    
    /**
     * 停止定位
     */
    public void stopLocation() {
        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient = null;
        }
        Log.d(TAG, "停止定位");
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
     * 定位监听器
     */
    private final BDAbstractLocationListener locationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "========== 收到定位回调 ==========");
            Log.d(TAG, "isLocationReceived: " + isLocationReceived);
            Log.d(TAG, "定位结果码: " + location.getLocType());
            Log.d(TAG, "================================");
            
            if (!isLocationReceived) {
                isLocationReceived = true;
                Log.i(TAG, "收到定位回调，停止定位服务");
                
                // 打印详细的定位结果信息
                printLocationDetails(location);
                
                // 检查定位是否成功
                if (location.getLocType() == BDLocation.TypeGpsLocation || 
                    location.getLocType() == BDLocation.TypeNetWorkLocation || 
                    location.getLocType() == BDLocation.TypeOffLineLocation) {
                    
                    Log.i(TAG, "========== 定位成功 ==========");
                    Log.i(TAG, "结果码: " + location.getLocType());
                    Log.i(TAG, "结果描述: " + getLocationTypeDescription(location.getLocType()));
                    Log.i(TAG, "开始解析数据");
                    Log.i(TAG, "=============================");
                    
                    // 清除超时处理
                    if (timeoutHandler != null) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                    }
                    
                    // 立即停止定位服务
                    stopLocation();
                    
                    // 解析定位结果
                    LocationVO locationVO = parseLocation(location);
                    if (locationVO != null) {
                        // 保存到数据库
                        saveLocation(locationVO);
                        
                        // 回调成功
                        if (callback != null) {
                            callback.onLocationSuccess(locationVO);
                        }
                    } else {
                        // 回调失败
                        if (callback != null) {
                            callback.onLocationFailed("定位数据解析失败");
                        }
                    }
                } else {
                    Log.e(TAG, "========== 定位失败 ==========");
                    Log.e(TAG, "结果码: " + location.getLocType());
                    Log.e(TAG, "结果描述: " + getLocationTypeDescription(location.getLocType()));
                    Log.e(TAG, "错误说明: " + getLocationErrorDescription(location.getLocType()));
                    Log.e(TAG, "定位质量: " + getLocationQuality(location.getLocType()));
                    Log.e(TAG, "=============================");
                    
                    // 清除超时处理
                    if (timeoutHandler != null) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                    }
                    
                    // 立即停止定位服务
                    stopLocation();
                    
                    // 对于167错误（服务端定位失败），尝试重试
                    if (location.getLocType() == 167 && retryCount < MAX_RETRY_COUNT) {
                        retryCount++;
                        Log.i(TAG, "服务端定位失败，尝试重试 " + retryCount + "/" + MAX_RETRY_COUNT);
                        
                        // 延迟2秒后重试
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            isLocationReceived = false;
                            startLocation(callback);
                        }, 2000);
                        return;
                    }
                    
                    // 回调失败
                    if (callback != null) {
                        String errorMsg = "定位失败，结果码: " + location.getLocType() + 
                                        " (" + getLocationErrorDescription(location.getLocType()) + ")";
                        callback.onLocationFailed(errorMsg);
                    }
                }
            }
        }
    };
    
    /**
     * 打印详细的定位结果信息
     */
    private void printLocationDetails(BDLocation location) {
        Log.d(TAG, "========== 定位结果详细信息 ==========");
        Log.d(TAG, "定位结果码: " + location.getLocType());
        Log.d(TAG, "定位结果码描述: " + getLocationTypeDescription(location.getLocType()));
        Log.d(TAG, "定位结果码详细说明: " + getLocationErrorDescription(location.getLocType()));
        Log.d(TAG, "经度: " + location.getLongitude());
        Log.d(TAG, "纬度: " + location.getLatitude());
        Log.d(TAG, "精度: " + location.getRadius() + "米");
        Log.d(TAG, "国家: " + location.getCountry());
        Log.d(TAG, "省份: " + location.getProvince());
        Log.d(TAG, "城市: " + location.getCity());
        Log.d(TAG, "区县: " + location.getDistrict());
        Log.d(TAG, "街道: " + location.getStreet());
        Log.d(TAG, "地址: " + location.getAddrStr());
        Log.d(TAG, "地址描述: " + location.getLocationDescribe());
        Log.d(TAG, "定位时间: " + location.getTime());
        Log.d(TAG, "定位来源: " + getLocationSource(location.getLocType()));
        Log.d(TAG, "网络类型: " + location.getNetworkLocationType());
        Log.d(TAG, "GPS状态: " + (location.getGpsAccuracyStatus() == 0 ? "正常" : "异常"));
        Log.d(TAG, "定位质量: " + getLocationQuality(location.getLocType()));
        Log.d(TAG, "=====================================");
    }
    
    /**
     * 获取定位类型描述
     */
    private String getLocationTypeDescription(int locType) {
        switch (locType) {
            case BDLocation.TypeGpsLocation: return "GPS定位";
            case BDLocation.TypeNetWorkLocation: return "网络定位";
            case BDLocation.TypeOffLineLocation: return "离线定位";
            case BDLocation.TypeCacheLocation: return "缓存定位";
            case BDLocation.TypeServerError: return "服务端错误";
            case BDLocation.TypeNetWorkException: return "网络异常";
            case BDLocation.TypeCriteriaException: return "定位条件异常";
            default: return "未知类型(" + locType + ")";
        }
    }
    
    /**
     * 获取定位来源描述
     */
    private String getLocationSource(int locType) {
        switch (locType) {
            case BDLocation.TypeGpsLocation: return "GPS卫星";
            case BDLocation.TypeNetWorkLocation: return "网络基站/WiFi";
            case BDLocation.TypeOffLineLocation: return "离线缓存";
            case BDLocation.TypeCacheLocation: return "本地缓存";
            default: return "未知来源";
        }
    }
    
    /**
     * 获取定位质量描述
     */
    private String getLocationQuality(int locType) {
        switch (locType) {
            case 61: return "GPS定位结果";
            case 62: return "扫描整合定位依据失败";
            case 63: return "网络异常，没有成功向服务器发起请求";
            case 65: return "定位缓存的结果";
            case 66: return "离线定位结果";
            case 67: return "离线定位失败";
            case 68: return "网络连接失败时，查找本地离线定位时对应的网络连接失败";
            case 161: return "网络定位成功";
            case 162: return "请求串密文解析失败";
            case 167: return "服务端定位失败";
            case 502: return "key参数错误";
            case 505: return "key不存在或者非法";
            case 601: return "key服务被开发者自己禁用";
            case 602: return "key mcode不匹配";
            case 501: return "key验证失败";
            default: return "未知质量(" + locType + ")";
        }
    }
    
    /**
     * 解析定位结果
     */
    private LocationVO parseLocation(BDLocation location) {
        try {
            Log.d(TAG, "开始解析定位结果");
            Log.d(TAG, "定位类型: " + location.getLocType());
            Log.d(TAG, "定位精度: " + location.getRadius());
            Log.d(TAG, "定位时间: " + location.getTime());
            
            String addr = location.getAddrStr();
            String country = location.getCountry();
            String province = location.getProvince();
            String city = location.getCity();
            String district = location.getDistrict();
            
            Log.d(TAG, "原始定位数据:");
            Log.d(TAG, "  addr: " + addr);
            Log.d(TAG, "  country: " + country);
            Log.d(TAG, "  province: " + province);
            Log.d(TAG, "  city: " + city);
            Log.d(TAG, "  district: " + district);
            
            if (district == null || district.isEmpty()) {
                Log.w(TAG, "district为空，使用默认值东城区");
                district = "东城区";
            }
            String street = location.getStreet();
            String adcode = location.getAdCode();
            String town = location.getTown();
            Double lat = location.getLatitude();
            Double lng = location.getLongitude();
            
            LocationVO locationVO = new LocationVO();
            locationVO.setAdcode(adcode);
            locationVO.setAddress(addr);
            locationVO.setCity(city);
            locationVO.setCountry(country);
            locationVO.setDistrict(district);
            locationVO.setProvince(province);
            locationVO.setStreet(street);
            locationVO.setTown(town);
            locationVO.setLat(lat);
            locationVO.setLng(lng);
            
            Log.i(TAG, "定位结果: " + locationVO);
            return locationVO;
        } catch (Exception e) {
            Log.e(TAG, "解析定位结果失败", e);
            return null;
        }
    }
    
    /**
     * 打印所有可能的定位结果码
     */
    public static void printAllLocationCodes() {
        Log.d(TAG, "========== 百度定位SDK所有结果码 ==========");
        Log.d(TAG, "61 - GPS定位结果");
        Log.d(TAG, "62 - 扫描整合定位依据失败");
        Log.d(TAG, "63 - 网络异常，没有成功向服务器发起请求");
        Log.d(TAG, "65 - 定位缓存的结果");
        Log.d(TAG, "66 - 离线定位结果");
        Log.d(TAG, "67 - 离线定位失败");
        Log.d(TAG, "68 - 网络连接失败时，查找本地离线定位时对应的网络连接失败");
        Log.d(TAG, "161 - 网络定位成功");
        Log.d(TAG, "162 - 请求串密文解析失败");
        Log.d(TAG, "167 - 服务端定位失败");
        Log.d(TAG, "501 - key验证失败");
        Log.d(TAG, "502 - key参数错误");
        Log.d(TAG, "505 - key不存在或者非法");
        Log.d(TAG, "601 - key服务被开发者自己禁用");
        Log.d(TAG, "602 - key mcode不匹配");
        Log.d(TAG, "=========================================");
    }
    
    /**
     * 获取定位错误描述
     */
    private String getLocationErrorDescription(int errorCode) {
        switch (errorCode) {
            case 61: return "GPS定位结果";
            case 62: return "扫描整合定位依据失败";
            case 63: return "网络异常，没有成功向服务器发起请求";
            case 65: return "定位缓存的结果";
            case 66: return "离线定位结果";
            case 67: return "离线定位失败";
            case 68: return "网络连接失败时，查找本地离线定位时对应的网络连接失败";
            case 161: return "网络定位成功";
            case 162: return "请求串密文解析失败";
            case 167: return "服务端定位失败 - 请检查网络连接和位置权限";
            case 502: return "key参数错误";
            case 505: return "key不存在或者非法";
            case 601: return "key服务被开发者自己禁用";
            case 602: return "key mcode不匹配";
            case 501: return "key验证失败";
            default: return "未知错误";
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        stopLocation();
        callback = null;
        
        // 清理超时处理
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
            timeoutHandler = null;
        }
        
        Log.d(TAG, "LocationManager资源已清理");
    }
}
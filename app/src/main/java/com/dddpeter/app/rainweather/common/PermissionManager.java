package com.dddpeter.app.rainweather.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理类 - Android 15+现代权限处理
 */
public class PermissionManager {
    
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
    
    private final Activity activity;
    private final Context context;
    private PermissionCallback callback;
    
    // 位置权限
    private static final String[] LOCATION_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    // 通知权限
    private static final String NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS;
    
    // 媒体权限（Android 13+）
    private static final String[] MEDIA_PERMISSIONS = {
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };
    
    // 兼容性权限（Android 12及以下）
    private static final String[] LEGACY_STORAGE_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    public PermissionManager(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }
    
    /**
     * 检查并请求位置权限
     */
    public void requestLocationPermissions(PermissionCallback callback) {
        this.callback = callback;
        
        if (hasLocationPermissions()) {
            callback.onPermissionGranted();
            return;
        }
        
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            requestPermissions(permissionsToRequest.toArray(new String[0]));
        }
    }
    
    /**
     * 检查并请求通知权限
     */
    public void requestNotificationPermission(PermissionCallback callback) {
        this.callback = callback;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission()) {
                callback.onPermissionGranted();
                return;
            }
            requestPermissions(new String[]{NOTIFICATION_PERMISSION});
        } else {
            // Android 12及以下不需要通知权限
            callback.onPermissionGranted();
        }
    }
    
    /**
     * 检查并请求媒体权限
     */
    public void requestMediaPermissions(PermissionCallback callback) {
        this.callback = callback;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasMediaPermissions()) {
                callback.onPermissionGranted();
                return;
            }
            requestPermissions(MEDIA_PERMISSIONS);
        } else {
            // Android 12及以下使用传统存储权限
            if (hasLegacyStoragePermissions()) {
                callback.onPermissionGranted();
                return;
            }
            requestPermissions(LEGACY_STORAGE_PERMISSIONS);
        }
    }
    
    /**
     * 检查位置权限
     */
    public boolean hasLocationPermissions() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查通知权限
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, NOTIFICATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 12及以下不需要通知权限
    }
    
    /**
     * 检查媒体权限
     */
    public boolean hasMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : MEDIA_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return hasLegacyStoragePermissions();
        }
    }
    
    /**
     * 检查传统存储权限
     */
    public boolean hasLegacyStoragePermissions() {
        for (String permission : LEGACY_STORAGE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 请求权限
     */
    private void requestPermissions(String[] permissions) {
        // 这里应该使用ActivityResultLauncher，但为了简化，使用传统方式
        // 在实际项目中，建议使用ActivityResultLauncher
        activity.requestPermissions(permissions, 1001);
    }
    
    /**
     * 处理权限请求结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1001 && callback != null) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                callback.onPermissionGranted();
            } else {
                callback.onPermissionDenied();
                showPermissionDeniedMessage();
            }
        }
    }
    
    /**
     * 显示权限被拒绝的消息
     */
    private void showPermissionDeniedMessage() {
        Toast.makeText(context, "需要相关权限才能正常使用应用功能", Toast.LENGTH_LONG).show();
    }
}

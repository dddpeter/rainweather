package com.dddpeter.app.rainweather;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.xuexiang.xui.XUI;

/**
 * 主Activity - 升级到Android 15+现代写法
 */
public class MainActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 启用边到边显示
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // 初始化字体
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        
        super.onCreate(savedInstanceState);
        
        // 设置状态栏和导航栏
        setupSystemBars();
        
        // 初始化缓存
        databaseManager = DatabaseManager.getInstance(this);
        
        // 设置布局
        setContentView(R.layout.activity_main);
        
        // 设置返回按钮处理
        setupBackPressedCallback();
    }
    
    /**
     * 设置系统栏（状态栏和导航栏）
     */
    private void setupSystemBars() {
        // 启用边到边显示
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // 设置状态栏为浅色内容（深色背景）
            controller.setAppearanceLightStatusBars(false);
            // 设置导航栏为浅色内容（深色背景）
            controller.setAppearanceLightNavigationBars(false);
        }
        
        // 设置状态栏和导航栏颜色为透明
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        
        // 处理系统窗口插入，确保内容避开安全区
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            // 获取系统窗口插入
            int statusBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                }
            }
            int navigationBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                }
            }

            // 设置内容区域的padding，避开安全区
            View contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setPadding(0, statusBarHeight, 0, navigationBarHeight);
            }
            
            return insets;
        });
        
        // 保持屏幕常亮（如果需要）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    /**
     * 设置返回按钮处理（Android 15+推荐方式）
     */
    private void setupBackPressedCallback() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 处理返回按钮逻辑
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // 如果没有Fragment在栈中，则关闭Activity
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
    }
}


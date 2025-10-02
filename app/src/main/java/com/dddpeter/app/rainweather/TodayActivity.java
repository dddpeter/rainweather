package com.dddpeter.app.rainweather;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dddpeter.app.rainweather.database.DatabaseManager;
import com.xuexiang.xui.XUI;

import android.view.View;

public class TodayActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initFontStyle("fonts/JetBrainsMono-Medium.ttf");
        super.onCreate(savedInstanceState);
        
        // 设置系统栏
        setupSystemBars();
        
        databaseManager = DatabaseManager.getInstance(this);
        setContentView(R.layout.activity_today);
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
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            
            // 设置内容区域的padding，避开安全区
            View contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setPadding(0, statusBarHeight, 0, navigationBarHeight);
            }
            
            return insets;
        });
    }
}

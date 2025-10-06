package com.dddpeter.app.rainweather.utils

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import timber.log.Timber

/**
 * 加载状态管理器
 * 统一管理各种加载状态的显示和隐藏
 */
class LoadingManager(
    private val context: Context
) {
    
    private var loadingViews = mutableListOf<View>()
    private var contentViews = mutableListOf<View>()
    private var errorViews = mutableListOf<View>()
    
    /**
     * 注册加载视图
     */
    fun registerLoadingViews(vararg views: View) {
        loadingViews.addAll(views)
    }
    
    /**
     * 注册内容视图
     */
    fun registerContentView(vararg views: View) {
        contentViews.addAll(views)
    }
    
    /**
     * 注册错误视图
     */
    fun registerErrorViews(vararg views: View) {
        errorViews.addAll(views)
    }
    
    /**
     * 显示加载状态
     */
    fun showLoading() {
        Timber.d("📱 显示加载状态")
        
        // 显示加载视图
        loadingViews.forEach { view ->
            view.visibility = View.VISIBLE
            if (view is ContentLoadingProgressBar) {
                view.show()
            }
        }
        
        // 隐藏内容视图
        contentViews.forEach { view ->
            view.visibility = View.GONE
        }
        
        // 隐藏错误视图
        errorViews.forEach { view ->
            view.visibility = View.GONE
        }
    }
    
    /**
     * 显示内容
     */
    fun showContent() {
        Timber.d("📱 显示内容")
        
        // 隐藏加载视图
        loadingViews.forEach { view ->
            view.visibility = View.GONE
            if (view is ContentLoadingProgressBar) {
                view.hide()
            }
        }
        
        // 显示内容视图
        contentViews.forEach { view ->
            view.visibility = View.VISIBLE
        }
        
        // 隐藏错误视图
        errorViews.forEach { view ->
            view.visibility = View.GONE
        }
    }
    
    /**
     * 显示错误状态
     */
    fun showError(errorMessage: String? = null) {
        Timber.d("📱 显示错误状态: $errorMessage")
        
        // 隐藏加载视图
        loadingViews.forEach { view ->
            view.visibility = View.GONE
            if (view is ContentLoadingProgressBar) {
                view.hide()
            }
        }
        
        // 隐藏内容视图
        contentViews.forEach { view ->
            view.visibility = View.GONE
        }
        
        // 显示错误视图
        errorViews.forEach { view ->
            view.visibility = View.VISIBLE
            
            // 如果错误视图包含TextView，设置错误信息
            if (view is TextView && errorMessage != null) {
                view.text = errorMessage
            }
        }
    }
    
    /**
     * 设置下拉刷新状态
     */
    fun setRefreshLayoutRefreshing(swipeRefreshLayout: SwipeRefreshLayout, isRefreshing: Boolean) {
        swipeRefreshLayout.isRefreshing = isRefreshing
    }
    
    /**
     * 显示空状态
     */
    fun showEmpty(emptyMessage: String = "暂无数据") {
        Timber.d("📱 显示空状态")
        
        // 隐藏加载视图
        loadingViews.forEach { view ->
            view.visibility = View.GONE
            if (view is ContentLoadingProgressBar) {
                view.hide()
            }
        }
        
        // 隐藏内容视图
        contentViews.forEach { view ->
            view.visibility = View.GONE
        }
        
        // 显示错误视图（空状态复用错误视图）
        errorViews.forEach { view ->
            view.visibility = View.VISIBLE
            
            // 如果错误视图包含TextView，设置空状态信息
            if (view is TextView) {
                view.text = emptyMessage
            }
        }
    }
    
    /**
     * 清除所有注册的视图
     */
    fun clear() {
        loadingViews.clear()
        contentViews.clear()
        errorViews.clear()
    }
    
    companion object {
        /**
         * 创建简单的加载管理器
         */
        fun createSimple(
            context: Context,
            loadingView: View,
            contentView: View,
            errorView: View? = null
        ): LoadingManager {
            return LoadingManager(context).apply {
                registerLoadingViews(loadingView)
                registerContentView(contentView)
                errorView?.let { registerErrorViews(it) }
            }
        }
    }
}

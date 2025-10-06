package com.dddpeter.app.rainweather.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * 现代化Fragment基类 - Android 15+写法
 * 支持ViewBinding和生命周期管理
 */
public abstract class BaseFragment<T extends ViewBinding> extends Fragment {
    
    protected T binding;
    protected LifecycleOwner lifecycleOwner;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleOwner = getViewLifecycleOwner();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = createBinding(inflater, container);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        initData();
        setupObservers();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    /**
     * 创建ViewBinding
     */
    @SuppressWarnings("unchecked")
    private T createBinding(LayoutInflater inflater, ViewGroup container) {
        try {
            Class<T> bindingClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            Method inflateMethod = bindingClass.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            return (T) inflateMethod.invoke(null, inflater, container, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ViewBinding", e);
        }
    }
    
    /**
     * 初始化视图
     */
    protected abstract void initViews();
    
    /**
     * 初始化数据
     */
    protected abstract void initData();
    
    /**
     * 设置观察者
     */
    protected abstract void setupObservers();
    
    /**
     * 获取ViewBinding
     */
    protected T getBinding() {
        return binding;
    }
    
    /**
     * 获取LifecycleOwner
     */
    protected LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }
}

package com.dddpeter.app.rainweather.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dddpeter.app.rainweather.R
import com.dddpeter.app.rainweather.databinding.DialogThemeSelectionBinding
import com.dddpeter.app.rainweather.utils.ThemeManager

/**
 * 主题选择对话框
 */
class ThemeDialog : DialogFragment() {
    
    interface ThemeSelectionListener {
        fun onThemeSelected(theme: String)
    }
    
    private var listener: ThemeSelectionListener? = null
    
    companion object {
        fun newInstance(listener: ThemeSelectionListener? = null): ThemeDialog {
            return ThemeDialog().apply {
                this.listener = listener
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogThemeSelectionBinding.inflate(layoutInflater)
        
        // 设置当前主题
        val currentTheme = ThemeManager.getCurrentTheme()
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.radio_light -> ThemeManager.THEME_LIGHT
                R.id.radio_dark -> ThemeManager.THEME_DARK
                R.id.radio_system -> ThemeManager.THEME_SYSTEM
                else -> ThemeManager.THEME_SYSTEM
            }
            
            // 实时预览主题变化
            ThemeManager.setTheme(selectedTheme)
        }
        
        // 设置当前选中的主题
        val radioButtonId = when (currentTheme) {
            ThemeManager.THEME_LIGHT -> R.id.radio_light
            ThemeManager.THEME_DARK -> R.id.radio_dark
            ThemeManager.THEME_SYSTEM -> R.id.radio_system
            else -> R.id.radio_system
        }
        binding.radioGroupTheme.check(radioButtonId)
        
        return AlertDialog.Builder(requireContext())
            .setTitle("选择主题")
            .setView(binding.root)
            .setPositiveButton("确定") { _, _ ->
                val checkedId = binding.radioGroupTheme.checkedRadioButtonId
                val selectedTheme = when (checkedId) {
                    R.id.radio_light -> ThemeManager.THEME_LIGHT
                    R.id.radio_dark -> ThemeManager.THEME_DARK
                    R.id.radio_system -> ThemeManager.THEME_SYSTEM
                    else -> ThemeManager.THEME_SYSTEM
                }
                listener?.onThemeSelected(selectedTheme)
            }
            .setNegativeButton("取消") { _, _ ->
                // 恢复到原来的主题
                ThemeManager.setTheme(currentTheme)
            }
            .create()
    }
    
    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        // 对话框关闭时，如果用户没有点击确定，恢复到原来的主题
        val currentTheme = ThemeManager.getCurrentTheme()
        ThemeManager.setTheme(currentTheme)
    }
}

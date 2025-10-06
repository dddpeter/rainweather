package com.dddpeter.app.rainweather.ui.dialogs

import android.content.Context
import com.dddpeter.app.rainweather.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 权限相关对话框
 */
object PermissionDialogs {
    
    /**
     * 显示权限说明对话框（首次请求前）
     */
    fun showLocationPermissionRationale(
        context: Context,
        onAllow: () -> Unit,
        onDeny: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.permission_location_title)
            .setMessage(R.string.permission_location_message)
            .setIcon(android.R.drawable.ic_dialog_map)
            .setPositiveButton(R.string.permission_button_allow) { dialog, _ ->
                dialog.dismiss()
                onAllow()
            }
            .setNegativeButton(R.string.permission_button_deny) { dialog, _ ->
                dialog.dismiss()
                onDeny()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 显示权限被拒绝后的说明对话框
     */
    fun showLocationPermissionDeniedDialog(
        context: Context,
        onRetry: () -> Unit,
        onCancel: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.permission_location_title)
            .setMessage(R.string.permission_location_denied_message)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(R.string.permission_button_retry) { dialog, _ ->
                dialog.dismiss()
                onRetry()
            }
            .setNegativeButton(R.string.permission_button_continue) { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .setCancelable(true)
            .show()
    }
    
    /**
     * 显示权限被永久拒绝的对话框
     */
    fun showLocationPermissionPermanentlyDeniedDialog(
        context: Context,
        onOpenSettings: () -> Unit,
        onCancel: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.permission_location_permanently_denied_title)
            .setMessage(R.string.permission_location_permanently_denied_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.permission_button_settings) { dialog, _ ->
                dialog.dismiss()
                onOpenSettings()
            }
            .setNegativeButton(R.string.permission_button_continue) { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .setCancelable(true)
            .show()
    }
    
    /**
     * 显示位置服务未开启的对话框
     */
    fun showLocationServiceDisabledDialog(
        context: Context,
        onOpenSettings: () -> Unit,
        onCancel: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle("位置服务未开启")
            .setMessage("请在系统设置中开启位置服务，以便获取您所在位置的天气信息。")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.permission_button_settings) { dialog, _ ->
                dialog.dismiss()
                onOpenSettings()
            }
            .setNegativeButton(R.string.permission_button_cancel) { dialog, _ ->
                dialog.dismiss()
                onCancel()
            }
            .setCancelable(true)
            .show()
    }
}


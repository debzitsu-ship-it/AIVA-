package com.aiva.security

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionManager(private val context: Context) {

    private val _permissionStates = MutableStateFlow<Map<String, PermissionState>>(emptyMap())
    val permissionStates: StateFlow<Map<String, PermissionState>> = _permissionStates

    fun checkAllPermissions(): Map<String, PermissionState> {
        val states = mapOf(
            "android.permission.RECORD_AUDIO" to PermissionState(
                permission = "android.permission.RECORD_AUDIO",
                granted = false,
                rationale = "Required for voice input",
                isSpecial = false
            )
        )
        _permissionStates.value = states
        return states
    }

    fun requestPermissions(activity: Activity) {
        // Runtime requests are handled by the onboarding activity.
    }

    fun requestSpecialPermission(activity: Activity, type: SpecialPermission) {
        val action = when (type) {
            SpecialPermission.ACCESSIBILITY -> android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
            SpecialPermission.SYSTEM_ALERT_WINDOW -> android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            SpecialPermission.PICTURE_IN_PICTURE -> android.provider.Settings.ACTION_SETTINGS
            SpecialPermission.MEDIA_PROJECTION -> null
        }
        if (action != null) {
            activity.startActivity(android.content.Intent(action))
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) = Unit

    fun isPermissionGranted(permission: String): Boolean =
        _permissionStates.value[permission]?.granted ?: false

    fun areAllRequiredPermissionsGranted(): Boolean = false

    enum class SpecialPermission {
        ACCESSIBILITY,
        SYSTEM_ALERT_WINDOW,
        PICTURE_IN_PICTURE,
        MEDIA_PROJECTION
    }

    data class PermissionState(
        val permission: String,
        val granted: Boolean,
        val rationale: String,
        val isSpecial: Boolean
    )
}

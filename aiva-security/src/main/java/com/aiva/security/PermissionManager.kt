package com.aiva.security

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    
    private val _permissionStates = MutableStateFlow<Map<String, PermissionState>>(emptyMap())
    val permissionStates: StateFlow<Map<String, PermissionState>> = _permissionStates
    
    private val requiredPermissions = listOf(
        PermissionInfo(
            permission = Manifest.permission.RECORD_AUDIO,
            rationale = "Required for voice input and speech recognition",
            requestCode = 1001
        ),
        PermissionInfo(
            permission = Manifest.permission.FOREGROUND_SERVICE,
            rationale = "Required for background AI processing",
            requestCode = 1002,
            isSpecial = true
        ),
        PermissionInfo(
            permission = Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION,
            rationale = "Required for screen capture and observation",
            requestCode = 1003,
            minSdk = Build.VERSION_CODES.Q
        ),
        PermissionInfo(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            rationale = "Required for foreground service notifications",
            requestCode = 1004,
            minSdk = Build.VERSION_CODES.TIRAMISU
        )
    )
    
    private val specialPermissions = listOf(
        SpecialPermissionInfo(
            type = SpecialPermission.ACCESSIBILITY,
            rationale = "Required for screen reading and Android automation",
            settingsAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
        ),
        SpecialPermissionInfo(
            type = SpecialPermission.SYSTEM_ALERT_WINDOW,
            rationale = "Required for floating Mini UI overlay",
            settingsAction = Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            minSdk = Build.VERSION_CODES.M
        ),
        SpecialPermissionInfo(
            type = SpecialPermission.PICTURE_IN_PICTURE,
            rationale = "Required for persistent floating window",
            settingsAction = Settings.ACTION_SETTINGS,
            minSdk = Build.VERSION_CODES.O
        ),
        SpecialPermissionInfo(
            type = SpecialPermission.MEDIA_PROJECTION,
            rationale = "Required for screen capture",
            settingsAction = null,
            minSdk = Build.VERSION_CODES.LOLLIPOP
        )
    )
    
    fun checkAllPermissions(): Map<String, PermissionState> {
        val states = mutableMapOf<String, PermissionState>()
        
        // Check regular permissions
        requiredPermissions.forEach { info ->
            if (info.minSdk <= Build.VERSION.SDK_INT) {
                val granted = ContextCompat.checkSelfPermission(context, info.permission) ==
                    PackageManager.PERMISSION_GRANTED
                states[info.permission] = PermissionState(
                    permission = info.permission,
                    granted = granted,
                    rationale = info.rationale,
                    isSpecial = false
                )
            }
        }
        
        // Check special permissions
        specialPermissions.forEach { info ->
            if (info.minSdk <= Build.VERSION.SDK_INT) {
                val granted = when (info.type) {
                    SpecialPermission.ACCESSIBILITY -> isAccessibilityEnabled()
                    SpecialPermission.SYSTEM_ALERT_WINDOW -> Settings.canDrawOverlays(context)
                    SpecialPermission.PICTURE_IN_PICTURE -> true // Checked at runtime
                    SpecialPermission.MEDIA_PROJECTION -> false // Always requires runtime grant
                }
                states[info.type.name] = PermissionState(
                    permission = info.type.name,
                    granted = granted,
                    rationale = info.rationale,
                    isSpecial = true
                )
            }
        }
        
        _permissionStates.value = states
        return states
    }
    
    fun requestPermissions(activity: Activity) {
        val permissionsToRequest = requiredPermissions
            .filter { it.minSdk <= Build.VERSION.SDK_INT }
            .filter { ContextCompat.checkSelfPermission(context, it.permission) != PackageManager.PERMISSION_GRANTED }
            .map { it.permission }
            .toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest, 1000)
        }
    }
    
    fun requestSpecialPermission(activity: Activity, type: SpecialPermission) {
        when (type) {
            SpecialPermission.ACCESSIBILITY -> {
                val intent = android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                activity.startActivity(intent)
            }
            SpecialPermission.SYSTEM_ALERT_WINDOW -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = android.content.Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${context.packageName}"))
                    activity.startActivityForResult(intent, 1005)
                }
            }
            SpecialPermission.PICTURE_IN_PICTURE -> {
                // Handled by entering PiP mode programmatically
            }
            SpecialPermission.MEDIA_PROJECTION -> {
                // Handled by MediaProjectionManager.createScreenCaptureIntent()
            }
        }
    }
    
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissions.forEachIndexed { index, permission ->
            val granted = grantResults[index] == PackageManager.PERMISSION_GRANTED
            _permissionStates.value = _permissionStates.value + (permission to PermissionState(
                permission = permission,
                granted = granted,
                rationale = requiredPermissions.find { it.permission == permission }?.rationale ?: "",
                isSpecial = false
            ))
        }
    }
    
    private fun isAccessibilityEnabled(): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = accessibilityManager.enabledAccessibilityServiceList ?: return false
        return enabledServices.any { it.id.contains(context.packageName) }
    }
    
    fun isPermissionGranted(permission: String): Boolean {
        return _permissionStates.value[permission]?.granted ?: false
    }
    
    fun areAllRequiredPermissionsGranted(): Boolean {
        return requiredPermissions
            .filter { it.minSdk <= Build.VERSION.SDK_INT }
            .all { ContextCompat.checkSelfPermission(context, it.permission) == PackageManager.PERMISSION_GRANTED }
    }
    
    data class PermissionInfo(
        val permission: String,
        val rationale: String,
        val requestCode: Int,
        val isSpecial: Boolean = false,
        val minSdk: Int = Build.VERSION_CODES.BASE
    )
    
    enum class SpecialPermission {
        ACCESSIBILITY,
        SYSTEM_ALERT_WINDOW,
        PICTURE_IN_PICTURE,
        MEDIA_PROJECTION
    }
    
    data class SpecialPermissionInfo(
        val type: SpecialPermission,
        val rationale: String,
        val settingsAction: String?,
        val minSdk: Int = Build.VERSION_CODES.BASE
    )
    
    data class PermissionState(
        val permission: String,
        val granted: Boolean,
        val rationale: String,
        val isSpecial: Boolean
    )
}
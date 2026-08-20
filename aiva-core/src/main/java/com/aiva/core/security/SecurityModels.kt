package com.aiva.core.security

import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyEntry(
    val id: String,
    val name: String,
    val keyHash: String,
    val encryptedKey: String,
    val models: List<String>,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTested: Long? = null,
    val testResult: KeyTestResult? = null
)

@Serializable
enum class KeyTestResult {
    UNTESTED,
    TESTING,
    SUCCESS,
    FAILED,
    INVALID_KEY,
    RATE_LIMITED,
    NETWORK_ERROR
}

@Serializable
data class SecurityConfig(
    val biometricRequired: Boolean = false,
    val autoLockTimeout: Long = 300000,
    val clearMemoryOnLock: Boolean = true,
    val allowExport: Boolean = false
)

@Serializable
sealed interface SecurityEvent {
    @Serializable
    data class KeyAccessed(val keyId: String) : SecurityEvent
    @Serializable
    data class KeyTested(val keyId: String, val success: Boolean) : SecurityEvent
    @Serializable
    data class KeyAdded(val keyId: String) : SecurityEvent
    @Serializable
    data class KeyRemoved(val keyId: String) : SecurityEvent
    @Serializable
    data class FailedAttempt(val reason: String) : SecurityEvent
    @Serializable
    data class BiometricPromptShown(val unused: Int = 0) : SecurityEvent
    @Serializable
    data class BiometricSuccess(val unused: Int = 0) : SecurityEvent
    @Serializable
    data class BiometricFailed(val unused: Int = 0) : SecurityEvent
}

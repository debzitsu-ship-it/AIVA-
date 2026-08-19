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

sealed interface SecurityEvent {
    data class KeyAccessed(val keyId: String) : SecurityEvent
    data class KeyTested(val keyId: String, val success: Boolean) : SecurityEvent
    data class KeyAdded(val keyId: String) : SecurityEvent
    data class KeyRemoved(val keyId: String) : SecurityEvent
    data class FailedAttempt(val reason: String) : SecurityEvent
    data class BiometricPromptShown : SecurityEvent
    data class BiometricSuccess : SecurityEvent
    data class BiometricFailed : SecurityEvent
}
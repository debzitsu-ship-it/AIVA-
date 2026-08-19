package com.aiva.security

import android.content.Context
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.KeyTestResult
import com.aiva.core.security.SecurityConfig
import com.aiva.core.security.SecurityEvent
import com.aiva.core.util.JsonUtil
import com.aiva.core.util.KeyStoreManager
import com.aiva.core.util.SecureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class ApiKeyManager(private val context: Context) {
    companion object {
        private const val KEYS_KEY = "api_keys"
        private const val CONFIG_KEY = "security_config"
        private const val EVENTS_KEY = "security_events"
    }

    private val _keys = MutableStateFlow<Map<String, ApiKeyEntry>>(emptyMap())
    val keys: StateFlow<Map<String, ApiKeyEntry>> = _keys

    private val _config = MutableStateFlow(SecurityConfig())
    val config: StateFlow<SecurityConfig> = _config

    private val _events = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val events: StateFlow<List<SecurityEvent>> = _events

    init {
        SecureStorage.init(context)
        loadKeys()
        loadConfig()
    }

    private fun loadKeys() {
        val raw = SecureStorage.getString(KEYS_KEY, "{}")
        _keys.value = runCatching {
            JsonUtil.json.decodeFromString(
                MapSerializer(String.serializer(), ApiKeyEntry.serializer()),
                raw
            )
        }.getOrDefault(emptyMap())
    }

    private fun saveKeys() {
        val json = JsonUtil.json.encodeToString(
            MapSerializer(String.serializer(), ApiKeyEntry.serializer()),
            _keys.value
        )
        SecureStorage.putString(KEYS_KEY, json)
    }

    private fun loadConfig() {
        val raw = SecureStorage.getString(CONFIG_KEY, "{}")
        _config.value = runCatching {
            JsonUtil.json.decodeFromString(SecurityConfig.serializer(), raw)
        }.getOrDefault(SecurityConfig())
    }

    private fun saveConfig() {
        SecureStorage.putString(
            CONFIG_KEY,
            JsonUtil.json.encodeToString(SecurityConfig.serializer(), _config.value)
        )
    }

    fun addKey(name: String, plainKey: String, models: List<String>): ApiKeyEntry {
        val id = java.util.UUID.randomUUID().toString()
        val entry = ApiKeyEntry(
            id = id,
            name = name,
            keyHash = hashKey(plainKey),
            encryptedKey = KeyStoreManager.encryptString(plainKey),
            models = models
        )
        _keys.value = _keys.value + (id to entry)
        saveKeys()
        logEvent(SecurityEvent.KeyAdded(id))
        return entry
    }

    fun replaceKey(id: String, plainKey: String, models: List<String>? = null): Boolean {
        val existing = _keys.value[id] ?: return false
        val updated = existing.copy(
            keyHash = hashKey(plainKey),
            encryptedKey = KeyStoreManager.encryptString(plainKey),
            models = models ?: existing.models,
            lastTested = null,
            testResult = null
        )
        _keys.value = _keys.value + (id to updated)
        saveKeys()
        return true
    }

    fun deleteKey(id: String): Boolean {
        if (!_keys.value.contains(id)) return false
        _keys.value = _keys.value - id
        saveKeys()
        logEvent(SecurityEvent.KeyRemoved(id))
        return true
    }

    fun getKey(id: String): ApiKeyEntry? {
        val entry = _keys.value[id]
        if (entry != null) logEvent(SecurityEvent.KeyAccessed(id))
        return entry
    }

    fun getDecryptedKey(id: String): String? {
        return _keys.value[id]?.let { entry ->
            logEvent(SecurityEvent.KeyAccessed(id))
            KeyStoreManager.decryptString(entry.encryptedKey)
        }
    }

    fun testKey(id: String, testCallback: (KeyTestResult) -> Unit) {
        val entry = _keys.value[id] ?: return
        _keys.value = _keys.value + (id to entry.copy(testResult = KeyTestResult.TESTING))
        saveKeys()
        CoroutineScope(Dispatchers.IO).launch {
            val result = performKeyTest(entry)
            _keys.value = _keys.value + (id to entry.copy(
                lastTested = System.currentTimeMillis(),
                testResult = result
            ))
            saveKeys()
            logEvent(SecurityEvent.KeyTested(id, result == KeyTestResult.SUCCESS))
            testCallback(result)
        }
    }

    private fun performKeyTest(entry: ApiKeyEntry): KeyTestResult {
        val key = runCatching { KeyStoreManager.decryptString(entry.encryptedKey) }.getOrDefault("")
        return if (key.startsWith("nvapi-") && key.length > 20) {
            KeyTestResult.SUCCESS
        } else {
            KeyTestResult.INVALID_KEY
        }
    }

    fun enableModel(keyId: String, modelId: String): Boolean {
        val entry = _keys.value[keyId] ?: return false
        if (entry.models.contains(modelId)) return false
        _keys.value = _keys.value + (keyId to entry.copy(models = entry.models + modelId))
        saveKeys()
        return true
    }

    fun disableModel(keyId: String, modelId: String): Boolean {
        val entry = _keys.value[keyId] ?: return false
        if (!entry.models.contains(modelId)) return false
        _keys.value = _keys.value + (keyId to entry.copy(models = entry.models - modelId))
        saveKeys()
        return true
    }

    fun getKeysForModel(modelId: String): List<ApiKeyEntry> {
        return _keys.value.values.filter { it.enabled && it.models.contains(modelId) }
    }

    fun getAllKeys(): List<ApiKeyEntry> = _keys.value.values.toList()

    fun updateConfig(config: SecurityConfig) {
        _config.value = config
        saveConfig()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        SecureStorage.getBoolean(key, defaultValue)

    fun setBoolean(key: String, value: Boolean) {
        SecureStorage.putBoolean(key, value)
    }

    fun getString(key: String, defaultValue: String = ""): String =
        SecureStorage.getString(key, defaultValue)

    fun setString(key: String, value: String) {
        SecureStorage.putString(key, value)
    }

    private fun hashKey(key: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun logEvent(event: SecurityEvent) {
        _events.value = (_events.value + event).takeLast(100)
        val json = JsonUtil.json.encodeToString(
            ListSerializer(SecurityEvent.serializer()),
            _events.value
        )
        SecureStorage.putString(EVENTS_KEY, json)
    }

    fun clearAllData() {
        _keys.value = emptyMap()
        _events.value = emptyList()
        SecureStorage.clear()
        SecureStorage.init(context)
        saveKeys()
        logEvent(SecurityEvent.FailedAttempt("All data cleared by user"))
    }
}

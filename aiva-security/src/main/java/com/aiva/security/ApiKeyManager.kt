package com.aiva.security

import android.content.Context
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.KeyTestResult
import com.aiva.core.security.SecurityConfig
import com.aiva.core.security.SecurityEvent
import com.aiva.core.util.JsonUtil
import com.aiva.core.util.SecureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor(
    private val context: Context
) {
    
    private const val KEYS_KEY = "api_keys"
    private const val CONFIG_KEY = "security_config"
    private const val EVENTS_KEY = "security_events"
    
    private val _keys = MutableStateFlow<Map<String, ApiKeyEntry>>(emptyMap())
    val keys: StateFlow<Map<String, ApiKeyEntry>> = _keys
    
    private val _config = MutableStateFlow<SecurityConfig>(SecurityConfig())
    val config: StateFlow<SecurityConfig> = _config
    
    private val _events = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val events: StateFlow<List<SecurityEvent>> = _events
    
    init {
        SecureStorage.init(context)
        loadKeys()
        loadConfig()
    }
    
    private fun loadKeys() {
        val json = SecureStorage.getString(KEYS_KEY, "{}")
        val map = JsonUtil.json.decodeFromString(
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .getSerializersModule()
                .getMapSerializer(
                    kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("String", kotlinx.serialization.descriptors.PrimitiveKind.STRING),
                    ApiKeyEntry.serializer()
                ),
            json
        )
        _keys.value = map
    }
    
    private fun saveKeys() {
        val json = JsonUtil.json.encodeToString(
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .getSerializersModule()
                .getMapSerializer(
                    kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("String", kotlinx.serialization.descriptors.PrimitiveKind.STRING),
                    ApiKeyEntry.serializer()
                ),
            _keys.value
        )
        SecureStorage.putString(KEYS_KEY, json)
    }
    
    private fun loadConfig() {
        val json = SecureStorage.getString(CONFIG_KEY, "{}")
        _config.value = JsonUtil.json.decodeFromString(SecurityConfig.serializer(), json)
    }
    
    private fun saveConfig() {
        val json = JsonUtil.json.encodeToString(SecurityConfig.serializer(), _config.value)
        SecureStorage.putString(CONFIG_KEY, json)
    }
    
    fun addKey(
        name: String,
        plainKey: String,
        models: List<String>
    ): ApiKeyEntry {
        val id = java.util.UUID.randomUUID().toString()
        val encryptedKey = com.aiva.core.util.KeyStoreManager.encryptString(plainKey)
        val keyHash = hashKey(plainKey)
        
        val entry = ApiKeyEntry(
            id = id,
            name = name,
            keyHash = keyHash,
            encryptedKey = encryptedKey,
            models = models
        )
        
        _keys.value = _keys.value + (id to entry)
        saveKeys()
        logEvent(SecurityEvent.KeyAdded(id))
        
        return entry
    }
    
    fun replaceKey(id: String, plainKey: String, models: List<String>? = null): Boolean {
        _keys.value[id]?.let { existing ->
            val encryptedKey = com.aiva.core.util.KeyStoreManager.encryptString(plainKey)
            val keyHash = hashKey(plainKey)
            
            val updated = existing.copy(
                keyHash = keyHash,
                encryptedKey = encryptedKey,
                models = models ?: existing.models,
                lastTested = null,
                testResult = null
            )
            
            _keys.value = _keys.value + (id to updated)
            saveKeys()
            return true
        }
        return false
    }
    
    fun deleteKey(id: String): Boolean {
        if (_keys.value.contains(id)) {
            _keys.value = _keys.value - id
            saveKeys()
            logEvent(SecurityEvent.KeyRemoved(id))
            return true
        }
        return false
    }
    
    fun getKey(id: String): ApiKeyEntry? {
        val entry = _keys.value[id]
        entry?.let { logEvent(SecurityEvent.KeyAccessed(id)) }
        return entry
    }
    
    fun getDecryptedKey(id: String): String? {
        return _keys.value[id]?.let { entry ->
            logEvent(SecurityEvent.KeyAccessed(id))
            com.aiva.core.util.KeyStoreManager.decryptString(entry.encryptedKey)
        }
    }
    
    fun testKey(id: String, testCallback: (KeyTestResult) -> Unit) {
        _keys.value[id]?.let { entry ->
            _keys.value = _keys.value + (id to entry.copy(testResult = KeyTestResult.TESTING))
            saveKeys()
            logEvent(SecurityEvent.KeyTested(id, false))
            
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
    }
    
    private suspend fun performKeyTest(entry: ApiKeyEntry): KeyTestResult {
        // This would call the NIM API with a simple test request
        // For now, return success if key format looks valid
        val key = com.aiva.core.util.KeyStoreManager.decryptString(entry.encryptedKey)
        return if (key.startsWith("nvapi-") && key.length > 20) {
            KeyTestResult.SUCCESS
        } else {
            KeyTestResult.INVALID_KEY
        }
    }
    
    fun enableModel(keyId: String, modelId: String): Boolean {
        _keys.value[keyId]?.let { entry ->
            if (!entry.models.contains(modelId)) {
                val updated = entry.copy(models = entry.models + modelId)
                _keys.value = _keys.value + (keyId to updated)
                saveKeys()
                return true
            }
        }
        return false
    }
    
    fun disableModel(keyId: String, modelId: String): Boolean {
        _keys.value[keyId]?.let { entry ->
            if (entry.models.contains(modelId)) {
                val updated = entry.copy(models = entry.models - modelId)
                _keys.value = _keys.value + (keyId to updated)
                saveKeys()
                return true
            }
        }
        return false
    }
    
    fun getKeysForModel(modelId: String): List<ApiKeyEntry> {
        return _keys.value.values
            .filter { it.enabled && it.models.contains(modelId) }
            .toList()
    }
    
    fun getAllKeys(): List<ApiKeyEntry> {
        return _keys.value.values.toList()
    }
    
    fun updateConfig(config: SecurityConfig) {
        _config.value = config
        saveConfig()
    }
    
    private fun hashKey(key: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    private fun logEvent(event: SecurityEvent) {
        val current = _events.value
        _events.value = (current + event).takeLast(100)
        
        val json = JsonUtil.json.encodeToString(
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .getSerializersModule()
                .getListSerializer(SecurityEvent.serializer()),
            _events.value
        )
        SecureStorage.putString(EVENTS_KEY, json)
    }
    
    fun clearAllData() {
        _keys.value = emptyMap()
        _events.value = emptyList()
        SecureStorage.clear()
        // Re-initialize
        SecureStorage.init(context)
        saveKeys()
        logEvent(SecurityEvent.FailedAttempt("All data cleared by user"))
    }
}
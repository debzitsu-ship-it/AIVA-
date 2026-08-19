package com.aiva.core.security

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.KeyTestResult
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecurityTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testApiKeyEntrySerialization() {
        val entry = ApiKeyEntry(
            id = "test-key",
            name = "Test Key",
            keyHash = "abc123",
            encryptedKey = "encrypted",
            models = listOf("model1", "model2"),
            enabled = true
        )
        
        val jsonStr = json.encodeToString(entry)
        val decoded = json.decodeFromString(ApiKeyEntry.serializer(), jsonStr)
        assertEquals(entry, decoded)
    }
    
    @Test
    fun testKeyTestResultValues() {
        val results = KeyTestResult.values()
        assertEquals(7, results.size)
        assertTrue(results.contains(KeyTestResult.UNTESTED))
        assertTrue(results.contains(KeyTestResult.TESTING))
        assertTrue(results.contains(KeyTestResult.SUCCESS))
        assertTrue(results.contains(KeyTestResult.FAILED))
        assertTrue(results.contains(KeyTestResult.INVALID_KEY))
        assertTrue(results.contains(KeyTestResult.RATE_LIMITED))
        assertTrue(results.contains(KeyTestResult.NETWORK_ERROR))
    }
    
    @Test
    fun testSecurityEventSerialization() {
        val events = listOf(
            SecurityEvent.KeyAccessed("key1"),
            SecurityEvent.KeyTested("key2", true),
            SecurityEvent.KeyAdded("key3"),
            SecurityEvent.KeyRemoved("key4"),
            SecurityEvent.FailedAttempt("reason"),
            SecurityEvent.BiometricPromptShown(),
            SecurityEvent.BiometricSuccess(),
            SecurityEvent.BiometricFailed()
        )
        
        events.forEach { event ->
            val jsonStr = json.encodeToString(event)
            val decoded = json.decodeFromString(SecurityEvent.serializer(), jsonStr)
            assertEquals(event, decoded)
        }
    }
}
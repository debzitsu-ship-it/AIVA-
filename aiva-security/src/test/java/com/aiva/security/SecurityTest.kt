package com.aiva.security

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.KeyTestResult
import com.aiva.core.util.KeyStoreManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SecurityTest {
    
    @Test
    fun testKeyStoreEncryptDecrypt() {
        val original = "test-api-key-nvapi-1234567890"
        val encrypted = KeyStoreManager.encryptString(original)
        val decrypted = KeyStoreManager.decryptString(encrypted)
        assertEquals(original, decrypted)
    }
    
    @Test
    fun testKeyStoreEncryptDecryptBytes() {
        val original = "test data".toByteArray()
        val encrypted = KeyStoreManager.encrypt(original)
        val decrypted = KeyStoreManager.decrypt(encrypted)
        assertEquals(original, decrypted)
    }
    
    @Test
    fun testKeyStoreDifferentValues() {
        val values = listOf(
            "nvapi-short",
            "nvapi-" + "a".repeat(100),
            "special!@#$%^&*()chars",
            "unicode: 🎉🤖🔐",
            ""
        )
        
        values.forEach { value ->
            val encrypted = KeyStoreManager.encryptString(value)
            val decrypted = KeyStoreManager.decryptString(encrypted)
            assertEquals(value, decrypted)
        }
    }
    
    @Test
    fun testApiKeyEntryHash() {
        val key = "nvapi-test-key-12345"
        val hash1 = KeyStoreManager.encryptString(key)
        val hash2 = KeyStoreManager.encryptString(key)
        // Same input should produce same encrypted output (deterministic with same key)
        // Note: In practice, GCM mode adds randomness, so they won't be equal
        // But both should decrypt correctly
        assertEquals(key, KeyStoreManager.decryptString(hash1))
        assertEquals(key, KeyStoreManager.decryptString(hash2))
    }
}
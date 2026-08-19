package com.aiva.security

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.KeyTestResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecurityTest {

    @Test
    fun testApiKeyEntryDefaults() {
        val entry = ApiKeyEntry(
            id = "1",
            name = "key",
            keyHash = "hash",
            encryptedKey = "enc",
            models = listOf("model")
        )
        assertTrue(entry.enabled)
        assertEquals("1", entry.id)
    }

    @Test
    fun testKeyTestResultValues() {
        assertEquals(7, KeyTestResult.entries.size)
        assertTrue(KeyTestResult.entries.contains(KeyTestResult.SUCCESS))
    }
}

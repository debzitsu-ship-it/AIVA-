package com.aiva.core.security

import kotlin.test.Test
import kotlin.test.assertTrue

class SecurityTest {
    @Test
    fun keyResultsExist() {
        assertTrue(KeyTestResult.entries.contains(KeyTestResult.SUCCESS))
    }
}

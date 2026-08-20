package com.aiva.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelTest {
    @Test
    fun chatMessage() {
        assertEquals("user", ChatMessage(role = "user", content = "hi").role)
    }
}

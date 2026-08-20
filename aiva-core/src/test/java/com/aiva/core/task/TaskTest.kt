package com.aiva.core.task

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskTest {
    @Test
    fun intentDefaults() {
        val intent = Intent(IntentType.CHAT, 0.5f, "hello")
        assertEquals(IntentType.CHAT, intent.type)
    }
}

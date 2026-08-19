package com.aiva.task

import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType
import com.aiva.core.task.TaskState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskTest {

    @Test
    fun testIntentDefaults() {
        val intent = Intent(IntentType.CHAT, 0.5f, "hello")
        assertEquals(IntentType.CHAT, intent.type)
        assertEquals(0.5f, intent.confidence)
        assertTrue(intent.extractedEntities.isEmpty())
    }

    @Test
    fun testTaskStates() {
        assertEquals(11, TaskState.entries.size)
        assertTrue(TaskState.entries.contains(TaskState.IDLE))
        assertTrue(TaskState.entries.contains(TaskState.SUCCESS))
    }
}

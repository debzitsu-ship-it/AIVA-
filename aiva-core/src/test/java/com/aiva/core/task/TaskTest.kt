package com.aiva.core.task

import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType
import com.aiva.core.task.TaskState
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testIntentSerialization() {
        val intent = Intent(
            type = IntentType.ACTION,
            confidence = 0.9f,
            originalQuery = "Open Settings",
            extractedEntities = mapOf("app" to "Settings"),
            requiresConfirmation = false,
            targetApp = "com.android.settings"
        )
        
        val jsonStr = json.encodeToString(intent)
        val decoded = json.decodeFromString(Intent.serializer(), jsonStr)
        assertEquals(intent, decoded)
    }
    
    @Test
    fun testTaskStateOrdering() {
        // Verify all states exist
        val states = TaskState.values()
        assertEquals(11, states.size)
        assertTrue(states.contains(TaskState.IDLE))
        assertTrue(states.contains(TaskState.LISTENING))
        assertTrue(states.contains(TaskState.UNDERSTANDING))
        assertTrue(states.contains(TaskState.PLANNING))
        assertTrue(states.contains(TaskState.OBSERVING))
        assertTrue(states.contains(TaskState.ACTING))
        assertTrue(states.contains(TaskState.VERIFYING))
        assertTrue(states.contains(TaskState.WAITING))
        assertTrue(states.contains(TaskState.SUCCESS))
        assertTrue(states.contains(TaskState.ERROR))
        assertTrue(states.contains(TaskState.STOPPED))
    }
}
package com.aiva.core.model

import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.model.ReasoningLevel
import com.aiva.core.model.SpeedProfile
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testModelInfoEquality() {
        val model1 = ModelInfo(
            id = "test/model",
            displayName = "Test Model",
            provider = "Test",
            speedProfile = SpeedProfile.FAST,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        )
        val model2 = ModelInfo(
            id = "test/model",
            displayName = "Test Model",
            provider = "Test",
            speedProfile = SpeedProfile.FAST,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        )
        assertEquals(model1, model2)
    }
    
    @Test
    fun testChatCompletionRequestSerialization() {
        val request = ChatCompletionRequest(
            model = "test/model",
            messages = listOf(
                ChatMessage(role = "system", content = "You are helpful"),
                ChatMessage(role = "user", content = "Hello")
            ),
            temperature = 0.7f,
            topP = 0.9f,
            maxTokens = 4096,
            stream = true
        )
        
        val jsonStr = json.encodeToString(request)
        val decoded = json.decodeFromString(ChatCompletionRequest.serializer(), jsonStr)
        assertEquals(request, decoded)
    }
    
    @Test
    fun testChatMessageWithReasoning() {
        val message = ChatMessage(
            role = "assistant",
            content = "Final answer",
            reasoningContent = "Let me think..."
        )
        
        val jsonStr = json.encodeToString(message)
        val decoded = json.decodeFromString(ChatMessage.serializer(), jsonStr)
        assertEquals(message, decoded)
    }
    
    @Test
    fun testSpeedProfileOrdering() {
        assertTrue(SpeedProfile.FASTEST.ordinal < SpeedProfile.FAST.ordinal)
        assertTrue(SpeedProfile.FAST.ordinal < SpeedProfile.BALANCED.ordinal)
        assertTrue(SpeedProfile.BALANCED.ordinal < SpeedProfile.SLOW.ordinal)
    }
    
    @Test
    fun testReasoningLevelOrdering() {
        assertTrue(ReasoningLevel.NONE.ordinal < ReasoningLevel.BASIC.ordinal)
        assertTrue(ReasoningLevel.BASIC.ordinal < ReasoningLevel.ADVANCED.ordinal)
        assertTrue(ReasoningLevel.ADVANCED.ordinal < ReasoningLevel.EXPERT.ordinal)
    }
}
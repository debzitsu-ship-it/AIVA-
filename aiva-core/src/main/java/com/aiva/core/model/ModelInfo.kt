package com.aiva.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelInfo(
    val id: String,
    val displayName: String,
    val provider: String,
    val speedProfile: SpeedProfile,
    val reasoningCapability: ReasoningLevel,
    val agentSuitability: Boolean,
    val visionCapability: Boolean,
    val streamingCapability: Boolean,
    val enabled: Boolean = true
)

enum class SpeedProfile {
    FASTEST,
    FAST,
    BALANCED,
    SLOW
}

enum class ReasoningLevel {
    NONE,
    BASIC,
    ADVANCED,
    EXPERT
}

@Serializable
data class ModelCapabilities(
    val supportsStreaming: Boolean,
    val supportsVision: Boolean,
    val supportsReasoning: Boolean,
    val supportsAgent: Boolean,
    val maxTokens: Int,
    val supportsChatTemplateKwargs: Boolean,
    val supportsReasoningBudget: Boolean
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String?,
    val reasoningContent: String? = null
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 1.0f,
    val topP: Float = 1.0f,
    val maxTokens: Int = 16384,
    val seed: Long? = null,
    val stream: Boolean = false,
    val chatTemplateKwargs: Map<String, Boolean>? = null,
    val reasoningBudget: Int? = null
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val created: Long,
    val model: String,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage?,
    val delta: ChatMessage?,
    val finishReason: String?
)

@Serializable
data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

@Serializable
data class StreamChunk(
    val id: String,
    val choices: List<StreamChoice>,
    val created: Long,
    val model: String
)

@Serializable
data class StreamChoice(
    val index: Int,
    val delta: ChatMessage,
    val finishReason: String?
)
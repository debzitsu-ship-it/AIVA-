package com.aiva.memory.db

import com.aiva.core.model.ChatMessage
import kotlinx.serialization.json.Json

data class ConversationEntity(
    val id: String,
    val title: String,
    val modelId: String,
    val messagesJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false
) {
    fun toMessages(): List<ChatMessage> {
        return runCatching {
            Json { ignoreUnknownKeys = true }.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(ChatMessage.serializer()),
                messagesJson
            )
        }.getOrDefault(emptyList())
    }

    companion object {
        fun fromMessages(
            id: String,
            title: String,
            modelId: String,
            messages: List<ChatMessage>
        ): ConversationEntity {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(ChatMessage.serializer()),
                messages
            )
            val now = System.currentTimeMillis()
            return ConversationEntity(
                id = id,
                title = title,
                modelId = modelId,
                messagesJson = json,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

data class TaskHistoryEntity(
    val id: String,
    val intentType: String,
    val originalQuery: String,
    val planJson: String,
    val resultJson: String,
    val state: String,
    val startedAt: Long,
    val completedAt: Long?,
    val duration: Long,
    val modelUsed: String
)

data class UserPreferenceEntity(
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class GameProfileEntity(
    val id: String,
    val name: String,
    val packageName: String,
    val profileJson: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ApiUsageEntity(
    val id: Long = 0,
    val modelId: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val latencyMs: Long,
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

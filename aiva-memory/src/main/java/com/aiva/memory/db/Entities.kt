package com.aiva.memory.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aiva.core.model.ChatMessage
import com.aiva.core.task.TaskState
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val packageName: String,
    val profileJson: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_usage")
data class ApiUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val latencyMs: Long,
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
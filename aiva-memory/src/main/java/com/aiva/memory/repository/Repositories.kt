package com.aiva.memory.repository

import com.aiva.core.model.ChatMessage
import com.aiva.core.task.TaskState
import com.aiva.memory.db.ApiUsageEntity
import com.aiva.memory.db.ConversationEntity
import com.aiva.memory.db.GameProfileEntity
import com.aiva.memory.db.TaskHistoryEntity
import com.aiva.memory.db.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ConversationRepository {
    private val items = MutableStateFlow<List<ConversationEntity>>(emptyList())

    suspend fun save(conversation: ConversationEntity) {
        items.value = items.value.filterNot { it.id == conversation.id } + conversation
    }

    suspend fun update(conversation: ConversationEntity) = save(conversation)

    suspend fun get(id: String): ConversationEntity? = items.value.firstOrNull { it.id == id }

    fun getAll(limit: Int = 50, offset: Int = 0): Flow<List<ConversationEntity>> {
        return items.map { it.drop(offset).take(limit) }
    }

    fun getAllFlow(): Flow<List<ConversationEntity>> = items

    suspend fun delete(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }

    suspend fun archive(id: String) {
        items.value = items.value.map { if (it.id == id) it.copy(isArchived = true) else it }
    }

    suspend fun getCount(): Int = items.value.count { !it.isArchived }

    fun createNew(title: String, modelId: String, messages: List<ChatMessage>): ConversationEntity {
        val id = java.util.UUID.randomUUID().toString()
        return ConversationEntity.fromMessages(id, title, modelId, messages)
    }
}

class TaskHistoryRepository {
    private val items = MutableStateFlow<List<TaskHistoryEntity>>(emptyList())

    suspend fun save(task: TaskHistoryEntity) {
        items.value = items.value + task
    }

    fun getRecent(limit: Int = 20, offset: Int = 0): Flow<List<TaskHistoryEntity>> {
        return items.map { it.sortedByDescending { row -> row.startedAt }.drop(offset).take(limit) }
    }

    fun getByState(state: TaskState): Flow<List<TaskHistoryEntity>> {
        return items.map { it.filter { row -> row.state == state.name } }
    }

    suspend fun cleanupOld(days: Int = 30) {
        val before = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        items.value = items.value.filter { it.startedAt >= before }
    }
}

class UserPreferencesRepository {
    private val items = MutableStateFlow<Map<String, UserPreferenceEntity>>(emptyMap())

    suspend fun setString(key: String, value: String) {
        items.value = items.value + (key to UserPreferenceEntity(key, value))
    }

    suspend fun getString(key: String, defaultValue: String = ""): String {
        return items.value[key]?.value ?: defaultValue
    }

    suspend fun setBoolean(key: String, value: Boolean) = setString(key, value.toString())

    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue
    }

    suspend fun setLong(key: String, value: Long) = setString(key, value.toString())

    suspend fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    }

    suspend fun delete(key: String) {
        items.value = items.value - key
    }

    fun getAll(): Flow<List<UserPreferenceEntity>> = items.map { it.values.toList() }
}

class GameProfileRepository {
    private val items = MutableStateFlow<List<GameProfileEntity>>(emptyList())

    suspend fun save(profile: GameProfileEntity) {
        items.value = items.value.filterNot { it.id == profile.id } + profile
    }

    suspend fun update(profile: GameProfileEntity) {
        save(profile.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun get(id: String): GameProfileEntity? = items.value.firstOrNull { it.id == id }

    fun getByPackageName(packageName: String): Flow<List<GameProfileEntity>> {
        return items.map { it.filter { row -> row.packageName == packageName } }
    }

    fun getActive(): Flow<GameProfileEntity?> = items.map { it.firstOrNull { row -> row.isActive } }

    fun getAll(): Flow<List<GameProfileEntity>> = items

    suspend fun delete(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }

    suspend fun setActive(id: String) {
        items.value = items.value.map { it.copy(isActive = it.id == id) }
    }
}

class ApiUsageRepository {
    private val items = MutableStateFlow<List<ApiUsageEntity>>(emptyList())

    suspend fun recordUsage(usage: ApiUsageEntity) {
        items.value = items.value + usage
    }

    fun getByModel(modelId: String, limit: Int = 100): Flow<List<ApiUsageEntity>> {
        return items.map { it.filter { row -> row.modelId == modelId }.take(limit) }
    }

    suspend fun getTotalTokensSince(since: Long): Long {
        return items.value.filter { it.timestamp > since }.sumOf { it.totalTokens.toLong() }
    }

    suspend fun getAverageLatency(modelId: String, since: Long): Double? {
        val rows = items.value.filter { it.modelId == modelId && it.timestamp > since }
        if (rows.isEmpty()) return null
        return rows.map { it.latencyMs.toDouble() }.average()
    }
}

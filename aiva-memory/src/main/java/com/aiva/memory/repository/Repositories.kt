package com.aiva.memory.repository

import com.aiva.core.model.ChatMessage
import com.aiva.core.task.TaskState
import com.aiva.memory.db.AivaDatabase
import com.aiva.memory.db.ConversationDao
import com.aiva.memory.db.ConversationEntity
import com.aiva.memory.db.TaskHistoryDao
import com.aiva.memory.db.TaskHistoryEntity
import com.aiva.memory.db.UserPreferenceDao
import com.aiva.memory.db.UserPreferenceEntity
import com.aiva.memory.db.GameProfileDao
import com.aiva.memory.db.GameProfileEntity
import com.aiva.memory.db.ApiUsageDao
import com.aiva.memory.db.ApiUsageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val database: AivaDatabase
) {
    private val dao: ConversationDao = database.conversationDao()
    
    suspend fun save(conversation: ConversationEntity) {
        dao.insert(conversation)
    }
    
    suspend fun update(conversation: ConversationEntity) {
        dao.update(conversation)
    }
    
    suspend fun get(id: String): ConversationEntity? {
        return dao.getById(id)
    }
    
    fun getAll(limit: Int = 50, offset: Int = 0): Flow<List<ConversationEntity>> {
        return dao.getAll(limit, offset)
    }
    
    fun getAllFlow(): Flow<List<ConversationEntity>> {
        return dao.getAllFlow()
    }
    
    suspend fun delete(id: String) {
        dao.delete(id)
    }
    
    suspend fun archive(id: String) {
        dao.archive(id)
    }
    
    suspend fun getCount(): Int {
        return dao.getCount()
    }
    
    fun createNew(title: String, modelId: String, messages: List<ChatMessage>): ConversationEntity {
        val id = java.util.UUID.randomUUID().toString()
        return ConversationEntity.fromMessages(id, title, modelId, messages)
    }
}

@Singleton
class TaskHistoryRepository @Inject constructor(
    private val database: AivaDatabase
) {
    private val dao: TaskHistoryDao = database.taskHistoryDao()
    
    suspend fun save(task: TaskHistoryEntity) {
        dao.insert(task)
    }
    
    fun getRecent(limit: Int = 20, offset: Int = 0): Flow<List<TaskHistoryEntity>> {
        return dao.getRecent(limit, offset)
    }
    
    fun getByState(state: TaskState): Flow<List<TaskHistoryEntity>> {
        return dao.getByState(state.name)
    }
    
    suspend fun cleanupOld(days: Int = 30) {
        val before = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        dao.deleteOld(before)
    }
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val database: AivaDatabase
) {
    private val dao: UserPreferenceDao = database.userPreferenceDao()
    
    suspend fun setString(key: String, value: String) {
        dao.set(UserPreferenceEntity(key, value))
    }
    
    suspend fun getString(key: String, defaultValue: String = ""): String {
        return dao.get(key)?.value ?: defaultValue
    }
    
    suspend fun setBoolean(key: String, value: Boolean) {
        setString(key, value.toString())
    }
    
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key, defaultValue.toString()).toBooleanOrNull() ?: defaultValue
    }
    
    suspend fun setLong(key: String, value: Long) {
        setString(key, value.toString())
    }
    
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    }
    
    suspend fun delete(key: String) {
        dao.delete(key)
    }
    
    fun getAll(): Flow<List<UserPreferenceEntity>> {
        return dao.getAll()
    }
}

@Singleton
class GameProfileRepository @Inject constructor(
    private val database: AivaDatabase
) {
    private val dao: GameProfileDao = database.gameProfileDao()
    
    suspend fun save(profile: GameProfileEntity) {
        dao.insert(profile)
    }
    
    suspend fun update(profile: GameProfileEntity) {
        dao.update(profile.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun get(id: String): GameProfileEntity? {
        return dao.getById(id)
    }
    
    fun getByPackageName(packageName: String): Flow<List<GameProfileEntity>> {
        return dao.getByPackageName(packageName)
    }
    
    fun getActive(): Flow<GameProfileEntity?> {
        return dao.getActive()
    }
    
    fun getAll(): Flow<List<GameProfileEntity>> {
        return dao.getAll()
    }
    
    suspend fun delete(id: String) {
        dao.delete(id)
    }
    
    suspend fun setActive(id: String) {
        dao.getAll().first().forEach { profile ->
            dao.update(profile.copy(isActive = profile.id == id))
        }
    }
}

@Singleton
class ApiUsageRepository @Inject constructor(
    private val database: AivaDatabase
) {
    private val dao: ApiUsageDao = database.apiUsageDao()
    
    suspend fun recordUsage(usage: ApiUsageEntity) {
        dao.insert(usage)
    }
    
    fun getByModel(modelId: String, limit: Int = 100): Flow<List<ApiUsageEntity>> {
        return dao.getByModel(modelId, limit)
    }
    
    suspend fun getTotalTokensSince(since: Long): Long {
        return dao.getTotalTokensSince(since) ?: 0
    }
    
    suspend fun getAverageLatency(modelId: String, since: Long): Double? {
        return dao.getAverageLatency(modelId, since)
    }
}
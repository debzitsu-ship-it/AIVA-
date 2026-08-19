package com.aiva.memory.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)
    
    @Update
    suspend fun update(conversation: ConversationEntity)
    
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?
    
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    fun getAll(limit: Int, offset: Int): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<ConversationEntity>>
    
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("UPDATE conversations SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: String)
    
    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0")
    suspend fun getCount(): Int
}

@Dao
interface TaskHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskHistoryEntity)
    
    @Query("SELECT * FROM task_history ORDER BY startedAt DESC LIMIT :limit OFFSET :offset")
    fun getRecent(limit: Int, offset: Int): Flow<List<TaskHistoryEntity>>
    
    @Query("SELECT * FROM task_history WHERE state = :state ORDER BY startedAt DESC")
    fun getByState(state: String): Flow<List<TaskHistoryEntity>>
    
    @Query("DELETE FROM task_history WHERE startedAt < :beforeTimestamp")
    suspend fun deleteOld(beforeTimestamp: Long)
}

@Dao
interface UserPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(preference: UserPreferenceEntity)
    
    @Query("SELECT * FROM user_preferences WHERE key = :key")
    suspend fun get(key: String): UserPreferenceEntity?
    
    @Query("SELECT * FROM user_preferences")
    fun getAll(): Flow<List<UserPreferenceEntity>>
    
    @Query("DELETE FROM user_preferences WHERE key = :key")
    suspend fun delete(key: String)
}

@Dao
interface GameProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: GameProfileEntity)
    
    @Update
    suspend fun update(profile: GameProfileEntity)
    
    @Query("SELECT * FROM game_profiles WHERE id = :id")
    suspend fun getById(id: String): GameProfileEntity?
    
    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName")
    fun getByPackageName(packageName: String): Flow<List<GameProfileEntity>>
    
    @Query("SELECT * FROM game_profiles WHERE isActive = 1")
    fun getActive(): Flow<GameProfileEntity?>
    
    @Query("SELECT * FROM game_profiles ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<GameProfileEntity>>
    
    @Query("DELETE FROM game_profiles WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ApiUsageDao {
    @Insert
    suspend fun insert(usage: ApiUsageEntity)
    
    @Query("SELECT * FROM api_usage WHERE modelId = :modelId ORDER BY timestamp DESC LIMIT :limit")
    fun getByModel(modelId: String, limit: Int): Flow<List<ApiUsageEntity>>
    
    @Query("SELECT SUM(totalTokens) FROM api_usage WHERE timestamp > :since")
    suspend fun getTotalTokensSince(since: Long): Long?
    
    @Query("SELECT AVG(latencyMs) FROM api_usage WHERE modelId = :modelId AND timestamp > :since")
    suspend fun getAverageLatency(modelId: String, since: Long): Double?
}
package com.aiva.memory.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aiva.core.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        TaskHistoryEntity::class,
        UserPreferenceEntity::class,
        GameProfileEntity::class,
        ApiUsageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AivaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun taskHistoryDao(): TaskHistoryDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun gameProfileDao(): GameProfileDao
    abstract fun apiUsageDao(): ApiUsageDao
    
    companion object {
        @Volatile private var INSTANCE: AivaDatabase? = null
        
        fun getInstance(context: Context): AivaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AivaDatabase::class.java,
                    "aiva_database"
                ).apply {
                    fallbackToDestructiveMigration()
                    setJournalMode(JournalMode.WAL)
                }.build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromChatMessages(messages: List<ChatMessage>): String {
        return kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.encodeToString(
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .getSerializersModule()
                .getListSerializer(ChatMessage.serializer()),
            messages
        )
    }
    
    @androidx.room.TypeConverter
    fun toChatMessages(json: String): List<ChatMessage> {
        return kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString(
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .getSerializersModule()
                .getListSerializer(ChatMessage.serializer()),
            json
        )
    }
}
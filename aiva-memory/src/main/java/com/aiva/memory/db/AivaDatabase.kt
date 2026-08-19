package com.aiva.memory.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aiva.core.model.ChatMessage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @androidx.room.TypeConverter
    fun fromChatMessages(messages: List<ChatMessage>): String {
        return json.encodeToString(ListSerializer(ChatMessage.serializer()), messages)
    }

    @androidx.room.TypeConverter
    fun toChatMessages(value: String): List<ChatMessage> {
        return runCatching {
            json.decodeFromString(ListSerializer(ChatMessage.serializer()), value)
        }.getOrDefault(emptyList())
    }
}

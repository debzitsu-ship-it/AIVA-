package com.aiva.memory.db

/** In-memory stand-in so the debug APK does not require Room/kapt. */
class AivaDatabase private constructor() {
    companion object {
        @Volatile private var INSTANCE: AivaDatabase? = null

        fun getInstance(@Suppress("UNUSED_PARAMETER") context: android.content.Context): AivaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AivaDatabase().also { INSTANCE = it }
            }
        }
    }
}

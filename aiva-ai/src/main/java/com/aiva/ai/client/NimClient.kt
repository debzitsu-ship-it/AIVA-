package com.aiva.ai.client

import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatCompletionResponse
import com.aiva.core.model.StreamChunk
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.util.KeyStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runInterruptible
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NimClient @Inject constructor() {
    private var retrofit: Retrofit? = null
    private var currentApiKey: String? = null
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private fun getOrCreateRetrofit(apiKey: String): Retrofit {
        if (retrofit != null && currentApiKey == apiKey) {
            return retrofit!!
        }
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        retrofit = Retrofit.Builder()
            .baseUrl("https://integrate.api.nvidia.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        
        currentApiKey = apiKey
        return retrofit!!
    }
    
    suspend fun createCompletion(
        apiKeyEntry: ApiKeyEntry,
        request: ChatCompletionRequest
    ): ChatCompletionResponse {
        val decryptedKey = KeyStoreManager.decryptString(apiKeyEntry.encryptedKey)
        val service = getOrCreateRetrofit(decryptedKey).create(NimApiService::class.java)
        return service.createCompletion(request)
    }
    
    fun createStreamCompletion(
        apiKeyEntry: ApiKeyEntry,
        request: ChatCompletionRequest
    ): Flow<StreamChunk> {
        val decryptedKey = KeyStoreManager.decryptString(apiKeyEntry.encryptedKey)
        val service = getOrCreateRetrofit(decryptedKey).create(NimApiService::class.java)
        
        return callbackFlow {
            val job = runInterruptible {
                service.createStreamCompletion(request)
                    .catch { e ->
                        // Handle stream errors
                    }
                    .collect { chunk ->
                        trySend(chunk)
                    }
            }
            awaitClose { job.cancel() }
        }.onCompletion { cause ->
            if (cause != null) {
                // Log error but don't expose API key
            }
        }
    }
    
    fun invalidateCache() {
        retrofit = null
        currentApiKey = null
    }
}
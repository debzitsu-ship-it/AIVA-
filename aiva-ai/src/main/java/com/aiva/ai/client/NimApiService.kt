package com.aiva.ai.client

import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatCompletionResponse
import com.aiva.core.model.StreamChunk
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface NimApiService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("chat/completions")
    suspend fun createCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse

    @Streaming
    @Headers(
        "Content-Type: application/json",
        "Accept: text/event-stream"
    )
    @POST("chat/completions")
    fun createStreamCompletion(
        @Body request: ChatCompletionRequest
    ): Flow<StreamChunk>
}
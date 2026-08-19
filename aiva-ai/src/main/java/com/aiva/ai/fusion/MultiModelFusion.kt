package com.aiva.ai.fusion

import com.aiva.ai.client.NimClient
import com.aiva.ai.registry.ModelRegistry
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.security.ApiKeyEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

class MultiModelFusion constructor(
    private val nimClient: NimClient,
    private val modelRegistry: ModelRegistry
) {

    data class FusionResult(
        val fusedResponse: String,
        val modelResponses: Map<String, String>,
        val conflicts: List<Conflict>,
        val confidence: Float
    )

    data class Conflict(
        val models: List<String>,
        val description: String,
        val resolution: String
    )

    suspend fun executeFusion(
        apiKeys: Map<String, ApiKeyEntry>,
        request: ChatCompletionRequest,
        modelIds: List<String>,
        fusionStrategy: FusionStrategy = FusionStrategy.CRITIC_FUSION
    ): FusionResult = coroutineScope {
        val responses = mutableMapOf<String, String>()
        val jobs = modelIds.mapNotNull { modelId ->
            val apiKey = apiKeys[modelId] ?: return@mapNotNull null
            async {
                val content = runCatching {
                    withTimeoutOrNull(60_000) {
                        nimClient.createCompletion(apiKey, request.copy(model = modelId))
                    }?.choices?.firstOrNull()?.message?.content.orEmpty()
                }.getOrDefault("")
                modelId to content
            }
        }
        jobs.forEach { deferred ->
            val (id, content) = deferred.await()
            responses[id] = content
        }
        val best = responses.values.maxByOrNull { it.length }.orEmpty()
        FusionResult(
            fusedResponse = best,
            modelResponses = responses,
            conflicts = emptyList(),
            confidence = if (responses.size > 1) 0.8f else 0.6f
        )
    }
}

enum class FusionStrategy {
    CRITIC_FUSION,
    MAJORITY_VOTE,
    BEST_REASONING,
    CONCATENATE
}

data class FusionProgress(
    val partial: Boolean,
    val mergedContent: String = "",
    val completedModels: Set<String> = emptySet()
)

package com.aiva.ai.fusion

import com.aiva.ai.client.NimClient
import com.aiva.ai.registry.ModelRegistry
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.security.ApiKeyEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiModelFusion @Inject constructor(
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
    ): FusionResult {
        return supervisorScope {
            val responses = mutableMapOf<String, String>()
            val errors = mutableMapOf<String, Throwable>()
            
            // Run models in parallel
            val jobs = modelIds.map { modelId ->
                val apiKey = apiKeys[modelId] ?: return@map null
                
                // Launch coroutine for each model
            }
            
            // Collect responses
            val channel = Channel<Pair<String, String>>(modelIds.size)
            
            modelIds.forEach { modelId ->
                val apiKey = apiKeys[modelId] ?: return@forEach
                launch {
                    try {
                        val modelRequest = request.copy(model = modelId)
                        val response = withTimeoutOrNull(60_000) {
                            nimClient.createCompletion(apiKey, modelRequest)
                        }
                        response?.choices?.firstOrNull()?.message?.content?.let { content ->
                            channel.send(modelId to content)
                        } ?: channel.send(modelId to "")
                    } catch (e: Exception) {
                        errors[modelId] = e
                        channel.send(modelId to "")
                    }
                }
            }
            
            // Close channel after all complete
            launch {
                jobs.forEach { it.join() }
                channel.close()
            }
            
            // Collect results
            for (result in channel) {
                responses[result.first] = result.second
            }
            
            // Perform fusion
            fuseResponses(request.messages, responses, fusionStrategy)
        }
    }
    
    fun executeStreamingFusion(
        apiKeys: Map<String, ApiKeyEntry>,
        request: ChatCompletionRequest,
        modelIds: List<String>
    ): Flow<FusionProgress> {
        return flow {
            val responseChannels = modelIds.map { modelId ->
                val apiKey = apiKeys[modelId] ?: return@map null
                val modelRequest = request.copy(model = modelId)
                nimClient.createStreamCompletion(apiKey, modelRequest)
                    .map { chunk ->
                        val content = chunk.choices.firstOrNull()?.delta?.content
                        modelId to content.orEmpty()
                    }
                    .asFlow()
            }.filterNotNull()
            
            // Merge streams - simplified approach
            val merged = combine(*responseChannels.toTypedArray()) { results ->
                results.map { it.second }.joinToString("")
            }
            
            emit(FusionProgress(partial = true, mergedContent = merged.first()))
            
            // Wait for completion and do final fusion
            // This is simplified - real implementation would be more complex
        }
    }
    
    private suspend fun fuseResponses(
        messages: List<ChatMessage>,
        responses: Map<String, String>,
        strategy: FusionStrategy
    ): FusionResult {
        return when (strategy) {
            FusionStrategy.CRITIC_FUSION -> criticFusion(messages, responses)
            FusionStrategy.MAJORITY_VOTE -> majorityVote(responses)
            FusionStrategy.BEST_REASONING -> bestReasoning(messages, responses)
            FusionStrategy.CONCATENATE -> concatenateResponses(responses)
        }
    }
    
    private suspend fun criticFusion(
        messages: List<ChatMessage>,
        responses: Map<String, String>
    ): FusionResult {
        // Use a critic model to evaluate and fuse responses
        // For now, implement simplified version
        val bestResponse = responses.values.maxByOrNull { it.length } ?: ""
        val conflicts = detectConflicts(responses)
        
        return FusionResult(
            fusedResponse = bestResponse,
            modelResponses = responses,
            conflicts = conflicts,
            confidence = if (conflicts.isEmpty()) 0.9f else 0.7f
        )
    }
    
    private suspend fun majorityVote(responses: Map<String, String>): FusionResult {
        // Simple implementation - in practice would use semantic similarity
        val bestResponse = responses.values.maxByOrNull { it.length } ?: ""
        return FusionResult(
            fusedResponse = bestResponse,
            modelResponses = responses,
            conflicts = emptyList(),
            confidence = 0.8f
        )
    }
    
    private suspend fun bestReasoning(
        messages: List<ChatMessage>,
        responses: Map<String, String>
    ): FusionResult {
        // Prefer responses from models with higher reasoning capability
        val sortedModels = responses.keys.sortedByDescending { modelId ->
            modelRegistry.getCapabilities(modelId)?.supportsReasoning ?: false
        }
        val bestResponse = responses[sortedModels.firstOrNull() ?: ""] ?: ""
        
        return FusionResult(
            fusedResponse = bestResponse,
            modelResponses = responses,
            conflicts = emptyList(),
            confidence = 0.85f
        )
    }
    
    private suspend fun concatenateResponses(responses: Map<String, String>): FusionResult {
        val combined = responses.entries
            .joinToString("\n\n---\n\n") { "${it.key}: ${it.value}" }
        return FusionResult(
            fusedResponse = combined,
            modelResponses = responses,
            conflicts = emptyList(),
            confidence = 0.6f
        )
    }
    
    private fun detectConflicts(responses: Map<String, String>): List<Conflict> {
        // Simplified conflict detection
        // Real implementation would use NLI or semantic similarity
        val values = responses.values.toList()
        if (values.distinct().size > 1) {
            return listOf(Conflict(
                models = responses.keys.toList(),
                description = "Models provided different responses",
                resolution = "Selected longest response as default"
            ))
        }
        return emptyList()
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
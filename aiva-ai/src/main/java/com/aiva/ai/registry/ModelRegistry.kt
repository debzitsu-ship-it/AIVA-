package com.aiva.ai.registry

import com.aiva.core.model.ModelCapabilities
import com.aiva.core.model.ModelInfo
import com.aiva.core.model.ReasoningLevel
import com.aiva.core.model.SpeedProfile

class ModelRegistry constructor() {
    
    private val allModels: Map<String, ModelInfo> = mapOf(
        "z-ai/glm-5.2" to ModelInfo(
            id = "z-ai/glm-5.2",
            displayName = "GLM 5.2",
            provider = "Z.ai",
            speedProfile = SpeedProfile.FAST,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        ),
        "poolside/laguna-xs-2.1" to ModelInfo(
            id = "poolside/laguna-xs-2.1",
            displayName = "Laguna XS 2.1",
            provider = "Poolside",
            speedProfile = SpeedProfile.FASTEST,
            reasoningCapability = ReasoningLevel.BASIC,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        ),
        "nvidia/nemotron-3.5-lightning-30b-a3b" to ModelInfo(
            id = "nvidia/nemotron-3.5-lightning-30b-a3b",
            displayName = "Nemotron 3.5 Lightning",
            provider = "NVIDIA",
            speedProfile = SpeedProfile.FASTEST,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        ),
        "meta/muse-glimmer-30b" to ModelInfo(
            id = "meta/muse-glimmer-30b",
            displayName = "Muse Glimmer 30B",
            provider = "Meta",
            speedProfile = SpeedProfile.FAST,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = false,
            visionCapability = false,
            streamingCapability = true
        ),
        "google/gemma-4-31b-it" to ModelInfo(
            id = "google/gemma-4-31b-it",
            displayName = "Gemma 4 31B IT",
            provider = "Google",
            speedProfile = SpeedProfile.BALANCED,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        ),
        "google/diffusiongemma-26b-a4b-it" to ModelInfo(
            id = "google/diffusiongemma-26b-a4b-it",
            displayName = "DiffusionGemma 26B",
            provider = "Google",
            speedProfile = SpeedProfile.FAST,
            reasoningCapability = ReasoningLevel.BASIC,
            agentSuitability = false,
            visionCapability = false,
            streamingCapability = true
        ),
        "nvidia/nemotron-3-ultra-550b-a55b" to ModelInfo(
            id = "nvidia/nemotron-3-ultra-550b-a55b",
            displayName = "Nemotron 3 Ultra",
            provider = "NVIDIA",
            speedProfile = SpeedProfile.SLOW,
            reasoningCapability = ReasoningLevel.EXPERT,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        ),
        "thinkingmachines/inkling" to ModelInfo(
            id = "thinkingmachines/inkling",
            displayName = "Inkling",
            provider = "Thinking Machines",
            speedProfile = SpeedProfile.BALANCED,
            reasoningCapability = ReasoningLevel.ADVANCED,
            agentSuitability = false,
            visionCapability = false,
            streamingCapability = true
        ),
        "openai/gpt-oss-120b" to ModelInfo(
            id = "openai/gpt-oss-120b",
            displayName = "GPT-OSS 120B",
            provider = "OpenAI",
            speedProfile = SpeedProfile.BALANCED,
            reasoningCapability = ReasoningLevel.EXPERT,
            agentSuitability = true,
            visionCapability = false,
            streamingCapability = true
        )
    )
    
    private val modelCapabilities: Map<String, ModelCapabilities> = mapOf(
        "z-ai/glm-5.2" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = true,
            supportsAgent = true,
            maxTokens = 16384,
            supportsChatTemplateKwargs = true,
            supportsReasoningBudget = true
        ),
        "poolside/laguna-xs-2.1" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = false,
            supportsAgent = true,
            maxTokens = 8192,
            supportsChatTemplateKwargs = false,
            supportsReasoningBudget = false
        ),
        "nvidia/nemotron-3.5-lightning-30b-a3b" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = true,
            supportsAgent = true,
            maxTokens = 16384,
            supportsChatTemplateKwargs = true,
            supportsReasoningBudget = true
        ),
        "meta/muse-glimmer-30b" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = false,
            supportsAgent = false,
            maxTokens = 8192,
            supportsChatTemplateKwargs = false,
            supportsReasoningBudget = false
        ),
        "google/gemma-4-31b-it" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = true,
            supportsAgent = true,
            maxTokens = 16384,
            supportsChatTemplateKwargs = true,
            supportsReasoningBudget = false
        ),
        "google/diffusiongemma-26b-a4b-it" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = false,
            supportsAgent = false,
            maxTokens = 4096,
            supportsChatTemplateKwargs = true,
            supportsReasoningBudget = false
        ),
        "nvidia/nemotron-3-ultra-550b-a55b" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = true,
            supportsAgent = true,
            maxTokens = 16384,
            supportsChatTemplateKwargs = true,
            supportsReasoningBudget = true
        ),
        "thinkingmachines/inkling" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = false,
            supportsAgent = false,
            maxTokens = 8192,
            supportsChatTemplateKwargs = false,
            supportsReasoningBudget = false
        ),
        "openai/gpt-oss-120b" to ModelCapabilities(
            supportsStreaming = true,
            supportsVision = false,
            supportsReasoning = true,
            supportsAgent = true,
            maxTokens = 4096,
            supportsChatTemplateKwargs = false,
            supportsReasoningBudget = false
        )
    )
    
    fun getAllModels(): List<ModelInfo> = allModels.values.toList()
    
    fun getModel(id: String): ModelInfo? = allModels[id]
    
    fun getCapabilities(id: String): ModelCapabilities? = modelCapabilities[id]
    
    fun getEnabledModels(enabledIds: Set<String>): List<ModelInfo> {
        return allModels.values
            .filter { it.id in enabledIds }
            .sortedBy { it.speedProfile.ordinal }
            .toList()
    }
    
    fun getModelsForTask(
        taskType: TaskType,
        enabledIds: Set<String>
    ): List<ModelInfo> {
        val candidates = getEnabledModels(enabledIds)
        
        return when (taskType) {
            TaskType.SIMPLE_CHAT -> candidates
                .filter { it.speedProfile in setOf(SpeedProfile.FASTEST, SpeedProfile.FAST) }
                .take(1)
            
            TaskType.COMPLEX_REASONING -> candidates
                .filter { it.reasoningCapability in setOf(ReasoningLevel.ADVANCED, ReasoningLevel.EXPERT) }
                .sortedByDescending { it.reasoningCapability.ordinal }
                .take(3)
            
            TaskType.AUTOMATION -> candidates
                .filter { it.agentSuitability }
                .sortedBy { it.speedProfile.ordinal }
                .take(2)
            
            TaskType.SCREEN_VISION -> candidates
                .filter { it.visionCapability }
                .take(1)
            
            TaskType.MULTI_MODEL_FUSION -> candidates
                .filter { it.reasoningCapability != ReasoningLevel.NONE }
                .sortedByDescending { it.reasoningCapability.ordinal }
                .take(3)
            
            TaskType.REAL_TIME_GAME -> candidates
                .filter { it.speedProfile == SpeedProfile.FASTEST }
                .take(1)
            
            TaskType.FORM_FILL -> candidates
                .filter { it.agentSuitability }
                .take(1)
            
            TaskType.QUIZ -> candidates
                .filter { it.reasoningCapability != ReasoningLevel.NONE }
                .take(1)
        }
    }
    
    fun getFallbackChain(primaryModelId: String, enabledIds: Set<String>): List<ModelInfo> {
        val primary = getModel(primaryModelId) ?: return emptyList()
        val candidates = getEnabledModels(enabledIds).filter { it.id != primaryModelId }
        
        return candidates
            .filter { it.reasoningCapability.ordinal >= primary.reasoningCapability.ordinal }
            .sortedBy { it.speedProfile.ordinal }
            .toList()
    }
}

enum class TaskType {
    SIMPLE_CHAT,
    COMPLEX_REASONING,
    AUTOMATION,
    SCREEN_VISION,
    MULTI_MODEL_FUSION,
    REAL_TIME_GAME,
    FORM_FILL,
    QUIZ
}
package com.aiva.ai.router

import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.registry.TaskType
import com.aiva.core.model.ModelInfo
import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRouter @Inject constructor(
    private val modelRegistry: ModelRegistry
) {
    
    fun routeForIntent(
        intent: Intent,
        enabledModelIds: Set<String>,
        userPreferredModel: String? = null
    ): ModelInfo? {
        // User explicitly selected a model
        if (userPreferredModel != null) {
            val model = modelRegistry.getModel(userPreferredModel)
            if (model != null && enabledModelIds.contains(model.id)) {
                return model
            }
        }
        
        val taskType = intentTypeToTaskType(intent.type)
        val candidates = modelRegistry.getModelsForTask(taskType, enabledModelIds)
        
        return candidates.firstOrNull()
    }
    
    fun routeForFusion(
        intent: Intent,
        enabledModelIds: Set<String>
    ): List<ModelInfo> {
        val taskType = when {
            intent.type == IntentType.COMPLEX_REASONING -> TaskType.MULTI_MODEL_FUSION
            else -> intentTypeToTaskType(intent.type)
        }
        return modelRegistry.getModelsForTask(taskType, enabledModelIds)
    }
    
    fun getFallbackChain(
        primaryModelId: String,
        enabledModelIds: Set<String>
    ): List<ModelInfo> {
        return modelRegistry.getFallbackChain(primaryModelId, enabledModelIds)
    }
    
    private fun intentTypeToTaskType(intentType: IntentType): TaskType {
        return when (intentType) {
            IntentType.CHAT, IntentType.QUESTION, IntentType.EXPLANATION -> TaskType.SIMPLE_CHAT
            IntentType.ACTION, IntentType.AUTOMATION, IntentType.FORM_FILL,
            IntentType.NAVIGATION, IntentType.MULTI_STEP_WORKFLOW -> TaskType.AUTOMATION
            IntentType.SCREEN_ANALYSIS -> TaskType.SCREEN_VISION
            IntentType.GAME_CONTROL -> TaskType.REAL_TIME_GAME
            IntentType.QUIZ -> TaskType.QUIZ
            IntentType.FORM_FILL -> TaskType.FORM_FILL
        }
    }
}
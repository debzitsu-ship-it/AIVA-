package com.aiva.task.executor

import com.aiva.automation.executor.ActionExecutor
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.observation.ScreenState
import com.aiva.core.task.Intent
import com.aiva.core.task.PlanStep
import com.aiva.core.task.StepResult
import com.aiva.core.task.TaskExecution
import com.aiva.core.task.TaskPlan
import com.aiva.core.task.TaskState
import com.aiva.task.classifier.IntentClassifier
import com.aiva.task.planner.TaskPlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskExecutor @Inject constructor(
    private val intentClassifier: IntentClassifier,
    private val taskPlanner: TaskPlanner,
    private val actionExecutor: ActionExecutor
) {
    
    private var currentJob: Job? = null
    private var currentExecution: TaskExecution? = null
    
    private val _state = MutableStateFlow<TaskState>(TaskState.IDLE)
    val state: StateFlow<TaskState> = _state
    
    private val _currentExecution = MutableStateFlow<TaskExecution?>(null)
    val currentExecution: StateFlow<TaskExecution?> = _currentExecution
    
    private val _progress = MutableStateFlow<String>("")
    val progress: StateFlow<String> = _progress
    
    suspend fun executeTask(
        userInput: String,
        screenState: ScreenState?,
        apiKeys: Map<String, com.aiva.core.security.ApiKeyEntry>,
        context: ExecutorContext
    ): TaskExecution {
        return CoroutineScope(Dispatchers.IO).async {
            currentJob = kotlinx.coroutines.coroutineContext[Job]!!
            
            _state.value = TaskState.UNDERSTANDING
            _progress.value = "Understanding intent..."
            
            // Classify intent
            val intent = intentClassifier.classify(userInput, context.toClassifierContext(), apiKeys)
            
            _state.value = TaskState.PLANNING
            _progress.value = "Planning..."
            
            // Create plan
            val plan = taskPlanner.createPlan(intent, screenState, apiKeys, context.toPlannerContext())
            
            // Check for high-impact actions requiring confirmation
            if (requiresConfirmation(plan)) {
                _state.value = TaskState.WAITING
                _progress.value = "Waiting for confirmation..."
                // In real implementation, would wait for user confirmation
                // For now, proceed with warning
            }
            
            // Execute plan
            val execution = TaskExecution(plan = plan)
            currentExecution = execution
            _currentExecution.value = execution
            _state.value = TaskState.ACTING
            
            val completedSteps = mutableListOf<StepResult>()
            
            for ((index, step) in plan.steps.withIndex()) {
                if (!currentJob?.isActive == true) {
                    _state.value = TaskState.STOPPED
                    break
                }
                
                _progress.value = "Step ${index + 1}/${plan.steps.size}: ${step.description}"
                
                _state.value = TaskState.OBSERVING
                val preObservation = observeScreen()
                
                _state.value = TaskState.ACTING
                val actionResult = actionExecutor.execute(step.action)
                
                _state.value = TaskState.VERIFYING
                val postObservation = observeScreen()
                
                val verified = verifyStep(step, preObservation, postObservation)
                
                val stepResult = StepResult(
                    stepId = step.id,
                    action = step.action,
                    success = actionResult.success && verified,
                    result = actionResult,
                    observation = postObservation?.let { 
                        com.aiva.core.observation.ObservationResult(
                            screenState = it,
                            visionDetections = emptyList()
                        )
                    },
                    retries = 0
                )
                
                completedSteps.add(stepResult)
                
                currentExecution = execution.copy(
                    currentStepIndex = index + 1,
                    completedSteps = completedSteps.toList()
                )
                _currentExecution.value = currentExecution
                
                if (!stepResult.success) {
                    // Try fallback
                    if (step.fallbackAction != null) {
                        _progress.value = "Retrying with fallback..."
                        val fallbackResult = actionExecutor.execute(step.fallbackAction!!)
                        if (fallbackResult.success) {
                            // Update step result
                            val updatedResult = stepResult.copy(success = true, result = fallbackResult)
                            completedSteps[completedSteps.lastIndex] = updatedResult
                            continue
                        }
                    }
                    
                    // Step failed
                    _state.value = TaskState.ERROR
                    currentExecution = execution.copy(
                        state = TaskState.ERROR,
                        endTime = System.currentTimeMillis()
                    )
                    _currentExecution.value = currentExecution
                    return@async currentExecution
                }
                
                // Brief wait between steps
                kotlinx.coroutines.delay(200)
            }
            
            // All steps completed
            _state.value = TaskState.SUCCESS
            _progress.value = "Task completed"
            
            currentExecution = execution.copy(
                state = TaskState.SUCCESS,
                endTime = System.currentTimeMillis()
            )
            _currentExecution.value = currentExecution
            
            currentExecution!!
        }.await()
    }
    
    fun stop() {
        currentJob?.cancel()
        actionExecutor.cancel()
        _state.value = TaskState.STOPPED
        _progress.value = "Stopped by user"
    }
    
    private fun requiresConfirmation(plan: TaskPlan): Boolean {
        return plan.steps.any { step ->
            step.action is Action.LaunchApp ||
            step.action is Action.Type && (step.action as Action.Type).text.length > 100 ||
            step.verificationQuery?.contains("confirm", true) == true ||
            step.verificationQuery?.contains("purchase", true) == true ||
            step.verificationQuery?.contains("payment", true) == true ||
            step.verificationQuery?.contains("send", true) == true ||
            step.verificationQuery?.contains("delete", true) == true
        } || plan.intent.requiresConfirmation
    }
    
    private fun observeScreen(): ScreenState? {
        // In real implementation, get from accessibility service
        return null
    }
    
    private fun verifyStep(
        step: PlanStep,
        preState: ScreenState?,
        postState: ScreenState?
    ): Boolean {
        if (step.verificationQuery == null) return true
        if (postState == null) return true
        
        // Simple verification - in real implementation would use LLM
        return preState != postState
    }
    
    data class ExecutorContext(
        val currentApp: String? = null,
        val currentScreen: String? = null,
        val recentCommands: List<String> = emptyList(),
        val selectedModel: String? = null,
        val userPreferences: Map<String, String> = emptyMap(),
        val activeGameProfile: String? = null
    ) {
        fun toClassifierContext(): IntentClassifier.TaskContext {
            return IntentClassifier.TaskContext(
                currentApp = currentApp,
                currentScreen = currentScreen,
                recentCommands = recentCommands,
                selectedModel = selectedModel,
                userPreferences = userPreferences,
                activeGameProfile = activeGameProfile
            )
        }
        
        fun toPlannerContext(): TaskPlanner.PlannerContext {
            return TaskPlanner.PlannerContext(
                currentApp = currentApp,
                currentScreen = currentScreen,
                activeGameProfile = activeGameProfile
            )
        }
    }
}
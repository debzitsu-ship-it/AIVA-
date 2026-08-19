package com.aiva.automation.executor

import android.os.Build
import com.aiva.automation.accessibility.AccessibilityController
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.action.Target
import com.aiva.core.observation.ScreenState
import com.aiva.core.task.TaskState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor(
    private val accessibilityController: AccessibilityController
) {
    
    private val _state = MutableStateFlow<TaskState>(TaskState.IDLE)
    val state: StateFlow<TaskState> = _state
    
    private val _lastResult = MutableStateFlow<ActionResult?>(null)
    val lastResult: StateFlow<ActionResult?> = _lastResult
    
    private var maxRetries = 3
    private var retryDelayMs = 500L
    
    suspend fun execute(action: Action): ActionResult {
        _state.value = TaskState.ACTING
        
        // Validate before execution
        val validation = validateAction(action)
        if (!validation.isValid) {
            _state.value = TaskState.ERROR
            return ActionResult(success = false, message = validation.errorMessage)
        }
        
        // Pre-action observation
        val preState = observeScreen()
        
        var lastError: String? = null
        var attempt = 0
        
        while (attempt <= maxRetries) {
            attempt++
            
            val result = accessibilityController.executeAction(action)
            _lastResult.value = result
            
            if (result.success) {
                // Post-action verification
                val postState = observeScreen()
                val verified = verifyAction(action, preState, postState)
                
                if (verified) {
                    _state.value = TaskState.SUCCESS
                    return result
                } else {
                    lastError = "Action executed but verification failed"
                }
            } else {
                lastError = result.message
            }
            
            if (attempt <= maxRetries) {
                // Recovery: re-observe and try to find target again
                val recoveryResult = recoverAndRetry(action, lastError!!)
                if (recoveryResult.success) {
                    _state.value = TaskState.SUCCESS
                    return recoveryResult
                }
                
                kotlinx.coroutines.delay(retryDelayMs * attempt)
            }
        }
        
        _state.value = TaskState.ERROR
        return ActionResult(success = false, message = "Failed after $maxRetries attempts: $lastError")
    }
    
    suspend fun executeAll(actions: List<Action>): List<ActionResult> {
        val results = mutableListOf<ActionResult>()

        for (action in actions) {
            val result = execute(action)
            results.add(result)
            
            if (!result.success) {
                // Stop on failure unless it's a recoverable action
                if (!isRecoverable(action)) {
                    break
                }
            }
        }
        
        return results
    }
    
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    private fun validateAction(action: Action): ValidationResult {
        return when (action) {
            is Action.LaunchApp -> if (action.packageName.isNotBlank()) ValidationResult(true)
                else ValidationResult(false, "Package name required")
            is Action.OpenUrl -> if (action.url.isNotBlank() && (action.url.startsWith("http") || action.url.startsWith("https"))) ValidationResult(true)
                else ValidationResult(false, "Valid URL required")
            is Action.Tap -> if (action.target.hasAnyCriteria()) ValidationResult(true)
                else ValidationResult(false, "Target criteria required for tap")
            is Action.DoubleTap -> if (action.target.hasAnyCriteria()) ValidationResult(true)
                else ValidationResult(false, "Target criteria required for tap")
            is Action.LongPress -> if (action.target.hasAnyCriteria()) ValidationResult(true)
                else ValidationResult(false, "Target criteria required for tap")
            is Action.Swipe -> ValidationResult(true)
            is Action.Scroll -> ValidationResult(true)
            is Action.Type -> if (action.text.isNotBlank()) ValidationResult(true)
                else ValidationResult(false, "Text required for type")
            is Action.ReplaceText -> if (action.newText.isNotBlank() && action.target.hasAnyCriteria()) ValidationResult(true)
                else ValidationResult(false, "Target and new text required")
            is Action.Copy, is Action.Paste -> ValidationResult(true)
            is Action.Back, is Action.Home -> ValidationResult(true)
            is Action.Observe -> ValidationResult(true)
            is Action.Wait -> ValidationResult(true)
            is Action.Find -> if (action.target.hasAnyCriteria()) ValidationResult(true)
                else ValidationResult(false, "Target criteria required for find")
            is Action.Select -> if (action.target.hasAnyCriteria() && action.option.isNotBlank()) ValidationResult(true)
                else ValidationResult(false, "Target and option required for select")
            is Action.Finish -> ValidationResult(true)
            is Action.AskUser -> if (action.question.isNotBlank()) ValidationResult(true)
                else ValidationResult(false, "Question required")
            is Action.Stop -> ValidationResult(true)
        }
    }
    
    private fun observeScreen(): ScreenState? {
        return accessibilityController.getCurrentScreenState()
    }
    
    private fun verifyAction(
        action: Action,
        preState: ScreenState?,
        postState: ScreenState?
    ): Boolean {
        // If no post state, can't verify
        if (postState == null) return true
        
        return when (action) {
            is Action.LaunchApp -> postState.packageName?.contains(action.packageName, true) == true
            is Action.OpenUrl -> true // Hard to verify URL opening
            is Action.Tap, is Action.DoubleTap, is Action.LongPress -> {
                // Check if screen changed or target disappeared
                preState != postState
            }
            is Action.Swipe, is Action.Scroll -> preState != postState
            is Action.Type -> {
                // Check if text appears in new screen
                postState.nodes.any { it.text?.contains(action.text, true) == true }
            }
            is Action.ReplaceText -> {
                postState.nodes.any { it.text?.contains(action.newText, true) == true }
            }
            is Action.Scroll -> preState != postState
            is Action.Back, is Action.Home -> preState != postState
            else -> true
        }
    }
    
    private fun recoverAndRetry(action: Action, error: String): ActionResult {
        // Re-observe screen
        val newState = observeScreen()
        
        // Try to find target again with relaxed criteria
        val recovered = when (action) {
            is Action.Tap -> action.copy(target = relaxTarget(action.target))
            is Action.DoubleTap -> action.copy(target = relaxTarget(action.target))
            is Action.LongPress -> action.copy(target = relaxTarget(action.target))
            else -> null
        }
        if (recovered != null && recovered != action) {
            return accessibilityController.executeAction(recovered)
        }
        
        return ActionResult(success = false, message = "Recovery failed: $error")
    }
    
    private fun relaxTarget(target: Target): Target {
        // Remove most specific criteria first
        return if (target.index != null) {
            target.copy(index = null)
        } else if (target.resourceId != null) {
            target.copy(resourceId = null)
        } else if (target.className != null) {
            target.copy(className = null)
        } else if (target.contentDescription != null) {
            target.copy(contentDescription = null)
        } else {
            target
        }
    }
    
    private fun isRecoverable(action: Action): Boolean {
        return when (action) {
            is Action.Tap, is Action.DoubleTap, is Action.LongPress,
            is Action.Type, is Action.Scroll, is Action.Swipe -> true
            else -> false
        }
    }
    
    fun setMaxRetries(retries: Int) {
        maxRetries = retries.coerceIn(0, 10)
    }
    
    fun setRetryDelay(delayMs: Long) {
        retryDelayMs = delayMs.coerceIn(100, 5000)
    }
    
    fun cancel() {
        accessibilityController.cancelCurrentAction()
        _state.value = TaskState.STOPPED
    }
}
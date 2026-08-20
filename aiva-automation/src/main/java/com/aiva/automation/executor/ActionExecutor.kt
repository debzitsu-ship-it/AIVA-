package com.aiva.automation.executor

import com.aiva.automation.accessibility.AccessibilityController
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.task.TaskState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ActionExecutor(
    private val accessibilityController: AccessibilityController
) {
    private val _state = MutableStateFlow(TaskState.IDLE)
    val state: StateFlow<TaskState> = _state

    suspend fun execute(action: Action): ActionResult {
        val result = accessibilityController.executeAction(action)
        _state.value = if (result.success) TaskState.SUCCESS else TaskState.ERROR
        return result
    }

    fun cancel() {
        accessibilityController.cancelCurrentAction()
        _state.value = TaskState.STOPPED
    }
}

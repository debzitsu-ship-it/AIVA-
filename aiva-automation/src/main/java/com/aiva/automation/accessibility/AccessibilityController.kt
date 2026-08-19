package com.aiva.automation.accessibility

import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.observation.ScreenState

interface AccessibilityController {
    fun executeAction(action: Action): ActionResult
    val screenState: kotlinx.coroutines.flow.StateFlow<ScreenState?>
    val serviceEnabled: kotlinx.coroutines.flow.StateFlow<Boolean>
    fun getCurrentScreenState(): ScreenState?
    fun cancelCurrentAction()
}
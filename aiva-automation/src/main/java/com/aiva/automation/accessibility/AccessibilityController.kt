package com.aiva.automation.accessibility

import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.observation.ScreenState
import kotlinx.coroutines.flow.StateFlow

interface AccessibilityController {
    fun executeAction(action: Action): ActionResult
    val screenState: StateFlow<ScreenState?>
    val serviceEnabled: StateFlow<Boolean>
    fun getCurrentScreenState(): ScreenState?
    fun cancelCurrentAction()
}

package com.aiva.automation.accessibility

import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.observation.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AivaAccessibilityService : AccessibilityController {

    companion object {
        @Volatile
        var instance: AivaAccessibilityService? = null
        fun getInstance(): AivaAccessibilityService? = instance
    }

    private val _screenState = MutableStateFlow<ScreenState?>(null)
    override val screenState: StateFlow<ScreenState?> = _screenState
    private val _serviceEnabled = MutableStateFlow(false)
    override val serviceEnabled: StateFlow<Boolean> = _serviceEnabled

    override fun executeAction(action: Action): ActionResult =
        ActionResult(success = false, message = "Accessibility actions are not available in this build")

    override fun getCurrentScreenState(): ScreenState? = _screenState.value

    override fun cancelCurrentAction() = Unit
}

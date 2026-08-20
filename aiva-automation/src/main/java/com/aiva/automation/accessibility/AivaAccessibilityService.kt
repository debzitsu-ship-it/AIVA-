package com.aiva.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.observation.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AivaAccessibilityService : AccessibilityService(), AccessibilityController {

    companion object {
        @Volatile
        var instance: AivaAccessibilityService? = null

        fun getInstance(): AivaAccessibilityService? = instance
    }

    private val _screenState = MutableStateFlow<ScreenState?>(null)
    override val screenState: StateFlow<ScreenState?> = _screenState

    private val _serviceEnabled = MutableStateFlow(false)
    override val serviceEnabled: StateFlow<Boolean> = _serviceEnabled

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _serviceEnabled.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        _serviceEnabled.value = false
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun executeAction(action: Action): ActionResult {
        return ActionResult(success = false, message = "Accessibility actions are not available in this build")
    }

    override fun getCurrentScreenState(): ScreenState? = _screenState.value

    override fun cancelCurrentAction() = Unit
}

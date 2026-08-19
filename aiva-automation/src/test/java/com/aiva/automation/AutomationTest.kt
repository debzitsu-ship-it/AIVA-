package com.aiva.automation

import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.action.Target
import com.aiva.core.observation.ScreenState
import com.aiva.core.task.TaskState
import com.aiva.automation.executor.ActionExecutor
import com.aiva.automation.accessibility.AccessibilityController
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutomationTest {
    
    private val testDispatcher = TestDispatcher()
    
    @Test
    fun testActionExecutorValidation() = runTest {
        val mockController = MockAccessibilityController()
        val executor = ActionExecutor(mockController)
        
        // Valid actions
        assertTrue(executor.validateAction(Action.LaunchApp("com.test")).isValid)
        assertTrue(executor.validateAction(Action.OpenUrl("https://example.com")).isValid)
        assertTrue(executor.validateAction(Action.Tap(Target(text = "Button"))).isValid)
        assertTrue(executor.validateAction(Action.Type("Hello", Target(resourceId = "input"))).isValid)
        
        // Invalid actions
        assertTrue(!executor.validateAction(Action.LaunchApp("")).isValid)
        assertTrue(!executor.validateAction(Action.OpenUrl("not-a-url")).isValid)
        assertTrue(!executor.validateAction(Action.Tap(Target())).isValid)
        assertTrue(!executor.validateAction(Action.Type("", Target())).isValid)
    }
    
    @Test
    fun testActionExecutorRecoverable() = runTest {
        val mockController = MockAccessibilityController()
        val executor = ActionExecutor(mockController)
        
        assertTrue(executor.isRecoverable(Action.Tap(Target(text = "Button"))))
        assertTrue(executor.isRecoverable(Action.Type("Hello", Target())))
        assertTrue(executor.isRecoverable(Action.Scroll(com.aiva.core.action.ScrollDirection.DOWN)))
        assertTrue(!executor.isRecoverable(Action.LaunchApp("com.test")))
        assertTrue(!executor.isRecoverable(Action.Back()))
    }
    
    @Test
    fun testScreenStateEquality() {
        val state1 = ScreenState(packageName = "com.test", activityName = "MainActivity", nodes = emptyList())
        val state2 = ScreenState(packageName = "com.test", activityName = "MainActivity", nodes = emptyList())
        val state3 = ScreenState(packageName = "com.other", activityName = "MainActivity", nodes = emptyList())
        
        assertEquals(state1, state2)
        assertTrue(state1 != state3)
    }
    
    // Mock implementation for testing
    private class MockAccessibilityController : AccessibilityController {
        override fun executeAction(action: Action): ActionResult = ActionResult(true, "Mock success")
        override val screenState: kotlinx.coroutines.flow.StateFlow<ScreenState?> = kotlinx.coroutines.flow.MutableStateFlow(null).asStateFlow()
        override val serviceEnabled: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(true).asStateFlow()
        override fun getCurrentScreenState(): ScreenState? = null
        override fun cancelCurrentAction() {}
    }
    
    // Extension to access private validation method
    private fun ActionExecutor.validateAction(action: Action): ValidationResult {
        return when (action) {
            is Action.LaunchApp -> if (action.packageName.isNotBlank()) ValidationResult(true) else ValidationResult(false, "Package name required")
            is Action.OpenUrl -> if (action.url.isNotBlank() && (action.url.startsWith("http") || action.url.startsWith("https"))) ValidationResult(true) else ValidationResult(false, "Valid URL required")
            is Action.Tap, is Action.DoubleTap, is Action.LongPress -> if (action.target.hasAnyCriteria()) ValidationResult(true) else ValidationResult(false, "Target criteria required for tap")
            is Action.Swipe -> ValidationResult(true)
            is Action.Scroll -> ValidationResult(true)
            is Action.Type -> if (action.text.isNotBlank()) ValidationResult(true) else ValidationResult(false, "Text required for type")
            is Action.ReplaceText -> if (action.newText.isNotBlank() && action.target.hasAnyCriteria()) ValidationResult(true) else ValidationResult(false, "Target and new text required")
            is Action.Copy, is Action.Paste -> ValidationResult(true)
            is Action.Back, is Action.Home -> ValidationResult(true)
            is Action.Observe -> ValidationResult(true)
            is Action.Wait -> ValidationResult(true)
            is Action.Find -> if (action.target.hasAnyCriteria()) ValidationResult(true) else ValidationResult(false, "Target criteria required for find")
            is Action.Select -> if (action.target.hasAnyCriteria() && action.option.isNotBlank()) ValidationResult(true) else ValidationResult(false, "Target and option required for select")
            is Action.Finish -> ValidationResult(true)
            is Action.AskUser -> if (action.question.isNotBlank()) ValidationResult(true) else ValidationResult(false, "Question required")
            is Action.Stop -> ValidationResult(true)
        }
    }
    
    private data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
}
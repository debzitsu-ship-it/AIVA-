package com.aiva.task

import com.aiva.ai.client.NimClient
import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.router.ModelRouter
import com.aiva.core.action.Action
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatCompletionResponse
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType
import com.aiva.core.task.PlanStep
import com.aiva.core.task.TaskPlan
import com.aiva.core.task.TaskState
import com.aiva.task.classifier.IntentClassifier
import com.aiva.task.planner.TaskPlanner
import com.aiva.task.executor.TaskExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class TaskTest {
    
    private val testDispatcher = TestDispatcher()
    
    @Test
    fun testIntentClassifierFallback() = runTest {
        val mockNimClient = mockk<NimClient>()
        val mockModelRegistry = mockk<ModelRegistry>()
        val mockModelRouter = mockk<ModelRouter>()
        
        every { mockModelRegistry.getAllModels() } returns listOf()
        
        val classifier = IntentClassifier(mockNimClient, mockModelRegistry, mockModelRouter)
        
        // Test fallback classification without API
        val chatIntent = classifier.fallbackClassification("Hello there!")
        assertEquals(IntentType.CHAT, chatIntent.type)
        
        val questionIntent = classifier.fallbackClassification("What is the weather?")
        assertEquals(IntentType.QUESTION, questionIntent.type)
        
        val actionIntent = classifier.fallbackClassification("Open Settings")
        assertEquals(IntentType.ACTION, actionIntent.type)
        
        val workflowIntent = classifier.fallbackClassification("Open browser then search for cats")
        assertEquals(IntentType.MULTI_STEP_WORKFLOW, workflowIntent.type)
    }
    
    @Test
    fun testTaskPlannerFallback() = runTest {
        val mockNimClient = mockk<NimClient>()
        val mockModelRegistry = mockk<ModelRegistry>()
        val mockModelRouter = mockk<ModelRouter>()
        
        every { mockModelRegistry.getAllModels() } returns listOf()
        
        val planner = TaskPlanner(mockNimClient, mockModelRegistry, mockModelRouter)
        
        val intent = Intent(IntentType.ACTION, 0.8f, "Open Settings")
        val plan = planner.fallbackPlan(intent)
        
        assertEquals(2, plan.steps.size)
        assertEquals(Action.Observe::class, plan.steps[0].action::class)
        assertEquals(Action.AskUser::class, plan.steps[1].action::class)
    }
    
    @Test
    fun testTaskExecutorStop() = runTest {
        val mockIntentClassifier = mockk<IntentClassifier>()
        val mockTaskPlanner = mockk<TaskPlanner>()
        val mockActionExecutor = mockk<com.aiva.automation.executor.ActionExecutor>()
        
        val executor = TaskExecutor(mockIntentClassifier, mockTaskPlanner, mockActionExecutor)
        
        executor.stop()
        
        assertEquals(TaskState.STOPPED, executor.state.value)
    }
}
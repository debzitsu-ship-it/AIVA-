package com.aiva.conversation

import com.aiva.ai.client.NimClient
import com.aiva.ai.fusion.MultiModelFusion
import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.router.ModelRouter
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatCompletionResponse
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.model.StreamChunk
import com.aiva.core.model.StreamChoice
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.ApiKeyManager
import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType
import com.aiva.core.task.TaskState
import com.aiva.memory.repository.ConversationRepository
import com.aiva.voice.viewmodel.VoiceViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {
    
    private val testDispatcher = TestDispatcher()
    
    @Test
    fun testConversationViewModelCreation() = runTest {
        val mockNimClient = mockk<NimClient>()
        val mockModelRegistry = mockk<ModelRegistry>()
        val mockModelRouter = mockk<ModelRouter>()
        val mockMultiModelFusion = mockk<MultiModelFusion>()
        val mockApiKeyManager = mockk<ApiKeyManager>()
        val mockConversationRepository = mockk<ConversationRepository>()
        val mockTaskExecutor = mockk<com.aiva.task.executor.TaskExecutor>()
        val mockVoiceViewModel = mockk<VoiceViewModel>()
        
        every { mockModelRegistry.getAllModels() } returns listOf(
            ModelInfo(
                id = "test/model",
                displayName = "Test Model",
                provider = "Test",
                speedProfile = com.aiva.core.model.SpeedProfile.FAST,
                reasoningCapability = com.aiva.core.model.ReasoningLevel.ADVANCED,
                agentSuitability = true,
                visionCapability = false,
                streamingCapability = true
            )
        )
        
        every { mockApiKeyManager.getAllKeys() } returns listOf(
            ApiKeyEntry(
                id = "key1",
                name = "Test Key",
                keyHash = "hash",
                encryptedKey = "encrypted",
                models = listOf("test/model")
            )
        )
        
        val viewModel = ConversationViewModel(
            nimClient = mockNimClient,
            modelRegistry = mockModelRegistry,
            modelRouter = mockModelRouter,
            multiModelFusion = mockMultiModelFusion,
            apiKeyManager = mockApiKeyManager,
            conversationRepository = mockConversationRepository,
            taskExecutor = mockTaskExecutor,
            voiceViewModel = mockVoiceViewModel
        )
        
        assertNotNull(viewModel)
        assertEquals(TaskState.IDLE, viewModel.state.value)
        assertEquals(0, viewModel.messages.value.size)
    }
    
    @Test
    fun testNewConversationResetsState() = runTest {
        val mockNimClient = mockk<NimClient>()
        val mockModelRegistry = mockk<ModelRegistry>()
        val mockModelRouter = mockk<ModelRouter>()
        val mockMultiModelFusion = mockk<MultiModelFusion>()
        val mockApiKeyManager = mockk<ApiKeyManager>()
        val mockConversationRepository = mockk<ConversationRepository>()
        val mockTaskExecutor = mockk<com.aiva.task.executor.TaskExecutor>()
        val mockVoiceViewModel = mockk<VoiceViewModel>()
        
        every { mockModelRegistry.getAllModels() } returns listOf()
        every { mockApiKeyManager.getAllKeys() } returns listOf()
        
        val viewModel = ConversationViewModel(
            nimClient = mockNimClient,
            modelRegistry = mockModelRegistry,
            modelRouter = mockModelRouter,
            multiModelFusion = mockMultiModelFusion,
            apiKeyManager = mockApiKeyManager,
            conversationRepository = mockConversationRepository,
            taskExecutor = mockTaskExecutor,
            voiceViewModel = mockVoiceViewModel
        )
        
        // Add a message
        viewModel.messages.value = listOf(ChatMessage(role = "user", content = "Test"))
        viewModel.newConversation()
        
        assertEquals(0, viewModel.messages.value.size)
        assertEquals(TaskState.IDLE, viewModel.state.value)
    }
    
    @Test
    fun testStopGeneration() = runTest {
        val mockNimClient = mockk<NimClient>()
        val mockModelRegistry = mockk<ModelRegistry>()
        val mockModelRouter = mockk<ModelRouter>()
        val mockMultiModelFusion = mockk<MultiModelFusion>()
        val mockApiKeyManager = mockk<ApiKeyManager>()
        val mockConversationRepository = mockk<ConversationRepository>()
        val mockTaskExecutor = mockk<com.aiva.task.executor.TaskExecutor>()
        val mockVoiceViewModel = mockk<VoiceViewModel>()
        
        every { mockModelRegistry.getAllModels() } returns listOf()
        every { mockApiKeyManager.getAllKeys() } returns listOf()
        
        val viewModel = ConversationViewModel(
            nimClient = mockNimClient,
            modelRegistry = mockModelRegistry,
            modelRouter = mockModelRouter,
            multiModelFusion = mockMultiModelFusion,
            apiKeyManager = mockApiKeyManager,
            conversationRepository = mockConversationRepository,
            taskExecutor = mockTaskExecutor,
            voiceViewModel = mockVoiceViewModel
        )
        
        viewModel.stopGeneration()
        
        assertEquals(TaskState.STOPPED, viewModel.state.value)
    }
}
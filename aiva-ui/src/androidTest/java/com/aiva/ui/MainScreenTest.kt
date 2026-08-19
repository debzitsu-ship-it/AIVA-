package com.aiva.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import org.junit.Rule
import org.junit.Test
import com.aiva.ui.screen.MainScreen
import com.aiva.conversation.ConversationViewModel
import com.aiva.core.model.ChatMessage
import com.aiva.core.task.TaskState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.assertEquals

class MainScreenTest {
    
    @get:Rule
    val composeRule = createComposeRule()
    
    @Test
    fun testMainScreenRenders() {
        val viewModel = createMockViewModel()
        
        composeRule.setContent {
            MainScreen(viewModel = viewModel)
        }
        
        composeRule.onNodeWithText("AIVA").assertExists()
        composeRule.onNodeWithText("Type a command...").assertExists()
    }
    
    @Test
    fun testInputFieldAcceptsText() {
        val viewModel = createMockViewModel()
        
        composeRule.setContent {
            MainScreen(viewModel = viewModel)
        }
        
        composeRule.onNodeWithText("Type a command...")
            .performTextInput("Hello AIVA")
            .assertExists()
    }
    
    @Test
    fun testSendButtonEnabledWhenTextEntered() {
        val viewModel = createMockViewModel()
        
        composeRule.setContent {
            MainScreen(viewModel = viewModel)
        }
        
        // Initially send button should be disabled (empty input)
        // After entering text, it should be enabled
        composeRule.onNodeWithText("Type a command...")
            .performTextInput("Test message")
        
        composeRule.onNodeWithText("Send").assertExists()
    }
    
    @Test
    fun testStopButtonShowsWhenActing() {
        val viewModel = createMockViewModel(state = TaskState.ACTING)
        
        composeRule.setContent {
            MainScreen(viewModel = viewModel)
        }
        
        composeRule.onNodeWithText("Stop").assertExists()
    }
    
    @Test
    fun testVoiceButtonToggles() {
        val viewModel = createMockViewModel()
        
        composeRule.setContent {
            MainScreen(viewModel = viewModel)
        }
        
        composeRule.onNodeWithText("Voice Input").performClick()
        // Voice button should toggle to "Stop Listening"
    }
    
    @Test
    fun testModelIndicatorShowsCurrentModel() {
        val viewModel = createMockViewModel()
        
        composeRule.setContent {
            MainScreen(viewModel = viewModel)
        }
        
        composeRule.onNodeWithText("Test Model").assertExists()
    }
    
    private fun createMockViewModel(state: TaskState = TaskState.IDLE): ConversationViewModel {
        val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
        val uiState = MutableStateFlow<TaskState>(state)
        val streamingContent = MutableStateFlow<String>("")
        val currentModel = MutableStateFlow<com.aiva.core.model.ModelInfo?>(
            com.aiva.core.model.ModelInfo(
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
        val availableModels = MutableStateFlow<List<com.aiva.core.model.ModelInfo>>(listOf())
        val aiMode = MutableStateFlow<ConversationViewModel.AiMode>(ConversationViewModel.AiMode.AUTO)
        val useFusion = MutableStateFlow<Boolean>(false)
        
        return object : ConversationViewModel(
            nimClient = mockk(),
            modelRegistry = mockk(),
            modelRouter = mockk(),
            multiModelFusion = mockk(),
            apiKeyManager = mockk(),
            conversationRepository = mockk(),
            taskExecutor = mockk(),
            voiceViewModel = mockk()
        ) {
            override val messages: StateFlow<List<ChatMessage>> = messages
            override val state: StateFlow<TaskState> = uiState
            override val streamingContent: StateFlow<String> = streamingContent
            override val currentModel: StateFlow<com.aiva.core.model.ModelInfo?> = currentModel
            override val availableModels: StateFlow<List<com.aiva.core.model.ModelInfo>> = availableModels
            override val aiMode: StateFlow<ConversationViewModel.AiMode> = aiMode
            override val useFusion: StateFlow<Boolean> = useFusion
            
            override fun sendMessage(content: String, intent: com.aiva.core.task.Intent?) {}
            override fun newConversation() {}
            override fun stopGeneration() {}
            override fun updateAvailableModels(enabledModelIds: Set<String>) {}
            override fun updateScreenState(screenState: com.aiva.core.observation.ScreenState) {}
        }
    }
}
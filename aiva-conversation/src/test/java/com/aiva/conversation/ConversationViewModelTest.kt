package com.aiva.conversation

import com.aiva.core.task.TaskState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConversationViewModelTest {

    @Test
    fun testAiModesExist() {
        val modes = ConversationViewModel.AiMode.entries
        assertTrue(modes.contains(ConversationViewModel.AiMode.AUTO))
        assertTrue(modes.contains(ConversationViewModel.AiMode.FASTEST))
        assertTrue(modes.contains(ConversationViewModel.AiMode.BEST_REASONING))
        assertTrue(modes.contains(ConversationViewModel.AiMode.MULTI_MODEL_FUSION))
        assertTrue(modes.contains(ConversationViewModel.AiMode.AGENT))
        assertEquals(5, modes.size)
    }

    @Test
    fun testTaskStateValues() {
        assertTrue(TaskState.entries.contains(TaskState.IDLE))
        assertTrue(TaskState.entries.contains(TaskState.SUCCESS))
    }
}

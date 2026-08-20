package com.aiva.core.action

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionTest {
    @Test
    fun targetCriteria() {
        assertTrue(Target(text = "ok").hasAnyCriteria())
        assertEquals("app", Action.LaunchApp("app").packageName)
    }
}

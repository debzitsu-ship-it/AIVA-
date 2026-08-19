package com.aiva.automation

import com.aiva.core.action.Action
import com.aiva.core.action.Target
import com.aiva.core.observation.ScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutomationTest {

    @Test
    fun testTargetCriteria() {
        assertTrue(!Target().hasAnyCriteria())
        assertTrue(Target(text = "Button").hasAnyCriteria())
    }

    @Test
    fun testLaunchAppAction() {
        val action = Action.LaunchApp("com.test")
        assertEquals("com.test", action.packageName)
    }

    @Test
    fun testScreenStateEquality() {
        val state1 = ScreenState(packageName = "com.test", activityName = "MainActivity", nodes = emptyList())
        val state2 = ScreenState(packageName = "com.test", activityName = "MainActivity", nodes = emptyList())
        assertEquals(state1.packageName, state2.packageName)
    }
}

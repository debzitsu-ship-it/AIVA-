package com.aiva.core.action

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionTest {

    @Test
    fun testTargetHasAnyCriteria() {
        assertTrue(!Target().hasAnyCriteria())
        assertTrue(Target(text = "test").hasAnyCriteria())
        assertTrue(Target(resourceId = "id").hasAnyCriteria())
        assertTrue(Target(index = 0).hasAnyCriteria())
    }

    @Test
    fun testTargetCopy() {
        val original = Target(text = "original", resourceId = "id", index = 1)
        val copied = original.copy(text = "modified")
        assertEquals("modified", copied.text)
        assertEquals("id", copied.resourceId)
        assertEquals(1, copied.index)
    }

    @Test
    fun testNormalizedBoundsCenter() {
        val bounds = NormalizedBounds(0.1f, 0.2f, 0.9f, 0.8f)
        val center = bounds.center()
        assertEquals(0.5f, center.x)
        assertEquals(0.5f, center.y)
    }

    @Test
    fun testActionTypesExist() {
        val tap = Action.Tap(Target(text = "Settings"))
        assertEquals("Settings", tap.target.text)
        val launch = Action.LaunchApp("com.example")
        assertEquals("com.example", launch.packageName)
    }
}

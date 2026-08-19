package com.aiva.core.action

import com.aiva.core.action.Action
import com.aiva.core.action.Target
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testActionSerialization() {
        val tap = Action.Tap(Target(text = "Settings", resourceId = "com.android.settings:id/button"))
        val jsonStr = json.encodeToString(tap)
        val decoded = json.decodeFromString(Action.serializer(), jsonStr)
        assertEquals(tap, decoded)
    }
    
    @Test
    fun testAllActionTypesSerialization() {
        val actions = listOf(
            Action.LaunchApp("com.example.app"),
            Action.OpenUrl("https://example.com"),
            Action.Tap(Target(text = "Button")),
            Action.DoubleTap(Target(resourceId = "id")),
            Action.LongPress(Target(className = "Button"), 2000),
            Action.Swipe(Point(0.1f, 0.5f), Point(0.9f, 0.5f), 500),
            Action.Scroll(ScrollDirection.DOWN, Target(text = "List"), 1000),
            Action.Type("Hello World", Target(resourceId = "input"), false),
            Action.ReplaceText(Target(text = "Field"), "New Text"),
            Action.Copy(Target(text = "Copy Me")),
            Action.Paste(Target(text = "Paste Here")),
            Action.Back(),
            Action.Home(),
            Action.Observe("Find the button"),
            Action.Wait(1000),
            Action.Find(Target(visionHint = "red button bottom right")),
            Action.Select(Target(text = "Option 1"), "true"),
            Action.Finish("Task completed"),
            Action.AskUser("Confirm?"),
            Action.Stop()
        )
        
        actions.forEach { action ->
            val jsonStr = json.encodeToString(action)
            val decoded = json.decodeFromString(Action.serializer(), jsonStr)
            assertEquals(action, decoded, "Failed for ${action::class.simpleName}")
        }
    }
    
    @Test
    fun testTargetHasAnyCriteria() {
        val empty = Target()
        assertTrue(!empty.hasAnyCriteria())
        
        val withText = Target(text = "test")
        assertTrue(withText.hasAnyCriteria())
        
        val withId = Target(resourceId = "id")
        assertTrue(withId.hasAnyCriteria())
        
        val withDesc = Target(contentDescription = "desc")
        assertTrue(withDesc.hasAnyCriteria())
        
        val withClass = Target(className = "Button")
        assertTrue(withClass.hasAnyCriteria())
        
        val withIndex = Target(index = 0)
        assertTrue(withIndex.hasAnyCriteria())
        
        val withVision = Target(visionHint = "hint")
        assertTrue(withVision.hasAnyCriteria())
        
        val withBounds = Target(normalizedBounds = NormalizedBounds(0f, 0f, 1f, 1f))
        assertTrue(withBounds.hasAnyCriteria())
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
    fun testNormalizedBoundsContains() {
        val bounds = NormalizedBounds(0.1f, 0.1f, 0.9f, 0.9f)
        assertTrue(bounds.contains(0.5f, 0.5f))
        assertTrue(!bounds.contains(0.05f, 0.5f))
        assertTrue(!bounds.contains(0.95f, 0.5f))
    }
}
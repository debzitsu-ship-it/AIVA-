package com.aiva.core.observation

import com.aiva.core.observation.DetectionCategory
import com.aiva.core.observation.NormalizedBounds
import com.aiva.core.observation.VisionDetection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservationTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testVisionDetectionSerialization() {
        val detection = VisionDetection(
            label = "button",
            confidence = 0.95f,
            bounds = NormalizedBounds(0.1f, 0.2f, 0.3f, 0.4f),
            category = DetectionCategory.BUTTON
        )
        
        val jsonStr = json.encodeToString(detection)
        val decoded = json.decodeFromString(VisionDetection.serializer(), jsonStr)
        assertEquals(detection, decoded)
    }
    
    @Test
    fun testAllDetectionCategories() {
        val categories = DetectionCategory.values()
        assertEquals(20, categories.size)
        assertTrue(categories.contains(DetectionCategory.BUTTON))
        assertTrue(categories.contains(DetectionCategory.ICON))
        assertTrue(categories.contains(DetectionCategory.GAME_HUD))
        assertTrue(categories.contains(DetectionCategory.HEALTH_BAR))
        assertTrue(categories.contains(DetectionCategory.CROSSHAIR))
        assertTrue(categories.contains(DetectionCategory.JOYSTICK))
        assertTrue(categories.contains(DetectionCategory.FIRE_BUTTON))
        assertTrue(categories.contains(DetectionCategory.MINIMAP))
    }
    
    @Test
    fun testNormalizedBoundsOperations() {
        val bounds = NormalizedBounds(0.1f, 0.1f, 0.9f, 0.9f)
        assertEquals(0.5f, bounds.center().x)
        assertEquals(0.5f, bounds.center().y)
        assertEquals(0.8f, bounds.width())
        assertEquals(0.8f, bounds.height())
        assertTrue(bounds.contains(0.5f, 0.5f))
        assertTrue(!bounds.contains(0.0f, 0.5f))
    }
}
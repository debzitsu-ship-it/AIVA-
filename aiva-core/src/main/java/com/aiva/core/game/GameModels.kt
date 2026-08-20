package com.aiva.core.game

import kotlinx.serialization.Serializable

@Serializable
data class GameProfile(
    val id: String,
    val name: String,
    val packageName: String,
    val activityNames: List<String>,
    val screenWidth: Int,
    val screenHeight: Int,
    val controls: GameControls,
    val detectionRegions: List<DetectionRegion>,
    val colorProfiles: Map<String, ColorProfile>,
    val calibration: CalibrationData? = null,
    val version: Int = 1
)

@Serializable
data class GameControls(
    val movementJoystick: ControlRegion,
    val aimArea: ControlRegion,
    val fireButton: ControlRegion,
    val reloadButton: ControlRegion?,
    val jumpButton: ControlRegion?,
    val crouchButton: ControlRegion?,
    val proneButton: ControlRegion?,
    val weaponSwitch: ControlRegion?,
    val mapButton: ControlRegion?,
    val inventoryButton: ControlRegion?,
    val interactButton: ControlRegion?,
    val vehicleControls: Map<String, ControlRegion> = emptyMap()
)

@Serializable
data class ControlRegion(
    val normalizedBounds: NormalizedBounds,
    val touchType: TouchType = TouchType.TAP,
    val swipeDirection: SwipeDirection? = null,
    val holdDuration: Int = 0
)

@Serializable
enum class TouchType {
    TAP, DOUBLE_TAP, LONG_PRESS, SWIPE, HOLD, JOYSTICK
}

@Serializable
enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
}

@Serializable
data class DetectionRegion(
    val id: String,
    val name: String,
    val normalizedBounds: NormalizedBounds,
    val detectionType: DetectionType,
    val templateImage: String? = null,
    val colorProfile: String? = null
)

@Serializable
enum class DetectionType {
    TEMPLATE_MATCH, COLOR_DETECTION, OCR, CUSTOM_MODEL
}

@Serializable
data class ColorProfile(
    val name: String,
    val targetColor: Int,
    val tolerance: Int = 30
)

@Serializable
data class CalibrationData(
    val screenWidth: Int,
    val screenHeight: Int,
    val dpi: Float,
    val referencePoints: List<Point>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class GameMode(
    val mode: GameModeType,
    val strategy: GameStrategy? = null,
    val aggressiveness: Float = 0.5f,
    val assistLevel: AssistLevel = AssistLevel.FULL
)

@Serializable
enum class GameModeType {
    MANUAL, ASSIST, FULL_AUTOMATION, OBSERVE_ONLY, PAUSED
}

@Serializable
enum class GameStrategy {
    AGGRESSIVE, DEFENSIVE, OBJECTIVE_FOCUSED, SURVIVAL, CUSTOM
}

@Serializable
enum class AssistLevel {
    NONE, LOW, MEDIUM, HIGH, FULL
}

@Serializable
data class FrameData(
    val timestamp: Long,
    val frameNumber: Long,
    val width: Int,
    val height: Int,
    val format: Int,
    val data: ByteArray
)

@Serializable
data class ControlCommand(
    val commands: List<TouchCommand>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class TouchCommand(
    val action: TouchAction,
    val x: Float,
    val y: Float,
    val duration: Int = 0,
    val pressure: Float = 1.0f
)

@Serializable
enum class TouchAction {
    DOWN, MOVE, UP, TAP, SWIPE
}

@Serializable
data class NormalizedBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun center(): Point = Point((left + right) / 2, (top + bottom) / 2)
    fun denormalize(width: Int, height: Int): Point =
        Point(center().x * width, center().y * height)
}

@Serializable
data class Point(
    val x: Float,
    val y: Float
)
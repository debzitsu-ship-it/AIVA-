package com.aiva.core.observation

import kotlinx.serialization.Serializable

@Serializable
data class ObservationResult(
    val screenState: ScreenState,
    val visionDetections: List<VisionDetection> = emptyList(),
    val accessibilityTree: AccessibilityTree? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class VisionDetection(
    val label: String,
    val confidence: Float,
    val bounds: NormalizedBounds,
    val category: DetectionCategory
)

enum class DetectionCategory {
    BUTTON, ICON, TEXT, MENU, DIALOG, FORM_FIELD,
    GAME_HUD, HEALTH_BAR, AMMO, MINIMAP, CROSSHAIR,
    JOYSTICK, FIRE_BUTTON, VEHICLE_CONTROL, INVENTORY,
    PLAYER, ENEMY, ALLY, OBJECTIVE, UNKNOWN
}

@Serializable
data class AccessibilityTree(
    val root: AccessibilityNodeSummary,
    val focusedNodeId: Int? = null
)

@Serializable
data class ScreenState(
    val packageName: String?,
    val activityName: String?,
    val nodes: List<AccessibilityNodeSummary>,
    val screenshot: ByteArray? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AccessibilityNodeSummary(
    val id: Int,
    val text: String?,
    val contentDescription: String?,
    val resourceId: String?,
    val className: String?,
    val bounds: NormalizedBounds?,
    val clickable: Boolean = false,
    val scrollable: Boolean = false,
    val editable: Boolean = false,
    val checkable: Boolean = false,
    val checked: Boolean = false,
    val focused: Boolean = false,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val children: List<AccessibilityNodeSummary> = emptyList()
)

@Serializable
data class NormalizedBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun center(): Point = Point((left + right) / 2, (top + bottom) / 2)
    fun width(): Float = right - left
    fun height(): Float = bottom - top
    fun contains(x: Float, y: Float): Boolean =
        x >= left && x <= right && y >= top && y <= bottom
}

@Serializable
data class Point(
    val x: Float,
    val y: Float
)

@Serializable
data class GameState(
    val gameProfile: String? = null,
    val playerPosition: Point? = null,
    val crosshairPosition: Point? = null,
    val health: Int? = null,
    val armor: Int? = null,
    val ammo: Int? = null,
    val currentWeapon: String? = null,
    val enemies: List<GameEntity> = emptyList(),
    val allies: List<GameEntity> = emptyList(),
    val minimapData: MinimapData? = null,
    val objectives: List<GameObjective> = emptyList(),
    val matchState: MatchState = MatchState.UNKNOWN,
    val controls: GameControls? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class GameEntity(
    val id: String,
    val type: EntityType,
    val position: Point,
    val distance: Float,
    val health: Int? = null
)

enum class EntityType {
    PLAYER, ENEMY, ALLY, VEHICLE, ITEM, OBJECTIVE
}

@Serializable
data class MinimapData(
    val playerPosition: Point,
    val enemyPositions: List<Point> = emptyList(),
    val objectivePositions: List<Point> = emptyList(),
    val bounds: NormalizedBounds
)

@Serializable
data class GameObjective(
    val id: String,
    val description: String,
    val position: Point?,
    val completed: Boolean
)

enum class MatchState {
    UNKNOWN, LOBBY, LOADING, PLAYING, PAUSED, RESPAWNING, VICTORY, DEFEAT, MENU
}

@Serializable
data class GameControls(
    val movementJoystick: NormalizedBounds?,
    val aimArea: NormalizedBounds?,
    val fireButton: NormalizedBounds?,
    val reloadButton: NormalizedBounds?,
    val jumpButton: NormalizedBounds?,
    val crouchButton: NormalizedBounds?,
    val proneButton: NormalizedBounds?,
    val weaponSwitch: NormalizedBounds?,
    val mapButton: NormalizedBounds?,
    val inventoryButton: NormalizedBounds?,
    val interactButton: NormalizedBounds?,
    val vehicleControls: Map<String, NormalizedBounds> = emptyMap()
)
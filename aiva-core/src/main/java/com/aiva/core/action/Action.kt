package com.aiva.core.action

import kotlinx.serialization.Serializable

@Serializable
sealed interface Action {
    @Serializable
    data class LaunchApp(val packageName: String, val action: String? = null) : Action

    @Serializable
    data class OpenUrl(val url: String) : Action

    @Serializable
    data class Tap(val target: Target) : Action

    @Serializable
    data class DoubleTap(val target: Target) : Action

    @Serializable
    data class LongPress(val target: Target, val duration: Int = 1000) : Action

    @Serializable
    data class Swipe(
        val from: Point,
        val to: Point,
        val duration: Int = 300
    ) : Action

    @Serializable
    data class Scroll(
        val direction: ScrollDirection,
        val target: Target? = null,
        val amount: Int = 500
    ) : Action

    @Serializable
    data class Type(
        val text: String,
        val target: Target? = null,
        val replace: Boolean = false
    ) : Action

    @Serializable
    data class ReplaceText(
        val target: Target,
        val newText: String
    ) : Action

    @Serializable
    data class Copy(val target: Target? = null) : Action

    @Serializable
    data class Paste(val target: Target? = null) : Action

    @Serializable
    data class Back(val unused: Int = 0) : Action

    @Serializable
    data class Home(val unused: Int = 0) : Action

    @Serializable
    data class Observe(val query: String) : Action

    @Serializable
    data class Wait(val ms: Long) : Action

    @Serializable
    data class Find(val target: Target) : Action

    @Serializable
    data class Select(val target: Target, val option: String) : Action

    @Serializable
    data class Finish(val result: String) : Action

    @Serializable
    data class AskUser(val question: String) : Action

    @Serializable
    data class Stop(val unused: Int = 0) : Action
}

@Serializable
data class Target(
    val text: String? = null,
    val resourceId: String? = null,
    val contentDescription: String? = null,
    val className: String? = null,
    val index: Int? = null,
    val visionHint: String? = null,
    val normalizedBounds: NormalizedBounds? = null
) {
    fun hasAnyCriteria(): Boolean {
        return text != null || resourceId != null || contentDescription != null
            || className != null || index != null || visionHint != null || normalizedBounds != null
    }
}

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
enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

@Serializable
data class ActionResult(
    val success: Boolean,
    val message: String? = null,
    val newScreenState: com.aiva.core.observation.ScreenState? = null
)

package com.aiva.core.action

sealed interface Action {
    data class LaunchApp(val packageName: String, val action: String? = null) : Action
    data class OpenUrl(val url: String) : Action
    data class Tap(val target: Target) : Action
    data class DoubleTap(val target: Target) : Action
    data class LongPress(val target: Target, val duration: Int = 1000) : Action
    data class Swipe(val from: Point, val to: Point, val duration: Int = 300) : Action
    data class Scroll(val direction: ScrollDirection, val target: Target? = null, val amount: Int = 500) : Action
    data class Type(val text: String, val target: Target? = null, val replace: Boolean = false) : Action
    data class ReplaceText(val target: Target, val newText: String) : Action
    data class Copy(val target: Target? = null) : Action
    data class Paste(val target: Target? = null) : Action
    data class Back(val unused: Int = 0) : Action
    data class Home(val unused: Int = 0) : Action
    data class Observe(val query: String) : Action
    data class Wait(val ms: Long) : Action
    data class Find(val target: Target) : Action
    data class Select(val target: Target, val option: String) : Action
    data class Finish(val result: String) : Action
    data class AskUser(val question: String) : Action
    data class Stop(val unused: Int = 0) : Action
}

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

data class Point(
    val x: Float,
    val y: Float
)

enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

data class ActionResult(
    val success: Boolean,
    val message: String? = null,
    val newScreenState: com.aiva.core.observation.ScreenState? = null
)

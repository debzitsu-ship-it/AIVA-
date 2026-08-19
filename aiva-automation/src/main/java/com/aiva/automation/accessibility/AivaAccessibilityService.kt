package com.aiva.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.action.Target
import com.aiva.core.observation.AccessibilityNodeSummary
import com.aiva.core.observation.ScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class AivaAccessibilityService : AccessibilityService(), AccessibilityController {
    
    companion object {
        private const val TAG = "AivaAccessibility"

        @Volatile var instance: AivaAccessibilityService? = null

        fun getInstance(): AivaAccessibilityService? = instance
    }
    
    private val uiHandler = Handler(Looper.getMainLooper())
    private val actionScope = CoroutineScope(Dispatchers.IO)
    private val pendingActions = ConcurrentHashMap<String, Channel<ActionResult>>()
    private var currentActionId: String? = null
    
    private val _screenState = MutableStateFlow<ScreenState?>(null)
    override val screenState: StateFlow<ScreenState?> = _screenState

    private val _serviceEnabled = MutableStateFlow(false)
    override val serviceEnabled: StateFlow<Boolean> = _serviceEnabled
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "AivaAccessibilityService created")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { e ->
            when (e.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                AccessibilityEvent.TYPE_VIEW_FOCUSED,
                AccessibilityEvent.TYPE_VIEW_SCROLLED,
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    updateScreenState()
                }
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
        _serviceEnabled.value = false
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        _serviceEnabled.value = true
        Log.d(TAG, "Accessibility service connected")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        setServiceInfo(info)
        
        updateScreenState()
    }
    
    override fun onDestroy() {
        instance = null
        super.onDestroy()
        Log.d(TAG, "AivaAccessibilityService destroyed")
    }
    
    override fun executeAction(action: Action): ActionResult {
        val actionId = java.util.UUID.randomUUID().toString()
        currentActionId = actionId
        
        val resultChannel = Channel<ActionResult>(1)
        pendingActions[actionId] = resultChannel
        
        actionScope.launch {
            try {
                val result = when (action) {
                    is Action.LaunchApp -> performLaunchApp(action)
                    is Action.OpenUrl -> performOpenUrl(action)
                    is Action.Tap -> performTap(action)
                    is Action.DoubleTap -> performDoubleTap(action)
                    is Action.LongPress -> performLongPress(action)
                    is Action.Swipe -> performSwipe(action)
                    is Action.Scroll -> performScroll(action)
                    is Action.Type -> performType(action)
                    is Action.ReplaceText -> performReplaceText(action)
                    is Action.Copy -> performCopy(action)
                    is Action.Paste -> performPaste(action)
                    is Action.Back -> performBack()
                    is Action.Home -> performHome()
                    is Action.Observe -> performObserve(action)
                    is Action.Wait -> performWait(action)
                    is Action.Find -> performFind(action)
                    is Action.Select -> performSelect(action)
                    is Action.Finish -> ActionResult(success = true, message = action.result)
                    is Action.AskUser -> ActionResult(success = true, message = "User asked: ${action.question}")
                    is Action.Stop -> ActionResult(success = true, message = "Stopped")
                    else -> ActionResult(success = false, message = "Unknown action")
                }
                
                resultChannel.trySend(result)
                pendingActions.remove(actionId)
                
                if (currentActionId == actionId) {
                    currentActionId = null
                }
                
                // Update screen state after action
                uiHandler.postDelayed(::updateScreenState, 500)
                
            } catch (e: Exception) {
                val errorResult = ActionResult(success = false, message = e.message)
                resultChannel.trySend(errorResult)
                pendingActions.remove(actionId)
            }
        }
        
        // Wait for result with timeout
        return kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeoutOrNull(30_000) {
                resultChannel.receive()
            } ?: ActionResult(success = false, message = "Action timeout")
        }
    }
    
    private fun performLaunchApp(action: Action.LaunchApp): ActionResult {
        val intent = packageManager.getLaunchIntentForPackage(action.packageName)
            ?.apply {
                action.action?.let { setAction(it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        
        return if (intent != null) {
            startActivity(intent)
            ActionResult(success = true, message = "Launched ${action.packageName}")
        } else {
            ActionResult(success = false, message = "App not found: ${action.packageName}")
        }
    }
    
    private fun performOpenUrl(action: Action.OpenUrl): ActionResult {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(action.url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        return try {
            startActivity(intent)
            ActionResult(success = true, message = "Opened URL")
        } catch (e: Exception) {
            ActionResult(success = false, message = "Failed to open URL: ${e.message}")
        }
    }
    
    private fun performTap(action: Action.Tap): ActionResult {
        val node = findNode(action.target)
        return if (node != null) {
            val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            node.recycle()
            ActionResult(success = success, message = if (success) "Tapped" else "Tap failed")
        } else {
            ActionResult(success = false, message = "Target not found for tap")
        }
    }
    
    private fun performDoubleTap(action: Action.DoubleTap): ActionResult {
        val node = findNode(action.target)
        return if (node != null) {
            val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (success) {
                Thread.sleep(100)
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            node.recycle()
            ActionResult(success = success, message = if (success) "Double tapped" else "Double tap failed")
        } else {
            ActionResult(success = false, message = "Target not found for double tap")
        }
    }
    
    private fun performLongPress(action: Action.LongPress): ActionResult {
        val node = findNode(action.target)
        return if (node != null) {
            val success = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            node.recycle()
            ActionResult(success = success, message = if (success) "Long pressed" else "Long press failed")
        } else {
            ActionResult(success = false, message = "Target not found for long press")
        }
    }
    
    private fun performSwipe(action: Action.Swipe): ActionResult {
        val fromX = (action.from.x * getScreenWidth()).toInt()
        val fromY = (action.from.y * getScreenHeight()).toInt()
        val toX = (action.to.x * getScreenWidth()).toInt()
        val toY = (action.to.y * getScreenHeight()).toInt()
        
        val path = android.graphics.Path().apply {
            moveTo(fromX.toFloat(), fromY.toFloat())
            lineTo(toX.toFloat(), toY.toFloat())
        }
        
        val gesture = android.accessibilityservice.GestureDescription.Builder()
            .addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, action.duration.toLong()))
            .build()
        
        val success = dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun completed(gestureDescription: android.accessibilityservice.GestureDescription) {
                // Completed
            }
            override fun cancelled(gestureDescription: android.accessibilityservice.GestureDescription) {
                // Cancelled
            }
        }, null)
        
        return ActionResult(success = success, message = if (success) "Swiped" else "Swipe failed")
    }
    
    private fun performScroll(action: Action.Scroll): ActionResult {
        val node = action.target?.let { findNode(it) } ?: rootInActiveWindow
            ?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?.let { findScrollableParent(it) }
        
        return if (node != null) {
            val success = when (action.direction) {
                com.aiva.core.action.ScrollDirection.UP -> node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                com.aiva.core.action.ScrollDirection.DOWN -> node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                com.aiva.core.action.ScrollDirection.LEFT -> false // Not directly supported
                com.aiva.core.action.ScrollDirection.RIGHT -> false
            }
            node.recycle()
            ActionResult(success = success, message = if (success) "Scrolled ${action.direction}" else "Scroll failed")
        } else {
            ActionResult(success = false, message = "No scrollable element found")
        }
    }
    
    private fun performType(action: Action.Type): ActionResult {
        val node = action.target?.let { findNode(it) } ?: rootInActiveWindow
            ?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        
        return if (node != null && node.isEditable) {
            val success = if (action.replace) {
                // Clear first then type
                node.performAction(AccessibilityNodeInfo.ACTION_SELECT_ALL)
                Thread.sleep(50)
                node.text?.let { _ -> }
                Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text) }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text)
                })
            } else {
                Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text) }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, action.text)
                })
            }
            node.recycle()
            ActionResult(success = success, message = if (success) "Typed text" else "Type failed")
        } else {
            ActionResult(success = false, message = "No editable field found")
        }
    }
    
    private fun performReplaceText(action: Action.ReplaceText): ActionResult {
        return performType(Action.Type(text = action.newText, target = action.target, replace = true))
    }
    
    private fun performCopy(action: Action.Copy): ActionResult {
        val node = action.target?.let { findNode(it) } ?: rootInActiveWindow
            ?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        
        return if (node != null) {
            val success = node.performAction(AccessibilityNodeInfo.ACTION_COPY)
            node.recycle()
            ActionResult(success = success, message = if (success) "Copied" else "Copy failed")
        } else {
            ActionResult(success = false, message = "No field to copy from")
        }
    }
    
    private fun performPaste(action: Action.Paste): ActionResult {
        val node = action.target?.let { findNode(it) } ?: rootInActiveWindow
            ?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        
        return if (node != null && node.isEditable) {
            val success = node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            node.recycle()
            ActionResult(success = success, message = if (success) "Pasted" else "Paste failed")
        } else {
            ActionResult(success = false, message = "No editable field to paste into")
        }
    }
    
    private fun performBack(): ActionResult {
        val success = performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        return ActionResult(success = success, message = if (success) "Back pressed" else "Back failed")
    }

    private fun performHome(): ActionResult {
        val success = performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        return ActionResult(success = success, message = if (success) "Home pressed" else "Home failed")
    }

    private fun performObserve(action: Action.Observe): ActionResult {
        updateScreenState()
        val screen = _screenState.value
        return ActionResult(
            success = true,
            message = "Screen observed: ${screen?.packageName} - ${screen?.nodes?.size ?: 0} nodes"
        )
    }

    private fun performWait(action: Action.Wait): ActionResult {
        Thread.sleep(action.ms)
        return ActionResult(success = true, message = "Waited ${action.ms}ms")
    }

    private fun performFind(action: Action.Find): ActionResult {
        val node = findNode(action.target)
        val found = node != null
        node?.recycle()
        return ActionResult(success = found, message = if (found) "Found target" else "Target not found")
    }
    
    private fun performSelect(action: Action.Select): ActionResult {
        val node = findNode(action.target)
        return if (node != null && node.isCheckable) {
            val success = if (action.option == "true" || action.option == "checked") {
                node.performAction(AccessibilityNodeInfo.ACTION_SELECT)
            } else {
                node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_SELECTION)
            }
            node.recycle()
            ActionResult(success = success, message = if (success) "Selected" else "Select failed")
        } else {
            ActionResult(success = false, message = "No selectable element found")
        }
    }
    
    private fun findNode(target: Target): AccessibilityNodeInfo? {
        var nodes = rootInActiveWindow?.let { getAllNodes(it) } ?: emptyList()
        
        // Filter by criteria
        target.text?.let { text ->
            nodes = nodes.filter { it.text?.toString()?.contains(text, true) == true }
        }
        target.resourceId?.let { id ->
            nodes = nodes.filter { it.viewIdResourceName?.contains(id, true) == true }
        }
        target.contentDescription?.let { desc ->
            nodes = nodes.filter { it.contentDescription?.toString()?.contains(desc, true) == true }
        }
        target.className?.let { cls ->
            nodes = nodes.filter { it.className?.toString()?.contains(cls, true) == true }
        }
        target.index?.let { idx ->
            nodes = nodes.drop(idx).take(1)
        }
        
        // Vision hint fallback - would use vision engine
        target.visionHint?.let { hint ->
            // TODO: Use vision to find element
        }
        
        return nodes.firstOrNull()?.apply { refresh() }
    }
    
    private fun findScrollableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current = node
        while (current != null) {
            if (current.isScrollable) return current
            current = current.parent
        }
        return null
    }
    
    private fun getAllNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        val queue = java.util.ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        
        while (queue.isNotEmpty()) {
            val node = queue.poll()
            nodes.add(node)
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return nodes
    }
    
    private fun updateScreenState() {
        actionScope.launch {
            val root = rootInActiveWindow
            if (root != null) {
                val nodes = getAllNodes(root).map { nodeToSummary(it) }
                val state = ScreenState(
                    packageName = root.packageName?.toString(),
                    activityName = getTopActivityName(),
                    nodes = nodes
                )
                uiHandler.post { _screenState.value = state }
            }
        }
    }
    
    private fun nodeToSummary(node: AccessibilityNodeInfo): AccessibilityNodeSummary {
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        return AccessibilityNodeSummary(
            id = System.identityHashCode(node),
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            resourceId = node.viewIdResourceName,
            className = node.className?.toString(),
            bounds = com.aiva.core.observation.NormalizedBounds(
                left = bounds.left / getScreenWidth().toFloat(),
                top = bounds.top / getScreenHeight().toFloat(),
                right = bounds.right / getScreenWidth().toFloat(),
                bottom = bounds.bottom / getScreenHeight().toFloat()
            ),
            clickable = node.isClickable,
            scrollable = node.isScrollable,
            editable = node.isEditable
        )
    }
    
    private fun getTopActivityName(): String? {
        val am = getSystemService(android.app.ActivityManager::class.java)
        return am?.runningTasks(1)?.firstOrNull()?.topActivity?.className
    }
    
    private fun getScreenWidth(): Int = resources.displayMetrics.widthPixels
    private fun getScreenHeight(): Int = resources.displayMetrics.heightPixels
    
    fun getCurrentScreenState(): ScreenState? = _screenState.value
    
    fun cancelCurrentAction() {
        currentActionId?.let { pendingActions.remove(it) }
        currentActionId = null
    }
}
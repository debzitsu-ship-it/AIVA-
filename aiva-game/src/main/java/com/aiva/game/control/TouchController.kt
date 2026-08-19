package com.aiva.game.control

import android.os.Build
import android.view.InputDevice
import android.view.MotionEvent
import com.aiva.core.game.ControlCommand
import com.aiva.core.game.TouchCommand
import com.aiva.core.game.TouchAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TouchController @Inject constructor() {
    
    private val commandChannel = Channel<ControlCommand>(100)
    private var injectionScope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false
    
    fun start() {
        if (isRunning) return
        isRunning = true
        
        injectionScope.launch {
            while (isRunning) {
                val command = commandChannel.receive()
                executeCommand(command)
            }
        }
    }
    
    fun stop() {
        isRunning = false
        injectionScope.cancel()
    }
    
    fun sendCommand(command: ControlCommand) {
        commandChannel.trySend(command)
    }
    
    private fun executeCommand(command: ControlCommand) {
        command.commands.forEach { touchCmd ->
            when (touchCmd.action) {
                TouchAction.DOWN -> injectTouchDown(touchCmd)
                TouchAction.MOVE -> injectTouchMove(touchCmd)
                TouchAction.UP -> injectTouchUp(touchCmd)
                TouchAction.TAP -> injectTap(touchCmd)
                TouchAction.SWIPE -> injectSwipe(touchCmd)
            }
        }
    }
    
    private fun injectTouchDown(cmd: TouchCommand) {
        val event = createMotionEvent(
            action = MotionEvent.ACTION_DOWN,
            x = cmd.x,
            y = cmd.y,
            pressure = cmd.pressure
        )
        injectEvent(event)
    }
    
    private fun injectTouchMove(cmd: TouchCommand) {
        val event = createMotionEvent(
            action = MotionEvent.ACTION_MOVE,
            x = cmd.x,
            y = cmd.y,
            pressure = cmd.pressure
        )
        injectEvent(event)
    }
    
    private fun injectTouchUp(cmd: TouchCommand) {
        val event = createMotionEvent(
            action = MotionEvent.ACTION_UP,
            x = cmd.x,
            y = cmd.y,
            pressure = 0f
        )
        injectEvent(event)
    }
    
    private fun injectTap(cmd: TouchCommand) {
        val downTime = System.currentTimeMillis()
        val eventTime = System.currentTimeMillis()
        
        val downEvent = MotionEvent.obtain(
            downTime, eventTime, MotionEvent.ACTION_DOWN,
            cmd.x, cmd.y, 0, cmd.pressure, 1f, 0, 0, 0, 0,
            InputDevice.SOURCE_TOUCHSCREEN, 0
        )
        injectEvent(downEvent)
        
        // Small delay
        Thread.sleep(16)
        
        val upEvent = MotionEvent.obtain(
            downTime, eventTime + 16, MotionEvent.ACTION_UP,
            cmd.x, cmd.y, 0, 0f, 1f, 0, 0, 0, 0,
            InputDevice.SOURCE_TOUCHSCREEN, 0
        )
        injectEvent(upEvent)
    }
    
    private fun injectSwipe(cmd: TouchCommand) {
        val downTime = System.currentTimeMillis()
        val duration = cmd.duration.toLong()
        val steps = (duration / 16).coerceAtLeast(2).coerceAtMost(100)
        
        val startX = cmd.x
        val startY = cmd.y
        // For swipe, we need end coordinates - would come from command
        val endX = cmd.x + 100 // placeholder
        val endY = cmd.y + 100 // placeholder
        
        (0..steps).forEach { i ->
            val progress = i.toFloat() / steps
            val x = startX + (endX - startX) * progress
            val y = startY + (endY - startY) * progress
            val eventTime = downTime + (duration * i / steps).toLong()
            
            val action = if (i == 0) MotionEvent.ACTION_DOWN
            else if (i == steps) MotionEvent.ACTION_UP
            else MotionEvent.ACTION_MOVE
            
            val event = MotionEvent.obtain(
                downTime, eventTime, action,
                x, y, 0, cmd.pressure, 1f, 0, 0, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            injectEvent(event)
            
            if (i < steps) Thread.sleep(16)
        }
    }
    
    private fun createMotionEvent(
        action: Int,
        x: Float,
        y: Float,
        pressure: Float
    ): MotionEvent {
        val downTime = System.currentTimeMillis()
        val eventTime = System.currentTimeMillis()
        
        return MotionEvent.obtain(
            downTime, eventTime, action,
            x, y, 0, pressure, 1f, 0, 0, 0, 0,
            InputDevice.SOURCE_TOUCHSCREEN, 0
        )
    }
    
    private fun injectEvent(event: MotionEvent) {
        // In real implementation, use InputManager.injectInputEvent
        // Requires INJECT_EVENTS permission (system app) or accessibility service
        // For non-system apps, use AccessibilityService dispatchGesture
        try {
            // This would be implemented via AccessibilityService
            // or via root/shell if available
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
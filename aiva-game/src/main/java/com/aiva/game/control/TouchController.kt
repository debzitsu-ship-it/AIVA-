package com.aiva.game.control

import android.os.SystemClock
import android.view.MotionEvent
import com.aiva.core.game.ControlCommand
import com.aiva.core.game.TouchAction
import com.aiva.core.game.TouchCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TouchController @Inject constructor() {

    private val commandChannel = Channel<ControlCommand>(100)
    private var injectionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        injectionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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
                TouchAction.DOWN -> injectEvent(createMotionEvent(MotionEvent.ACTION_DOWN, touchCmd))
                TouchAction.MOVE -> injectEvent(createMotionEvent(MotionEvent.ACTION_MOVE, touchCmd))
                TouchAction.UP -> injectEvent(createMotionEvent(MotionEvent.ACTION_UP, touchCmd))
                TouchAction.TAP -> {
                    injectEvent(createMotionEvent(MotionEvent.ACTION_DOWN, touchCmd))
                    injectEvent(createMotionEvent(MotionEvent.ACTION_UP, touchCmd))
                }
                TouchAction.SWIPE -> injectEvent(createMotionEvent(MotionEvent.ACTION_MOVE, touchCmd))
            }
        }
    }

    private fun createMotionEvent(action: Int, cmd: TouchCommand): MotionEvent {
        val now = SystemClock.uptimeMillis()
        return MotionEvent.obtain(now, now, action, cmd.x, cmd.y, 0)
    }

    private fun injectEvent(event: MotionEvent) {
        // Real injection is done through AccessibilityService.dispatchGesture.
        event.recycle()
    }
}

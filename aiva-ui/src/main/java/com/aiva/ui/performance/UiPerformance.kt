package com.aiva.ui.performance

import androidx.compose.runtime.Composable
import com.aiva.core.performance.PerformanceMonitor

@Composable
fun PerformanceOverlay(enabled: Boolean = false) {
    if (!enabled) return
}

@Composable
fun TrackScreenRender(screenName: String) {
    PerformanceMonitor.record("ScreenRender:$screenName", 0)
}

@Composable
fun PerformanceMarker(name: String) {
    PerformanceMonitor.record("Marker:$name", 0)
}

object DebugPerformance {
    @Composable
    fun ShowFps(enabled: Boolean = true) {
        if (!enabled) return
    }

    @Composable
    fun ShowMemory(enabled: Boolean = true) {
        if (!enabled) return
    }

    fun dumpAll() {
        PerformanceMonitor.dump()
    }
}

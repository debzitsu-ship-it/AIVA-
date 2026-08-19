package com.aiva.ui.performance

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.aiva.core.performance.FrameRateCounter
import com.aiva.core.performance.MemoryTracker
import com.aiva.core.performance.PerformanceMonitor

/**
 * Compose utilities for performance monitoring
 */
@Composable
fun PerformanceOverlay(enabled: Boolean = false) {
    if (!enabled) return
    
    val frameCounter = remember { FrameRateCounter() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        while (true) {
            frameCounter.frame()
            delay(16) // ~60fps
        }
    }
    
    DisposableEffect(Unit) {
        val fpsText = remember { 
            androidx.compose.ui.graphics.Canvas(modifier = androidx.compose.ui.Modifier
                .size(200.dp, 60.dp)
                .padding(16.dp)
                .graphicsLayer { translationX = 0f; translationY = 0f }
            ) {
                // Draw FPS overlay
            }
        }
        onDispose { }
    }
    
    // This would render a small overlay in debug builds
    // Implementation would use a custom view or overlay service
}

/**
 * Measure Compose recomposition
 */
@Composable
fun <T> MeasureRecomposition(
    key: String,
    content: @Composable () -> T
): T {
    val start = SystemClock.elapsedRealtimeNanos()
    val result = content()
    val elapsed = SystemClock.elapsedRealtimeNanos() - start
    
    // In debug builds, record recomposition time
    if (BuildConfig.DEBUG) {
        PerformanceMonitor.record("Recomposition:$key", elapsed)
    }
    
    return result
}

/**
 * Track screen render time
 */
@Composable
fun TrackScreenRender(screenName: String) {
    val start = SystemClock.elapsedRealtimeNanos()
    
    DisposableEffect(Unit) {
        onDispose {
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            PerformanceMonitor.record("ScreenRender:$screenName", elapsed)
        }
    }
}

/**
 * Measure async operation
 */
suspend fun <T> MeasureAsync(
    name: String,
    block: suspend () -> T
): T {
    return PerformanceMonitor.measureSuspend(name, block)
}

/**
 * Measure on IO dispatcher
 */
suspend fun <T> MeasureOnIO(
    name: String,
    block: suspend () -> T
): T {
    return PerformanceMonitor.measureOn(name, kotlinx.coroutines.Dispatchers.IO, block)
}

/**
 * Performance marker for critical paths
 */
@Composable
fun PerformanceMarker(name: String) {
    val start = SystemClock.elapsedRealtimeNanos()
    
    DisposableEffect(Unit) {
        onDispose {
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            PerformanceMonitor.record("Marker:$name", elapsed)
        }
    }
}

/**
 * Debug build performance utilities
 */
object DebugPerformance {
    
    @Composable
    fun ShowFps(enabled: Boolean = true) {
        if (!enabled || !BuildConfig.DEBUG) return
        
        // Would show a small FPS counter in corner
    }
    
    @Composable
    fun ShowMemory(enabled: Boolean = true) {
        if (!enabled || !BuildConfig.DEBUG) return
        
        // Would show memory usage
    }
    
    fun dumpAll() {
        PerformanceMonitor.dump()
        MemoryTracker.dump()
    }
}

import android.os.BuildConfig
package com.aiva.core.performance

import android.os.SystemClock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance measurement utilities for AIVA
 */
object PerformanceMonitor {
    
    private val measurements = mutableMapOf<String, MeasurementData>()
    private val lock = Any()
    
    data class MeasurementData(
        val name: String,
        var count: Long = 0,
        var totalNanos: Long = 0,
        var minNanos: Long = Long.MAX_VALUE,
        var maxNanos: Long = Long.MIN_VALUE,
        var lastNanos: Long = 0
    ) {
        val avgNanos: Double
            get() = if (count > 0) totalNanos.toDouble() / count else 0.0
        
        val avgMs: Double
            get() = avgNanos / 1_000_000
        
        val minMs: Double
            get() = minNanos.toDouble() / 1_000_000
        
        val maxMs: Double
            get() = maxNanos.toDouble() / 1_000_000
        
        val lastMs: Double
            get() = lastNanos.toDouble() / 1_000_000
    }
    
    /**
     * Measure execution time of a block
     */
    inline fun <T> measure(name: String, block: () -> T): T {
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return block()
        } finally {
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            record(name, elapsed)
        }
    }
    
    /**
     * Measure execution time of a suspending block
     */
    suspend fun <T> measureSuspend(name: String, block: suspend () -> T): T {
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return block()
        } finally {
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            record(name, elapsed)
        }
    }
    
    /**
     * Measure execution time on specific dispatcher
     */
    suspend fun <T> measureOn(
        name: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): T = withContext(dispatcher) {
        measureSuspend(name, block)
    }
    
    fun record(name: String, nanos: Long) {
        synchronized(lock) {
            val data = measurements.getOrPut(name) { MeasurementData(name) }
            data.count++
            data.totalNanos += nanos
            data.minNanos = minOf(data.minNanos, nanos)
            data.maxNanos = maxOf(data.maxNanos, nanos)
            data.lastNanos = nanos
        }
    }
    
    /**
     * Get measurement for a specific name
     */
    fun getMeasurement(name: String): MeasurementData? {
        synchronized(lock) {
            return measurements[name]
        }
    }
    
    /**
     * Get all measurements
     */
    fun getAllMeasurements(): Map<String, MeasurementData> {
        synchronized(lock) {
            return measurements.toMap()
        }
    }
    
    /**
     * Reset all measurements
     */
    fun reset() {
        synchronized(lock) {
            measurements.clear()
        }
    }
    
    /**
     * Print all measurements to log
     */
    fun dump() {
        synchronized(lock) {
            println("=== Performance Measurements ===")
            measurements.values
                .sortedByDescending { it.avgNanos }
                .forEach { data ->
                    println("${data.name}: count=${data.count}, avg=${String.format("%.2f", data.avgMs)}ms, " +
                        "min=${String.format("%.2f", data.minMs)}ms, max=${String.format("%.2f", data.maxMs)}ms, " +
                        "last=${String.format("%.2f", data.lastMs)}ms")
                }
        }
    }
}

/**
 * High-frequency counter for frame rate monitoring
 */
class FrameRateCounter {
    private val frameTimes = mutableListOf<Long>()
    private val maxSamples = 120
    private var lastFrameTime = 0L
    
    fun frame() {
        val now = SystemClock.elapsedRealtimeNanos()
        if (lastFrameTime > 0) {
            frameTimes.add(now - lastFrameTime)
            if (frameTimes.size > maxSamples) frameTimes.removeAt(0)
        }
        lastFrameTime = now
    }
    
    fun getFps(): Double {
        if (frameTimes.size < 2) return 0.0
        val avgFrameTime = frameTimes.average()
        return if (avgFrameTime > 0) 1_000_000_000.0 / avgFrameTime else 0.0
    }
    
    fun getFrameTimeMs(): Double {
        if (frameTimes.isEmpty()) return 0.0
        return frameTimes.average() / 1_000_000.0
    }
    
    fun getPercentile(percentile: Double): Double {
        if (frameTimes.isEmpty()) return 0.0
        val sorted = frameTimes.sorted()
        val index = ((sorted.size - 1) * percentile).toInt()
        return sorted[index].toDouble() / 1_000_000.0
    }
    
    fun reset() {
        frameTimes.clear()
        lastFrameTime = 0
    }
}

/**
 * Memory usage tracker
 */
object MemoryTracker {
    
    private val runtime = Runtime.getRuntime()
    
    fun getUsedMemoryMb(): Double {
        return (runtime.totalMemory - runtime.freeMemory) / 1024.0 / 1024.0
    }
    
    fun getTotalMemoryMb(): Double {
        return runtime.totalMemory / 1024.0 / 1024.0
    }
    
    fun getMaxMemoryMb(): Double {
        return runtime.maxMemory / 1024.0 / 1024.0
    }
    
    fun getFreeMemoryMb(): Double {
        return runtime.freeMemory / 1024.0 / 1024.0
    }
    
    fun getMemoryPressure(): Double {
        return (runtime.totalMemory - runtime.freeMemory).toDouble() / runtime.maxMemory
    }
    
    fun dump() {
        println("=== Memory Usage ===")
        println("Used: ${String.format("%.2f", getUsedMemoryMb())} MB")
        println("Total: ${String.format("%.2f", getTotalMemoryMb())} MB")
        println("Max: ${String.format("%.2f", getMaxMemoryMb())} MB")
        println("Free: ${String.format("%.2f", getFreeMemoryMb())} MB")
        println("Pressure: ${String.format("%.1f", getMemoryPressure() * 100)}%")
    }
}

/**
 * Network latency tracker
 */
class NetworkLatencyTracker {
    private val latencies = mutableListOf<Long>()
    private val maxSamples = 100
    
    fun record(latencyMs: Long) {
        latencies.add(latencyMs)
        if (latencies.size > maxSamples) latencies.removeAt(0)
    }
    
    fun getAverageMs(): Double {
        return if (latencies.isEmpty()) 0.0 else latencies.average()
    }
    
    fun getMinMs(): Long = latencies.minOrNull() ?: 0
    fun getMaxMs(): Long = latencies.maxOrNull() ?: 0
    
    fun getPercentile(percentile: Double): Double {
        if (latencies.isEmpty()) return 0.0
        val sorted = latencies.sorted()
        val index = ((sorted.size - 1) * percentile).toInt()
        return sorted[index].toDouble()
    }
    
    fun reset() {
        latencies.clear()
    }
}
package com.aiva.core.performance

import android.os.SystemClock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PerformanceMonitor {
    data class MeasurementData(
        val name: String,
        var count: Long = 0,
        var totalNanos: Long = 0,
        var minNanos: Long = Long.MAX_VALUE,
        var maxNanos: Long = Long.MIN_VALUE,
        var lastNanos: Long = 0
    ) {
        val avgNanos: Double get() = if (count > 0) totalNanos.toDouble() / count else 0.0
        val avgMs: Double get() = avgNanos / 1_000_000
        val minMs: Double get() = minNanos.toDouble() / 1_000_000
        val maxMs: Double get() = maxNanos.toDouble() / 1_000_000
        val lastMs: Double get() = lastNanos.toDouble() / 1_000_000
    }

    private val measurements = mutableMapOf<String, MeasurementData>()
    private val lock = Any()

    fun <T> measure(name: String, block: () -> T): T {
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return block()
        } finally {
            record(name, SystemClock.elapsedRealtimeNanos() - start)
        }
    }

    suspend fun <T> measureSuspend(name: String, block: suspend () -> T): T {
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return block()
        } finally {
            record(name, SystemClock.elapsedRealtimeNanos() - start)
        }
    }

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

    fun getMeasurement(name: String): MeasurementData? = synchronized(lock) { measurements[name] }

    fun getAllMeasurements(): Map<String, MeasurementData> = synchronized(lock) { measurements.toMap() }

    fun reset() {
        synchronized(lock) { measurements.clear() }
    }
}

class FrameRateCounter {
    private val frameTimes = mutableListOf<Long>()
    private var lastFrameTime = 0L

    fun frame() {
        val now = SystemClock.elapsedRealtimeNanos()
        if (lastFrameTime > 0) {
            frameTimes.add(now - lastFrameTime)
            if (frameTimes.size > 120) frameTimes.removeAt(0)
        }
        lastFrameTime = now
    }

    fun getFps(): Double {
        if (frameTimes.size < 2) return 0.0
        val avg = frameTimes.average()
        return if (avg > 0) 1_000_000_000.0 / avg else 0.0
    }
}

object MemoryTracker {
    private val runtime = Runtime.getRuntime()
    fun getUsedMemoryMb(): Double = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0
}

class NetworkLatencyTracker {
    private val latencies = mutableListOf<Long>()
    fun record(latencyMs: Long) {
        latencies.add(latencyMs)
        if (latencies.size > 100) latencies.removeAt(0)
    }
    fun getAverageMs(): Double = if (latencies.isEmpty()) 0.0 else latencies.average()
}

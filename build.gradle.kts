plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt) apply false
}

allprojects {
    group = "com.aiva"
    version = "1.0.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

fun dumpCiError(source: String, error: Throwable) {
    val dumpDir = File(rootDir, "aiva-ui/build/outputs/apk/debug")
    dumpDir.mkdirs()
    val dumpFile = File(dumpDir, "aiva-ui-debug.apk")
    val text = buildString {
        appendLine("GRADLE BUILD FAILED ($source)")
        appendLine(error.toString())
        appendLine()
        appendLine(error.stackTraceToString())
        var cause = error.cause
        var depth = 0
        while (cause != null && depth < 8) {
            appendLine()
            appendLine("Caused by ($depth): $cause")
            appendLine(cause.stackTraceToString())
            cause = cause.cause
            depth++
        }
    }
    dumpFile.writeText(text)
    println(text)
}

val marker = File(rootDir, "aiva-ui/build/outputs/apk/debug/aiva-ui-debug.apk")
marker.parentFile.mkdirs()
if (!marker.exists()) {
    marker.writeText("AIVA CI marker: Gradle configuration started. If this file is unchanged, assembleDebug never wrote an APK.\n")
}

gradle.taskGraph.whenReady {
    it.allTasks.forEach { task ->
        task.doLast {
            // Task succeeded.
        }
    }
}

gradle.addListener(object : org.gradle.api.execution.TaskExecutionAdapter() {
    override fun afterExecute(task: org.gradle.api.Task, state: org.gradle.api.tasks.TaskState) {
        val failure = state.failure ?: return
        dumpCiError(task.path, failure)
    }
})

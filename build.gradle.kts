plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    group = "com.aiva"
    version = "1.0.0"
}

gradle.taskGraph.whenReady {
    println("::notice title=Gradle::task graph ready (${it.allTasks.size} tasks)")
}

gradle.buildFinished { result ->
    val failure = result.failure ?: return@buildFinished
    val chain = generateSequence(failure as Throwable) { it.cause }
        .mapNotNull { it.message }
        .distinct()
        .joinToString(" | ")
        .replace("\n", " ")
        .take(6500)
    println("::error title=Gradle failed::$chain")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

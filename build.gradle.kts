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

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    afterEvaluate {
        val dump = tasks.register("reportKotlinFailures") {
            doLast {
                val buildDirFile = layout.buildDirectory.get().asFile
                if (!buildDirFile.exists()) {
                    println("::warning title=${project.name}::no build dir")
                    return@doLast
                }
                val names = buildDirFile.walkTopDown().maxDepth(6)
                    .filter { it.isFile }
                    .map { it.relativeTo(buildDirFile).path }
                    .take(40)
                    .joinToString(",")
                println("::warning title=${project.name} files::$names")
                buildDirFile.walkTopDown().maxDepth(8).forEach { file ->
                    if (!file.isFile || file.length() !in 1..80_000L) return@forEach
                    if (file.extension !in setOf("txt", "log", "out")) return@forEach
                    val text = runCatching { file.readText() }.getOrDefault("")
                    if (text.isBlank()) return@forEach
                    println("::error title=${project.name} ${file.name}::${text.take(2000).replace("\n", " | ")}")
                }
            }
        }
        tasks.matching { it.name == "compileDebugKotlin" }.configureEach {
            doLast { println("::warning title=${project.name}::ok compileDebugKotlin") }
            finalizedBy(dump)
        }
    }
}

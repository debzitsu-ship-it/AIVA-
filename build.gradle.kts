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
                val chunks = mutableListOf<String>()
                if (buildDirFile.exists()) {
                    buildDirFile.walkTopDown().maxDepth(10).forEach { file ->
                        if (!file.isFile || file.length() !in 1..250_000L) return@forEach
                        if (file.extension !in setOf("log", "txt", "out")) return@forEach
                        val text = runCatching { file.readText() }.getOrNull() ?: return@forEach
                        if (text.contains("e: ") || text.contains("error:") || text.contains("FAILED")) {
                            chunks += "${file.name}: ${text.take(2000)}"
                        }
                    }
                }
                if (chunks.isNotEmpty()) {
                    println("::error title=${project.name}::${chunks.joinToString(" | ").take(6000)}")
                } else {
                    println("::warning title=${project.name}::reportKotlinFailures ran, no log fragments")
                }
            }
        }
        tasks.matching {
            it.name == "compileDebugKotlin" || it.name == "assembleDebug" || it.name == "lintDebug"
        }.configureEach {
            finalizedBy(dump)
        }
    }
}

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
                val files = if (buildDirFile.exists()) {
                    buildDirFile.walkTopDown().maxDepth(6)
                        .filter { it.isFile }
                        .map { it.relativeTo(buildDirFile).path }
                        .take(50)
                        .joinToString(",")
                } else {
                    "missing"
                }
                println("::warning title=${project.name} files::$files")
                if (buildDirFile.exists()) {
                    buildDirFile.walkTopDown().maxDepth(10).forEach { file ->
                        if (!file.isFile || file.length() !in 1..250_000L) return@forEach
                        if (file.extension !in setOf("log", "txt", "out", "json")) return@forEach
                        val text = runCatching { file.readText() }.getOrNull() ?: return@forEach
                        if (text.contains("e: ") || text.contains("error:") || text.contains("FAILED")) {
                            println("::error title=${project.name} ${file.name}::${text.take(2500).replace("\n", " | ")}")
                        }
                    }
                }
            }
        }
        tasks.matching {
            it.name == "compileDebugKotlin" ||
                it.name == "compileDebugJavaWithJavac" ||
                it.name == "processDebugManifest" ||
                it.name == "mergeDebugResources" ||
                it.name == "processDebugResources" ||
                it.name == "bundleLibCompileToJarDebug" ||
                it.name == "assembleDebug"
        }.configureEach {
            val taskName = name
            doFirst { println("::warning title=${project.name}::start $taskName") }
            doLast { println("::warning title=${project.name}::ok $taskName") }
            finalizedBy(dump)
        }
    }
}

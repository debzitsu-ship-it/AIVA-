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
                val names = buildDirFile.walkTopDown().maxDepth(5)
                    .filter { it.isFile }
                    .map { it.relativeTo(buildDirFile).path }
                    .take(30)
                    .joinToString(",")
                println("::warning title=${project.name} files::$names")
            }
        }
        tasks.matching { it.name == "compileDebugKotlin" }.configureEach {
            doLast { println("::warning title=${project.name}::ok compileDebugKotlin") }
            finalizedBy(dump)
        }
    }
}

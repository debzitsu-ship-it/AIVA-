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
                val console = rootProject.file("build/gradle-console.txt")
                if (console.exists()) {
                    val errors = console.readLines().filter { line ->
                        line.contains("e: ") || line.contains("error:") || line.contains("FAILED") ||
                            line.contains("e:") && line.contains(".kt")
                    }.take(40)
                    if (errors.isNotEmpty()) {
                        println("::error title=${project.name} console::${errors.joinToString(" | ").take(6500)}")
                    } else {
                        val tail = console.readLines().takeLast(30).joinToString(" | ")
                        println("::warning title=${project.name} console tail::${tail.take(4000)}")
                    }
                }
            }
        }
        tasks.matching { it.name == "compileDebugKotlin" }.configureEach {
            doLast { println("::warning title=${project.name}::ok compileDebugKotlin") }
            finalizedBy(dump)
        }
    }
}

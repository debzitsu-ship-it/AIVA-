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
        tasks.matching { it.name == "processDebugManifest" || it.name == "compileDebugKotlin" }.configureEach {
            val taskName = name
            doLast { println("::warning title=${project.name}::ok $taskName") }
        }
    }
}

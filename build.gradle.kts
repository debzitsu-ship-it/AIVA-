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

subprojects {
    plugins.withId("org.jetbrains.kotlin.kapt") {
        extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension>("kapt") {
            correctErrorTypes = true
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

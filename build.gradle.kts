plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
}

println("::notice title=config::root after plugins")

allprojects {
    group = "com.aiva"
    version = "1.0.0"
}

gradle.beforeProject {
    println("::notice title=config::before ${it.path}")
}

gradle.afterProject {
    println("::notice title=config::after ${it.path}")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

// Register before any other settings work so later failures still emit annotations.
gradle.buildFinished { result ->
    val failure = result.failure
    if (failure == null) {
        println("::notice title=Gradle::build finished successfully")
        return@buildFinished
    }
    val chain = generateSequence(failure as Throwable) { it.cause }
        .mapNotNull { it.message }
        .distinct()
        .joinToString(" | ")
        .replace("\r", " ")
        .replace("\n", " ")
        .take(6500)
    println("::error title=Gradle failed::$chain")
}

val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
if (!sdkDir.isNullOrBlank()) {
    java.io.File(settingsDir, "local.properties").writeText(
        "sdk.dir=${sdkDir.replace("\\", "\\\\")}\n"
    )
    println("::notice title=settings::wrote local.properties sdk.dir=$sdkDir")
} else {
    println("::warning title=settings::ANDROID_HOME and ANDROID_SDK_ROOT are unset")
}

rootProject.name = "AIVA"

include(
    ":aiva-core",
    ":aiva-ui",
    ":aiva-conversation",
    ":aiva-voice",
    ":aiva-automation",
    ":aiva-observation",
    ":aiva-task",
    ":aiva-game",
    ":aiva-ai",
    ":aiva-security",
    ":aiva-memory"
)

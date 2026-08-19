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

val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
if (!sdkDir.isNullOrBlank()) {
    file("local.properties").writeText("sdk.dir=${sdkDir.replace("\\", "\\\\")}\n")
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

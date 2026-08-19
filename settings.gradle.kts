pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library" || requested.id.id == "org.jetbrains.kotlin.android") {
                useModule("com.android.tools.build:gradle:8.5.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
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
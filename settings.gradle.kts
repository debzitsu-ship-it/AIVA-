pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Accept Android SDK licenses when the runner SDK dir is writable (GitHub Actions).
runCatching {
    val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (!sdkDir.isNullOrBlank()) {
        val licenses = java.io.File(sdkDir, "licenses")
        licenses.mkdirs()
        java.io.File(licenses, "android-sdk-license")
            .writeText("24333f8a63b6825ea9c5514f83c2829b004d1fee\n")
        java.io.File(licenses, "android-sdk-preview-license")
            .writeText("84831b9409646161da1d3268a0436ddba1bbb494\n")
    }
}



dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
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

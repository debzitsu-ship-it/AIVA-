pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

runCatching {
    val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        ?: "/usr/local/lib/android/sdk"
    val sdk = java.io.File(sdkDir)
    java.io.File(rootDir, "local.properties").writeText(
        "sdk.dir=${sdk.absolutePath.replace("\\", "\\\\")}\n"
    )
    val licenses = java.io.File(sdk, "licenses")
    licenses.mkdirs()
    java.io.File(licenses, "android-sdk-license")
        .writeText("24333f8a63b6825ea9c5514f83c2829b004d1fee\n")
    java.io.File(licenses, "android-sdk-preview-license")
        .writeText("84831b9409646161da1d3268a0436ddba1bbb494\n")
    val platforms = java.io.File(sdk, "platforms").list()?.sorted()?.joinToString(",") ?: "none"
    val buildTools = java.io.File(sdk, "build-tools").list()?.sorted()?.joinToString(",") ?: "none"
    val msg = "sdk=${sdk.absolutePath}; exists=${sdk.exists()}; platforms=$platforms; build-tools=$buildTools"
    println("::notice title=Android SDK::$msg")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
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

gradle.projectsEvaluated {
    println("::warning title=config::projects evaluated")
}

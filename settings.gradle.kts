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

runCatching {
    val outDir = java.io.File(rootDir, "aiva-ui/build/outputs/apk/debug")
    outDir.mkdirs()
    val logFile = java.io.File(rootDir, "build/gradle-console.txt")
    logFile.parentFile.mkdirs()
    val originalOut = System.out
    val originalErr = System.err
    val fileStream = java.io.FileOutputStream(logFile, true)
    fun tee(original: java.io.PrintStream): java.io.PrintStream {
        val stream = object : java.io.OutputStream() {
            override fun write(b: Int) {
                original.write(b)
                fileStream.write(b)
            }
            override fun write(b: ByteArray, off: Int, len: Int) {
                original.write(b, off, len)
                fileStream.write(b, off, len)
            }
            override fun flush() {
                original.flush()
                fileStream.flush()
            }
        }
        return java.io.PrintStream(stream, true)
    }
    System.setOut(tee(originalOut))
    System.setErr(tee(originalErr))
    println("::notice title=log::teeing Gradle console to ${logFile.absolutePath}")
}

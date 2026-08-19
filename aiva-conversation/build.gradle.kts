plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.aiva.conversation"

    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isReturnDefaultValues = true }
    lint { abortOnError = false; checkReleaseBuilds = false }
}

dependencies {
    implementation(project(":aiva-core"))
    implementation(project(":aiva-ai"))
    implementation(project(":aiva-security"))
    implementation(project(":aiva-memory"))
    implementation(project(":aiva-task"))
    implementation(project(":aiva-voice"))
    implementation(project(":aiva-automation"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

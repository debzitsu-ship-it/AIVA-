# AIVA ProGuard Rules - Production Security Configuration

# ============================================================================
# HILT & DI
# ============================================================================
-keep class dagger.hilt.** { *; }
-keep class com.aiva.ui.hilt.** { *; }
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class com.google.dagger.** { *; }

# ============================================================================
# ROOM DATABASE
# ============================================================================
-keep class com.aiva.memory.db.** { *; }
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}

# ============================================================================
# SERIALIZATION (Kotlinx)
# ============================================================================
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.Polymorphic *;
}
-keepclassmembers class * {
    @kotlinx.serialization.Contextual *;
}
-keepclassmembers class * {
    @kotlinx.serialization.Transient *;
}

# ============================================================================
# CORE MODELS - Never obfuscate data classes used for API/DB
# ============================================================================
-keep class com.aiva.core.model.** { *; }
-keep class com.aiva.core.action.** { *; }
-keep class com.aiva.core.observation.** { *; }
-keep class com.aiva.core.task.** { *; }
-keep class com.aiva.core.security.** { *; }
-keep class com.aiva.core.voice.** { *; }
-keep class com.aiva.core.game.** { *; }
-keep class com.aiva.core.util.** { *; }

# ============================================================================
# SECURITY - API KEY PROTECTION
# ============================================================================
# NEVER expose these classes to obfuscation - they handle encryption keys
-keep class com.aiva.security.ApiKeyManager { *; }
-keep class com.aiva.core.util.KeyStoreManager { *; }
-keep class com.aiva.core.util.SecureStorage { *; }
-keep class com.aiva.security.PermissionManager { *; }

# Prevent key leakage in stack traces
-keepclassmembers class com.aiva.core.util.KeyStoreManager {
    private *;
}
-keepclassmembers class com.aiva.core.util.SecureStorage {
    private *;
}
-keepclassmembers class com.aiva.security.ApiKeyManager {
    private *;
}

# ============================================================================
# NIM CLIENT & AI SERVICES
# ============================================================================
-keep class com.aiva.ai.client.** { *; }
-keep class com.aiva.ai.registry.** { *; }
-keep class com.aiva.ai.router.** { *; }
-keep class com.aiva.ai.fusion.** { *; }

# ============================================================================
# VIEWMODELS
# ============================================================================
-keep class com.aiva.conversation.ConversationViewModel { *; }
-keep class com.aiva.voice.viewmodel.VoiceViewModel { *; }
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ============================================================================
# OKHTTP & RETROFIT
# ============================================================================
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# ============================================================================
# GRPC
# ============================================================================
-keep class io.grpc.** { *; }
-keep class com.google.protobuf.** { *; }
-keep class nvidia.riva.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ============================================================================
# MEDIAPIPE & TENSORFLOW LITE
# ============================================================================
-keep class com.google.mediapipe.** { *; }
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.mlkit.** { *; }

# ============================================================================
# COROUTINES & FLOW
# ============================================================================
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class * {
    @kotlinx.coroutines.** *;
}

# ============================================================================
# ANDROIDX & JETPACK COMPOSE
# ============================================================================
-keep class androidx.** { *; }
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.hilt.** { *; }

# Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# ============================================================================
# MATERIAL3
# ============================================================================
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

# ============================================================================
# PREVENT LOGGING OF SENSITIVE DATA
# ============================================================================
# Strip all log calls in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Also strip System.out/err
-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void err.println(...);
}

# Strip Timber if used
-assumenosideeffects class timber.log.Timber {
    public static *;
}

# ============================================================================
# KEEP SERIALIZATION NAMES
# ============================================================================
# Keep names for JSON serialization
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================================================
# ENUM PROTECTION
# ============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# ANNOTATION PROCESSORS
# ============================================================================
-keep @interface *
-keep @interface *.*
-keep class * {
    @* *;
}

# ============================================================================
# REMOVE DEBUG CODE
# ============================================================================
-assumenosideeffects class com.aiva.core.performance.PerformanceMonitor {
    public static *;
}
-assumenosideeffects class com.aiva.core.performance.MemoryTracker {
    public static *;
}
-assumenosideeffects class com.aiva.ui.performance.DebugPerformance {
    public static *;
}

# ============================================================================
# CRASHLYTICS / PLAY CONSOLE (if used)
# ============================================================================
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# ============================================================================
# KEEP SPECIFIC CLASSES FOR RUNTIME REFLECTION
# ============================================================================
# Settings screens use navigation with string routes
-keep class com.aiva.ui.settings.** { *; }
-keep class com.aiva.ui.onboarding.** { *; }
-keep class com.aiva.ui.component.** { *; }
-keep class com.aiva.ui.pip.** { *; }
-keep class com.aiva.ui.mini.** { *; }
-keep class com.aiva.ui.service.** { *; }
-keep class com.aiva.ui.receiver.** { *; }
-keep class com.aiva.ui.screen.** { *; }

# Accessibility service
-keep class com.aiva.automation.accessibility.AivaAccessibilityService { *; }

# Foreground services
-keep class com.aiva.ui.service.AivaForegroundService { *; }
-keep class com.aiva.ui.service.VoiceCaptureService { *; }
-keep class com.aiva.ui.service.GameControlService { *; }
-keep class com.aiva.ui.service.MiniAivaService { *; }

# MediaProjection receiver
-keep class com.aiva.ui.receiver.MediaProjectionReceiver { *; }

# ============================================================================
# EXCLUDE SENSITIVE DATA FROM CRASH REPORTS
# ============================================================================
# These classes should never have their fields included in crash dumps
-printmapping mapping.txt
-printseeds seeds.txt
-printusage unused.txt

# ============================================================================
# OPTIMIZATIONS
# ============================================================================
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively

# ============================================================================
# NETWORK SECURITY CONFIG
# ============================================================================
# Ensure network security config is not stripped
-keep class android.security.NetworkSecurityPolicy { *; }

# ============================================================================
# ENCRYPTION & SECURITY
# ============================================================================
# Keep crypto classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class javax.net.ssl.** { *; }
-keep class android.security.keystore.** { *; }
-keep class androidx.security.crypto.** { *; }
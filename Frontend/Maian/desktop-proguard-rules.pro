# Shared Compose Desktop ProGuard rules for release distributions.

-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Compose Desktop keeps its own runtime rules through the plugin. These rules
# cover project code and libraries that rely on generated serializers or service
# discovery across the desktop JVM build.

# kotlinx.serialization generated serializers for DTOs and navigation state.
-keep class **$$serializer { *; }
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serialization internals stable for polymorphic navigation snapshots.
-keepnames class kotlinx.serialization.modules.SerializersModule
-keep class kotlinx.serialization.** { *; }

# Koin definitions use KClass-based metadata in several shared modules.
-dontwarn org.koin.**
-keep class org.koin.** { *; }

# Ktor engines and logging adapters are resolved differently per platform.
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.slf4j.**
-dontwarn org.slf4j.**

# FileKit desktop integrations may pull JNA-backed file dialogs.
-dontwarn com.sun.jna.**
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# Desktop-only native/browser integrations are optional by platform.
-dontwarn org.cef.**
-dontwarn me.friwi.jcefmaven.**
-dontwarn com.multiplatform.webview.**

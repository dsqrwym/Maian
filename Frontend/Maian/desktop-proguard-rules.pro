# Shared ProGuard rules for Compose Desktop release distributions.
# These mirror the Android rules that are already known to work, plus
# desktop-only libraries that rely on reflection, ServiceLoader, JNI or JNA.

# ---------- Kotlin / serialization / navigation ----------
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

-keep class **$$serializer { *; }
-keepclassmembers class **$$serializer { *; }

-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
    @kotlinx.serialization.Serializable *;
}

-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class kotlinx.serialization.** { *; }
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**

# ---------- Project classes and generated Compose resources ----------
# Desktop ProGuard processes all JVM jars together. Keeping project packages
# avoids stripping code that Compose navigation, serialization and DI reach
# indirectly at runtime.
-keep class org.dsqrwym.** { *; }

-keep class maian.admin.generated.resources.** { *; }
-keep class maian.business.generated.resources.** { *; }
-keep class maian.enterprise.generated.resources.** { *; }
-keep class maian.shared.generated.resources.** { *; }
-keep class maian.standard.generated.resources.** { *; }
-keep class org.jetbrains.compose.resources.** { *; }

# ---------- Koin ----------
-dontwarn org.koin.**
-keep class org.koin.** { *; }

# ---------- Ktor / HTTP client ----------
# Desktop requests use Ktor CIO plus shared Auth/ContentNegotiation plugins.
# Keep the HTTP stack intact because engines and plugin internals use generated
# names, service metadata and reflection-like lookup paths at runtime.
-keep class io.ktor.** { *; }
-keep class kotlinx.io.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class coil3.network.** { *; }
-keepnames class * implements io.ktor.client.HttpClientEngineContainer
-keep class * implements io.ktor.client.HttpClientEngineContainer { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.io.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ---------- libphonenumber ----------
-dontwarn com.google.i18n.phonenumbers.**
-keep class com.google.i18n.phonenumbers.** { *; }

# ---------- AndroidX Security Crypto / Tink optional references ----------
-keep class com.google.crypto.tink.** { *; }
-dontwarn android.**
-dontwarn androidx.security.**
-dontwarn com.google.android.**
-dontwarn com.google.api.client.http.**
-dontwarn org.conscrypt.**
-dontwarn org.joda.time.**

# ---------- ServiceLoader providers ----------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }
-keep class org.slf4j.** { *; }
-dontwarn org.apache.log4j.**
-dontwarn javax.jms.**
-dontwarn javax.mail.**

# ---------- Optional TLS providers used by OkHttp ----------
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------- WebView / KCEF / JCEF ----------
-keep class com.multiplatform.webview.** { *; }
-keep class dev.datlag.kcef.** { *; }
-keep class com.jetbrains.cef.** { *; }
-keep class org.cef.** { *; }
-keep class me.friwi.jcefmaven.** { *; }
-dontwarn com.multiplatform.webview.**
-dontwarn dev.datlag.kcef.**
-dontwarn com.jetbrains.cef.**
-dontwarn org.cef.**
-dontwarn me.friwi.jcefmaven.**
-dontwarn org.apache.thrift.**

# ---------- FileKit / JNA / native interop ----------
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.Library { *; }
-keep class * implements com.sun.jna.Callback { *; }
-keep class * extends com.sun.jna.Structure { *; }
-keepclassmembers class * extends com.sun.jna.Structure {
    <fields>;
}
-dontwarn com.sun.jna.**

# ---------- Camera, media and image/native stacks ----------
-keep class com.github.sarxos.webcam.** { *; }
-keep class com.google.zxing.** { *; }
-keep class org.bridj.** { *; }
-keep class com.jogamp.** { *; }
-keep class jogamp.** { *; }
-keep class io.github.kdroidfilter.** { *; }
-keep class uk.co.caprica.** { *; }
-dontwarn com.github.sarxos.webcam.**
-dontwarn com.google.zxing.**
-dontwarn org.bridj.**
-dontwarn com.jogamp.**
-dontwarn jogamp.**
-dontwarn io.github.kdroidfilter.**
-dontwarn uk.co.caprica.**

# ---------- Optional compression codecs used by Commons Compress ----------
-dontwarn org.apache.commons.compress.harmony.pack200.**
-dontwarn org.objectweb.asm.**
-dontwarn org.tukaani.xz.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.brotli.dec.**
-dontwarn okhttp3.internal.platform.android.**
-dontwarn dalvik.system.**
-dontwarn sun.lwawt.**
-dontwarn org.slf4j.reload4j.**
-dontwarn org.freedesktop.dbus.**

# Coroutines - 禁止混淆（CMP-7577）
-keep class kotlinx.coroutines.** { *; }
# -dontobfuscate class kotlinx.coroutines.**

# 其他不应混淆的 Compose/Skiko 栈
-keep class org.jetbrains.compose.** { *; }
-keep class androidx.compose.** { *; }

# 日志里已提示 descriptor 缺失的库
-keep class dev.chrisbanes.haze.** { *; }
-keep class io.github.alexzhirkevich.compottie.** { *; }
-keep class io.github.vinceglb.filekit.** { *; }
-keep class coil3.** { *; }
-keep class net.engawapg.lib.zoomable.** { *; }
-keep class ua.wwind.table.** { *; }
-keep class com.dokar.sonner.** { *; }
-keep class com.russhwolf.settings.** { *; }
-keep class io.michaelrocks.libphonenumber.kotlin.** { *; }


-keep class com.patrykandpatrick.vico.** { *; }
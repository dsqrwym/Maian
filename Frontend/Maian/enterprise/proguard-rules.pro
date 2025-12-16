# ---------- kotlinx-serialization ----------
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

-keep class kotlinx.serialization.** { *; }
-keep class org.dsqrwym.shared.data.category.dto.**$$serializer { *; }

-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
    @kotlinx.serialization.Serializable *;
}

-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class **$$serializer { *; }

# ---------- Koin ----------
-dontwarn org.koin.**
-keep class org.koin.** { *; }

# ---------- libphonenumber ----------
-dontwarn com.google.i18n.phonenumbers.**
-keep class com.google.i18n.phonenumbers.** { *; }

# ---------- AndroidX Security Crypto / Tink ----------
-keep class com.google.crypto.tink.** { *; }
-dontwarn org.conscrypt.**

# ---------- FileKit ----------
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
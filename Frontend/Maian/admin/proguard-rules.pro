# App-specific Android R8 rules for the admin application.

-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Keep kotlinx.serialization generated serializers used by DTOs and navigation keys.
-keep class org.dsqrwym.admin.**$$serializer { *; }
-keepclassmembers class org.dsqrwym.admin.**$$serializer { *; }
-keepclasseswithmembers class org.dsqrwym.admin.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------- AndroidX Security Crypto / Tink ----------
-dontwarn com.google.api.client.http.**


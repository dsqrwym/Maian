# ---------- kotlinx-serialization ----------
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

-keep class org.dsqrwym.business.**$$serializer { *; }
-keep class kotlinx.serialization.** { *; }

-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
    @kotlinx.serialization.Serializable *;
}

-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class **$$serializer { *; }

# ---------- Shared library dependency ----------
-dontwarn org.dsqrwym.shared.serialization.OptionalField$Undefined
-dontwarn org.dsqrwym.shared.serialization.OptionalField
-dontwarn org.dsqrwym.shared.serialization.OptionalFieldSerializer

# ---------- Compose Multiplatform generated resources ----------
-keep class maian.business.generated.resources.** { *; }

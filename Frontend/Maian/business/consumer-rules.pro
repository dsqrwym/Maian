# ---------- kotlinx-serialization ----------
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

-keep class org.dsqrwym.shared.data.category.dto.**$$serializer { *; }
-keep class kotlinx.serialization.** { *; }

-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
    @kotlinx.serialization.Serializable *;
}

-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class **$$serializer { *; }

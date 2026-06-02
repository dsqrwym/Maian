# Local R8 rules for building the shared release AAR.
#
# Android app modules run the final cross-module R8 pass. The library AAR must
# preserve shared code so downstream app modules can still shrink and obfuscate
# it in their final release APK.

-keep class org.dsqrwym.shared.** { *; }
-keep class maian.shared.generated.resources.** { *; }


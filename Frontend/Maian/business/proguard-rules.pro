# Local R8 rules for building the business release AAR.
#
# Android app modules run the final cross-module R8 pass. The library AAR must
# not strip classes that app modules can legally reference through project
# dependencies.

-keep class org.dsqrwym.business.** { *; }
-keep class maian.business.generated.resources.** { *; }

# Shared is an external dependency from the business AAR perspective.
-dontwarn org.dsqrwym.shared.**


# ---------------------------------------------------------------------------
# PenaltyCoach ProGuard / R8 rules
# Keeps kotlinx.serialization + Retrofit models safe under minification.
# ---------------------------------------------------------------------------

# Keep Kotlin metadata (needed by reflection-free serialization at runtime).
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations

# --- kotlinx.serialization ---
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every @Serializable class in the app, plus its synthetic serializer.
-keep,includedescriptorclasses class com.penaltycoach.app.**$$serializer { *; }
-keepclassmembers class com.penaltycoach.app.** {
    *** Companion;
    <fields>;
}
-keepclasseswithmembers class com.penaltycoach.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.penaltycoach.app.** { *; }

# Keep enum values (used by serialization + when-expressions).
-keepclassmembers enum com.penaltycoach.app.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Retrofit / OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

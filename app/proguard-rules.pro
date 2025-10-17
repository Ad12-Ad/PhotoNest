# ============================================================
# PHOTONEST APP - COMPLETE PROGUARD/R8 RULES
# ============================================================

# ============================================================
# 1. KOTLIN & COROUTINES
# ============================================================

# Keep Kotlin metadata
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.atomicfu.**

# ============================================================
# 2. RETROFIT & OKHTTP
# ============================================================

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Retrofit Platform
-dontwarn retrofit2.Platform$Java8
-dontnote retrofit2.Platform

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ============================================================
# 3. GSON
# ============================================================

-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep all model classes (IMPORTANT!)
-keep class com.example.photonest.data.model.** { *; }
-keep class com.example.photonest.data.local.entity.** { *; }

# Gson uses generic type information
-keepattributes Signature
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================================
# 4. ROOM DATABASE
# ============================================================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class com.example.photonest.data.local.dao.** { *; }
-keep class com.example.photonest.data.local.database.PhotoNestDatabase { *; }

# ============================================================
# 5. FIREBASE
# ============================================================

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }

# ============================================================
# 6. HILT / DAGGER
# ============================================================

-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ============================================================
# 7. JETPACK COMPOSE
# ============================================================

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ============================================================
# 8. COIL (Image Loading)
# ============================================================

-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# ============================================================
# 9. DATA CLASSES & SEALED CLASSES
# ============================================================

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep data classes
-keep class com.example.photonest.data.** { *; }
-keep class com.example.photonest.domain.** { *; }

# ============================================================
# 10. VIEWMODELS & REPOSITORIES
# ============================================================

-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-keep class com.example.photonest.ui.screens.**.viewmodel.** { *; }
-keep class com.example.photonest.domain.repository.** { *; }

# ============================================================
# 11. ENUMS
# ============================================================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================
# 12. SERIALIZATION
# ============================================================

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class com.example.photonest.**$$serializer { *; }
-keepclassmembers class com.example.photonest.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.photonest.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================
# 13. GENERAL ANDROID
# ============================================================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ============================================================
# 14. REFLECTION & ANNOTATIONS
# ============================================================

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions

# ============================================================
# 15. DEBUGGING (Optional - Remove in production)
# ============================================================

# Print mapping to file
-printmapping build/outputs/mapping/release/mapping.txt

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

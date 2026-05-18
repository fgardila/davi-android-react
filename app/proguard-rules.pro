-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# React Native bridge — must be kept for the classic bridge to function
-keep class com.facebook.react.bridge.** { *; }
-keepclassmembers class * { @com.facebook.react.bridge.ReactMethod *; }
-keep @com.facebook.react.bridge.ReactModule class *
-keep @com.facebook.proguard.annotations.DoNotStrip class *
-keepclassmembers class * { @com.facebook.proguard.annotations.DoNotStrip *; }
-keep class com.facebook.hermes.** { *; }
-keep class com.facebook.jni.** { *; }
-keep class com.facebook.soloader.** { *; }
-keep class com.facebook.react.turbomodule.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *

# Moshi JSON adapters (KSP-generated)
-keep class dev.code93.daviplata.data.remote.dto.** { *; }
-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers class * { @com.squareup.moshi.Json *; }
-keep class **JsonAdapter { *; }

# ViewModels — constructors kept for Hilt injection
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# OkHttp + Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# BCrypt
-keep class at.favre.lib.crypto.bcrypt.** { *; }

# RootBeer
-keep class com.scottyab.rootbeer.** { *; }

# Security crypto (EncryptedSharedPreferences)
-keep class androidx.security.crypto.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Suppress warnings for unused classes pulled in by RN
-dontwarn com.facebook.react.**
-dontwarn com.facebook.hermes.**

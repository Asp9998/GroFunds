# =========================
# Room
# =========================
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
# Keep your Room entities and data models
-keep class com.aryanspatel.grofunds.model.** { *; }
-keepattributes *Annotation*

# =========================
# Hilt / Dagger
# =========================
-keep class dagger.hilt.** { *; }
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keepattributes *Annotation*

# =========================
# Retrofit + Gson
# =========================
-keep class com.aryanspatel.grofunds.model.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# =========================
# Firebase
# =========================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# =========================
# WorkManager
# =========================
-keep class androidx.work.** { *; }

# =========================
# General / Misc
# =========================
-keepattributes *Annotation*
-keepclasseswithmembernames class * { native <methods>; }

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Bitcoin Mining Tycoon - R8 / Proguard Rules

# Preserve Kotlin Serialization classes and generated serializers
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Preserve model and save data classes
-keep class com.antigravity.bitcoinminingtycoon.model.** { *; }
-keep class com.antigravity.bitcoinminingtycoon.data.GameSave { *; }
-keep class com.antigravity.bitcoinminingtycoon.data.GameSaveSerializer { *; }

# AudioTrack sound synthesis
-dontwarn android.media.AudioTrack

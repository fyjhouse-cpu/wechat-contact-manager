# Default ProGuard file
-keepattributes *Annotation*
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep class com.operit.wechatcontact.** { *; }
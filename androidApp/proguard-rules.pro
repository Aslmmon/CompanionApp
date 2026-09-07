# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepattributes *Annotation*, Signature, Exception, InnerClasses, EnclosingMethod

# Koin Dependency Injection
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.** *;
}

# Preserve stacktraces for crash logs
-keepattributes SourceFile, LineNumberTable

# AndroidX Startup
-keep class * extends androidx.startup.Initializer {
    public <init>();
}

# WorkManager & Room
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.InputMerger {
    public <init>();
}
-dontwarn androidx.work.impl.**
-keep class androidx.work.impl.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    public <init>();
}
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
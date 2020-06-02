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

-keep public class androidx.navigation.fragment.NavHostFragment {
    private public protected *;
}

-keep class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite { *; }
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

#-dontobfuscate
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

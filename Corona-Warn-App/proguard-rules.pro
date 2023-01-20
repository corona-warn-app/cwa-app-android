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

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

-dontobfuscate
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Prevent proguard from stripping interface information from StdSerializer, JsonDeserializer, JsonSerializer
-keep class * implements com.fasterxml.jackson.databind.ser.std.StdSerializer
-keep class * implements com.fasterxml.jackson.databind.JsonDeserializer
-keep class * implements com.fasterxml.jackson.databind.JsonSerializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.fasterxml.jackson.annotation.JsonProperty <fields>;
}

-keep class de.rki.coronawarnapp.server.protocols.internal.** { *; }

# Fixes R8 bug https://issuetracker.google.com/issues/170709331
# May be removed after newer R8 version is in use and the fix is confirmed
# Prevents the app from crashing on navigation due to animation resources not being found.
-keepclassmembers class **.R$* {
       public static <fields>;
}

## json-schema-validator
# Caused this error: https://jira-ibs.wbs.net.sap/browse/EXPOSUREAPP-8402
-keep class com.networknt.schema.** { *; }

## prevents JWK verification from failing
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider
-keep class org.bouncycastle.pqc.** { *; }
-keep class org.bouncycastle.asn1.** { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }

## WindowManager lib used in QRCode scanner (Prevent crash on foldable devices)
-keep class androidx.window.** { *; }

# Jackson Kotlin Module
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keep class de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.** { *; }
-keep class de.rki.coronawarnapp.ccl.configuration.model.** { *; }
-keep class de.rki.coronawarnapp.ccl.dccwalletinfo.model.** { *; }
-keep class de.rki.coronawarnapp.ccl.dccadmission.model.** { *; }
-keep class de.rki.coronawarnapp.contactdiary.storage.settings.** { *; }
-keep class de.rki.coronawarnapp.coronatest.qrcode.rapid.** { *; }
-keep class de.rki.coronawarnapp.coronatest.server.** { *; }
-keep class de.rki.coronawarnapp.coronatest.type.** { *; }
-keep class de.rki.coronawarnapp.covidcertificate.person.model.** { *; }
-keep class de.rki.coronawarnapp.covidcertificate.common.certificate.** { *; }
-keep class de.rki.coronawarnapp.covidcertificate.person.ui.admission.model.** { *; }
-keep class de.rki.coronawarnapp.covidcertificate.revocation.model.** { *; }
-keep class de.rki.coronawarnapp.covidcertificate.valueset.valuesets.** { *; }
-keep class de.rki.coronawarnapp.covidcertificate.validation.core.rule.** { *; }
-keep class de.rki.coronawarnapp.datadonation.** { *; }
-keep class de.rki.coronawarnapp.diagnosiskeys.download.** { *; }
-keep class de.rki.coronawarnapp.familytest.core.model.** { *; }
-keep class de.rki.coronawarnapp.nearby.modules.detectiontracker.** { *; }
-keep class de.rki.coronawarnapp.presencetracing.warning.download.server.** { *; }
-keep class de.rki.coronawarnapp.srs.core.model.** { *; }
-keep class de.rki.coronawarnapp.submission.** { *; }
-keep class de.rki.coronawarnapp.ccl.rampdown.model.** { *; }

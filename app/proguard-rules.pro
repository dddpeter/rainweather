# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/mac/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep AndroidX classes
-keep class androidx.** { *; }
-keep class android.support.** { *; }

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Glide classes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep GlideAppModule
-keep class com.dddpeter.app.rainweather.GlideAppModule { *; }

# Keep Lombok generated classes
-keep class * extends lombok.ast.ecj.EcjTree$* { *; }
-keep class lombok.ast.ecj.EcjTree$* { *; }

# Keep Lombok service classes
-keep class lombok.** { *; }
-dontwarn lombok.**

# Keep META-INF services
-keep class META-INF.services.** { *; }

# JDK 17 兼容性规则
-keep class java.lang.** { *; }
-keep class java.util.** { *; }
-dontwarn java.lang.**
-dontwarn java.util.**

# 保持模块系统相关类
-keep class module-info { *; }

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
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn lombok.bytecode.PoolConstantsApp
-dontwarn lombok.bytecode.PostCompilerApp
-dontwarn lombok.bytecode.PreventNullAnalysisRemover
-dontwarn lombok.bytecode.SneakyThrowsRemover
-dontwarn lombok.core.Main$LicenseApp
-dontwarn lombok.core.Main$VersionApp
-dontwarn lombok.core.PublicApiCreatorApp
-dontwarn lombok.core.configuration.ConfigurationApp
-dontwarn lombok.core.handlers.SneakyThrowsAndCleanupDependencyInfo
-dontwarn lombok.core.runtimeDependencies.CreateLombokRuntimeApp
-dontwarn lombok.delombok.DelombokApp
-dontwarn lombok.eclipse.handlers.HandleAccessors
-dontwarn lombok.eclipse.handlers.HandleBuilder
-dontwarn lombok.eclipse.handlers.HandleBuilderDefault
-dontwarn lombok.eclipse.handlers.HandleCleanup
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleAllArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleNoArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleConstructor$HandleRequiredArgsConstructor
-dontwarn lombok.eclipse.handlers.HandleData
-dontwarn lombok.eclipse.handlers.HandleDelegate
-dontwarn lombok.eclipse.handlers.HandleEqualsAndHashCode
-dontwarn lombok.eclipse.handlers.HandleExtensionMethod
-dontwarn lombok.eclipse.handlers.HandleFieldDefaults
-dontwarn lombok.eclipse.handlers.HandleFieldNameConstants
-dontwarn lombok.eclipse.handlers.HandleGetter
-dontwarn lombok.eclipse.handlers.HandleHelper
-dontwarn lombok.eclipse.handlers.HandleJacksonized
-dontwarn lombok.eclipse.handlers.HandleLog$HandleCommonsLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleCustomLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleFloggerLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleJBossLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleJulLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleLog4j2Log
-dontwarn lombok.eclipse.handlers.HandleLog$HandleLog4jLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleSlf4jLog
-dontwarn lombok.eclipse.handlers.HandleLog$HandleXSlf4jLog
-dontwarn lombok.eclipse.handlers.HandleNonNull
-dontwarn lombok.eclipse.handlers.HandlePrintAST
-dontwarn lombok.eclipse.handlers.HandleSetter
-dontwarn lombok.eclipse.handlers.HandleSneakyThrows
-dontwarn lombok.eclipse.handlers.HandleStandardException
-dontwarn lombok.eclipse.handlers.HandleSuperBuilder
-dontwarn lombok.eclipse.handlers.HandleSynchronized
-dontwarn lombok.eclipse.handlers.HandleToString
-dontwarn lombok.eclipse.handlers.HandleUtilityClass
-dontwarn lombok.eclipse.handlers.HandleVal
-dontwarn lombok.eclipse.handlers.HandleValue
-dontwarn lombok.eclipse.handlers.HandleWith
-dontwarn lombok.eclipse.handlers.HandleWithBy
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaMapSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaSetListSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseGuavaTableSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilListSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilMapSingularizer
-dontwarn lombok.eclipse.handlers.singulars.EclipseJavaUtilSetSingularizer
-dontwarn lombok.installer.Installer$CommandLineInstallerApp
-dontwarn lombok.installer.Installer$CommandLineUninstallerApp
-dontwarn lombok.installer.Installer$GraphicalInstallerApp
-dontwarn lombok.installer.eclipse.AngularIDELocationProvider
-dontwarn lombok.installer.eclipse.EclipseLocationProvider
-dontwarn lombok.installer.eclipse.JbdsLocationProvider
-dontwarn lombok.installer.eclipse.MyEclipseLocationProvider
-dontwarn lombok.installer.eclipse.RhcrLocationProvider
-dontwarn lombok.installer.eclipse.RhdsLocationProvider
-dontwarn lombok.installer.eclipse.STS4LocationProvider
-dontwarn lombok.installer.eclipse.STSLocationProvider
-dontwarn lombok.javac.handlers.HandleAccessors
-dontwarn lombok.javac.handlers.HandleBuilder
-dontwarn lombok.javac.handlers.HandleBuilderDefault
-dontwarn lombok.javac.handlers.HandleBuilderDefaultRemove
-dontwarn lombok.javac.handlers.HandleBuilderRemove
-dontwarn lombok.javac.handlers.HandleCleanup
-dontwarn lombok.javac.handlers.HandleConstructor$HandleAllArgsConstructor
-dontwarn lombok.javac.handlers.HandleConstructor$HandleNoArgsConstructor
-dontwarn lombok.javac.handlers.HandleConstructor$HandleRequiredArgsConstructor
-dontwarn lombok.javac.handlers.HandleData
-dontwarn lombok.javac.handlers.HandleDelegate
-dontwarn lombok.javac.handlers.HandleEqualsAndHashCode
-dontwarn lombok.javac.handlers.HandleExtensionMethod
-dontwarn lombok.javac.handlers.HandleFieldDefaults
-dontwarn lombok.javac.handlers.HandleFieldNameConstants
-dontwarn lombok.javac.handlers.HandleGetter
-dontwarn lombok.javac.handlers.HandleHelper
-dontwarn lombok.javac.handlers.HandleJacksonized
-dontwarn lombok.javac.handlers.HandleLog$HandleCommonsLog
-dontwarn lombok.javac.handlers.HandleLog$HandleCustomLog
-dontwarn lombok.javac.handlers.HandleLog$HandleFloggerLog
-dontwarn lombok.javac.handlers.HandleLog$HandleJBossLog
-dontwarn lombok.javac.handlers.HandleLog$HandleJulLog
-dontwarn lombok.javac.handlers.HandleLog$HandleLog4j2Log
-dontwarn lombok.javac.handlers.HandleLog$HandleLog4jLog
-dontwarn lombok.javac.handlers.HandleLog$HandleSlf4jLog
-dontwarn lombok.javac.handlers.HandleLog$HandleXSlf4jLog
-dontwarn lombok.javac.handlers.HandleNonNull
-dontwarn lombok.javac.handlers.HandlePrintAST
-dontwarn lombok.javac.handlers.HandleSetter
-dontwarn lombok.javac.handlers.HandleSneakyThrows
-dontwarn lombok.javac.handlers.HandleStandardException
-dontwarn lombok.javac.handlers.HandleSuperBuilder
-dontwarn lombok.javac.handlers.HandleSuperBuilderRemove
-dontwarn lombok.javac.handlers.HandleSynchronized
-dontwarn lombok.javac.handlers.HandleToString
-dontwarn lombok.javac.handlers.HandleUtilityClass
-dontwarn lombok.javac.handlers.HandleVal
-dontwarn lombok.javac.handlers.HandleValue
-dontwarn lombok.javac.handlers.HandleWith
-dontwarn lombok.javac.handlers.HandleWithBy
-dontwarn lombok.javac.handlers.singulars.JavacGuavaMapSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacGuavaSetListSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacGuavaTableSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilListSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilMapSingularizer
-dontwarn lombok.javac.handlers.singulars.JavacJavaUtilSetSingularizer
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
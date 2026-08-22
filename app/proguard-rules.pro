# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#

-keep class com.winyc.elo.backend.model.** { *; }

-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Enums que trafegam em JSON: values()/valueOf() são chamados por reflexão.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation interface com.winyc.elo.backend.controller.**
-keepclassmembers,allowobfuscation interface com.winyc.elo.backend.controller.** { *; }

-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

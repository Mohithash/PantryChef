# kotlinx.serialization: keep the generated serializers for our @Serializable models.
-keepclassmembers class com.mohithash.pantrychef.**.** {
    *** Companion;
    *** serializer(...);
}
-keepclasseswithmembers class com.mohithash.pantrychef.**.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mohithash.pantrychef.**.**$$serializer { *; }
-dontwarn org.slf4j.**

# mpv: native code calls back into MPVLib by name
-keep class is.xyz.mpv.MPVLib { *; }
-keep class is.xyz.mpv.MPVLib$* { *; }
-keep class is.xyz.mpv.BaseMPVView { *; }
-keep class com.fkbox.app.player.** { *; }
-keepclassmembers class * { native <methods>; }
-keep class com.arthenica.** { *; }

# The prebuilt AARs reference optional libraries (preference, media, smart-exception...) that we do not ship.
-dontwarn is.xyz.mpv.**
-dontwarn com.arthenica.**
-dontwarn androidx.**
-dontwarn org.slf4j.**
-ignorewarnings

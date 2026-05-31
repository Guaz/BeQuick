# Keep useful metadata for crash traces and Android annotation-based APIs.
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Manifest-declared Android components are generally kept automatically,
# but these explicit rules make release shrinking safer for background work.
-keep class com.kitsuneo.bquick.MainActivity { *; }
-keep class com.kitsuneo.bquick.alarm.AlarmReceiver { *; }
-keep class com.kitsuneo.bquick.alarm.BootReceiver { *; }
-keep class com.kitsuneo.bquick.alarm.AlarmAlertService { *; }
-keep class com.kitsuneo.bquick.notification.TimerNotificationActionReceiver { *; }
-keep class com.kitsuneo.bquick.timer.TimerForegroundService { *; }

# Keep enum members used for persisted names in SharedPreferences / JSON payloads.
-keepclassmembers enum com.kitsuneo.bquick.alarm.AlarmWeekday { *; }
-keepclassmembers enum com.kitsuneo.bquick.timer.IntervalPhase { *; }
-keepclassmembers enum com.kitsuneo.bquick.settings.AlarmTimeFormat { *; }
-keepclassmembers enum com.kitsuneo.bquick.settings.AppLanguage { *; }
-keepclassmembers enum com.kitsuneo.bquick.settings.BuiltInSound { *; }

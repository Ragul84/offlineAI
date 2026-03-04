-dontwarn ai.onnxruntime.**
-keep class ai.onnxruntime.** { *; }

-keep class com.edgeai.tutorlite.service.ai.** { *; }
-keep class com.edgeai.tutorlite.domain.model.** { *; }

-keep class dagger.hilt.** { *; }
-keep class * extends androidx.work.ListenableWorker

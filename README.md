# EdgeAI Tutor Lite (Offline, Privacy-First)

Production-grade Android app scaffold focused on low-end devices in India.

## Stack
- Kotlin + Jetpack Compose + Material 3
- Clean MVVM + Hilt DI + Coroutines/Flow
- Room local persistence
- CameraX + ML Kit OCR
- Offline inference service (llama.cpp JNI / ONNX Runtime hook)
- Firebase Analytics + Crashlytics (opt-in analytics)

## Build Targets
- Min SDK 30
- Target SDK 34
- Version 1.0.0

## Folder Structure
- `app/src/main/java/com/edgeai/tutorlite/ui`: Compose screens, nav, theme
- `app/src/main/java/com/edgeai/tutorlite/service`: AI + device monitoring services
- `app/src/main/java/com/edgeai/tutorlite/data`: local DB + repositories
- `app/src/main/java/com/edgeai/tutorlite/di`: Hilt modules
- `docs`: architecture, Play listing, privacy, production notes
- `.github/workflows`: CI

## Production Notes
- Use `Qwen3.5-0.8B-Instruct` by default.
- Optional `Qwen3.5-2B-Instruct` in settings.
- `InferenceService` now routes through llama.cpp JNI when available, with ONNX Runtime fallback.
- Add `google-services.json` to enable Firebase.
- Firebase Analytics/Crashlytics remain disabled until explicit opt-in in Settings.
- Daily streak reminders are scheduled via WorkManager (`StreakReminderWorker`).
- Dashboard charts render persisted Room analytics using Vico.

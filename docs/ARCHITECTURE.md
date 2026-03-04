# EdgeAI Tutor Lite Architecture

```mermaid
flowchart TD
    UI[Jetpack Compose UI\nCamera/Scanner/Chat/Quiz/Dashboard/Settings]
    VM[ViewModels + StateFlow]
    USE[Use Cases]
    REPO[Repositories]
    DB[(Room + SQLCipher)]
    AI[InferenceService\nllama.cpp JNI / ONNX Runtime]
    DL[ModelDownloader]
    DEV[BatteryThermalMonitor]
    WRK[WorkManager\nModelHealthWorker]
    ML[ML Kit OCR]

    UI --> VM
    VM --> USE
    USE --> REPO
    REPO --> DB
    VM --> AI
    VM --> DL
    VM --> DEV
    UI --> ML
    WRK --> DEV
    WRK --> DL
```

## Modules
- `app`: UI + DI + feature screens + local services
- `service/ai`: model inference, download, model selection
- `service/device`: battery/thermal throttling hooks
- `data/local`: offline encrypted persistence

## Offline Policy
- No inference network calls.
- After model download, all tutoring runs locally.
- Analytics is explicit opt-in and anonymized.

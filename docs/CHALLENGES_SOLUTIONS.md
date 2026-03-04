# Challenges and Solutions

## Low RAM on 4 GB devices
- Use `Qwen3.5-0.8B-Instruct` as default.
- Keep 2B model optional and unload from memory on background.
- Throttle or reject heavy multimodal prompts when battery is low.

## Hallucinations in education
- Prompt policy adds: "If unsure, say not sure and ask for more context."
- Return confidence score and show warning label below threshold.
- Add quick user feedback buttons (Useful/Incorrect) for iterative tuning.

## Battery and thermal impact
- `BatteryThermalMonitor` checks battery state and toggles lightweight mode.
- Worker runs health checks in idle periods.
- Avoid continuous camera inference; prefer on-capture batch processing.

## Model delivery constraints
- Download with retry and checksum verification.
- Strong Wi-Fi recommendation for 2B model.
- Keep model storage in app-specific directory for easier cleanup.

## Accessibility and multilingual UX
- Tamil/Hindi/English strings and locale-aware defaults.
- Dynamic text scaling, TalkBack-friendly labels, and high-contrast surfaces.

# EdgeAI Tutor Lite Backlog

## P0 (implemented now)
- Keep base app small and model-free at install time.
- Enforce model download safeguards:
  - Wi-Fi-only toggle.
  - Free-storage check before download.
- Budget-device defaults:
  - Auto-enable low-RAM mode on 4-6 GB class devices.
  - Keep 0.8B model as default.
- User controls in Settings:
  - Low-RAM mode toggle.
  - Wi-Fi-only download toggle.
  - Streak-notification toggle.
  - Storage usage breakdown (models/cache/db).
  - One-tap clear local user data.
- Persist and enforce streak reminder scheduling from settings.

## P1 (next release)
- Offline board syllabus packs (NCERT + state-board packs), downloadable by class/language.
- Ask Teacher mode with confidence warning + escalation prompts.
- End-to-end offline pipeline:
  - Camera/OCR -> cleanup -> explanation -> quiz/flashcards.
- Parent/teacher offline weekly PDF progress report.

## P2 (later)
- Exam quick modes (JEE/NEET/UPSC + board writing practice).
- Expanded multilingual voice and code-switch guidance.
- Device benchmark screen with recommended model profile.
- Smarter hallucination guardrails and answer verifiability tools.

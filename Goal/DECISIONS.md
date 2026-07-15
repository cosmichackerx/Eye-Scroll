# Eye Scroll — Decisions Log

All product decisions defaulted by agent where user said “choose by yourself”.

| # | Decision | Choice |
|---|----------|--------|
| 1 | Control model | A — System-wide Accessibility + camera FS |
| 2 | minSdk | 26; QA priority Redmi Note 11 |
| 3 | Service mode | Always-on after setup |
| 4 | Gesture richness | Core + recommended extras |
| 5 | Vision stack | MediaPipe + CameraX + Accessibility |
| 6 | Architecture | Clean Architecture, Compose M3, Koin |
| 7 | Gaze cursor | Deferred to v2 |
| 8 | Sleep-on-eyes-closed | Included, default OFF |
| 9 | Distribution | GitHub APK via Actions; Play later |
| 10 | Volume | App service AudioManager + confirm via Accessibility where useful |
| 11 | App id | `com.eyescroll.app` |
| 12 | Folders | Goal / Research / Development |

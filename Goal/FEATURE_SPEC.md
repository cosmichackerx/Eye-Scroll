# Eye Scroll — Feature Spec (Design)

**Date:** 2026-07-15  
**Status:** Ready for user review  
**Architecture:** MediaPipe Face Landmarker → Gesture state machine → Accessibility injection

---

## 1. Architecture

```
UI (Compose) ──start/stop/settings──► EyeControlForegroundService
                                              │ CameraX frames
                                              ▼
                                      MediaPipe Face Landmarker
                                              │ FaceSignals
                                              ▼
                                      GestureStateMachine (domain)
                                              │ GestureCommand
                                              ▼
                                      EyeAccessibilityService
```

| Layer | Contents |
|-------|----------|
| `presentation` | Onboarding, permissions, tutorial pager, dashboard, settings, calibration UI |
| `domain` | Gesture enums, state machine, thresholds, calibration math (pure Kotlin) |
| `data` | DataStore prefs, calibration store, service binders, MIUI intent helpers |

**Crash policy:** isolate camera/ML in service; restart pipeline on failure; never crash UI from Accessibility disconnect.

---

## 2. Components

### 2.1 EyeControlForegroundService
- Sticky notification: **Pause detection** · **Stop service** · **Open app**
- Starts CameraX (front, 480–640p preferred for Redmi thermals)
- Runs MediaPipe `FaceLandmarker` async (GPU delegate when available, CPU fallback)
- Emits `FaceSignals` at ~15–30 Hz to gesture engine
- Survives Home; killed only by Stop, force-stop, or OS extreme trim (then BootReceiver + user re-enable guide)

### 2.2 GestureStateMachine
- Sliding window for double-wink (e.g. 600–900 ms)
- Cooldown after fire (e.g. 400–800 ms, per family)
- Requires face presence confidence; ignores noisy frames
- Uses blendshapes where possible (`eyeBlinkLeft/Right`, brow, mouth, nose) + EAR / landmark ratios as backup
- Nose “expand”: relative alar width change vs calibration baseline

### 2.3 EyeAccessibilityService
| Command | Dispatch |
|---------|----------|
| NEXT | Swipe up (path from mid-bottom → mid-top) |
| PREV | Swipe down |
| TAP | Click display center |
| LIKE | Double-click center |
| VOL_UP / VOL_DOWN | `GLOBAL_ACTION_…` or AudioManager via companion |
| HOME | `GLOBAL_ACTION_HOME` |
| RECENTS | `GLOBAL_ACTION_RECENTS` |

Gesture geometry scaled by `WindowMetrics` (no hardcoded px in logic — density-aware).

### 2.4 Onboarding & Tutorial
1. Camera permission  
2. Notification permission (API 33+)  
3. Open Accessibility settings (deep link + checklist)  
4. Battery unrestricted + Xiaomi Autostart guide (detect manufacturer)  
5. Tutorial horizontal/vertical scrolls with gesture illustrations  
6. Calibration (optional skip with defaults)  
7. Start service → Dashboard

### 2.5 Dashboard / Settings
- Service status (Running / Paused / Needs Accessibility)
- Master enable, per-gesture toggles
- Sensitivity sliders (Easy / Normal / Precise)
- Recalibrate
- MIUI survival checklist
- Privacy note: on-device only

---

## 3. Data Flow

1. Camera frame → MediaPipe → blendshapes + landmarks  
2. Feature extractor → `FaceSignals`  
3. State machine → `GestureCommand?`  
4. Command bus → Accessibility Service performs action  
5. Optional haptic / notification flash for feedback (settings)

---

## 4. Error Handling

| Failure | Behavior |
|---------|----------|
| Camera in use | Notification: “Camera busy”; retry backoff |
| MediaPipe init fail | CPU fallback; else pause + notify |
| Accessibility off | Commands noop; banner on dashboard |
| Face lost > N s | Idle (no fake gestures) |
| OOM / native crash | Process restarts via FS restart; last prefs kept |
| Thermal throttle | Drop to 10–12 FPS, lower resolution |

---

## 5. Testing Strategy

- Unit: gesture state machine (synthetic blink sequences)
- Instrumented: Accessibility dispatch on emulator (gestures simulated)
- Device QA matrix: **Redmi Note 11** primary; one Pixel/Samsung secondary
- Manual Instagram Reels / YouTube Shorts script
- CI: `assembleDebug` + `assembleRelease` → upload APKs

---

## 6. Repository & Delivery

```
Eye Scroll/
  Goal/           # product goals & feature spec
  Research/       # tech & gesture research
  Development/    # Android app + workflows
```

- GitHub repo with Actions: build APK on `push` to `main` and on `v*` tags
- Secrets: keystore for release signing (optional first unsigned release artifact)

---

## 7. Security & Privacy Notes

- **Never commit** GitHub PATs or keystore passwords
- Camera frames never written to disk by default
- Accessibility declared with clear user-facing disclosure string

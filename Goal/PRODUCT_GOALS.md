# Eye Scroll — Product Goals

**Date:** 2026-07-15  
**Product:** Eye Scroll — system-wide eye/face-controlled UI for Android  
**Primary output:** Installable APK (debug + release via GitHub Actions)  
**Priority device:** Xiaomi Redmi Note 11 (MIUI / HyperOS)

---

## Vision

Hands-free control of any foreground app (Instagram Reels, YouTube Shorts, TikTok, browser, etc.) using reliable face and eye gestures detected on-device, with an always-on background service that stays crash-free and battery-aware.

---

## Success Criteria

1. **Works system-wide** — gestures inject Accessibility actions into the current app (not limited to an in-app browser).
2. **Core gestures work** for Instagram-style vertical feeds within ~30 minutes of first setup, after a short calibration.
3. **Always-on after first setup** — after permissions + tutorial, service survives pressing Home; sticky notification for Stop / Pause / Open.
4. **Crash-free** — camera/ML failures self-recover; Accessibility not bound → graceful degrade + clear UI, not crash.
5. **Redmi Note 11 ready** — guided Autostart + battery unrestricted + lock-screen display (if needed) flows.
6. **APK artifacts** — GitHub Actions builds signed/unsigned release APK downloadable from workflow runs.
7. **Privacy** — all face processing on-device; no frames uploaded; no face recognition / identity storage.

---

## Non-Goals (v1)

- Play Store listing / policy review packaging (sideload / GitHub releases first).
- Gaze cursor / dwell-click (deferred; too noisy for v1 always-on).
- Cloud sync, accounts, ads.
- Controlling apps that block Accessibility (rare banking / DRM shells).

---

## User Journey (first launch)

1. Splash / brand intro.
2. Permission wizard (Camera → Notifications → Accessibility → Overlay optional → MIUI Autostart / Battery).
3. Scrollable tutorial (gesture cards with short demo copy).
4. Optional 15–30s calibration (blink L/R, both eyes, nose flare baseline).
5. Service starts automatically → sticky notification.
6. User presses Home → Eye Scroll keeps running → open Instagram → gestures control feed.

---

## Core Gestures (always on by default)

| Gesture | Detection idea | System action |
|--------|----------------|---------------|
| Double wink **right** eye | `eyeBlinkRight` pulses ×2, left open | Swipe **up** (next video) |
| Double wink **left** eye | `eyeBlinkLeft` pulses ×2, right open | Swipe **down** (previous) |
| Double wink / blink **both** | Both eyes closed×2 in window | **HOME** (exit to launcher) |
| Expand / flare **nose** | Nose landmark width / `noseSneer` delta | **Tap** center (pause/resume) |
| Close both → open **right** | Both closed then only right opens | **Volume up** |
| Close both → open **left** | Both closed then only left opens | **Volume down** |

## Recommended Extra Gestures (toggleable, on by default after tutorial)

| Gesture | Action |
|--------|--------|
| Eyebrow raise (both) | Double-tap center (like / heart on many feeds) |
| Mouth open hold ~0.6s | Recent apps (or optional: notifications) |
| Head tilt left / right | Horizontal seek / skip (when media supports; else disable) |
| Prolonged both-eyes closed ~2.5s | Turn screen off / sleep (optional; default **off** for safety) |
| Slow double blink both (distinct timing from exit) | Pause service camera (save battery) — mapping configurable |

---

## Quality Bar

- Debounce + cooldown per gesture family (no double-fire).
- Calibration profiles stored locally (DataStore).
- Foreground service type `camera` (API 34+) with persistent notification.
- CI: assembleRelease + upload APK artifact on push/tag.

---

## Decisions Locked (2026-07-15)

| Topic | Choice |
|------|--------|
| Control model | System-wide Accessibility + camera FS |
| minSdk | 26 (Android 8), focus QA on Redmi Note 11 |
| Service mode | Always-on after setup |
| Gesture set | Core + recommended extras |
| Vision stack | MediaPipe Face Landmarker + CameraX + Accessibility |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + Koin |
| Repo layout | `Goal/` · `Research/` · `Development/` |

# Eye Scroll Implementation Plan

> **For agentic workers:** Execute task-by-task. Steps use checkbox syntax.

**Goal:** Ship a crash-resilient Android app that uses CameraX + MediaPipe Face Landmarker in a foreground service and AccessibilityService to inject wink/nose/volume/home gestures system-wide; deliver APKs via GitHub Actions.

**Architecture:** Clean Architecture (domain/data/presentation). Foreground camera service → FaceSignals → GestureStateMachine → Accessibility gesture injection. Compose Material 3 UI for onboarding, tutorial, dashboard.

**Tech Stack:** Kotlin, Compose, CameraX, MediaPipe Tasks Vision, Koin, DataStore, Foreground Service (camera), AccessibilityService, GitHub Actions.

## Global Constraints

- minSdk 26, targetSdk 35, package `com.eyescroll.app`
- Official AndroidX / Google libs only + MediaPipe Tasks + Koin
- Strings in `res/values/strings.xml`; dp/sp; Material 3 ColorScheme/Typography
- Domain layer pure Kotlin (no Android)
- Never commit secrets (PAT, keystore)
- Project root content: Goal/, Research/, Development/
- APK via `.github/workflows/build-apk.yml`

---

### Task 1: Scaffold Gradle project

**Files:** Create under `Development/`: settings.gradle.kts, build.gradle.kts, gradle.properties, app/build.gradle.kts, AndroidManifest, theme, Application class, Koin modules.

- [ ] Create Android app module `com.eyescroll.app`
- [ ] Dependencies: Compose BOM, CameraX, mediapipe-tasks-vision, Koin, DataStore, Lifecycle
- [ ] Manifest permissions: CAMERA, FOREGROUND_SERVICE, FOREGROUND_SERVICE_CAMERA, POST_NOTIFICATIONS, WAKE_LOCK, RECEIVE_BOOT_COMPLETED

### Task 2: Domain gesture engine

**Files:** `Development/app/src/main/java/com/eyescroll/app/domain/...`

- [ ] `FaceSignals`, `GestureCommand`, `GestureType`, `GestureThresholds`
- [ ] `GestureStateMachine.process(signals, nowMs): GestureCommand?`
- [ ] Unit tests for double wink L/R, both→home, volume sequence

### Task 3: Data layer

- [ ] DataStore prefs: onboardingDone, serviceDesired, toggles, sensitivity, calibration
- [ ] CalibrationRepository

### Task 4: Accessibility + Foreground services

- [ ] `EyeAccessibilityService` — swipe/tap/home/recents
- [ ] `EyeControlForegroundService` — CameraX + MediaPipe + state machine bridge
- [ ] `CommandBus` singleton for service↔a11y
- [ ] Notification actions Pause/Stop/Open

### Task 5: Presentation

- [ ] Nav: Splash → Permissions → Tutorial → Calibration → Home
- [ ] Settings gesture toggles + MIUI checklist
- [ ] Theme ColorScheme/Typography (no hardcoded hex in composables — tokens in theme)

### Task 6: CI + GitHub

- [ ] `.github/workflows/build-apk.yml`
- [ ] README, .gitignore
- [ ] Push repo; verify Actions artifacts

---

## Execution

User directed: decide all + implement immediately (inline execution). Token via env for push only; revoke after delivery.

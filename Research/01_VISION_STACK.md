# Research: On-Device Face / Eye Control for Android

**Date:** 2026-07-15  
**Project:** Eye Scroll

## Summary

For system-wide Instagram-style control, the best practical stack is **CameraX + MediaPipe Face Landmarker (landmarks + blendshapes) + AccessibilityService**, running inside a **camera foreground service**. ML Kit alone is too weak for reliable per-eye winks and nose/brow extras. OpenCV EAR is maintainable but less robust in poor light on mid-range Redmi devices.

## MediaPipe Face Landmarker

- ~478 3D landmarks  
- ~52 ARKit-style blendshapes including `eyeBlinkLeft`, `eyeBlinkRight`, brow, mouth, nose-related  
- On-device CPU/GPU; no network  
- Docs: https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/android  

**Why chosen:** wink L/R needs independent eye signals; blendshapes + EAR hybrid is industry-standard for assistive face mice and blink keyboards.

## Alternatives considered

| Option | Verdict |
|--------|---------|
| ML Kit Face Detection (eyes open/close) | Too coarse for double-wink L/R + nose |
| ML Kit Face Mesh | Mesh without rich blendshapes for expressions |
| OpenCV EAR only | Higher false positives; more engineering |
| Commercial AR SDKs | Cost / licensing; unnecessary for gestures |

## Accessibility injection

- `dispatchGesture` for swipe/tap  
- `performGlobalAction` for HOME / RECENTS / notifications  
- Volume via `AudioManager.adjustStreamVolume` from app process (service) or global actions where available  
- Must show Accessibility disclosure; user must enable manually (cannot auto-grant)

## Always-on camera legality/UX

- Use `foregroundServiceType="camera"` + persistent notification  
- Disclose continuously in notification text  
- Pause detection when face absent long enough (optional power save without full stop)

## Redmi Note 11 / MIUI / HyperOS

Known killers of background camera services:

1. Battery saver / “Battery optimization” killing FS  
2. Autostart disabled  
3. “Display pop-up windows while running in background”  
4. MIUI aggressive RAM freeze  

Mitigations: OEM settings intents, in-app checklist, “Don’t kill my app” style guidance, sticky FS, BOOT_COMPLETED re-prompt (cannot silently restart Accessibility).

## Performance budget (Redmi Note 11)

- Target 15–20 FPS inference; 640×480 or similar  
- Prefer GPU delegate; fallback CPU  
- Single face mode (`numFaces=1`)  
- Drop frames if pipeline backlog > 1

## Privacy

- No embedding of faces for ID  
- No gallery of frames  
- Settings export = thresholds only  

## Gesture research inspirations

- Eye Aspect Ratio (EAR) — Soukupová & Čech blink detection  
- Assistive tech: camera mouse, Switch Access, Face Control apps  
- ARKit blendshape naming (MediaPipe aligns conceptually)  

## Extra gestures worth shipping (validated vs risk)

| Gesture | Benefit | Risk |
|---------|---------|------|
| Brow raise → like | High for Reels | False positives if surprise face |
| Mouth open → Recents | Escape hatch | Talking triggers — require hold |
| Head tilt → seek | Power users | Walking vibration — sensitive threshold |
| Long both-eye close → lock | Accessibility | Accidental lock — default OFF |
| Gaze cursor | Universal | Battery + jitter — defer v2 |

## Sources (primary)

1. Google AI Edge — Face Landmarker Android guide  
2. Android AccessibilityService / GestureDescription docs  
3. Android Foreground service types (camera)  
4. Xiaomi MIUI background activity / autostart behavior (community knowledge + OEM settings patterns)

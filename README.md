# Eye Scroll

Hands-free, system-wide eye/face gestures for Android (Instagram Reels, Shorts, etc.).

**Build:** APKs are produced **only on GitHub Actions** (no local Gradle/SDK required).

## Repo layout

| Path | Contents |
|------|----------|
| `Goal/` | Product goals, feature spec, gesture map |
| `Research/` | Vision stack, Redmi Note 11, CI notes |
| `Development/` | Android app sources |
| `.github/workflows/` | APK build workflow |

## Download APK

1. Open **Actions** → **Build APKs**
2. Open the latest green run
3. Download artifacts `eye-scroll-debug-apk` / `eye-scroll-release-apk`

## First-run on phone (esp. Redmi Note 11)

1. Install APK (allow unknown sources)
2. Grant Camera + Notifications
3. Enable **Eye Scroll** in Accessibility
4. Xiaomi: Autostart + unrestricted battery
5. Finish tutorial → service runs under a sticky notification
6. Press Home → open Instagram → use gestures

## Core gestures

- Double wink **right** → next (swipe up)
- Double wink **left** → previous (swipe down)
- Double blink **both** → Home
- Nose expand → tap (pause/play)
- Close both, open right/left → volume up/down

Extras (toggleable): brow raise → like, mouth open → Recents, head tilt → horizontal seek, long both-eyes close → lock (default off).

Privacy: face processing is on-device only.

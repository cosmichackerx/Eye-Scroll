# Research: Redmi Note 11 Survival Checklist

**Device:** Redmi Note 11 (family)  
**OS:** MIUI 13+/HyperOS, Android 11–13 typical

## Must enable for always-on Eye Scroll

1. **Autostart** — allow Eye Scroll  
2. **Battery saver** — No restrictions / Unrestricted  
3. **Other permissions** — Display pop-up windows (if overlay used)  
4. **Lock screen** — show/continue (if OEM offers)  
5. **Accessibility** — Eye Scroll service ON  
6. **Camera** — Allowed while using / all the time as prompted  
7. **Notifications** — Allowed (required for FGS transparency)

## In-app UX decisions

- Detect `Build.MANUFACTURER` ∈ {xiaomi, redmi, poco} → show Xiaomi-specific steps first  
- Deep-link where possible; else open app details + illustrated steps  
- “Test gesture” screen after setup to verify swipe fires  

## Thermal / camera notes

- Prefer lower resolution + 15 FPS when `ThermalStatus` elevated  
- If Instagram also using camera (Stories), show “Camera busy” — rare for Reels viewing  

## QA script (manual)

1. Fresh install → complete wizard  
2. Press Home → open Instagram Reels  
3. Double wink right ×10 → next advances ≥8/10  
4. Double wink left ×5 → previous works  
5. Nose expand → pause/play  
6. Volume both-then-open L/R  
7. Double both → Home  
8. Wait 30 min Reels → service still alive; no crash  
9. Airplane mode (ensure no network dependency)  

# Eye Scroll — Gesture Map

Authoritative gesture → action mapping for v1.

## Timing defaults (tunable after calibration)

| Parameter | Default |
|-----------|---------|
| Blink closed threshold (blendshape) | 0.55 |
| Blink open threshold | 0.25 |
| Double-wink window | 750 ms |
| Min inter-wink gap | 80 ms |
| Gesture cooldown | 550 ms |
| Nose expand delta (vs baseline) | +8% width or blendshape Δ |
| Mouth open hold | 600 ms |
| Both-eyes sleep hold | 2500 ms (feature default OFF) |
| Face lost timeout | 1500 ms |

## Core (default ON)

1. **DoubleWinkRight** → `SWIPE_NEXT` (up)  
2. **DoubleWinkLeft** → `SWIPE_PREV` (down)  
3. **DoubleBlinkBoth** → `HOME`  
4. **NoseExpand** → `TAP_CENTER`  
5. **BothThenOpenRight** → `VOLUME_UP`  
6. **BothThenOpenLeft** → `VOLUME_DOWN`

## Extras (default ON except Sleep)

7. **BrowRaise** → `DOUBLE_TAP_LIKE`  
8. **MouthOpenHold** → `RECENTS`  
9. **HeadTiltLeft / HeadTiltRight** → horizontal swipe (seek); disable if false positives high  
10. **BothEyesSleep** → `GLOBAL_ACTION_LOCK_SCREEN` if available else noop — **default OFF**  
11. **ServicePauseBlink** (slow double both, longer window) → pause camera — optional remap in settings

## Confusability rules

- Prefer **asymmetric** wink over both-eye when one side clearly dominant.
- Volume gesture requires **both closed first** so normal blinks don’t change volume.
- HOME uses shorter, snappier double both than pause-service pattern.
- After any fire, ignore same family until cooldown.

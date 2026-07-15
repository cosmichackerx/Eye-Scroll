# Research: CI / APK Delivery

**Date:** 2026-07-15

## Goal

Every push to `main` (and version tags) produces downloadable APKs via GitHub Actions.

## Planned workflow (`Development/.github/workflows/build-apk.yml`)

- JDK 17  
- Android SDK  
- `./gradlew assembleDebug assembleRelease`  
- Upload `app-debug.apk` and `app-release-unsigned.apk` (or signed if secrets present)  

## Signing (later)

Repo secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`  
If missing → still publish unsigned release for sideload testing (user can sign locally).

## Security

- **Never** store GitHub PATs in the repository  
- Use `gh auth` / Actions `GITHUB_TOKEN` / environment secrets only  
- Rotate any PAT pasted into chat immediately  

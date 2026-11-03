# Atomic Admin — Android

A separate Android launcher app for the Atomic Weekly editorial dashboard.

## Identity
- App name: Atomic Admin
- Application ID: `com.atomicweekly.admin`
- Project directory: `android-app/`
- Admin web dashboard: https://iammdtanvirrahman.github.io/atomic-weekly-admin-panel/

This is a separate Android project in the Admin Panel repository. It does not modify or share the Reader app's Android package (`com.atomicweekly.app`).

## Open in Android Studio
1. Open the `android-app/` directory.
2. Use JDK 17 and install Android SDK 35 when prompted.
3. Sync Gradle and run the `app` configuration.

## Build
Use **Build → Build Bundle(s) / APK(s) → Build APK(s)**. The debug APK should be generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Authentication integration status
The dashboard is currently loaded inside Android WebView. Firebase Google sign-in from an embedded WebView is restricted by Google's OAuth policies and may not work reliably. A production-native sign-in bridge requires registering the Android app (`com.atomicweekly.admin`) in Firebase project `atomictanvir`, adding its SHA-1/SHA-256 fingerprints, downloading the matching `google-services.json`, and configuring the OAuth web client ID. Do not weaken Firebase Database Rules to work around sign-in issues.

## Shared publishing backend
The dashboard continues to use the existing Firebase project and data paths. Reader app remains a separate app and continues to consume the published article feed.

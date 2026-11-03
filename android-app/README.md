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

A GitHub Actions workflow also builds the debug APK on pushes to `android-admin-app`. Download the `atomic-admin-debug-apk` artifact from the completed workflow run.

## Native Google sign-in setup (required once)
The Android app now uses Android Credential Manager to obtain a Google ID token and passes it to the Firebase Web SDK running in the dashboard. Android WebView no longer uses the restricted Google popup/redirect flow.

Before Google sign-in can succeed:

1. In Firebase Console, open project `atomictanvir` → Project settings → Your apps. Register an Android app with package name `com.atomicweekly.admin`.
2. Add the SHA-1 and SHA-256 signing certificate fingerprints for the build you will install. For a local debug build, run `./gradlew signingReport` from `android-app/` (or use the Gradle signingReport task in Android Studio).
3. In Google Cloud / Firebase Authentication, ensure Google is enabled and that the OAuth **Web application** client exists for the same project.
4. Copy that OAuth Web client ID (it ends in `.apps.googleusercontent.com`) into `android-app/app/src/main/res/values/strings.xml`, replacing `REPLACE_WITH_WEB_CLIENT_ID`.
5. Ensure the deployed Admin dashboard includes the native bridge code in `index.html`. The default branch and `android-admin-app` branch have been updated for this bridge.

The Web client ID is configuration, not a secret. Never put a client secret in the Android app. Do not weaken Firebase Database Rules to work around sign-in issues.

## Shared publishing backend
The dashboard continues to use the existing Firebase project and data paths. Reader app remains a separate app and continues to consume the published article feed.

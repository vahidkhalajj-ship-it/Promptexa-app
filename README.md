# Promptexa Android App

A lightweight Android wrapper for **https://www.promptexa.ir** built with Kotlin and a single
optimized WebView (no separate backend, no local database, no unnecessary libraries).

## Why WebView instead of TWA

The spec's first preference was a Trusted Web Activity. TWA was evaluated but is not a good fit
here because the spec also requires:

- a **custom bottom navigation bar** that switches between 4 site sections inside the app,
- a **custom offline screen** with a retry button,
- a **custom exit-confirmation dialog** on the back button,
- **in-app** handling of notification taps (open a specific page without leaving the app).

TWA hands full-screen browser chrome to Chrome and doesn't give the native layer a place to draw
a persistent bottom nav bar or intercept load failures the way this spec needs. A single-Activity
WebView gives full control over all four of those requirements while staying just as lightweight
(no WebView-specific frameworks, no duplicated site code, session/cookies persisted natively).

## Project structure

```
PromptexaApp/
├── build.gradle, settings.gradle, gradle.properties
└── app/
    ├── build.gradle
    ├── google-services.json          (placeholder — replace, see Firebase section)
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/ir/promptexa/app/
        │   ├── PromptexaApplication.kt
        │   ├── Constants.kt          (all site URLs + Webpushr placeholders)
        │   ├── SplashActivity.kt     (fade + scale animation, ~1.4s)
        │   ├── MainActivity.kt       (WebView, bottom nav, back button, offline handling)
        │   └── MyFirebaseMessagingService.kt  (push notifications)
        └── res/
            ├── layout/ (activity_splash.xml, activity_main.xml)
            ├── menu/bottom_nav_menu.xml
            ├── drawable/ (icons, splash logo, offline background)
            ├── mipmap-*/ (app launcher icon, generated from your logo)
            └── values/ (strings, colors, themes)
```

## What's implemented against the spec

| Requirement | Where |
|---|---|
| Splash: white bg, centered logo, fade + scale, 1-2s | `SplashActivity.kt`, `activity_splash.xml` |
| Bottom nav: Home / Categories / Cart / Account | `MainActivity.kt` + `bottom_nav_menu.xml` |
| Keep WordPress/WooCommerce login session | `CookieManager` persistent cookies in `MainActivity.setupWebView()` |
| Back button: normal in-page back, confirm dialog on home | `setupBackPressHandling()` in `MainActivity.kt` |
| Offline screen with retry | `offlineLayout` in `activity_main.xml`, shown from `WebViewClient.onReceivedError` |
| Notification tap opens page in-app | `MyFirebaseMessagingService` → `SplashActivity` → `MainActivity` via `EXTRA_NOTIFICATION_URL` |
| Only Internet + Notification permissions | `AndroidManifest.xml` |
| External (non-promptexa) links open in system browser | `shouldOverrideUrlLoading` in `MainActivity.kt` |

## 1. Build instructions

1. Install **Android Studio** (Koala or newer recommended).
2. Open the `PromptexaApp/` folder as an existing project. The Gradle wrapper's binary jar
   (`gradle/wrapper/gradle-wrapper.jar`) isn't included in this package — Android Studio
   generates it automatically the first time it syncs (it reads `gradle-wrapper.properties`,
   which is included). Building from the command line instead, run `gradle wrapper` once first.
3. Let Gradle sync (it will download the AGP/Kotlin/Firebase dependencies listed in
   `app/build.gradle` — an internet connection is required for this step).
4. Replace the placeholder `app/google-services.json` with your real one (see Firebase section
   below) **before** building, or the Gradle sync for the `google-services` plugin will fail.
5. Run on a device/emulator with the green ▶ Run button, or from a terminal:
   ```bash
   ./gradlew assembleDebug
   ```

## 2. Generating a release APK / AAB

1. Build → Generate Signed Bundle / APK in Android Studio, or:
   ```bash
   ./gradlew assembleRelease   # APK
   ./gradlew bundleRelease     # AAB (for Play Store)
   ```
2. You'll need a signing keystore (Build → Generate Signed Bundle/APK → Create new... if you
   don't have one yet). Keep the keystore and its password safe — you'll need the same one for
   every future update.
3. The signed output appears under `app/build/outputs/apk/release/` or
   `app/build/outputs/bundle/release/`.
4. `minifyEnabled` and `shrinkResources` are already turned on for release builds to keep the
   APK small, per the spec's "small APK size" requirement.

## 3. Configuration guide

All site URLs live in one place: `app/src/main/java/ir/promptexa/app/Constants.kt`. If any
WordPress permalink changes, update it there — nothing else in the app hardcodes URLs.

App name, exit dialog text, and offline text are in `app/src/main/res/values/strings.xml`.

Brand colors in `app/src/main/res/values/colors.xml` were sampled from your logo (deep
blue `#1E2A5A` / purple accent `#7B2FF7`). Swap these for the exact hex values from your
website's stylesheet if you want a pixel-perfect match.

**Do not commit real secrets.** For a production build, prefer injecting the Webpushr keys via
`local.properties` + `BuildConfig` fields rather than editing `Constants.kt` directly, e.g.:

```groovy
// app/build.gradle, inside defaultConfig
buildConfigField "String", "WEBPUSHR_API_KEY", "\"${project.findProperty('WEBPUSHR_API_KEY') ?: ""}\""
```
(and enable `buildFeatures { buildConfig true }`).

## 4. Firebase setup guide

The spec gave a **web** Firebase config (`apiKey`, `appId` starting with `...:web:...`). Firebase
Cloud Messaging on Android needs its own **Android app** registered in the same project:

1. Go to the [Firebase console](https://console.firebase.google.com/) → project **promptexa-push**
   (project number `374327379041`, this is already set up per the spec).
2. Project settings → **Add app → Android**.
3. Package name: `ir.promptexa.app` (must match `applicationId` in `app/build.gradle`).
4. Download the generated **`google-services.json`** and replace the placeholder file at
   `app/google-services.json` with it.
5. That's it — `com.google.gms.google-services` (already applied in the Gradle files) and
   `firebase-messaging-ktx` (already a dependency) handle the rest automatically.
6. Test: Firebase console → Cloud Messaging → "Send test message" using a device's FCM token
   (log `FirebaseMessaging.getInstance().token` once during development to grab it).

## 5. Webpushr setup guide

Webpushr is what your WordPress site already uses to send push notifications; on Android it
delivers through Firebase Cloud Messaging.

1. In your Webpushr dashboard: **Settings → Firebase Server Key / Android setup**.
2. Enter the **Server key** and **Sender ID (`374327379041`)** from the same Firebase project
   (Project settings → Cloud Messaging tab in Firebase console).
3. Webpushr will now deliver notifications through FCM as **data messages** with `title`,
   `message`, `image`, and `url` fields — these are exactly the keys
   `MyFirebaseMessagingService.kt` reads (`remoteMessage.data["title"]`, `["message"]`,
   `["image"]`, `["url"]`).
4. Tapping a notification opens `MainActivity` and loads `url` directly inside the WebView — no
   browser hand-off.
5. `Constants.WEBPUSHR_API_KEY` / `WEBPUSHR_AUTH_TOKEN` are placeholders for any server-side calls
   you make to the Webpushr REST API later (e.g. custom subscribe/unsubscribe flows); the basic
   notification delivery above doesn't require the app to call Webpushr directly at all.

## Permissions

Only `INTERNET`, `ACCESS_NETWORK_STATE` (to detect offline state), and `POST_NOTIFICATIONS`
(required on Android 13+ to show notifications) are declared — nothing else.

## Notes on future additions

The structure intentionally keeps `MainActivity` as the single source of truth for navigation, so
adding a chatbot, richer notifications, or an iOS counterpart later won't require restructuring
what's here — per the spec, none of that is implemented in this v1.

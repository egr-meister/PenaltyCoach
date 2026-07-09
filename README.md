# PenaltyCoach

PenaltyCoach is a native Android football **penalty training log**. Its main purpose is penalty practice tracking: you choose a shot zone on a goal board, record the result (Goal, Miss, or Saved), and review how your penalties are performing over time. A secondary Match Schedule screen can show upcoming football fixtures from the football-data.org API as an optional reference.

The app is deliberately simple and stable: Kotlin, Jetpack Compose, Material 3, and a single local DataStore file for all user data. No account, no ads, no analytics, no payments, no betting.

---

## Features

- **Goal zone board** — a 3x3 football goal grid that is the main visual identity of the app. Tap a zone to record a shot.
- **Shot recording** — log zone, result (Goal / Miss / Saved), foot, shot power, and a note. Edit or delete any shot.
- **Penalty series training** — run 5-shot or 10-shot series with live progress ("Shot 3 of 5") and an end-of-series summary.
- **Statistics** — total shots, goals, misses, saved, conversion %, best/weakest/most-used zone, per-zone breakdown, and a per-foot breakdown.
- **Shot history** — daily summary cards in reverse chronological order, drill into any day, and reset a day after confirmation.
- **Match Schedule (secondary)** — optional football fixtures from football-data.org for the next 10 days.
- **Private by design** — all your data stays on the device.

---

## Penalty tracking disclaimer

> PenaltyCoach is a manual football penalty training log. Penalty shots, results, zones, and notes are added by the user. The app is not an official football tool and does not provide professional coaching or medical advice.

There is **no automatic tracking** of any kind — every shot is entered manually by you.

## Match Schedule API disclaimer

> Match data is provided by football-data.org. Availability, accuracy, competitions, and update frequency depend on the API provider and the current API plan.

---

## Match Schedule: football-data.org API v4

The secondary Match Schedule screen uses the **football-data.org API v4** matches endpoint only:

- Base URL: `https://api.football-data.org/v4`
- Endpoint: `GET /matches?dateFrom=<today>&dateTo=<todayPlus9Days>`
- Auth header: `X-Auth-Token: <your token>`

No odds, bookmaker, prediction, or live-betting endpoints are used anywhere in the app.

### Default 10-day API window

By default the screen requests a **10-day window**: from today through today + 9 days.

- `dateFrom` = today (device local date, `YYYY-MM-DD`)
- `dateTo` = today + 9 days

Example: if today is `2026-07-08`, the app requests `GET /matches?dateFrom=2026-07-08&dateTo=2026-07-17`.

Dates are always calculated locally from the device date — none are hardcoded. You can override the window (and add an optional competition code) in **Match Schedule Settings**; leaving the dates empty always falls back to the default 10-day window.

### API usage policy

- Manual **Refresh** button only. The app never polls, never auto-refreshes in the background, and never fetches live scores every few seconds.
- On open, the app shows cached matches first, and fetches once only if the cache is empty or not from today.
- The latest successful response is cached locally (matches, last-updated time, and the requested date range).
- Friendly messages are shown when the API token is missing, the API limit is reached, there is no internet, or the response is invalid — the app never crashes.

### Demo data fallback

If `FOOTBALL_DATA_API_TOKEN` is missing, empty, or still equals `your_api_token_here`, the app shows built-in **demo matches** with generic placeholder team/competition names (no logos, no real players) and a friendly message. The rest of the app is fully usable offline.

---

## `local.properties` setup

The API token is read from `local.properties` (for local development) or from an environment variable (for CI) and exposed through `BuildConfig`. It is **never** hardcoded in source.

1. Copy `local.properties.example` to `local.properties`.
2. Fill in your values:

   ```
   sdk.dir=/path/to/Android/sdk
   FOOTBALL_API_BASE_URL=https://api.football-data.org/v4
   FOOTBALL_DATA_API_TOKEN=your_real_token_here
   ```

3. Get a free token at <https://www.football-data.org/client/register>.

> **Never commit `local.properties` or your real API token** to Git. `local.properties` is already listed in `.gitignore`. The real token must never appear in source, README, screenshots, tests, or CI logs.

These values reach the app through:

- `BuildConfig.FOOTBALL_API_BASE_URL`
- `BuildConfig.FOOTBALL_DATA_API_TOKEN`

---

## Privacy, permissions, and what the app does NOT do

**Privacy note:**

> PenaltyCoach stores penalty shots, penalty series, notes, settings, and cached match data on this device. The app uses internet only to load football match data from football-data.org. No account, no ads, no analytics, no payments, no Firebase, no location, no notifications, no sensors, no Google Fit, and no Health Connect.

- **No ads, no analytics, no payments.** None are integrated.
- **No betting, no odds, no bookmakers, no predictions.** No gambling language or mechanics of any kind.
- **No official logos.** No FIFA/UEFA/league/club branding or logos; competition and team names appear only as plain text returned by the API.
- **No TeApuesto branding.** No TeApuesto name, logo, or brand identity is used; only a general orange/black/white color mood.
- **No account registration** and **no cloud sync**. All user data is local.

**Permissions:** the app declares exactly **one** permission — `INTERNET` — used solely by the secondary Match Schedule feature.

It does **not** request or use: location, camera, microphone, contacts, storage/gallery/files, notifications (no push notifications), calendar, alarms, activity recognition, body/other sensors, Google Fit, Health Connect, wearable integration, or background services/workers.

---

## Local storage & DataStore

All user data is stored locally using **DataStore Preferences**, as a single JSON string serialized with **kotlinx.serialization**. There is no database (Room is intentionally not used — DataStore JSON is enough).

Stored data includes: penalty shots, penalty series, settings, onboarding flag, match schedule settings, cached match schedule, last API update time, last API error, and last requested match date range.

The repository is defensive: it merges with default values, handles empty storage and missing fields, and recovers from corrupted JSON with a safe fallback. The UI never crashes on empty shots/series, missing days, missing shots/series, missing settings, missing cache, invalid dates/times, or missing navigation arguments.

---

## Internet usage

The app connects to the internet **only** to load match data from `https://api.football-data.org` when you open or refresh the Match Schedule screen. Everything else works fully offline.

---

## App concept, screens & flows

The main screen answers: *"How did my penalty shots perform today?"* The Match Schedule screen answers: *"What football matches can I view as an extra reference?"*

Screens: Onboarding, Home, Shot Entry, Penalty Series, Series Summary, Statistics, Shot History, Day Detail, Shot Detail/Edit, Match Schedule, Match Schedule Settings, and Settings — wired together with Navigation Compose. Navigation tolerates missing/invalid arguments and always has a friendly fallback.

---

## Visual design

**Style:** *"Orange Penalty Coach Board"* — bold, sporty, clean, and practical, built only from Compose shapes, borders, and text (no heavy assets, no external image packs, no complex animations).

- **Layout uniqueness:** the app avoids the generic "mascot → title → subtitle → stats card → stack of buttons → settings" template. The goal zone board is the centerpiece, with an orange header, dark goal frame, white content cards, and result chips. It never looks like a live-score, betting, casino, or fitness dashboard.
- **Palette:** bright orange (`#FF6200`), deep orange (`#E65300`), black/graphite contrast areas, and clean white cards. Result colors are used strictly: green = Goal, red = Miss, blue = Saved. No casino gold, neon, or heavy gradients.

**App icon concept:** a rounded-square bright-orange icon with a dark goal frame, a simple 3x3 zone grid, one highlighted orange zone, and a white penalty spot — readable at small sizes. No official logos, club branding, player photos, betting symbols, coins/cash, or TeApuesto branding. (Adaptive icon for API 26+, with legacy PNG fallbacks for API 24–25.)

**Splash screen concept:** a bright-orange background with a centered dark goal-zone mark and a white penalty dot, using the `androidx.core:core-splashscreen` API so it works from API 24. No default Android splash, no heavy illustration.

---

## Technology stack

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · Kotlin Coroutines · ViewModel · DataStore Preferences · kotlinx.serialization · Retrofit · OkHttp · Gradle Kotlin DSL.

Architecture is a simple MVVM: one repository for local app data (`AppRepository`), one isolated repository for the football-data.org API (`FootballDataRepository`), and ViewModels (`AppViewModel`, `MatchViewModel`) for the screens.

---

## Open the project in Android Studio

1. Install a recent stable Android Studio.
2. `File → Open` and select the `PenaltyCoach` folder.
3. Android Studio will sync Gradle and regenerate the Gradle wrapper JAR automatically.
4. Create `local.properties` (see above) if you want live match data.
5. Run the `app` configuration on a device/emulator (portrait, API 24+).

> The Gradle wrapper JAR (`gradle/wrapper/gradle-wrapper.jar`) is not committed. Android Studio regenerates it on sync; from a terminal you can run `gradle wrapper --gradle-version 8.9` once (requires a locally installed Gradle) to create it, after which `./gradlew` works.

---

## How to build (command line)

```bash
# Debug build
./gradlew assembleDebug

# Release build (see signing below)
./gradlew assembleRelease   # APK
./gradlew bundleRelease     # AAB (Google Play upload target)
```

---

## Android configuration & Google Play compatibility

- `compileSdk = 35`, `targetSdk = 35` (not 34), `minSdk = 24`.
- Current stable Android Gradle Plugin and Kotlin (Compose compiler plugin).
- **16 KB page size:** the app ships no native libraries of its own, so the AAB is compatible with Android 15+ 16 KB memory page sizes. No old native libraries or unnecessary native SDKs are added.
- Avoids the common Play rejections: target API 34, unsupported 16 KB page sizes, and release artifacts signed with a debug key.

---

## Signing (PKCS12) & release stability

Release builds (APK and AAB) must be signed with a **real PKCS12 keystore**, never the debug key. Signing material is provided only through environment variables / GitHub Secrets and is never committed.

### Generate a PKCS12 keystore

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore penaltycoach-release-key.p12 -alias penaltycoach_key -keyalg RSA -keysize 2048 -validity 10000
```

Use the **same password** for the keystore and the key.

### Add GitHub Secrets

In your repository: `Settings → Secrets and variables → Actions → New repository secret`.

| Secret | Description |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 of your `.p12` keystore (`base64 -i penaltycoach-release-key.p12`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias (e.g. `penaltycoach_key`) |
| `ANDROID_KEY_PASSWORD` | Key password (same as keystore password) |
| `FOOTBALL_DATA_API_TOKEN` | *(optional)* football-data.org token; omit to build with demo data |

The `app/build.gradle.kts` release build type uses `signingConfigs.release`, populated from these values. Keystore files and passwords are never stored in the repository.

### Release optimization (R8 / shrinking)

R8 minification and resource shrinking are enabled for release, with keep rules in `app/proguard-rules.pro` that protect kotlinx.serialization models and Retrofit. Recommended workflow if you change dependencies:

1. First verify a **non-minified** release build (`isMinifyEnabled = false`, `isShrinkResources = false`).
2. Then enable `isMinifyEnabled = true` and `isShrinkResources = true` (the default in this project) and re-test launch.

---

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and:

1. Sets up JDK 17 and the Android SDK.
2. Installs `platforms;android-35` and `build-tools;35.0.0`.
3. Creates `local.properties` from the optional `FOOTBALL_DATA_API_TOKEN` secret (or a safe placeholder). If the secret is missing, the build still succeeds and the app uses demo data.
4. Decodes the release keystore from GitHub Secrets (when provided).
5. Builds the signed release **AAB** and **APK**.
6. **Verifies the APK signature** with `apksigner verify --print-certs`, prints the certificate, and **fails the build** if the certificate contains `CN=Android Debug`.
7. Uploads the APK and AAB as workflow artifacts.

CI is responsible only for a fast, stable APK/AAB build and signing verification. Emulator smoke-tests are intentionally not run on free runners.

> **Google Play upload target is the `.aab` only**, not the `.apk`.

---

## Local launch verification checklist

A green CI build is not proof the app launches. Before release, install the release APK on a device/emulator and check `adb logcat` for the absence of: `ClassNotFoundException` / "Cannot find class", `NoSuchMethodError`, serialization crashes, DataStore JSON parse crashes, and missing navigation-argument / missing-shot / missing-series / invalid-zone / invalid-result / invalid-date / invalid-time / invalid-API-response / missing-token crashes, and signature misconfiguration.

```bash
adb install app-release.apk
adb logcat
```

Recommended manual test pass:

- First launch with empty storage; complete onboarding.
- Record a shot; select every zone; save Goal, Miss, and Saved results; edit a shot; delete a shot.
- Start and finish a 5-shot series; start and cancel an incomplete series; start and finish a 10-shot series; open the series summary.
- Open Statistics, Shot History, and a Day Detail; reset a selected day.
- Open Match Schedule with no API token (demo), then with a token; confirm the default request uses today + 9 days; refresh manually; simulate API failure; check cached matches; clear the match cache.
- Reset all local data; relaunch; launch in airplane mode.
- Verify the release APK signature; confirm only the `INTERNET` permission is used and no location/camera/microphone/contacts/storage/notifications/sensors/Google Fit/Health Connect/wearable permissions are requested.

---

## Project structure

```
PenaltyCoach/
├─ app/
│  ├─ build.gradle.kts            # module config, BuildConfig token, signing, R8
│  ├─ proguard-rules.pro
│  └─ src/main/
│     ├─ AndroidManifest.xml      # INTERNET permission only, portrait
│     ├─ java/com/penaltycoach/app/
│     │  ├─ MainActivity.kt, PenaltyCoachApp.kt
│     │  ├─ data/model/           # @Serializable models + enums
│     │  ├─ data/local/           # AppRepository (DataStore JSON)
│     │  ├─ data/remote/          # Retrofit service, repository, DTOs
│     │  ├─ data/demo/            # offline demo matches
│     │  ├─ util/                 # DateUtils, Stats
│     │  └─ ui/                   # theme, components, screens, navigation, viewmodels
│     └─ res/                     # icon, splash, themes, strings, colors, backup rules
├─ .github/workflows/android-build.yml
├─ gradle/libs.versions.toml
├─ local.properties.example
└─ README.md
```

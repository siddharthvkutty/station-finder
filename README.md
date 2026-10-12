# Station Finder

An Android app and home-screen widget that shows which **Mumbai Central Railway** station you're closest to.

- **Widget:** nearest station, distance and last-updated time. It updates itself and has a refresh button.
- **App:** the same info, with a Refresh button.
- **Coverage:** 50 stations on the main line (CSMT → Kalyan), the Kasara branch and the Khopoli branch. The Harbour line is not included.
- **No dependencies:** plain Java and Android framework APIs. There are no Google Play Services and no third-party libraries.

## Install on your phone

Every push to GitHub builds the app and publishes the APK to a rolling release.

1. On your phone, open this repo's **Releases** page and download the `.apk` from **Latest build**.
2. Install it. Android will ask you to allow installs from your browser.
3. Open the app and allow location. When it sends you to settings, choose **Allow all the time**. Without that, the widget can't update while the app is closed.
4. Long-press the home screen, open **Widgets**, and add **Station Finder**.

Requires Android 12 or newer.

## How it works

- `Finder.java` holds the station list and does the lookup. It gets a one-off location fix, picks the nearest station with `Location.distanceBetween`, and caches the result in `SharedPreferences`.
- `StationWidget.java` is the widget. Android triggers it every 30 minutes, and the refresh icon triggers it on demand. Tapping the rest of the widget opens the app.
- `MainActivity.java` asks for permissions and shows the same cached result.

Updates run every 30 minutes because that is the shortest interval Android allows for `updatePeriodMillis`. For faster updates you'd need WorkManager or a foreground service.

## Building

### GitHub Actions (no local setup)

`.github/workflows/build.yml` runs `gradle assembleDebug` on every push and attaches the APK to a `latest` release.

**One-time setup:** commit a fixed signing key so every build has the same signature. Without it, each CI build is signed with a new random key. Your phone would refuse to update the app until you uninstalled it, and that removes the widget.

```sh
keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey \
  -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"
git add debug.keystore && git commit -m "Add debug keystore"
```

This is the standard Android debug key and its password is public. Don't reuse it for a Play Store release.

### Locally

You need JDK 17, the Android SDK (platform 35) and Gradle 8.9 or newer. Android Studio bundles all of these. Open the folder in Android Studio, or run:

```sh
gradle assembleDebug
adb install -r build/outputs/apk/debug/station-finder-debug.apk
```

## Station data

Coordinates are station centres taken from OpenStreetMap, except **CSMT, Parel and Kanjurmarg**. Those three weren't in the data I pulled, so they are approximate, within roughly 200 m. To fix or add a station, edit the `STATIONS` array in `src/main/java/app/stationfinder/Finder.java`. Each row is `{name, latitude, longitude}`.

## Project layout

```
build.gradle.kts, settings.gradle.kts    single-module Gradle build
.github/workflows/build.yml              CI: build APK, publish to the "latest" release
src/main/AndroidManifest.xml             permissions, activity, widget receiver
src/main/java/app/stationfinder/         Finder, StationWidget, MainActivity
src/main/res/                            widget layout and widget config
```

## Privacy

Your location is used on the device only. The app has no network access and sends nothing anywhere.

## Data and licence

Station coordinates © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors, available under the ODbL.

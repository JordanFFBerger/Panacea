# Panacea Android

The native Kotlin / Jetpack Compose Android app lives in [android/](android/README.md).

Open the `android` folder in Android Studio and press Run. Build an APK with `gradlew.bat assembleDebug` from that folder.

The root `app/` directory is the earlier browser design reference; it is not the Android runtime.

Android reads upcoming events from a live Baton Rouge community feed, with optional Ticketmaster listings. See the [Android setup guide](android/README.md#android-event-catalog).

## Continuous integration

The [GitHub Actions workflow](.github/workflows/ci.yml) runs on pushes to main, pull requests, and manual dispatch. It checks browser JavaScript syntax, runs the backend suite, and runs Android unit tests before uploading a debug APK to the workflow run.

Run the backend suite locally with `npm run qa -- syntax integration api-e2e` (Node 20 or newer). Android build and test instructions are in the [Android README](android/README.md).

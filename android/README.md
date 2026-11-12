# Panacea — native Android app

Kotlin + Jetpack Compose. This is an installable Android app, with no WebView and no web server.

## Open and run

1. Open this `android` directory in Android Studio, let Gradle sync, and select an emulator or Android device (Android 8.0/API 26 or newer).
2. Press Run. For command-line builds, configure Java 17+ and Android SDK 36, then run `./gradlew assembleDebug testDebugUnitTest` (Windows: `gradlew.bat assembleDebug testDebugUnitTest`).

APK output: `app/build/outputs/apk/debug/app-debug.apk`. Android Studio supplies a suitable JDK; point `JAVA_HOME` to its `jbr` folder when building from a terminal. Point `ANDROID_HOME` to your local SDK, or create an untracked `local.properties` with `sdk.dir`.

## Native features

- Discover cards show admission price, date/time, venue, activities and sample signup counts immediately. Tap once for the full summary.
- Search by name, activity, category or venue; filter by category/date/price and sort by date, price or recent signups.
- Saved events and demo plans persist on the device. Device Back returns from an event to discovery.
- Nearby lists events by illustrative distance with links to their sample area on OpenStreetMap. It does not request location access.
- Android share sheet for event summaries.
- Day: aqua and green gradients, luminous sky and river. Night: midnight teal, brass accents and serif hero type. All buttons use square corners.
- Auto theme follows America/Chicago: day 7am–6pm; night otherwise, checked every minute. Tap the theme control for Auto → Day → Night.
- Trending requires at least 20 signups in 24 hours and at least 50% growth over the previous 24 hours. Boundary tests cover this rule.

## Prototype boundary

Events, prices, coordinates, distances, dates and signup counts are fictional fixtures. Dates are relative to today in Baton Rouge. Signups are local demonstrations, not reservations; no payments or actual tickets. This skeleton has no server or login. The nearby page is a distance list with external area maps, not a live in-app map.

Replace `SampleEvents` with an event repository/API and normalized price/activities/location fields. Move authenticated, deduplicated signup counts and rolling trending calculations to a backend before real use. Connect verified organizer ticket links, and add a geographic map provider if needed.

The repository root's `app/` directory contains the earlier browser design reference. The Android application lives entirely here.

The Discover hero features Panacea’s historical woodcut and a **Pick a cure for me** button that chooses an event from the current filters. Event cards and details display human-made, offline cover photos. See the [photo credits and license links](PHOTO-CREDITS.md).

Music details offer an opt-in Listening Room. The demo jazz event features [Take Five by The Dave Brubeck Quartet](https://www.youtube.com/watch?v=-DHuW1h1wHw) as a mood match, with the official YouTube embed and a watch link. Playback needs internet access and is subject to YouTube availability. No media is downloaded or bundled.

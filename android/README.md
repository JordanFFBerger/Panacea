# Panacea — native Android app

Kotlin + Jetpack Compose. This is an installable Android app, with no WebView. The event feed uses the Panacea API.

## Open and run

1. Open this `android` directory in Android Studio, let Gradle sync, and select an emulator or Android device (Android 8.0/API 26 or newer).
2. Press Run. For command-line builds, configure Java 17+ and Android SDK 36, then run `./gradlew assembleDebug testDebugUnitTest` (Windows: `gradlew.bat assembleDebug testDebugUnitTest`).

APK output: `app/build/outputs/apk/debug/app-debug.apk`. Android Studio supplies a suitable JDK; point `JAVA_HOME` to its `jbr` folder when building from a terminal. Point `ANDROID_HOME` to your local SDK, or create an untracked `local.properties` with `sdk.dir`.

## Native features

- Discover cards show price, date/time and venue. Tap once for details and the original listing when available.
- Search and filter by category, date and price. Saved events persist on the device.
- Nearby shows the catalog with distances first when available. Panacea does not request your location.
- Day uses aqua and green; night uses midnight teal and brass. Auto follows Baton Rouge time. Buttons have square corners.
- Live listings do not include verified signup totals or recordings. Panacea does not infer popularity from provider rankings.

The repository root's `app/` directory contains the earlier browser design reference. The Android application lives entirely here.

The Discover hero features a cropped portrait of the draped Panakeia statue at the Archaeological Museum of Dion and a **Pick a cure for me** button that chooses an event from the current filters. Event cards and details display human-made, offline cover photos. See the [photo credits and license links](PHOTO-CREDITS.md).

Discover uses a credited Baton Rouge photograph for each color theme. **Meet Panacea** displays a verbatim excerpt from the [Theoi Project](https://www.theoi.com/Ouranios/AsklepiasPanakeia.html) followed by the app's boredom line in the `panacea_boredom_note` string in `app/src/main/res/values/strings.xml`.

## Android event catalog

1. From the repository root, run `node backend/apiServer.cjs` (Node 20+). The [Baton Rouge Has Things JSON feed](https://baton-rouge.hasthings.com/feeds) works without credentials and includes publicly advertised shows, library programs and community events.
2. Optionally set `TICKETMASTER_API_KEY` in the server environment before startup to add listings within 25 miles of downtown Baton Rouge. The API key stays on the server.
3. Emulator debug builds use `http://10.0.2.2:3000`. For a physical phone, host the API behind a reachable HTTPS endpoint and build with `-PpanaceaApiUrl=https://your-api.example`. Set `API_HOST=0.0.0.0` if your hosting environment requires it. This repository does not configure a production endpoint.

Prices are shown when the provider supplies them, the listing advertises free admission, or it is an eligible library program. Other listings show **Price not listed**. The backend refreshes each feed after 15 minutes on demand. CI uses fixtures and does not depend on live providers.

Data: [Has Things (hasthings.com)](https://hasthings.com), [ODbL 1.0](https://opendatacommons.org/licenses/odbl/1-0/). Library program information: [East Baton Rouge Parish Library](https://www.ebrpl.com/events-classes/).

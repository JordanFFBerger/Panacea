# Panacea QA agent

Run all stages with `npm run qa`, or pass stage names after `--` to run a subset.

| Stage | Coverage |
| --- | --- |
| `syntax` | Browser and backend JavaScript syntax |
| `integration` | HTTP routing, filtering, and curated event validation |
| `api-e2e` | Launches the production server entry point on an ephemeral port and follows discovery through detail |
| `android-unit` | JVM unit tests and debug APK assembly |
| `android-e2e` | Compose journeys on an emulator and real HTTP parsing from the local backend |

Node 20+ suffices for the JavaScript stages. For Android, install Java 17+ and Android SDK 36; start a dedicated Android emulator for the device stage. The agent starts the API on port 3000 for that stage, which must be free. Android's debug API URL uses the emulator host alias, so a physical phone will not reach that URL without configuration. Use `PANACEA_GRADLE` to select a local Gradle executable compatible with the wrapper version.

Example: `npm run qa -- syntax integration api-e2e`. The agent reports each stage in the terminal and writes `qa-results/summary.json`. It continues after a failure so the report includes independent stages, then exits nonzero if any stage failed.

The [GitHub workflow](../.github/workflows/ci.yml) runs on pushes to main, pull requests, and manual dispatch. It publishes stage summaries, native test reports, and a debug APK. Device tests reset app preferences, so use a test emulator. The in-app player journey exercises play and pause on the bundled CC-licensed track, with no external streaming dependency. The device suite verifies both that the native client can fetch curated rows and that Discover renders a fetched event and opens its detail screen.

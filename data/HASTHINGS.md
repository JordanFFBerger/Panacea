# Baton Rouge event feed

`hasthings-baton-rouge-2026-11-05.json` is an unmodified snapshot from [Has Things' Baton Rouge JSON feed](https://baton-rouge.hasthings.com/feeds/events.json?days=60), generated November 5, 2026 at 10:30 a.m. Central. It contains 665 event occurrences in a 60-day window; the response reports `truncated: false`. Occurrences are individual showtimes, so an event may appear more than once. This is a snapshot, not a live database; events can change or be cancelled.

The listing metadata is provided under the [Open Database License 1.0](https://opendatacommons.org/licenses/odbl/1-0/). Attribution: **Data: Has Things (hasthings.com)**. The provider describes attribution and share-alike requirements for published derived databases in its [feed documentation](https://baton-rouge.hasthings.com/feeds#license). The unchanged response also contains weather data with separate Open-Meteo attribution and licensing in its `weather.source` block.

## Refreshing

Fetch the [60-day JSON feed](https://baton-rouge.hasthings.com/feeds/events.json?days=60) again to update the data. The feed is cached for 15 minutes and can contain at most 1,000 records; check `truncated` and `generatedAt`. For larger or selective queries use the documented `/api/events` endpoint with date filters and pagination. Identify the client with a descriptive User-Agent and respect HTTP 429 responses. See the [feed and API documentation](https://baton-rouge.hasthings.com/feeds) for parameters, current limits, and licensing.

## Integration notes

- Records are in `items`; `id` identifies the occurrence and `eventId` groups occurrences. `startsAt` and `endsAt` are UTC instants; format them in `city.timezone` (`America/Chicago`). When `startTimeKnown` is false, the supplied noon timestamp is a placeholder, not a real start time.
- Preserve `status`, `confidence`, `sources`, and `url`. In this snapshot 584 occurrences are single-source and one is cancelled; verify listings with the venue or ticket seller before relying on them. `ticketUrl` can be null.
- `venue` contains a name and slug, but the bulk feed does not provide street addresses or coordinates. The provider documents venue detail endpoints; missing locations must remain unknown until verified. The bulk feed does not include prices, descriptions, images, or signup counts.
- Android currently reads hard-coded `SampleEvents`; this file is not wired into the app. Its model requires relative days, prices, counts, and coordinates, so a repository and a model that supports absolute dates and missing fields are needed before integration. Do not interpret missing values as zero or invent trend counts.

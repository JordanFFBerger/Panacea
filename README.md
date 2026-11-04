# Rouge Radar

Responsive, buildless Baton Rouge event discovery skeleton for Panacea. Open `app/index.html` or run `npm start` and visit http://localhost:4173.

## Features
- At-a-glance price, date, time, venue, included activities and signup counts.
- Instant search, category/date/price filters and sorting.
- One-click event details with sample signups; no payment or real reservation.
- Saved events and plans stored in this browser via localStorage.
- Illustrative nearby map with clickable markers; not a geographic mapping service.
- Automatic America/Chicago day (7am–6pm) and night themes. Theme button cycles Auto → Day → Night.
- Trending requires ≥20 sample signups in 24 hours and ≥50% growth over the preceding 24 hours.

All listings, prices, distances and signup counts are fictional examples. Dates are relative to the current Baton Rouge date. Fonts use Google Fonts with local sans-serif fallbacks.

## Extend into a live app
Replace `app/data.js` fixtures with a verified event API and normalize price, activities, venue coordinates and start times. Track authenticated, deduplicated signups and rolling 24-hour counts on the server. Add real ticket links and cancellation handling. Replace the illustrative map with a map provider and geocoded venues. Keep price and included activities directly on discovery cards.

## Verification
Run `npm run check` for JavaScript syntax validation. Browser QA should cover keyboard navigation, search, combined filters, empty states, detail dialog, signup/undo, saved-event persistence and all three theme modes at mobile and desktop widths.

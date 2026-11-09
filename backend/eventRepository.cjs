
const fs = require("node:fs");
const path = require("node:path");

const DATA_FILE = path.join(
  __dirname,
  "..",
  "data",
  "curated-events.json"
);

function loadEvents(filePath = DATA_FILE) {
  const raw = fs.readFileSync(filePath, "utf8");
  const data = JSON.parse(raw);

  if (!Array.isArray(data.events)) {
    throw new Error("Invalid event data: events must be an array");
  }

  const ids = new Set();

  return data.events.map((event, index) => {
    const requiredFields = [
      "id",
      "title",
      "category",
      "start_local",
      "timezone",
      "time_type",
      "venue"
    ];

    for (const field of requiredFields) {
      if (
        typeof event?.[field] !== "string" ||
        !event[field].trim()
      ) {
        throw new Error(`Event ${index}: invalid ${field}`);
      }
    }

    if (ids.has(event.id)) {
      throw new Error(`Duplicate event ID: ${event.id}`);
    }
    ids.add(event.id);

    const validDate = (value) => {
      if (typeof value !== "string" ||
          !/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d+)?)?(?:Z|[+-]\d{2}:\d{2})$/.test(value) ||
          !Number.isFinite(Date.parse(value))) return false;

      const calendarDay = value.slice(0, 10);
      const midnight = new Date(calendarDay + "T00:00:00Z");
      return Number.isFinite(midnight.getTime()) &&
        midnight.toISOString().slice(0, 10) === calendarDay;
    };

    if (!validDate(event.start_local)) {
      throw new Error(`Invalid start date: ${event.id}`);
    }

    if (event.end_local != null && !validDate(event.end_local)) {
      throw new Error(`Invalid end date: ${event.id}`);
    }

    if (
      event.price_min_usd != null &&
      (typeof event.price_min_usd !== "number" ||
        !Number.isFinite(event.price_min_usd) ||
        event.price_min_usd < 0)
    ) {
      throw new Error(`Invalid price: ${event.id}`);
    }

    if (event.end_local != null && Date.parse(event.end_local) < Date.parse(event.start_local)) {
      throw new Error(`End date precedes start date: ${event.id}`);
    }

    if (!Array.isArray(event.source_urls) || event.source_urls.length === 0) {
      throw new Error(`Missing sources: ${event.id}`);
    }

    return {
      id: event.id,
      title: event.title,
      category: event.category,
      startLocal: event.start_local,
      endLocal: event.end_local,
      timezone: event.timezone,
      timeType: event.time_type,
      venue: event.venue,
      address: event.address,
      description: event.description,
      priceMinUsd: event.price_min_usd,
      ticketUrl: event.ticket_url,
      ageRestriction: event.age_restriction,
      latitude: event.latitude,
      longitude: event.longitude,
      sourceUrls: event.source_urls,
      checkedDate: event.checked_date
    };
  });
}

module.exports = { loadEvents };

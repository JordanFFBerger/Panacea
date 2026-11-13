const { toLocal, zone, cachedCatalog } = require("./liveCatalog.cjs");
const clean = value => typeof value === "string" && value.trim() ? value.trim() : null;
const coordinate = (value, max) => {
  if (typeof value !== "number" && !clean(value)) return null;
  const number = Number(value);
  return Number.isFinite(number) && Math.abs(number) <= max ? number : null;
};
function httpsUrl(value) {
  try { return new URL(value).protocol === "https:" ? value : null; }
  catch { return null; }
}

function normalize(event, now) {
  const title = clean(event?.name);
  const id = clean(event?.id);
  const venue = event?._embedded?.venues?.[0];
  const start = event?.dates?.start;
  if (!id || !title || !start || !clean(venue?.name) || event.test ||
      event.dates.status?.code === "canceled" || event.dates.status?.code === "postponed") return null;

  let startLocal;
  const instant = Date.parse(start.dateTime);
  const hasTime = Number.isFinite(instant) && !start.noSpecificTime && !start.timeTBA;
  if (hasTime) {
    if (instant < now) return null;
    startLocal = toLocal(instant);
  } else {
    const date = start.localDate;
    if (start.dateTBA || start.dateTBD || !/^\d{4}-\d{2}-\d{2}$/.test(date)) return null;
    const noon = Date.parse(date + "T12:00:00Z");
    if (!Number.isFinite(noon) || new Date(noon).toISOString().slice(0, 10) !== date ||
        date < toLocal(now).slice(0, 10)) return null;
    startLocal = toLocal(noon).replace(/T.*(?=[+-]\d{2}:\d{2}$)/, "T12:00:00");
    // Date-only listings need a calendar date, not a fabricated performance time.
  }

  const prices = (event.priceRanges ?? []).filter(range =>
    range?.currency === "USD" && Number.isFinite(range.min) && range.min >= 0).map(range => range.min);
  const segment = clean(event.classifications?.[0]?.segment?.name);
  const categories = { Music: "Music", Sports: "Sports", "Arts & Theatre": "Arts", Film: "Film" };
  const end = Date.parse(event.dates.end?.dateTime);
  const ticketUrl = httpsUrl(event.url);
  const images = Array.isArray(event.images) ? event.images : [];
  const cover = images.filter(image => image?.fallback === false && httpsUrl(image.url))
    .sort((a, b) => Number(b.ratio === "16_9") - Number(a.ratio === "16_9") ||
      Math.abs((a.width ?? 0) - 800) - Math.abs((b.width ?? 0) - 800))[0];
  return {
    id: "ticketmaster:" + id, title, category: Object.hasOwn(categories, segment) ? categories[segment] : "Other",
    venue: venue.name.trim(), startLocal, endLocal: hasTime && end >= instant ? toLocal(end) : null,
    timezone: zone, timeType: hasTime ? "performance" : "date_only",
    address: [venue.address?.line1, venue.city?.name, venue.state?.stateCode, venue.postalCode].map(clean).filter(Boolean).join(", ") || null,
    description: clean(event.info) ?? clean(event.pleaseNote),
    priceMinUsd: prices.length ? Math.min(...prices) : null,
    ticketUrl, imageUrl: cover?.url ?? null, ageRestriction: null,
    latitude: coordinate(venue.location?.latitude, 90), longitude: coordinate(venue.location?.longitude, 180),
    sourceUrls: ticketUrl ? [ticketUrl] : [], listingUrl: ticketUrl, sourceName: "Ticketmaster", checkedDate: toLocal(now).slice(0, 10)
  };
}

function createTicketmasterCatalog({ apiKey, fetchPage = globalThis.fetch, clock = Date.now } = {}) {
  async function refresh() {
    if (!apiKey?.trim()) throw new Error("Ticketmaster key missing");
    const now = clock();
    const signal = AbortSignal.timeout(3500);
    const events = new Map();
    const params = new URLSearchParams({
      apikey: apiKey.trim(), geoPoint: "9vrjhzx", radius: "25", unit: "miles",
      countryCode: "US", startDateTime: new Date(now).toISOString().replace(/\.\d+Z$/, "Z"),
      size: "100", sort: "date,asc"
    });
    for (let page = 0; page < 5; page++) {
      params.set("page", String(page));
      const response = await fetchPage("https://app.ticketmaster.com/discovery/v2/events.json?" + params, {
        signal, redirect: "error"
      });
      if (!response.ok) throw new Error("Ticketmaster request failed");
      const body = await response.json();
      const items = body?._embedded?.events ?? (body?.page?.totalElements === 0 ? [] : null);
      if (!Array.isArray(items) || !Number.isInteger(body?.page?.totalPages) || body.page.totalPages < 0) {
        throw new Error("Invalid Ticketmaster event response");
      }
      for (const item of items) {
        const event = normalize(item, now);
        if (event) events.set(event.id, event);
      }
      if (page + 1 >= body.page.totalPages) break;
    }
    return [...events.values()];
  }

  return cachedCatalog(refresh, clock);
}

module.exports = { createTicketmasterCatalog };

const { toLocal, zone, cachedCatalog } = require("./liveCatalog.cjs");
const feed = "https://baton-rouge.hasthings.com/feeds/events.json";
const library = "East Baton Rouge Parish Library";
const libraryCalendar = "https://www.ebrpl.com/events-classes/";
const categories = { music: "Music", "dj-party": "Music", art: "Arts", theatre: "Arts",
  comedy: "Comedy", film: "Film", talk: "Talks", "food-drink": "Food", community: "Community", sports: "Sports" };

function httpsUrl(value) {
  try { return new URL(value).protocol === "https:" ? value : null; }
  catch { return null; }
}

function normalize(item) {
  if (!["scheduled", "sold-out"].includes(item?.status) ||
      ![item.id, item.title, item.venue?.name].every(value => typeof value === "string" && value.trim())) return null;
  const instant = Date.parse(item.startsAt);
  const listingUrl = httpsUrl(item.url);
  if (!Number.isFinite(instant) || !listingUrl) return null;
  const end = Date.parse(item.endsAt);
  const ticketUrl = httpsUrl(item.ticketUrl);
  const title = item.title.trim();
  const fromLibrary = Array.isArray(item.sources) && item.sources.includes(library);
  const explicitlyFree = /^(?:\[free\]|\(free\))\s|\bfree (?:admission|entry)\b|\((?:free|no cover)\)\s*$/i.test(title);
  const freeLibraryProgram = fromLibrary && !ticketUrl && !/\binformational only\b/i.test(title);
  const primary = Object.hasOwn(categories, item.category) ? categories[item.category] : "Other";
  const tags = new Set(Array.isArray(item.tags)
    ? item.tags.filter(tag => tag?.kind === "genre").map(tag => tag.tag) : []);
  const nightclub = /\bnight[\s-]*club\b/i.test(item.venue.name);
  const screening = /\b(screening|movie night|film festival|watch party)\b/i.test(title) || tags.has("movies-film");
  const category = nightclub && !screening ? "Nightclubs" : ["Community", "Other"].includes(primary)
    ? tags.has("book-club") || tags.has("author-event") || /\b(book club|story\s?time)\b/i.test(title) ? "Books"
      : tags.has("outdoor") || tags.has("neighborhood-cleanup") ? "Outdoors"
      : tags.has("games") ? "Gaming"
      : tags.has("food-and-beverage") ? "Food"
      : tags.has("arts-and-culture") || tags.has("crafts") ? "Arts"
      : primary
    : primary;
  return {
    id: "hasthings:" + item.id, title, venue: item.venue.name, category,
    startLocal: toLocal(instant), endLocal: end >= instant ? toLocal(end) : null,
    timezone: zone, timeType: item.startTimeKnown === true ? "performance" : "date_only",
    address: null, description: null, latitude: null, longitude: null,
    ageRestriction: typeof item.ageRestriction === "string" ? item.ageRestriction : null,
    priceMinUsd: explicitlyFree || freeLibraryProgram ? 0 : null,
    ticketUrl, sourceUrls: [listingUrl, ...(fromLibrary ? [libraryCalendar] : [])],
    sourceName: "Has Things", listingUrl
  };
}

function createCommunityCatalog({ fetchFeed = globalThis.fetch, clock = Date.now } = {}) {
  return cachedCatalog(async () => {
    const response = await fetchFeed(feed, { signal: AbortSignal.timeout(3500),
      headers: { "User-Agent": "Panacea/1.0 (+https://github.com/JordanFFBerger/Panacea)" } });
    if (!response.ok) throw new Error("Community feed request failed");
    const body = await response.json();
    if (body?.city?.slug !== "baton-rouge" || !Array.isArray(body?.items)) throw new Error("Invalid community feed");
    const events = new Map();
    for (const item of body.items) {
      const event = normalize(item);
      if (event) events.set(event.id, event);
    }
    return [...events.values()];
  }, clock);
}

module.exports = { createCommunityCatalog };

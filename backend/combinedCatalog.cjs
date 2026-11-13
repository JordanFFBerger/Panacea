const { createCommunityCatalog } = require("./communityCatalog.cjs");
const { createTicketmasterCatalog } = require("./ticketmaster.cjs");

function normalizedTitle(title) {
  return title.normalize("NFKC").toLowerCase().replace(/[^\p{L}\p{N}]+/gu, " ").trim();
}

function ticketKey(value) {
  if (!value) return null;
  const url = new URL(value);
  return url.hostname.toLowerCase().replace(/^www\./, "") + url.pathname.replace(/\/$/, "");
}

function createCombinedCatalog({ community = createCommunityCatalog(), ticketmaster } = {}) {
  const providers = [community, ...(ticketmaster ? [ticketmaster] : [])];
  return async () => {
    const result = await Promise.allSettled(providers.map(provider => provider()));
    if (!result.some(source => source.status === "fulfilled")) throw new Error("All event sources unavailable");

    const communityEvents = result[0].value ?? [];
    const paid = result[1]?.value ?? [];
    const ticketKeys = new Map();
    for (const event of paid) {
      const key = ticketKey(event.ticketUrl);
      if (key) ticketKeys.set(key + "|" + event.startLocal, event);
    }
    const used = new Set();
    const merged = communityEvents.map(event => {
      const key = ticketKey(event.ticketUrl);
      const match = key && ticketKeys.get(key + "|" + event.startLocal);
      // Match the ticket URL, title and showtime to avoid merging matinee and evening shows.
      if (!match || used.has(match.id) || event.startLocal !== match.startLocal ||
          normalizedTitle(event.title) !== normalizedTitle(match.title)) return event;
      used.add(match.id);
      return { ...match, ...event,
        imageUrl: event.imageUrl ?? match.imageUrl, description: event.description ?? match.description,
        address: event.address ?? match.address,
        latitude: event.latitude ?? match.latitude, longitude: event.longitude ?? match.longitude,
        priceMinUsd: event.priceMinUsd ?? match.priceMinUsd,
        sourceUrls: [...new Set([...event.sourceUrls, ...match.sourceUrls])]
      };
    });
    return [...merged, ...paid.filter(event => !used.has(event.id))]
      .sort((a, b) => Date.parse(a.startLocal) - Date.parse(b.startLocal));
  };
}

function createLiveCatalog(apiKey = process.env.TICKETMASTER_API_KEY) {
  return createCombinedCatalog({ ticketmaster: apiKey?.trim() ? createTicketmasterCatalog({ apiKey }) : undefined });
}

module.exports = { createLiveCatalog, createCombinedCatalog };

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { createTicketmasterCatalog } = require("./ticketmaster.cjs");
const { createCombinedCatalog } = require("./combinedCatalog.cjs");

const show = {
  id: "show1", name: "Live Jazz", url: "https://www.ticketmaster.com/event/show1",
  dates: { start: { dateTime: "2026-11-14T01:30:00Z" } },
  _embedded: { venues: [{ name: "Main Hall" }] },
  classifications: [{ segment: { name: "Music" } }],
  priceRanges: [{ currency: "USD", min: 20 }, { currency: "CAD", min: 1 }]
};

test("optional Ticketmaster source fetches pages, translates time and preserves US pricing", async () => {
  const pages = [];
  const getEvents = createTicketmasterCatalog({
    apiKey: "test-key", clock: () => Date.parse("2026-11-13T12:00:00Z"),
    fetchPage: async url => {
      const params = new URL(url).searchParams;
      assert.equal(params.get("radius"), "25");
      assert.equal(params.get("geoPoint"), "9vrjhzx");
      pages.push(params.get("page"));
      return { ok: true, json: async () => ({ page: { totalPages: 2 },
        _embedded: { events: [show] } }) };
    }
  });
  const events = await getEvents();
  assert.deepEqual(pages, ["0", "1"]);
  assert.equal(events.length, 1);
  assert.equal(events[0].startLocal, "2026-11-13T19:30:00-06:00");
  assert.equal(events[0].priceMinUsd, 20);
});

test("community events remain available when Ticketmaster fails", async () => {
  const event = { id: "community-1", priceMinUsd: 0 };
  const getEvents = createCombinedCatalog({ community: async () => [event],
    ticketmaster: async () => { throw new Error("rate limited"); } });
  assert.deepEqual(await getEvents(), [event]);
});

test("a matched show gets known pricing without hiding different showtimes", async () => {
  const startLocal = "2026-11-13T19:30:00-06:00";
  const entry = { title: show.name, startLocal, ticketUrl: show.url, sourceUrls: [show.url] };
  const getEvents = createCombinedCatalog({
    community: async () => [{ ...entry, id: "community", ticketUrl: show.url + "?ref=feed", priceMinUsd: null }],
    ticketmaster: async () => [{ ...entry, id: "paid", priceMinUsd: 20 },
      { ...entry, id: "matinee", startLocal: "2026-11-13T14:30:00-06:00", priceMinUsd: 15 }]
  });
  assert.deepEqual((await getEvents()).map(e => [e.id, e.priceMinUsd]), [["matinee", 15], ["community", 20]]);
});

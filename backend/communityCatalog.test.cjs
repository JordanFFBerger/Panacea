const { test } = require("node:test");
const assert = require("node:assert/strict");
const { createCommunityCatalog } = require("./communityCatalog.cjs");
const { createServer } = require("./apiServer.cjs");
const day = Date.parse("2026-11-13T12:00:00Z");
const item = {
  id: "club-1", title: "Evening Book Club", category: "community", status: "scheduled",
  startsAt: "2026-11-14T01:00:00Z", startTimeKnown: true,
  venue: { name: "Main Library" }, url: "https://baton-rouge.hasthings.com/events/evening-book-club",
  sources: ["East Baton Rouge Parish Library"]
};
const feed = (...items) => ({ ok: true, json: async () => ({ city: { slug: "baton-rouge" }, items }) });

test("community feed flows through HTTP search with local date, free price, and a source", async t => {
  const server = createServer({ getEvents: createCommunityCatalog({
    clock: () => day, fetchFeed: async () => feed(item)
  }) });
  await new Promise(resolve => server.listen(0, "127.0.0.1", resolve));
  t.after(() => server.close());
  const base = "http://127.0.0.1:" + server.address().port;
  const { count, events: [event] } = await (await fetch(base + "/api/events?category=Books&date=2026-11-13&q=evening")).json();
  assert.equal(count, 1);
  assert.equal(event.startLocal, "2026-11-13T19:00:00-06:00");
  assert.equal(event.priceMinUsd, 0);
  assert.equal(event.listingUrl, item.url);
  const detail = await fetch(base + "/api/events/" + encodeURIComponent(event.id));
  assert.deepEqual((await detail.json()).event, event);
});

test("unknown prices stay unknown and canceled or expired listings disappear", async () => {
  const getEvents = createCommunityCatalog({ clock: () => day, fetchFeed: async () => feed(
    { ...item, id: "a", title: 'Band "Free" album release', sources: [], startTimeKnown: false },
    { ...item, id: "b", title: "Craft Fair (Free)", sources: [] },
    { ...item, id: "c", status: "cancelled" },
    { ...item, id: "d", startsAt: "2026-11-11T18:00:00Z" }
  ) });
  const events = await getEvents();
  assert.deepEqual(events.map(e => [e.id, e.priceMinUsd, e.timeType]), [
    ["hasthings:a", null, "date_only"], ["hasthings:b", 0, "performance"]
  ]);
});

test("concurrent requests share a refresh and a provider error retains the last good response", async () => {
  let calls = 0, now = day;
  const getEvents = createCommunityCatalog({ clock: () => now,
    fetchFeed: async () => ++calls === 1 ? feed(item) : { ok: false } });
  await Promise.all([getEvents(), getEvents()]);
  assert.equal(calls, 1);
  now += 16 * 60_000;
  assert.equal((await getEvents()).length, 1);
  assert.equal(calls, 2);
  now += 6 * 60 * 60_000;
  await assert.rejects(getEvents(), /catalog unavailable/);
});

test("bad upstream response is HTTP 503 rather than a pretend empty calendar", async t => {
  const server = createServer({ getEvents: createCommunityCatalog({
    fetchFeed: async () => ({ ok: true, json: async () => ({}) })
  }) });
  await new Promise(resolve => server.listen(0, "127.0.0.1", resolve));
  t.after(() => server.close());
  assert.equal((await fetch("http://127.0.0.1:" + server.address().port + "/api/events")).status, 503);
});

test("community tags expose activities without overriding specific provider categories", async () => {
  const tags = ["outdoor", "games", "food-and-beverage", "crafts"];
  const rows = tags.map(tag => ({ ...item, id: tag, title: "Local activity", tags: [{ kind: "genre", tag }] }));
  rows.push({ ...rows[3], id: "film", category: "film" });
  const getEvents = createCommunityCatalog({ clock: () => day, fetchFeed: async () => feed(...rows) });
  assert.deepEqual((await getEvents()).map(event => event.category), ["Outdoors", "Gaming", "Food", "Arts", "Film"]);
});

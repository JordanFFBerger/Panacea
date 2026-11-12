import assert from "node:assert/strict";
import { after, before, test } from "node:test";
import { startApi } from "./api-process.mjs";

let api;
before(async () => { api = await startApi(); });
after(async () => { await api?.stop(); });
const request = path => fetch(api.baseUrl + path, { signal: AbortSignal.timeout(5000) });

test("a client can discover a concert and fetch the same event details", async () => {
  const health = await request("/api/health");
  assert.equal(health.status, 200);
  const search = await request("/api/events?category=Music&q=Mariachi&date=2026-11-06");
  assert.equal(search.status, 200);
  const { count, events } = await search.json();
  assert.equal(count, 1);
  const detail = await request("/api/events/" + encodeURIComponent(events[0].id));
  assert.equal(detail.status, 200);
  assert.deepEqual((await detail.json()).event, events[0]);
  assert.ok(events[0].sourceUrls.length > 0);
});

test("a client can distinguish free, paid and unknown prices in the full catalog", async () => {
  const response = await request("/api/events");
  assert.equal(response.status, 200);
  const { events } = await response.json();
  assert.ok(events.some(e => e.priceMinUsd === 0));
  assert.ok(events.some(e => e.priceMinUsd > 0));
  assert.ok(events.some(e => e.priceMinUsd === null));
});

test("an invalid filter does not break the next valid search", async () => {
  const invalid = await request("/api/events?date=2026-02-30");
  assert.equal(invalid.status, 400);
  assert.match((await invalid.json()).error, /Invalid date/);
  const valid = await request("/api/events?q=Bingo");
  assert.equal(valid.status, 200);
  const { count, events } = await valid.json();
  assert.equal(count, 2);
  assert.equal(new Set(events.map(e => e.id)).size, count);
});

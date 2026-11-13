
const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");

const { loadEvents } = require("./eventRepository.cjs");
const { createServer } = require("./apiServer.cjs");

const events = loadEvents();
const server = createServer({ getEvents: async () => events });
let baseUrl;

// Start a separate test server on a free port.
before(async () => {
  await new Promise((resolve) => {
    server.listen(0, "127.0.0.1", resolve);
  });

  const port = server.address().port;
  baseUrl = `http://127.0.0.1:${port}`;
});

after(async () => {
  await new Promise((resolve, reject) => {
    server.close((error) => {
      if (error) reject(error);
      else resolve();
    });
  });
});

// Test 1: The repository loads real event data.
test("loads curated events", () => {
  const events = loadEvents();

  assert.ok(events.length > 0);
  assert.ok(events.every((e) => e.id && e.title));
});

// Test 2: Health endpoint responds correctly.
test("GET /api/health returns OK", async () => {
  const response = await fetch(`${baseUrl}/api/health`);
  const data = await response.json();

  assert.equal(response.status, 200);
  assert.equal(data.status, "ok");
});

// Test 3: API returns the complete event list.
test("GET /api/events returns events", async () => {
  const response = await fetch(`${baseUrl}/api/events`);
  const data = await response.json();

  assert.equal(response.status, 200);
  assert.equal(data.count, loadEvents().length);
  assert.equal(data.events.length, data.count);
});

// Test 4: Category filtering works.
test("filters events by category", async () => {
  const response = await fetch(
    `${baseUrl}/api/events?category=Music`
  );
  const data = await response.json();

  assert.equal(response.status, 200);
  assert.ok(data.count > 0);
  assert.ok(data.events.every((e) => e.category === "Music"));
});

// Test 5: Both Bingo performances are returned.
test("search finds both Bingo events", async () => {
  const response = await fetch(
    `${baseUrl}/api/events?q=Bingo`
  );
  const data = await response.json();

  assert.equal(response.status, 200);
  assert.equal(data.count, 2);
});

// Test 6: Individual event lookup works.
test("retrieves a specific event", async () => {
  const id = "weekend-kick-off-2026-11-05";

  const response = await fetch(
    `${baseUrl}/api/events/${id}`
  );
  const data = await response.json();

  assert.equal(response.status, 200);
  assert.equal(data.event.id, id);
});

// Test 7: Unknown events return 404.
test("returns 404 for an unknown event", async () => {
  const response = await fetch(
    `${baseUrl}/api/events/does-not-exist`
  );

  assert.equal(response.status, 404);
});

// Test 8: Invalid dates return 400.
test("rejects invalid date filters", async () => {
  const response = await fetch(
    `${baseUrl}/api/events?date=2026-99-99`
  );

  assert.equal(response.status, 400);
});

// Test 9: Unsupported methods return 405.
test("rejects unsupported HTTP methods", async () => {
  const response = await fetch(`${baseUrl}/api/events`, {
    method: "POST"
  });

  assert.equal(response.status, 405);
});

// Test 10: Unknown prices remain unknown.
test("preserves missing price information", () => {
  const events = loadEvents();
  const event = events.find(
    (e) => e.id === "weekend-kick-off-2026-11-05"
  );

  assert.ok(event);
  assert.equal(event.priceMinUsd, null);
});

// Helper for testing invalid JSON records.
function testInvalidData(modify, expectedError) {
  const folder = fs.mkdtempSync(
    path.join(os.tmpdir(), "panacea-test-")
  );

  try {
    const original = JSON.parse(
      fs.readFileSync(
        path.join(__dirname, "../data/curated-events.json"),
        "utf8"
      )
    );

    modify(original.events);

    const file = path.join(folder, "events.json");
    fs.writeFileSync(file, JSON.stringify(original));

    assert.throws(() => loadEvents(file), expectedError);
  } finally {
    fs.rmSync(folder, { recursive: true, force: true });
  }
}

// Test 11: Reject duplicate IDs.
test("rejects duplicate event IDs", () => {
  testInvalidData(
    (events) => events.push({ ...events[0] }),
    /Duplicate event ID/
  );
});

// Test 12: Reject missing titles.
test("rejects missing event titles", () => {
  testInvalidData(
    (events) => { events[0].title = ""; },
    /invalid title/
  );
});

// Test 13: Reject invalid start dates.
test("rejects invalid event timestamps", () => {
  testInvalidData(
    (events) => { events[0].start_local = "invalid"; },
    /Invalid start date/
  );
});

test("combines case-insensitive category, search and date filters", async () => {
  const params = new URLSearchParams({ category: " mUsIc ", q: "  mArIaChI ", date: "2026-11-06" });
  const response = await fetch(`${baseUrl}/api/events?${params}`);
  assert.equal(response.status, 200);
  const result = await response.json();
  assert.equal(result.count, 1);
  assert.deepEqual(result.events.map(e => e.id), ["mariachi-heritage-2026"]);
});

test("filters evening events by Baton Rouge calendar date, not UTC date", async () => {
  const response = await fetch(`${baseUrl}/api/events?date=2026-11-05`);
  assert.equal(response.status, 200);
  const result = await response.json();
  assert.ok(result.events.some(e => e.id === "weekend-kick-off-2026-11-05"));
  assert.ok(result.events.every(e => e.startLocal.startsWith("2026-11-05T")));
});

test("returns an empty result for a valid search with no matches", async () => {
  const response = await fetch(`${baseUrl}/api/events?q=nonexistent-rouge-radar-test-event`);
  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), { count: 0, events: [] });
});

test("rejects impossible dates that JavaScript would roll into the next month", async () => {
  const response = await fetch(`${baseUrl}/api/events?date=2026-02-29`);
  assert.equal(response.status, 400);
  assert.match((await response.json()).error, /Invalid date/);
});

test("reports malformed percent-encoded IDs as bad requests", async () => {
  const response = await fetch(`${baseUrl}/api/events/%ZZ`);
  assert.equal(response.status, 400);
  assert.deepEqual(await response.json(), { error: "Invalid event ID" });
});

test("does not accept an encoded slash inside an event ID", async () => {
  const response = await fetch(`${baseUrl}/api/events/part%2Fother`);
  assert.equal(response.status, 404);
  assert.deepEqual(await response.json(), { error: "Event not found" });
});

test("preserves zero as an explicit free price in the API", async () => {
  const response = await fetch(`${baseUrl}/api/events/white-light-night-2026`);
  assert.equal(response.status, 200);
  assert.equal((await response.json()).event.priceMinUsd, 0);
});

test("rejects an impossible calendar date in an event timestamp", () => {
  testInvalidData(events => {
    events[0].start_local = "2026-02-29T19:00:00-06:00";
  }, /Invalid start date/);
});

test("rejects an event ending before it starts even with different offsets", () => {
  testInvalidData(events => {
    events[0].end_local = "2026-11-05T23:00:00Z";
  }, /End date precedes start date/);
});

test("rejects records without any source URLs", () => {
  testInvalidData(events => {
    events[0].source_urls = [];
  }, /Missing sources/);
});

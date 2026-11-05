
const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");

const { loadEvents } = require("./eventRepository.cjs");
const { server } = require("./apiServer.cjs");

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

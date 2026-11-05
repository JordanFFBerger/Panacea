
const http = require("node:http");
const { loadEvents } = require("./eventRepository.cjs");

const PORT = Number(process.env.API_PORT || 3000);
const HOST = "127.0.0.1";

if (!Number.isInteger(PORT) || PORT < 1 || PORT > 65535) {
  throw new Error("Invalid API port");
}

const events = loadEvents();

function sendJson(res, status, data) {
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store"
  });

  res.end(JSON.stringify(data));
}

function validDate(date) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return false;

  const parsed = new Date(`${date}T00:00:00Z`);

  return (
    !Number.isNaN(parsed.getTime()) &&
    parsed.toISOString().slice(0, 10) === date
  );
}

function filterEvents(params) {
  const category = (params.get("category") || "").trim().toLowerCase();
  const query = (params.get("q") || "").trim().toLowerCase();
  const date = params.get("date");

  if (date !== null && !validDate(date)) {
    return { error: "Invalid date. Use YYYY-MM-DD." };
  }

  const filtered = events.filter((event) => {
    const matchesCategory =
      !category || event.category.toLowerCase() === category;

    const searchableText = [
      event.title,
      event.category,
      event.venue,
      event.address,
      event.description
    ].filter(Boolean).join(" ").toLowerCase();

    const matchesQuery =
      !query || searchableText.includes(query);

    const matchesDate =
      date === null || event.startLocal.slice(0, 10) === date;

    return matchesCategory && matchesQuery && matchesDate;
  });

  return {
    count: filtered.length,
    events: filtered
  };
}

const server = http.createServer((req, res) => {
  let url;

  try {
    url = new URL(req.url, "http://localhost");
  } catch {
    return sendJson(res, 400, {
      error: "Invalid request URL"
    });
  }

  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    return sendJson(res, 405, {
      error: "Method not allowed"
    });
  }

  if (url.pathname === "/api/health") {
    return sendJson(res, 200, {
      status: "ok"
    });
  }

  if (url.pathname === "/api/events") {
    const result = filterEvents(url.searchParams);

    if (result.error) {
      return sendJson(res, 400, result);
    }

    return sendJson(res, 200, result);
  }

  if (url.pathname.startsWith("/api/events/")) {
    const rawId = url.pathname.slice("/api/events/".length);
    let id;

    try {
      id = decodeURIComponent(rawId);
    } catch {
      return sendJson(res, 400, {
        error: "Invalid event ID"
      });
    }

    if (!id || id.includes("/")) {
      return sendJson(res, 404, {
        error: "Event not found"
      });
    }

    const event = events.find((item) => item.id === id);

    if (!event) {
      return sendJson(res, 404, {
        error: "Event not found"
      });
    }

    return sendJson(res, 200, { event });
  }

  return sendJson(res, 404, {
    error: "Endpoint not found"
  });
});


if (require.main === module) {
  server.listen(PORT, HOST, () => {
    console.log(`Panacea API running at http://${HOST}:${PORT}`);
    console.log(`Loaded ${events.length} events`);
  });
}

module.exports = { server };


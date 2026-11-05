
const http = require("node:http");
const { loadEvents } = require("./eventRepository.cjs");

const PORT = Number(process.env.API_PORT || 3000);
const HOST = "127.0.0.1";

if (!Number.isInteger(PORT) || PORT < 1 || PORT > 65535) {
  throw new Error("Invalid API port");
}

// Validate and load the events when the server starts.
const events = loadEvents();

function sendJson(res, status, data) {
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store"
  });

  res.end(JSON.stringify(data));
}

const server = http.createServer((req, res) => {
  let pathname;

  try {
    pathname = new URL(req.url, "http://localhost").pathname;
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

  if (pathname === "/api/health") {
    return sendJson(res, 200, {
      status: "ok"
    });
  }

  if (pathname === "/api/events") {
    return sendJson(res, 200, {
      count: events.length,
      events: events
    });
  }

  return sendJson(res, 404, {
    error: "Endpoint not found"
  });
});

server.listen(PORT, HOST, () => {
  console.log(`Panacea API running at http://${HOST}:${PORT}`);
  console.log(`Loaded ${events.length} events`);
});

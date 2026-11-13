const allowed = /^https:\/\/www\.eventbrite\.com\/e\/[a-z0-9-]+-tickets-\d+$/i;
const eventType = /(?:^|[\/#])\w*Event$/;

function listedPrice(html, startLocal) {
  const start = Date.parse(startLocal);
  if (!Number.isFinite(start)) return null;
  const prices = [];
  for (const script of html.matchAll(/<script\b[^>]*\btype\s*=\s*["']application\/ld\+json["'][^>]*>([\s\S]*?)<\/script\s*>/gi)) {
    let data;
    try { data = JSON.parse(script[1]); } catch { continue; }
    const nodes = Array.isArray(data) ? data : [data, ...(Array.isArray(data?.["@graph"]) ? data["@graph"] : [])];
    for (const node of nodes) {
      if (![node?.["@type"]].flat().some(type => typeof type === "string" && eventType.test(type)) ||
          Math.abs(Date.parse(node.startDate) - start) >= 60_000 || !Number.isFinite(Date.parse(node.startDate))) continue;
      for (const offer of [node.offers].flat()) {
        const raw = offer?.lowPrice ?? offer?.price;
        if (offer?.priceCurrency !== "USD" || /SoldOut|Discontinued/i.test(offer.availability ?? "") ||
            !/^\d+(?:\.\d{1,2})?$/.test(String(raw))) continue;
        const price = Number(raw);
        if (Number.isFinite(price)) prices.push(price);
      }
    }
  }
  return prices.length ? Math.min(...prices) : null;
}

function createPriceLookup(fetchPage = globalThis.fetch) {
  const cache = new Map();
  let active = 0;
  function lookup(event) {
    if (event.priceMinUsd != null || event.timeType === "date_only" || !event.ticketUrl) return;
    const url = event.ticketUrl.split(/[?#]/, 1)[0];
    if (!allowed.test(url)) return;
    const key = url + "|" + event.startLocal;
    return { url, key };
  }

  function cached(event) {
    const entry = cache.get(lookup(event)?.key);
    return entry?.expires > Date.now() && entry.price != null
      ? { ...event, priceMinUsd: entry.price } : event;
  }

  async function resolve(event) {
    const match = lookup(event);
    if (!match) return event;
    const { url, key } = match;
    const existing = cache.get(key);
    if (existing?.pending) {
      const price = await existing.pending;
      return price === null ? event : { ...event, priceMinUsd: price };
    }
    if (existing?.expires > Date.now() || active >= 3) return cached(event);
    active++;
    const pending = (async () => {
      let price = null;
      try {
        const response = await fetchPage(url, { signal: AbortSignal.timeout(3000), redirect: "error" });
        if (response.ok && response.body) {
          let html = "";
          const reader = response.body.getReader();
          const decoder = new TextDecoder();
          try {
            while (html.length < 1_000_000) {
              const { done, value } = await reader.read();
              if (done) break;
              html += decoder.decode(value, { stream: true });
            }
            price = listedPrice(html, event.startLocal);
          } finally {
            await reader.cancel();
          }
        }
      } catch {
        // An unavailable ticket page must not prevent opening the event.
      } finally {
        active--;
        cache.delete(key);
        if (cache.size < 500) cache.set(key, { price, expires: Date.now() + 15 * 60_000 });
      }
      return price;
    })();
    cache.set(key, { pending });
    const price = await pending;
    return price === null ? event : { ...event, priceMinUsd: price };
  }

  return { cached, resolve };
}

module.exports = { createPriceLookup, listedPrice };

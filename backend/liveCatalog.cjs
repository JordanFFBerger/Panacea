const zone = "America/Chicago";
const localClock = new Intl.DateTimeFormat("en-CA", {
  timeZone: zone, year: "numeric", month: "2-digit", day: "2-digit",
  hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23",
  timeZoneName: "longOffset"
});
function toLocal(instant) {
  const p = Object.fromEntries(localClock.formatToParts(instant).map(({type, value}) => [type, value]));
  return p.year + "-" + p.month + "-" + p.day + "T" +
    p.hour + ":" + p.minute + ":" + p.second + p.timeZoneName.slice(3);
}

function cachedCatalog(refresh, clock = Date.now) {
  let snapshot, fetchedAt, retryAt = -Infinity, pending;
  return async function getEvents() {
    const now = clock();
    if ((!snapshot || now - fetchedAt >= 15 * 60_000) && now >= retryAt) {
      pending ??= Promise.resolve().then(refresh).then(events => {
        snapshot = events;
        fetchedAt = clock();
      }).catch(() => { retryAt = clock() + 60_000; }).finally(() => { pending = undefined; });
      await pending;
    }
    if (!snapshot || now - fetchedAt > 6 * 60 * 60_000) throw new Error("Live event catalog unavailable");
    const today = toLocal(now).slice(0, 10);
    return snapshot.filter(event => event.timeType === "date_only"
      ? event.startLocal.slice(0, 10) >= today : Date.parse(event.endLocal ?? event.startLocal) >= now);
  };
}

module.exports = { toLocal, zone, cachedCatalog };

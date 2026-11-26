export function fetchParkingPrice(zoneId, durationMinutes) {
  const query = new URLSearchParams({ durationMinutes: String(durationMinutes) })
  return fetch(`/api/parking-zones/${encodeURIComponent(zoneId)}/price?${query}`, {
    headers: { Accept: 'application/json' },
  })
}

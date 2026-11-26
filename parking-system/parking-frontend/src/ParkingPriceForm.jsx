import { useState } from 'react'
import { fetchParkingPrice } from './parkingPrice.js'

export default function ParkingPriceForm({ zones, numberFormat }) {
  const [zoneId, setZoneId] = useState('')
  const [duration, setDuration] = useState('')
  const [result, setResult] = useState(null)
  const loading = result?.status === 'loading'

  async function requestPrice(event) {
    event.preventDefault()
    setResult({ status: 'loading' })

    try {
      const response = await fetchParkingPrice(zoneId, Number(duration))

      if (response.status === 400) {
        const zone = zones.find(({ id }) => String(id) === zoneId)
        setResult({
          status: 'error',
          message: `Zona ne prihvata uneto trajanje. Unesite najmanje ${zone.minDurationMinutes} min. Efektivno trajanje koje server obračuna sme biti najviše ${zone.maxDurationMinutes} min.`,
        })
        return
      }

      if (response.status === 404) {
        setResult({ status: 'error', message: 'Izabrana zona više nije dostupna. Osvežite stranicu.' })
        return
      }

      if (!response.ok) throw new Error('Zahtev nije uspeo')

      const price = await response.json()
      setResult({ status: 'success', price })
    } catch {
      setResult({
        status: 'error',
        message: 'Obračun trenutno nije dostupan. Pokušajte ponovo.',
      })
    }
  }

  return (
    <section className="price-panel" aria-labelledby="price-heading">
      <h2 id="price-heading">Obračun cene parkiranja</h2>
      <form className="price-form" onSubmit={requestPrice}>
        <label>
          Parking zona
          <select
            required
            disabled={loading}
            value={zoneId}
            onChange={(event) => {
              setZoneId(event.target.value)
              setResult(null)
            }}
          >
            <option value="">Izaberite zonu</option>
            {zones.map((zone) => <option key={zone.id} value={zone.id}>{zone.name}</option>)}
          </select>
        </label>
        <label>
          Trajanje u minutima
          <input
            type="number"
            inputMode="numeric"
            min="1"
            max="2147483647"
            step="1"
            required
            disabled={loading}
            value={duration}
            onChange={(event) => {
              setDuration(event.target.value)
              setResult(null)
            }}
          />
        </label>
        <button type="submit" disabled={loading}>Izračunaj cenu</button>
      </form>

      <div role="status">
        {loading && <p className="price-feedback">Obračun cene je u toku…</p>}
        {result?.status === 'success' && (
          <dl className="zone-duration price-result">
            <div>
              <dt>Traženo trajanje</dt>
              <dd>{result.price.durationMinutes} min</dd>
            </div>
            <div>
              <dt>Efektivno trajanje</dt>
              <dd>{result.price.effectiveDurationMinutes} min</dd>
            </div>
            <div>
              <dt>Naplativi sati</dt>
              <dd>{result.price.billableHours}</dd>
            </div>
            <div>
              <dt>Ukupna cena</dt>
              <dd>{numberFormat.format(result.price.totalPrice)} {result.price.currency}</dd>
            </div>
          </dl>
        )}
      </div>
      {result?.status === 'error' && (
        <p className="notice notice-error price-feedback" role="alert">{result.message}</p>
      )}
    </section>
  )
}

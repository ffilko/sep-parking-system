import { useEffect, useState } from 'react'
import { Link, useNavigate, useOutletContext, useParams } from 'react-router'
import { fetchParkingPrice } from './parkingPrice.js'

const statusLabels = {
  PENDING_PAYMENT: 'Čeka potvrdu plaćanja',
  PAID: 'Plaćeno',
  PAYMENT_FAILED: 'Plaćanje nije uspelo',
}

const sessionDate = new Intl.DateTimeFormat('sr-Latn-RS', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'Europe/Belgrade',
})

function PriceDetails({ price, numberFormat }) {
  return (
    <>
      <div><dt>Traženo trajanje</dt><dd>{price.durationMinutes} min</dd></div>
      <div><dt>Efektivno trajanje</dt><dd>{price.effectiveDurationMinutes} min</dd></div>
      <div><dt>Naplativi sati</dt><dd>{price.billableHours}</dd></div>
      <div><dt>Ukupan iznos</dt><dd>{numberFormat.format(price.totalPrice)} {price.currency}</dd></div>
    </>
  )
}

export default function ParkingOrder({ zones, numberFormat }) {
  const { token, list: vehicles, onUnauthorized } = useOutletContext()
  const navigate = useNavigate()
  const [preview, setPreview] = useState(null)
  const [operation, setOperation] = useState(null)
  const [error, setError] = useState('')
  const authorization = { Authorization: `Bearer ${token}`, Accept: 'application/json' }
  const ready = zones.status === 'success' && zones.zones.length > 0
    && vehicles.status === 'success' && vehicles.vehicles.length > 0

  async function submit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const page = form.closest('main')
    const fields = new FormData(form)
    const zoneId = Number(fields.get('zoneId'))
    const vehicleId = Number(fields.get('vehicleId'))
    const durationMinutes = Number(fields.get('durationMinutes'))
    const zone = zones.zones.find(({ id }) => id === zoneId)
    const previewOnly = event.nativeEvent.submitter?.value !== 'create'

    setPreview(null)
    setError('')
    setOperation(previewOnly ? 'Obračun cene je u toku…' : 'Kreiranje parking naloga…')

    try {
      const response = previewOnly
        ? await fetchParkingPrice(zoneId, durationMinutes)
        : await fetch('/api/parking-orders', {
            method: 'POST',
            headers: { ...authorization, 'Content-Type': 'application/json' },
            body: JSON.stringify({ zoneId, vehicleId, durationMinutes }),
          })
      if (!page.isConnected) return
      if (response.status === 401) {
        onUnauthorized()
      } else if (response.status === 400) {
        setError(`Proverite trajanje. Zona prihvata najmanje ${zone.minDurationMinutes} min, a efektivno trajanje može biti najviše ${zone.maxDurationMinutes} min.`)
      } else if (response.status === 404) {
        setError('Izabrana zona ili vozilo više nije dostupno. Osvežite podatke i pokušajte ponovo.')
      } else if (!response.ok) {
        throw new Error('Zahtev nije uspeo')
      } else {
        const result = await response.json()
        if (!page.isConnected) return
        if (previewOnly) {
          setPreview(result)
        } else {
          navigate(`/parking-nalozi/${encodeURIComponent(result.id)}`)
        }
      }
    } catch {
      if (page.isConnected) {
        setError(previewOnly ? 'Obračun trenutno nije dostupan.' : 'Kreiranje parking naloga trenutno nije dostupno.')
      }
    } finally {
      if (page.isConnected) setOperation(null)
    }
  }

  return (
    <main className="parking-page order-page">
      <header className="page-heading"><h1>Kupi parking</h1></header>

      {zones.status === 'loading' && <p className="notice" role="status">Učitavanje parking zona…</p>}
      {zones.status === 'error' && <p className="notice notice-error" role="alert">Zone trenutno nije moguće učitati. Osvežite stranicu.</p>}
      {zones.status === 'success' && zones.zones.length === 0 && <p className="notice">Trenutno nema parking zona.</p>}

      {vehicles.status === 'loading' && <p className="notice" role="status">Učitavanje vozila…</p>}
      {vehicles.status === 'error' && (
        <div className="notice account-form">
          <p role="alert">Vozila trenutno nije moguće učitati.</p>
          <Link className="form-link" to="/moja-vozila">Idi na moja vozila</Link>
        </div>
      )}
      {vehicles.status === 'success' && vehicles.vehicles.length === 0 && (
        <div className="notice account-form">
          <p>Dodajte vozilo da biste mogli da kreirate parking nalog.</p>
          <Link className="form-link" to="/moja-vozila">Idi na moja vozila</Link>
        </div>
      )}

      {ready && (
        <form className="zone-card account-form order-form" onSubmit={submit} onChange={() => setPreview(null)}>
          <label>
            Parking zona
            <select name="zoneId" required disabled={Boolean(operation)}>
              <option value="">Izaberite zonu</option>
              {zones.zones.map((zone) => <option key={zone.id} value={zone.id}>{zone.name}</option>)}
            </select>
          </label>
          <label>
            Vozilo
            <select name="vehicleId" required disabled={Boolean(operation)}>
              <option value="">Izaberite vozilo</option>
              {vehicles.vehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.registrationPlate}</option>)}
            </select>
          </label>
          <label>
            Trajanje u minutima
            <input name="durationMinutes" type="number" inputMode="numeric" min="1" max="2147483647" step="1" required disabled={Boolean(operation)} />
          </label>
          <div className="order-actions">
            <button type="submit" value="preview" className="button-secondary" disabled={Boolean(operation)}>Izračunaj cenu</button>
            <button type="submit" value="create" disabled={Boolean(operation)}>Kreiraj parking nalog</button>
          </div>
          {preview && (
            <dl className="zone-duration price-result" aria-label="Obračun cene">
              <PriceDetails price={preview} numberFormat={numberFormat} />
            </dl>
          )}
        </form>
      )}

      {operation && <p className="notice price-feedback" role="status">{operation}</p>}
      {error && <p className="notice notice-error price-feedback" role="alert">{error}</p>}

    </main>
  )
}

export function ParkingOrderDetails({ numberFormat }) {
  const { orderId } = useParams()
  return <OrderDetails key={orderId} orderId={orderId} numberFormat={numberFormat} />
}

function OrderDetails({ orderId, numberFormat }) {
  const { token, onUnauthorized } = useOutletContext()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function loadOrder() {
      try {
        const response = await fetch(`/api/parking-orders/${encodeURIComponent(orderId)}`, {
          headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
          signal: controller.signal,
        })

        if (controller.signal.aborted) return
        if (response.status === 401) {
          onUnauthorized()
        } else if (response.status === 404) {
          setError('Parking nalog nije pronađen.')
        } else if (!response.ok) {
          throw new Error('Zahtev nije uspeo')
        } else {
          const result = await response.json()
          if (!controller.signal.aborted) setOrder(result)
        }
      } catch {
        if (!controller.signal.aborted) {
          setError('Status naloga trenutno nije moguće proveriti. Pokušajte ponovo.')
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }

    loadOrder()
    return () => controller.abort()
  }, [orderId, token, onUnauthorized, revision])

  function checkStatus() {
    setLoading(true)
    setError('')
    setRevision((previous) => previous + 1)
  }

  return (
    <main className="parking-page">
      <header className="page-heading"><h1>Parking nalog #{orderId}</h1></header>
      {loading && <p className="notice" role="status">Provera statusa naloga…</p>}
      {error && <p className="notice notice-error" role="alert">{error}</p>}
      {(order || !loading) && (
        <section className="price-panel" aria-labelledby="order-heading">
          <h2 id="order-heading">Status naloga</h2>
          {order && (
            <>
              <p className="order-status" role="status">
                <strong>{statusLabels[order.status] ?? 'Nepoznat status'}</strong>
              </p>
              <dl className="zone-duration price-result">
                <div><dt>Zona</dt><dd>{order.zoneName}</dd></div>
                <div><dt>Vozilo</dt><dd>{order.registrationPlate}</dd></div>
                <PriceDetails price={order} numberFormat={numberFormat} />
              </dl>

              {order.status === 'PAID' && order.session?.id != null && (
                <dl className="zone-duration price-result" aria-label="Parking sesija">
                  <div><dt>ID sesije</dt><dd>{order.session.id}</dd></div>
                  <div><dt>Početak sesije</dt><dd>{sessionDate.format(new Date(order.session.startsAt))}</dd></div>
                  <div><dt>Ističe</dt><dd>{sessionDate.format(new Date(order.session.expiresAt))}</dd></div>
                </dl>
              )}
            </>
          )}

          <div className="account-form price-feedback">
            <button type="button" onClick={checkStatus} disabled={loading}>Proveri status</button>
          </div>
        </section>
      )}
    </main>
  )
}

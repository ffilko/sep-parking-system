import { useCallback, useEffect, useState } from 'react'
import AuthForm from './AuthForm.jsx'
import { Link, Navigate, NavLink, Route, Routes, useLocation, useNavigate } from 'react-router'
import VehiclesLayout, { Vehicles } from './Vehicles.jsx'
import ParkingOrder, { ParkingOrderDetails } from './ParkingOrder.jsx'
import ParkingPriceForm from './ParkingPriceForm.jsx'
import './App.css'

const numberFormat = new Intl.NumberFormat('sr-Latn-RS', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

function useParkingZones() {
  const [result, setResult] = useState({ status: 'loading' })

  useEffect(() => {
    const controller = new AbortController()

    async function loadZones() {
      try {
        const response = await fetch('/api/parking-zones', {
          headers: { Accept: 'application/json' },
          signal: controller.signal,
        })
        if (!response.ok) throw new Error('Zahtev nije uspeo')

        const zones = await response.json()
        if (!Array.isArray(zones)) throw new Error('Neočekivan odgovor servera')

        if (!controller.signal.aborted) setResult({ status: 'success', zones })
      } catch {
        if (!controller.signal.aborted) setResult({ status: 'error' })
      }
    }

    loadZones()
    return () => controller.abort()
  }, [])

  return result
}

function ParkingZones({ result }) {
  return (
    <main className="parking-page zones-page">
      <header className="page-heading">
        <h1>Parking zone</h1>
        <p>Cene po satu i dozvoljeno trajanje parkiranja.</p>
      </header>

      {result.status === 'loading' && (
        <p className="notice" role="status">Učitavanje parking zona…</p>
      )}
      {result.status === 'error' && (
        <p className="notice notice-error" role="alert">
          Parking zone trenutno nije moguće učitati. Pokušajte ponovo osvežavanjem stranice.
        </p>
      )}
      {result.status === 'success' && (
        result.zones.length === 0 ? (
          <p className="notice" role="status">Trenutno nema parking zona.</p>
        ) : (
          <ul className="zone-list" role="list">
            {result.zones.map((zone) => (
              <li className="zone-card" key={zone.id}>
                <h2>{zone.name}</h2>
                <p className="zone-price">
                  <strong>{numberFormat.format(zone.hourlyRate)} {zone.currency}</strong> po satu
                </p>
                <dl className="zone-duration">
                  <div>
                    <dt>Minimalno trajanje</dt>
                    <dd>{zone.minDurationMinutes} min</dd>
                  </div>
                  <div>
                    <dt>Maksimalno trajanje</dt>
                    <dd>{zone.maxDurationMinutes} min</dd>
                  </div>
                </dl>
              </li>
            ))}
          </ul>
        )
      )}
      {result.status === 'success' && result.zones.length > 0 && (
        <ParkingPriceForm zones={result.zones} numberFormat={numberFormat} />
      )}
    </main>
  )
}

const tokenKey = 'parking.accessToken'

export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem(tokenKey))
  const zones = useParkingZones()
  const navigate = useNavigate()
  const location = useLocation()

  function login(accessToken) {
    sessionStorage.setItem(tokenKey, accessToken)
    setToken(accessToken)
    navigate(location.state?.returnTo ?? '/moja-vozila', { replace: true })
  }

  const endSession = useCallback((message) => {
    sessionStorage.removeItem(tokenKey)
    setToken(null)
    navigate('/prijava', { replace: true, state: { notice: message } })
  }, [navigate])

  const expireSession = useCallback(() => {
    endSession('Prijava je istekla ili više nije važeća. Prijavite se ponovo.')
  }, [endSession])

  return (
    <>
      <header className="site-header">
        <div className="site-header-inner">
          <Link className="site-brand" to="/parking-zone">
            <span className="site-mark" aria-hidden="true">P</span>
            <span>Parking servis</span>
          </Link>

          <nav className="parking-nav" aria-label="Glavna navigacija">
            <NavLink to="/parking-zone">Parking zone</NavLink>
            {token && (
              <>
                <NavLink to="/kupi-parking">Kupi parking</NavLink>
                <NavLink to="/moja-vozila">Moja vozila</NavLink>
              </>
            )}

            <div className="parking-nav-account">
              {token ? (
                <button type="button" onClick={() => endSession('Uspešno ste se odjavili.')}>Odjava</button>
              ) : (
                <>
                  <NavLink to="/registracija">Registracija</NavLink>
                  <NavLink to="/prijava">Prijava</NavLink>
                </>
              )}
            </div>
          </nav>
        </div>
      </header>

      <Routes>
        <Route path="/" element={<Navigate to="/parking-zone" replace />} />
        <Route path="/parking-zone" element={<ParkingZones result={zones} />} />
        <Route path="/registracija" element={token ? <Navigate to="/moja-vozila" replace /> : (
          <AuthForm key="register" mode="register" />
        )} />
        <Route path="/prijava" element={token ? <Navigate to="/moja-vozila" replace /> : (
          <AuthForm key="login" mode="login" notice={location.state?.notice} onLogin={login} />
        )} />
        <Route element={token ? (
          <VehiclesLayout key={token} token={token} onUnauthorized={expireSession} />
        ) : (
          <Navigate to="/prijava" replace state={{ returnTo: location.pathname }} />
        )}>
          <Route path="/moja-vozila" element={<Vehicles />} />
          <Route path="/kupi-parking" element={<ParkingOrder zones={zones} numberFormat={numberFormat} />} />
          <Route path="/parking-nalozi/:orderId" element={
            <ParkingOrderDetails numberFormat={numberFormat} />
          } />
        </Route>
        <Route path="*" element={
          <main className="parking-page">
            <header className="page-heading"><h1>Stranica nije pronađena</h1></header>
            <div className="notice"><Link to="/parking-zone">Idi na parking zone</Link></div>
          </main>
        } />
      </Routes>
    </>
  )
}

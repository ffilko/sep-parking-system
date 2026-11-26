import { useEffect, useState } from 'react'
import { Outlet, useOutletContext } from 'react-router'

export default function VehiclesLayout({ token, onUnauthorized }) {
  const [list, setList] = useState({ status: 'loading' })
  const [revision, setRevision] = useState(0)


  useEffect(() => {
    const controller = new AbortController()

    async function loadVehicles() {
      try {
        const response = await fetch('/api/vehicles', {
          headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
          signal: controller.signal,
        })
        if (controller.signal.aborted) return
        if (response.status === 401) {
          onUnauthorized()
          return
        }
        if (!response.ok) throw new Error('Zahtev nije uspeo')

        const vehicles = await response.json()
        if (!Array.isArray(vehicles)) throw new Error('Neočekivan odgovor servera')
        if (!controller.signal.aborted) setList({ status: 'success', vehicles })
      } catch {
        if (!controller.signal.aborted) setList({ status: 'error' })
      }
    }

    loadVehicles()
    return () => controller.abort()
  }, [token, revision, onUnauthorized])

  function refreshList() {
    setList({ status: 'loading' })
    setRevision((previous) => previous + 1)
  }

  return <Outlet context={{ token, onUnauthorized, list, refreshList }} />
}

export function Vehicles() {
  const { token, onUnauthorized, list, refreshList } = useOutletContext()
  const [addition, setAddition] = useState(null)

  async function addVehicle(event) {
    event.preventDefault()
    const form = event.currentTarget
    const registrationPlate = new FormData(form).get('registrationPlate')
    setAddition({ status: 'loading' })

    try {
      const response = await fetch('/api/vehicles', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify({ registrationPlate }),
      })

      if (!form.isConnected) return
      if (response.status === 401) {
        onUnauthorized()
      } else if (response.status === 400) {
        setAddition({ status: 'error', message: 'Unesite registarsku oznaku od 1 do 20 znakova.' })
      } else if (response.status === 409) {
        setAddition({ status: 'error', message: 'Vozilo sa ovom registarskom oznakom već postoji na vašem nalogu.' })
      } else if (!response.ok) {
        throw new Error('Zahtev nije uspeo')
      } else {
        const vehicle = await response.json()
        if (!form.isConnected) return
        form.reset()
        setAddition({ status: 'success', message: `Dodato vozilo sa oznakom ${vehicle.registrationPlate}.` })
        refreshList()
      }
    } catch {
      if (form.isConnected) {
        setAddition({ status: 'error', message: 'Vozilo trenutno nije moguće dodati. Pokušajte ponovo.' })
      }
    }
  }

  return (
    <main className="parking-page vehicles-page">
      <header className="page-heading"><h1>Moja vozila</h1></header>
      {list.status === 'loading' && <p className="notice" role="status">Učitavanje vozila…</p>}
      {list.status === 'error' && (
        <div className="notice account-form">
          <p role="alert">Vozila trenutno nije moguće učitati.</p>
          <button type="button" onClick={refreshList}>Pokušaj ponovo</button>
        </div>
      )}
      {list.status === 'success' && (
        list.vehicles.length === 0 ? (
          <p className="notice" role="status">Još nemate dodatih vozila.</p>
        ) : (
          <ul className="zone-list" role="list" aria-label="Registarske oznake">
            {list.vehicles.map((vehicle) => (
              <li className="zone-card" key={vehicle.id}>{vehicle.registrationPlate}</li>
            ))}
          </ul>
        )
      )}

      <section className="price-panel" aria-labelledby="vehicle-heading">
        <h2 id="vehicle-heading">Dodaj vozilo</h2>
        <form className="account-form" onSubmit={addVehicle}>
          <label>
            Registarska oznaka
            <input name="registrationPlate" type="text" required disabled={addition?.status === 'loading'} />
          </label>
          <button type="submit" disabled={addition?.status === 'loading'}>Dodaj vozilo</button>
          {addition?.status === 'loading' && <p role="status">Dodavanje vozila…</p>}
          {addition?.status === 'success' && <p role="status">{addition.message}</p>}
          {addition?.status === 'error' && <p className="notice notice-error" role="alert">{addition.message}</p>}
        </form>
      </section>
    </main>
  )
}

import { useState } from 'react'
import { Link } from 'react-router'

export default function AuthForm({ mode, notice, onLogin }) {
  const registering = mode === 'register'
  const title = registering ? 'Registracija' : 'Prijava'
  const [status, setStatus] = useState('idle')
  const [error, setError] = useState('')

  async function submit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const { email, password } = Object.fromEntries(new FormData(form))
    setStatus('loading')
    setError('')

    try {
      const response = await fetch(`/api/auth/${mode}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      if (!form.isConnected) return
      if (registering && response.status === 409) {
        setError('Nalog sa ovom adresom e-pošte već postoji.')
      } else if (!registering && response.status === 401) {
        setError('Adresa e-pošte ili lozinka je pogrešna.')
      } else if (response.status === 400) {
        setError('Proverite adresu e-pošte i lozinku. Lozinka može imati najviše 72 bajta.')
      } else if (!response.ok) {
        throw new Error('Zahtev nije uspeo')
      } else if (registering) {
        form.reset()
        setStatus('registered')
        return
      } else {
        const { accessToken } = await response.json()
        if (!form.isConnected) return
        if (typeof accessToken !== 'string' || !accessToken) throw new Error('Nedostaje token')
        onLogin(accessToken)
        return
      }
    } catch {
      setError(registering
        ? 'Registracija trenutno nije dostupna. Pokušajte ponovo.'
        : 'Prijava trenutno nije dostupna. Pokušajte ponovo.')
    }

    setStatus('idle')
  }

  return (
    <main className="parking-page auth-page">
      <header className="page-heading"><h1>{title}</h1></header>
      {notice && <p className="notice account-notice" role="status">{notice}</p>}
      {status === 'registered' ? (
        <div className="zone-card account-form">
          <p role="status">Registracija je uspešna. Sada možete da se prijavite.</p>
          <Link className="form-link" to="/prijava">Idi na prijavu</Link>
        </div>
      ) : (
        <form className="zone-card account-form" onSubmit={submit}>
          <label>
            Adresa e-pošte
            <input name="email" type="email" autoComplete="username" maxLength={254} readOnly={status === 'loading'} required />
          </label>
          <label>
            Lozinka
            <input name="password" type="password" autoComplete={registering ? 'new-password' : 'current-password'} readOnly={status === 'loading'} required />
          </label>
          <button type="submit" disabled={status === 'loading'}>
            {registering ? 'Registruj se' : 'Prijavi se'}
          </button>
          {status === 'loading' && <p role="status">Slanje zahteva…</p>}
          {error && <p className="notice notice-error" role="alert">{error}</p>}
        </form>
      )}
    </main>
  )
}

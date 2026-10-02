import { createContext, useContext, useEffect, useState } from 'react'
import api from './api'

const AuthContext = createContext(null)
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    if (!sessionStorage.getItem('ecotrack.token')) { setLoading(false); return }
    api.get('/auth/me').then(({ data }) => setUser(data)).catch(() => sessionStorage.removeItem('ecotrack.token')).finally(() => setLoading(false))
  }, [])
  useEffect(() => {
    const unauthorized = () => setUser(null)
    window.addEventListener('ecotrack:unauthorized', unauthorized)
    return () => window.removeEventListener('ecotrack:unauthorized', unauthorized)
  }, [])
  async function signIn(email, password) {
    const { data } = await api.post('/auth/login', { email, password })
    sessionStorage.setItem('ecotrack.token', data.token); setUser(data.user); return data.user
  }
  async function register(payload) {
    const { data } = await api.post('/auth/register', payload)
    sessionStorage.setItem('ecotrack.token', data.token); setUser(data.user); return data.user
  }
  async function updateProfile(payload) {
    const { data } = await api.put('/auth/me', payload)
    sessionStorage.setItem('ecotrack.token', data.token)
    setUser(data.user)
    return data.user
  }
  function signOut() { sessionStorage.removeItem('ecotrack.token'); setUser(null) }
  return <AuthContext.Provider value={{ user, loading, signIn, register, updateProfile, signOut }}>{children}</AuthContext.Provider>
}
export function useAuth() { return useContext(AuthContext) }
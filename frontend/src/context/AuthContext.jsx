import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { authService } from '../services/authService'
import { CLAVE_TOKEN, EVENTO_SESION_VENCIDA } from '../api/axiosClient'

const AuthContext = createContext(null)
const CLAVE_SESION = 'bv_sesion'

const leerSesion = () => {
  try {
    const guardada = localStorage.getItem(CLAVE_SESION)
    return guardada && localStorage.getItem(CLAVE_TOKEN) ? JSON.parse(guardada) : null
  } catch {
    return null
  }
}

const borrarSesion = () => {
  try {
    localStorage.removeItem(CLAVE_TOKEN)
    localStorage.removeItem(CLAVE_SESION)
  } catch {
    return
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerSesion)
  const [sesionVencida, setSesionVencida] = useState(false)

  const cerrarSesion = useCallback(() => {
    borrarSesion()
    setUsuario(null)
  }, [])

  useEffect(() => {
    const alVencer = () => {
      if (leerSesion()) {
        setSesionVencida(true)
      }
      cerrarSesion()
    }
    window.addEventListener(EVENTO_SESION_VENCIDA, alVencer)
    return () => window.removeEventListener(EVENTO_SESION_VENCIDA, alVencer)
  }, [cerrarSesion])

  const iniciarSesion = useCallback(async (email, password) => {
    const { token } = await authService.login(email, password)
    const perfil = await authService.perfil(token)
    const sesion = { id: perfil.id, nombre: perfil.nombre, email: perfil.email, rol: perfil.rol }
    try {
      localStorage.setItem(CLAVE_TOKEN, token)
      localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
    } catch {
      sesion.soloEnMemoria = true
    }
    setSesionVencida(false)
    setUsuario(sesion)
    return sesion
  }, [])

  const registrarse = useCallback(
    async (datos) => {
      await authService.registrar(datos)
      return iniciarSesion(datos.email, datos.password)
    },
    [iniciarSesion]
  )

  const valor = useMemo(
    () => ({ usuario, sesionVencida, iniciarSesion, registrarse, cerrarSesion }),
    [usuario, sesionVencida, iniciarSesion, registrarse, cerrarSesion]
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)

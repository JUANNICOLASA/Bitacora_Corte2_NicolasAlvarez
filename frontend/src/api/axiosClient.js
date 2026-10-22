import axios from 'axios'

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
export const EVENTO_SESION_VENCIDA = 'bv:sesion-vencida'
export const CLAVE_TOKEN = 'bv_token'

const leerToken = () => {
  try {
    return localStorage.getItem(CLAVE_TOKEN)
  } catch {
    return null
  }
}

const axiosClient = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000
})

axiosClient.interceptors.request.use((config) => {
  const token = leerToken()
  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axiosClient.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const conToken = Boolean(error?.config?.headers?.Authorization)
    if (error?.response?.status === 401 && conToken) {
      window.dispatchEvent(new Event(EVENTO_SESION_VENCIDA))
    }
    return Promise.reject(error)
  }
)

const textoDeDetalle = (detalle) => {
  const separador = detalle.indexOf(': ')
  return separador > -1 ? detalle.slice(separador + 2) : detalle
}

export const mensajeDeError = (error) => {
  const datos = error?.response?.data
  if (datos?.detalles?.length) {
    return datos.detalles.map(textoDeDetalle).join('. ')
  }
  if (datos?.mensaje) {
    return datos.mensaje
  }
  if (error?.code === 'ECONNABORTED') {
    return 'El servidor tardó demasiado en responder. Intenta de nuevo.'
  }
  if (error?.response?.status === 403) {
    return 'Tu cuenta no tiene permiso para hacer esto.'
  }
  if (error instanceof Error && !error.isAxiosError) {
    return error.message
  }
  return 'No pudimos conectarnos con Blue Velvet. Revisa tu conexión e intenta de nuevo.'
}

export default axiosClient

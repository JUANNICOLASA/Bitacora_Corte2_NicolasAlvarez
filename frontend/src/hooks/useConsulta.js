import { useCallback, useEffect, useRef, useState } from 'react'
import { mensajeDeError } from '../api/axiosClient'

export function useConsulta(consultar, dependencias = [], intervaloMs = 0) {
  const [datos, setDatos] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const consultaActual = useRef(consultar)
  consultaActual.current = consultar

  const recargar = useCallback(async (silenciosa = false) => {
    if (!silenciosa) {
      setCargando(true)
    }
    try {
      const resultado = await consultaActual.current()
      setDatos(resultado)
      setError(null)
    } catch (e) {
      setError(mensajeDeError(e))
    } finally {
      setCargando(false)
    }
  }, [])

  useEffect(() => {
    recargar()
    if (!intervaloMs) {
      return undefined
    }
    const temporizador = setInterval(() => recargar(true), intervaloMs)
    return () => clearInterval(temporizador)
  }, [recargar, intervaloMs, ...dependencias])

  return { datos, setDatos, cargando, error, recargar }
}

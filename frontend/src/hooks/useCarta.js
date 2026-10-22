import { useEffect, useState } from 'react'
import { cartaService } from '../services/cartaService'
import { mensajeDeError } from '../api/axiosClient'

export function useCarta() {
  const [cocteles, setCocteles] = useState([])
  const [cocina, setCocina] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let activo = true
    cartaService
      .listarResumenes()
      .then((resumenes) => Promise.all([cartaService.listarCocteles(resumenes), cartaService.listarCocina(resumenes)]))
      .then(([listaCocteles, listaCocina]) => {
        if (activo) {
          setCocteles(listaCocteles)
          setCocina(listaCocina)
        }
      })
      .catch((e) => activo && setError(mensajeDeError(e)))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [])

  return { cocteles, cocina, cargando, error }
}

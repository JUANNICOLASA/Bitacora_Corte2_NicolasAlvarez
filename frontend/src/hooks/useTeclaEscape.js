import { useEffect } from 'react'

export function useTeclaEscape(activo, alPresionar) {
  useEffect(() => {
    if (!activo) {
      return undefined
    }
    const manejar = (evento) => {
      if (evento.key === 'Escape') {
        alPresionar()
      }
    }
    window.addEventListener('keydown', manejar)
    return () => window.removeEventListener('keydown', manejar)
  }, [activo, alPresionar])
}

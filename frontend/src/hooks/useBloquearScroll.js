import { useEffect } from 'react'

export function useBloquearScroll(activo) {
  useEffect(() => {
    if (!activo) {
      return undefined
    }
    const anterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = anterior
    }
  }, [activo])
}

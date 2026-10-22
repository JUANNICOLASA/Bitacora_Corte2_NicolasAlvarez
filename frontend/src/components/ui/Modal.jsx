import { useEffect, useRef } from 'react'
import { useBloquearScroll } from '../../hooks/useBloquearScroll'
import { useTeclaEscape } from '../../hooks/useTeclaEscape'
import { IconoCerrar } from './Iconos'

export default function Modal({ abierto, alCerrar, titulo, children, className = '' }) {
  const botonCerrar = useRef(null)
  useBloquearScroll(abierto)
  useTeclaEscape(abierto, alCerrar)

  useEffect(() => {
    if (abierto) {
      botonCerrar.current?.focus()
    }
  }, [abierto])

  if (!abierto) {
    return null
  }

  return (
    <div className="modal" onMouseDown={(e) => e.target === e.currentTarget && alCerrar()}>
      <div className={`modal__panel ${className}`} role="dialog" aria-modal="true" aria-label={titulo}>
        <button ref={botonCerrar} type="button" className="modal__cerrar" onClick={alCerrar} aria-label="Cerrar">
          <IconoCerrar />
        </button>
        {children}
      </div>
    </div>
  )
}

import { useState } from 'react'
import { mensajeDeError } from '../../api/axiosClient'
import { IconoCandado } from './Iconos'

export default function Disponibilidad({ nombre, disponible, alCambiar, textoAgotado = 'AGOTADO EN BARRA' }) {
  const [trabajando, setTrabajando] = useState(false)
  const [error, setError] = useState(null)

  const cambiar = async () => {
    setTrabajando(true)
    setError(null)
    try {
      await alCambiar(!disponible)
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setTrabajando(false)
    }
  }

  return (
    <span className="disponibilidad">
      <button
        type="button"
        className={`interruptor ${disponible ? 'interruptor--activo' : ''}`}
        aria-pressed={disponible}
        aria-label={`${nombre}: ${disponible ? 'disponible' : textoAgotado.toLowerCase()}. Cambiar`}
        onClick={cambiar}
        disabled={trabajando}
      >
        <span className="interruptor__perilla" aria-hidden="true" />
      </button>
      <span className={disponible ? 'disponibilidad__texto' : 'disponibilidad__texto disponibilidad__texto--agotado'}>
        {disponible ? 'Disponible' : <><IconoCandado /> {textoAgotado}</>}
      </span>
      {error && <span className="campo__error" role="alert">{error}</span>}
    </span>
  )
}

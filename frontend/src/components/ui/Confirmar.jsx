import { useState } from 'react'

export default function Confirmar({ texto, pregunta, alConfirmar, deshabilitado }) {
  const [preguntando, setPreguntando] = useState(false)

  if (!preguntando) {
    return (
      <button type="button" className="boton boton--secundario boton--compacto" onClick={() => setPreguntando(true)} disabled={deshabilitado}>
        {texto}
      </button>
    )
  }

  return (
    <span className="confirmar" role="group" aria-label={pregunta}>
      <span className="confirmar__pregunta">{pregunta}</span>
      <button
        type="button"
        className="boton boton--primario boton--compacto"
        onClick={() => {
          setPreguntando(false)
          alConfirmar()
        }}
      >
        Sí
      </button>
      <button type="button" className="boton boton--secundario boton--compacto" onClick={() => setPreguntando(false)}>
        No
      </button>
    </span>
  )
}

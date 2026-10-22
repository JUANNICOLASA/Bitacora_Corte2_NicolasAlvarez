import { useEffect, useRef } from 'react'

export default function Pestanas({ opciones, activa, alCambiar, etiqueta, idPanel }) {
  const contenedor = useRef(null)

  useEffect(() => {
    const lista = contenedor.current
    const boton = lista?.querySelector('[aria-selected="true"]')
    if (lista && boton && lista.scrollWidth > lista.clientWidth) {
      lista.scrollTo({ left: boton.offsetLeft - (lista.clientWidth - boton.offsetWidth) / 2, behavior: 'smooth' })
    }
  }, [activa])

  return (
    <div ref={contenedor} className="filtros__secciones pestanas" role="tablist" aria-label={etiqueta}>
      {opciones.map((opcion) => (
        <button
          key={opcion.id}
          type="button"
          role="tab"
          id={`pestana-${opcion.id}`}
          aria-selected={activa === opcion.id}
          aria-controls={idPanel}
          className={`filtros__seccion ${activa === opcion.id ? 'filtros__seccion--activa' : ''}`}
          onClick={() => alCambiar(opcion.id)}
        >
          {opcion.nombre}
        </button>
      ))}
    </div>
  )
}

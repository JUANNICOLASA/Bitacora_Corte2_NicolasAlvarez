import { useEffect, useState } from 'react'
import { cartaService } from '../../services/cartaService'

export default function RecetaReferencia({ coctel }) {
  const [receta, setReceta] = useState(null)

  useEffect(() => {
    let activo = true
    cartaService
      .obtenerReceta(coctel.id)
      .then((encontrada) => activo && setReceta(encontrada))
      .catch(() => activo && setReceta(null))
    return () => {
      activo = false
    }
  }, [coctel.id])

  if (!receta) {
    return null
  }

  return (
    <section className="receta" aria-labelledby={`receta-${coctel.slug}`}>
      <h3 id={`receta-${coctel.slug}`} className="detalle__subtitulo">Receta clásica de referencia</h3>
      <p className="receta__fuente">
        Así se prepara el {receta.nombre} según {receta.fuente}{receta.vaso ? `, servido en ${receta.vaso.toLowerCase()}` : ''}.
      </p>
      {receta.ingredientes.length > 0 && (
        <ul className="receta__ingredientes">
          {receta.ingredientes.map((ingrediente) => (
            <li key={ingrediente}>{ingrediente}</li>
          ))}
        </ul>
      )}
      {receta.instrucciones && <p className="receta__pasos">{receta.instrucciones}</p>}
    </section>
  )
}

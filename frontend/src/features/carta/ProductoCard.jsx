import ImagenConRespaldo from '../../components/ui/ImagenConRespaldo'
import Estrellas from '../../components/ui/Estrellas'
import { IconoCandado } from '../../components/ui/Iconos'
import { formatoPrecio } from '../../utils'

export default function ProductoCard({ producto, alAbrir }) {
  const esMocktail = producto.tipoBebida === 'MOCKTAIL'

  return (
    <article className={`producto ${producto.disponible ? '' : 'producto--agotado'}`}>
      <button type="button" className="producto__boton" onClick={() => alAbrir(producto.slug)} aria-label={`Ver detalle de ${producto.nombre}`}>
        <div className="producto__marco">
          <ImagenConRespaldo src={producto.imagen} alt={producto.nombre} tono={producto.tipo === 'cocina' ? 'vino' : 'azul'} className="producto__imagen" />
          {!producto.disponible && (
            <span className="etiqueta etiqueta--agotado">
              <IconoCandado /> {producto.tipo === 'cocina' ? 'AGOTADO EN COCINA' : 'AGOTADO EN BARRA'}
            </span>
          )}
          {esMocktail && producto.disponible && <span className="etiqueta etiqueta--mocktail">Sin alcohol</span>}
        </div>
        <div className="producto__cuerpo">
          <div className="producto__titulo">
            <h3>{producto.nombre}</h3>
            <span className="producto__puntos" aria-hidden="true" />
            <span className="precio">{formatoPrecio(producto.precio)}</span>
          </div>
          <p className="producto__descripcion">{producto.descripcion}</p>
          <div className="producto__pie">
            <Estrellas valor={producto.calificacion.promedio} tamano={14} />
            <span className="producto__votos">
              {producto.calificacion.total === 0
                ? 'Sin opiniones todavía'
                : `${producto.calificacion.total} ${producto.calificacion.total === 1 ? 'opinión' : 'opiniones'}`}
            </span>
          </div>
        </div>
      </button>
    </article>
  )
}

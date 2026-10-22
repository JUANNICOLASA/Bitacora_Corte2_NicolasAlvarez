import { useState } from 'react'
import Modal from '../../components/ui/Modal'
import ImagenConRespaldo from '../../components/ui/ImagenConRespaldo'
import { IconoAmpliar, IconoCandado, IconoCerrar } from '../../components/ui/Iconos'
import { formatoPrecio } from '../../utils'
import Calificaciones from './Calificaciones'
import PedirCoctel from './PedirCoctel'
import RecetaReferencia from './RecetaReferencia'

export default function ProductoDetalle({ producto, alCerrar }) {
  const [ampliada, setAmpliada] = useState(false)

  if (!producto) {
    return null
  }

  const esCoctel = producto.tipo === 'coctel'
  const tono = esCoctel ? 'azul' : 'vino'
  const ficha = esCoctel
    ? [
        { etiqueta: 'Tipo', valor: producto.tipoBebida === 'MOCKTAIL' ? 'Sin alcohol' : 'Con alcohol' },
        { etiqueta: 'Destilado base', valor: producto.destiladoBase ?? 'No aplica' },
        ...(producto.graduacion ? [{ etiqueta: 'Graduación', valor: producto.graduacion }] : [])
      ]
    : [{ etiqueta: 'Porción', valor: producto.porcion }]

  return (
    <Modal abierto alCerrar={ampliada ? () => setAmpliada(false) : alCerrar} titulo={producto.nombre} className="detalle">
      <div className="detalle__imagen">
        <button type="button" className="detalle__ampliar" onClick={() => setAmpliada(true)} aria-label={`Ampliar la foto de ${producto.nombre}`}>
          <ImagenConRespaldo src={producto.imagen} alt={producto.nombre} tono={tono} />
          <span className="detalle__ampliar-icono" aria-hidden="true"><IconoAmpliar /></span>
        </button>
      </div>

      <div className="detalle__contenido">
        <div className="detalle__encabezado">
          <h2>{producto.nombre}</h2>
          <span className="precio precio--grande">{formatoPrecio(producto.precio)}</span>
        </div>
        {!producto.disponible && (
          <p className="etiqueta etiqueta--agotado etiqueta--en-linea">
            <IconoCandado /> {esCoctel ? 'AGOTADO EN BARRA' : 'AGOTADO EN COCINA'}
          </p>
        )}
        <p className="detalle__descripcion">{producto.descripcion}</p>
        {producto.historia && <p className="detalle__historia">{producto.historia}</p>}

        <dl className="detalle__ficha">
          {ficha.map((dato) => (
            <div key={dato.etiqueta}>
              <dt>{dato.etiqueta}</dt>
              <dd>{dato.valor}</dd>
            </div>
          ))}
          {producto.maridaje && (
            <div>
              <dt>{esCoctel ? 'Acompáñalo con' : 'Va bien con'}</dt>
              <dd>{producto.maridaje}</dd>
            </div>
          )}
        </dl>

        {esCoctel && <PedirCoctel coctel={producto} />}
        {esCoctel && <RecetaReferencia coctel={producto} />}

        <Calificaciones producto={producto} />
      </div>

      {ampliada && (
        <div className="visor" onClick={() => setAmpliada(false)}>
          <button type="button" className="visor__cerrar" onClick={() => setAmpliada(false)} aria-label="Cerrar la foto ampliada">
            <IconoCerrar />
          </button>
          <figure onClick={(e) => e.stopPropagation()}>
            <ImagenConRespaldo src={producto.imagen} alt={producto.nombre} tono={tono} className="visor__imagen" />
            <figcaption>{producto.nombre}</figcaption>
          </figure>
        </div>
      )}
    </Modal>
  )
}

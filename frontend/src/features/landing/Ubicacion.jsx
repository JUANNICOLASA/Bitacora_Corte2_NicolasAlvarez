import { contacto } from '../../data/bar'
import Ornamento from '../../components/ui/Ornamento'

const busqueda = encodeURIComponent(`${contacto.direccion}, Bogotá, Colombia`)

export default function Ubicacion() {
  return (
    <section id="ubicacion" className="seccion ubicacion" aria-labelledby="ubicacion-titulo">
      <Ornamento />
      <h2 id="ubicacion-titulo" className="titulo-seccion">Dónde encontrarnos</h2>
      <address className="ubicacion__direccion">
        <span className="ubicacion__calle">{contacto.direccion}</span>
        <span>{contacto.barrio}</span>
      </address>
      <p className="ubicacion__referencia">{contacto.referencia}</p>
      <div className="ubicacion__acciones">
        <a className="boton boton--secundario" href={`https://www.google.com/maps/search/?api=1&query=${busqueda}`} target="_blank" rel="noreferrer">
          Abrir en Google Maps
        </a>
        <a className="boton boton--secundario" href={`https://waze.com/ul?q=${busqueda}`} target="_blank" rel="noreferrer">
          Abrir en Waze
        </a>
      </div>
    </section>
  )
}

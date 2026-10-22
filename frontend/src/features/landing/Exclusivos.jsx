import { Link } from 'react-router-dom'
import { cocteles } from '../../data/cocteles'
import ImagenConRespaldo from '../../components/ui/ImagenConRespaldo'

export default function Exclusivos() {
  const exclusivos = cocteles.filter((coctel) => coctel.categoria === 'DE_AUTOR')

  return (
    <section className="exclusivos" aria-labelledby="exclusivos-titulo">
      <div className="exclusivos__contenido">
        <div className="exclusivos__encabezado">
          <h2 id="exclusivos-titulo" className="titulo-seccion">Exclusivos de la casa</h2>
          <Link to="/carta" className="enlace-subrayado">Ver la carta completa</Link>
        </div>
        <ul className="exclusivos__lista">
          {exclusivos.map((coctel) => (
            <li key={coctel.slug}>
              <Link to={`/carta?item=${coctel.slug}`} className="exclusivo">
                <div className="exclusivo__foto">
                  <ImagenConRespaldo src={coctel.imagen} alt={coctel.nombre} />
                </div>
                <div className="exclusivo__texto">
                  <h3 className="exclusivo__nombre">{coctel.nombre}</h3>
                  <p>{coctel.descripcion}</p>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

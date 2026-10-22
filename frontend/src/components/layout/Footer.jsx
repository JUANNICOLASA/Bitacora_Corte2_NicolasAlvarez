import { Link } from 'react-router-dom'
import { contacto } from '../../data/bar'
import Ornamento from '../ui/Ornamento'

export default function Footer() {
  return (
    <footer className="pie">
      <Ornamento className="pie__ornamento" />
      <div className="pie__contenido">
        <div className="pie__marca">
          <p className="marca marca--grande">Blue Velvet</p>
          <p className="pie__lema">Coctelería de autor. Abrimos cuando cae la noche.</p>
        </div>
        <div className="pie__bloque">
          <h2>Visítanos</h2>
          <p>{contacto.direccion}</p>
          <p>{contacto.barrio}</p>
        </div>
        <div className="pie__bloque">
          <h2>Escríbenos</h2>
          <p><a href={contacto.telefonoLink}>{contacto.telefono}</a></p>
          <p><a href={`mailto:${contacto.correo}`}>{contacto.correo}</a></p>
          <p>{contacto.instagram}</p>
        </div>
        <div className="pie__bloque">
          <h2>La casa</h2>
          <p><Link to="/carta">La carta</Link></p>
          <p><Link to="/#reservas">Reservas</Link></p>
          <p><Link to="/acceso">Ingreso del personal</Link></p>
        </div>
      </div>
      <p className="pie__legal">
        © {new Date().getFullYear()} Blue Velvet. El consumo de alcohol es solo para mayores de 18 años. Bebe con moderación.
      </p>
    </footer>
  )
}

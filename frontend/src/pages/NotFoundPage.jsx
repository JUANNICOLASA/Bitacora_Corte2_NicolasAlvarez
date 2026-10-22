import { Link } from 'react-router-dom'
import Ornamento from '../components/ui/Ornamento'

export default function NotFoundPage() {
  return (
    <section className="pagina-vacia">
      <Ornamento />
      <h1 className="titulo-seccion">Esta puerta no lleva a ninguna parte</h1>
      <p>La página que buscas no existe o cambió de lugar.</p>
      <div className="pagina-vacia__acciones">
        <Link to="/" className="boton boton--primario">Volver al inicio</Link>
        <Link to="/carta" className="boton boton--secundario">Ver la carta</Link>
      </div>
    </section>
  )
}

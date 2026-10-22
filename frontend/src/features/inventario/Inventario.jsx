import { coctelService, modificadorService } from '../../services/inventarioService'
import { useConsulta } from '../../hooks/useConsulta'
import Disponibilidad from '../../components/ui/Disponibilidad'
import { formatoPrecio } from '../../utils'

function ListaInventario({ titulo, consulta, servicio, detalle }) {
  const { datos, setDatos, cargando, error } = useConsulta(consulta)

  const cambiar = async (id, disponible) => {
    const actualizado = await servicio.cambiarDisponibilidad(id, disponible)
    setDatos((lista) => lista.map((item) => (item.id === id ? actualizado : item)))
  }

  return (
    <section className="panel__seccion" aria-labelledby={`inventario-${titulo}`}>
      <h2 id={`inventario-${titulo}`} className="panel__subtitulo">{titulo}</h2>
      {cargando && <p className="panel__nota">Cargando...</p>}
      {error && <p className="formulario__error" role="alert">{error}</p>}
      {datos && (
        <ul className="registro">
          {datos.map((item) => (
            <li key={item.id} className="registro__fila">
              <div className="registro__datos">
                <span className="registro__nombre">{item.nombre}</span>
                <span className="registro__detalle">{detalle(item)}</span>
              </div>
              <Disponibilidad nombre={item.nombre} disponible={item.disponible} alCambiar={(valor) => cambiar(item.id, valor)} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default function Inventario() {
  return (
    <div className="panel__columnas">
      <ListaInventario
        titulo="Cócteles"
        consulta={() => coctelService.listar()}
        servicio={coctelService}
        detalle={(coctel) => `${coctel.tipo === 'MOCKTAIL' ? 'Sin alcohol' : coctel.destiladoBase} · ${formatoPrecio(coctel.precio)}`}
      />
      <ListaInventario
        titulo="Modificadores"
        consulta={() => modificadorService.listar()}
        servicio={modificadorService}
        detalle={(modificador) =>
          `${modificador.graduacionAlcoholica > 0 ? `${modificador.graduacionAlcoholica} % vol.` : 'Sin alcohol'} · ${formatoPrecio(modificador.precioExtra)}`
        }
      />
    </div>
  )
}

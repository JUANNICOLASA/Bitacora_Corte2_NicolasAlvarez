import { useState } from 'react'
import { eventoService } from '../../services/inventarioService'
import { useConsulta } from '../../hooks/useConsulta'
import EstadoEtiqueta from '../../components/ui/EstadoEtiqueta'
import { formatoFechaHora } from '../../utils'

const tipos = {
  CREADA: 'Comanda creada',
  CAMBIO_ESTADO: 'Cambio de estado',
  CAMBIO_LICOR: 'Cambio de licor',
  CANCELADA: 'Comanda cancelada'
}

export default function HistorialEventos() {
  const [filtro, setFiltro] = useState('')
  const [idPedido, setIdPedido] = useState(null)
  const { datos, cargando, error } = useConsulta(() => eventoService.listar(idPedido), [idPedido])

  const buscar = (e) => {
    e.preventDefault()
    const numero = Number(filtro)
    setIdPedido(Number.isInteger(numero) && numero > 0 ? numero : null)
  }

  return (
    <section className="panel__seccion" aria-labelledby="historial-titulo">
      <div className="panel__encabezado">
        <h2 id="historial-titulo" className="panel__subtitulo">Historial de las comandas</h2>
      </div>
      <form className="panel__busqueda" onSubmit={buscar}>
        <div className="campo">
          <label htmlFor="historial-pedido">Número de comanda</label>
          <input id="historial-pedido" type="number" min="1" value={filtro} onChange={(e) => setFiltro(e.target.value)} placeholder="Todas" />
        </div>
        <button type="submit" className="boton boton--secundario">Buscar</button>
        {idPedido && (
          <button
            type="button"
            className="enlace-boton"
            onClick={() => {
              setFiltro('')
              setIdPedido(null)
            }}
          >
            Ver todo el historial
          </button>
        )}
      </form>
      {cargando && <p className="panel__nota">Cargando historial...</p>}
      {error && <p className="formulario__error" role="alert">{error}</p>}
      {!cargando && datos?.length === 0 && <p className="panel__nota">No hay eventos registrados.</p>}
      {!cargando && datos?.length > 0 && (
        <ol className="historial">
          {datos.map((evento) => (
            <li key={evento.id} className="historial__evento">
              <time dateTime={evento.fecha}>{formatoFechaHora(evento.fecha)}</time>
              <div>
                <p className="historial__titulo">
                  {tipos[evento.tipo] ?? evento.tipo} · Comanda #{evento.idPedido}
                </p>
                <p className="historial__detalle">{evento.descripcion}</p>
                <p className="historial__usuario">{evento.usuario}</p>
              </div>
              {evento.estado && <EstadoEtiqueta estado={evento.estado} />}
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}

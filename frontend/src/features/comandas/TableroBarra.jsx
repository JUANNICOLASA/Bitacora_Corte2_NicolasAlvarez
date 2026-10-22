import { pedidoService } from '../../services/pedidoService'
import { useConsulta } from '../../hooks/useConsulta'
import TarjetaComanda from './TarjetaComanda'

const columnas = [
  { estado: 'RECIBIDO', titulo: 'Recibidas', siguiente: 'EN_PREPARACION', accion: 'Empezar a preparar' },
  { estado: 'EN_PREPARACION', titulo: 'En preparación', siguiente: 'LISTO', accion: 'Marcar como lista' },
  { estado: 'LISTO', titulo: 'Listas para entregar', siguiente: 'ENTREGADO', accion: 'Entregada en la mesa' }
]

export default function TableroBarra({ puedeAvanzar }) {
  const { datos, setDatos, cargando, error, recargar } = useConsulta(() => pedidoService.listarActivas(), [], 15000)

  const avanzar = async (comanda, siguiente) => {
    const actualizada = await pedidoService.cambiarEstado(comanda.id, siguiente)
    setDatos((lista) =>
      actualizada.estado === 'ENTREGADO'
        ? lista.filter((item) => item.id !== actualizada.id)
        : lista.map((item) => (item.id === actualizada.id ? actualizada : item))
    )
  }

  return (
    <section className="panel__seccion" aria-labelledby="tablero-titulo">
      <div className="panel__encabezado">
        <h2 id="tablero-titulo" className="panel__subtitulo">Tablero de la barra</h2>
        <button type="button" className="enlace-boton" onClick={() => recargar()}>Actualizar</button>
      </div>
      <p className="panel__nota">
        Las comandas aparecen en orden de llegada y se actualizan solas cada pocos segundos.
        {!puedeAvanzar && ' Solo el bartender puede cambiar el estado de una comanda.'}
      </p>
      {error && <p className="formulario__error" role="alert">{error}</p>}
      {cargando ? (
        <p className="panel__nota">Cargando el tablero...</p>
      ) : (
        <div className="tablero">
          {columnas.map((columna) => {
            const comandas = (datos ?? []).filter((comanda) => comanda.estado === columna.estado)
            return (
              <section key={columna.estado} className="tablero__columna" aria-labelledby={`columna-${columna.estado}`}>
                <h3 id={`columna-${columna.estado}`} className="tablero__titulo">
                  {columna.titulo} <span className="tablero__conteo">{comandas.length}</span>
                </h3>
                {comandas.length === 0 ? (
                  <p className="panel__nota">Sin comandas.</p>
                ) : (
                  <ul className="tablero__lista">
                    {comandas.map((comanda) => (
                      <li key={comanda.id}>
                        <TarjetaComanda
                          comanda={comanda}
                          accion={puedeAvanzar ? { texto: columna.accion, ejecutar: () => avanzar(comanda, columna.siguiente) } : null}
                        />
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            )
          })}
        </div>
      )}
    </section>
  )
}

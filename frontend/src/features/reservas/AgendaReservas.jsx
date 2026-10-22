import { useState } from 'react'
import { reservaService } from '../../services/reservaService'
import { useConsulta } from '../../hooks/useConsulta'
import TarjetaReserva from './TarjetaReserva'
import { formatoFecha, hoy } from '../../utils'

export default function AgendaReservas() {
  const [fecha, setFecha] = useState(hoy())
  const [proximas, setProximas] = useState(false)
  const { datos, setDatos, cargando, error, recargar } = useConsulta(() => reservaService.listar(proximas ? null : fecha), [fecha, proximas], 60000)

  const actualizar = (actualizada) => setDatos((lista) => lista.map((reserva) => (reserva.id === actualizada.id ? actualizada : reserva)))

  const activas = (datos ?? []).filter((reserva) => reserva.estado === 'CONFIRMADA')
  const personas = activas.reduce((suma, reserva) => suma + reserva.personas, 0)

  return (
    <section className="panel__seccion" aria-labelledby="agenda-titulo">
      <div className="panel__encabezado">
        <h2 id="agenda-titulo" className="panel__subtitulo">
          {proximas ? 'Próximas reservas' : `Reservas del ${formatoFecha(fecha, { weekday: 'long', day: 'numeric', month: 'long' })}`}
        </h2>
        <button type="button" className="enlace-boton" onClick={() => recargar()}>Actualizar</button>
      </div>
      <div className="panel__busqueda">
        <div className="campo">
          <label htmlFor="agenda-fecha">Día</label>
          <input
            id="agenda-fecha"
            type="date"
            value={fecha}
            onChange={(e) => {
              setFecha(e.target.value || hoy())
              setProximas(false)
            }}
          />
        </div>
        <button type="button" className={`chip ${proximas ? 'chip--activo' : ''}`} aria-pressed={proximas} onClick={() => setProximas((valor) => !valor)}>
          Desde hoy en adelante
        </button>
      </div>
      {!cargando && !error && (
        <p className="panel__nota">
          {activas.length} {activas.length === 1 ? 'reserva confirmada' : 'reservas confirmadas'}, {personas} {personas === 1 ? 'persona' : 'personas'} en total.
        </p>
      )}
      {cargando && <p className="panel__nota">Cargando la agenda...</p>}
      {error && <p className="formulario__error" role="alert">{error}</p>}
      {!cargando && datos?.length > 0 && (
        <ul className="panel__grilla">
          {datos.map((reserva) => (
            <li key={reserva.id}>
              <TarjetaReserva
                reserva={reserva}
                mostrarContacto
                puedeCumplir
                alCumplir={async () => actualizar(await reservaService.cambiarEstado(reserva.id, 'CUMPLIDA'))}
                alCancelar={async () => {
                  await reservaService.cancelar(reserva.id)
                  actualizar({ ...reserva, estado: 'CANCELADA' })
                }}
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

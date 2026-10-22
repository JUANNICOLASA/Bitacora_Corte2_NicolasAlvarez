import { useState } from 'react'
import { mensajeDeError } from '../../api/axiosClient'
import EstadoEtiqueta from '../../components/ui/EstadoEtiqueta'
import Confirmar from '../../components/ui/Confirmar'
import { formatoFecha } from '../../utils'

export default function TarjetaReserva({ reserva, mostrarContacto, puedeCumplir, alCumplir, alCancelar }) {
  const [error, setError] = useState(null)
  const [trabajando, setTrabajando] = useState(false)
  const confirmada = reserva.estado === 'CONFIRMADA'

  const ejecutar = async (tarea) => {
    setTrabajando(true)
    setError(null)
    try {
      await tarea()
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setTrabajando(false)
    }
  }

  return (
    <article className="comanda reserva-tarjeta" aria-labelledby={`reserva-${reserva.id}`}>
      <header className="comanda__encabezado">
        <div>
          <h3 id={`reserva-${reserva.id}`}>{mostrarContacto ? reserva.nombre : formatoFecha(reserva.fecha, { weekday: 'long', day: 'numeric', month: 'long' })}</h3>
          <p className="comanda__meta">
            {mostrarContacto && `${formatoFecha(reserva.fecha, { day: 'numeric', month: 'short' })} · `}
            {reserva.hora} · {reserva.personas} {reserva.personas === 1 ? 'persona' : 'personas'}
          </p>
        </div>
        <EstadoEtiqueta estado={reserva.estado} />
      </header>
      <dl className="reserva-tarjeta__datos">
        <div>
          <dt>Código</dt>
          <dd className="reserva-tarjeta__codigo">{reserva.codigo}</dd>
        </div>
        {reserva.ocasion && (
          <div>
            <dt>Ocasión</dt>
            <dd>{reserva.ocasion}</dd>
          </div>
        )}
        {mostrarContacto && (
          <div>
            <dt>Contacto</dt>
            <dd>
              <a href={`tel:${reserva.telefono.replace(/[^0-9+]/g, '')}`}>{reserva.telefono}</a> · {reserva.correo}
            </dd>
          </div>
        )}
        {reserva.notas && (
          <div className="reserva-tarjeta__notas">
            <dt>Notas</dt>
            <dd>{reserva.notas}</dd>
          </div>
        )}
      </dl>
      {confirmada && (
        <footer className="comanda__acciones">
          {puedeCumplir && (
            <button type="button" className="boton boton--primario boton--compacto" disabled={trabajando} onClick={() => ejecutar(alCumplir)}>
              Llegaron a la mesa
            </button>
          )}
          <Confirmar texto="Cancelar reserva" pregunta="¿Cancelar esta reserva?" deshabilitado={trabajando} alConfirmar={() => ejecutar(alCancelar)} />
        </footer>
      )}
      {error && <p className="formulario__error" role="alert">{error}</p>}
    </article>
  )
}

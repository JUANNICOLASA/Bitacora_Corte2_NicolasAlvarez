import { Link } from 'react-router-dom'
import { reservaService } from '../../services/reservaService'
import { useConsulta } from '../../hooks/useConsulta'
import TarjetaReserva from './TarjetaReserva'

export default function MisReservas() {
  const { datos, setDatos, cargando, error } = useConsulta(() => reservaService.listarMias())

  return (
    <section className="panel__seccion" aria-labelledby="mis-reservas-titulo">
      <div className="panel__encabezado">
        <h2 id="mis-reservas-titulo" className="panel__subtitulo">Mis reservas</h2>
        <Link to="/#reservas" className="enlace-subrayado">Hacer una reserva</Link>
      </div>
      <p className="panel__nota">Aquí aparecen las reservas que hiciste con tu cuenta iniciada.</p>
      {cargando && <p className="panel__nota">Cargando reservas...</p>}
      {error && <p className="formulario__error" role="alert">{error}</p>}
      {!cargando && datos?.length === 0 && <p className="panel__nota">Todavía no tienes reservas.</p>}
      {!cargando && datos?.length > 0 && (
        <ul className="panel__grilla">
          {datos.map((reserva) => (
            <li key={reserva.id}>
              <TarjetaReserva
                reserva={reserva}
                alCancelar={async () => {
                  await reservaService.cancelar(reserva.id)
                  setDatos((lista) => lista.map((item) => (item.id === reserva.id ? { ...item, estado: 'CANCELADA' } : item)))
                }}
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { contacto, horasReserva, ocasiones } from '../../data/bar'
import { reservaService } from '../../services/reservaService'
import { mensajeDeError } from '../../api/axiosClient'
import Ornamento from '../../components/ui/Ornamento'
import { useAuth } from '../../context/AuthContext'
import { ROLES } from '../../context/roles'
import { formatoFecha, hoy } from '../../utils'

const formularioVacio = {
  nombre: '',
  telefono: '',
  correo: '',
  fecha: '',
  hora: '20:00',
  personas: '2',
  ocasion: ocasiones[0],
  notas: ''
}

const validar = (datos) => {
  const errores = {}
  if (datos.nombre.trim().length < 3) {
    errores.nombre = 'Escribe tu nombre completo para guardar la mesa.'
  }
  if (!/^[0-9+\s()-]{7,}$/.test(datos.telefono.trim())) {
    errores.telefono = 'Escribe un teléfono de al menos 7 dígitos para confirmarte la reserva.'
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(datos.correo.trim())) {
    errores.correo = 'Revisa el correo: debe tener la forma nombre@dominio.com.'
  }
  if (!datos.fecha) {
    errores.fecha = 'Elige el día de tu visita.'
  } else if (datos.fecha < hoy()) {
    errores.fecha = 'La fecha ya pasó. Elige hoy o un día próximo.'
  } else if (new Date(`${datos.fecha}T12:00:00`).getDay() === 1) {
    errores.fecha = 'Los lunes la casa descansa. Elige otro día.'
  }
  const personas = Number(datos.personas)
  if (!Number.isInteger(personas) || personas < 1 || personas > 12) {
    errores.personas = 'Reservamos mesas de 1 a 12 personas. Para grupos más grandes escríbenos.'
  }
  return errores
}

const formularioDe = (usuario) =>
  usuario?.rol === ROLES.CLIENTE ? { ...formularioVacio, nombre: usuario.nombre, correo: usuario.email } : formularioVacio

export default function Reservas() {
  const { usuario } = useAuth()
  const [datos, setDatos] = useState(() => formularioDe(usuario))
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [confirmada, setConfirmada] = useState(null)

  const cambiar = (e) => {
    const { name, value } = e.target
    setDatos((previo) => ({ ...previo, [name]: value }))
    if (errores[name]) {
      setErrores((previo) => ({ ...previo, [name]: undefined }))
    }
  }

  const enviar = async (e) => {
    e.preventDefault()
    const encontrados = validar(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) {
      document.getElementById(`reserva-${Object.keys(encontrados)[0]}`)?.focus()
      return
    }
    setEnviando(true)
    setErrorGeneral(null)
    try {
      const reserva = await reservaService.crear({
        nombre: datos.nombre.trim(),
        telefono: datos.telefono.trim(),
        correo: datos.correo.trim(),
        fecha: datos.fecha,
        hora: datos.hora,
        personas: Number(datos.personas),
        ocasion: datos.ocasion === ocasiones[0] ? null : datos.ocasion,
        notas: datos.notas.trim() || null
      })
      setConfirmada(reserva)
      setDatos(formularioDe(usuario))
    } catch (error) {
      setErrorGeneral(mensajeDeError(error))
    } finally {
      setEnviando(false)
    }
  }

  const campo = (nombre) => ({
    id: `reserva-${nombre}`,
    name: nombre,
    value: datos[nombre],
    onChange: cambiar,
    'aria-invalid': Boolean(errores[nombre]),
    'aria-describedby': errores[nombre] ? `reserva-${nombre}-error` : undefined
  })

  const mensaje = (nombre) =>
    errores[nombre] && (
      <p id={`reserva-${nombre}-error`} className="campo__error">{errores[nombre]}</p>
    )

  return (
    <section id="reservas" className="seccion reservas" aria-labelledby="reservas-titulo">
      <div className="reservas__invitacion">
        <Ornamento />
        <h2 id="reservas-titulo" className="titulo-seccion">Guarda tu mesa</h2>
        <p className="parrafo-destacado">
          Las mesas junto al escenario se llenan primero los viernes y sábados. Reserva con anticipación y te
          esperamos con la vela encendida.
        </p>
        <p>
          Sostenemos la reserva quince minutos después de la hora elegida. Si prefieres hablar con alguien,
          llámanos al <a href={contacto.telefonoLink}>{contacto.telefono}</a>.
        </p>
      </div>

      <div className="reservas__panel">
        {confirmada ? (
          <div className="reserva-confirmada" role="status">
            <p className="reserva-confirmada__titulo">Tu mesa está reservada</p>
            <p>
              Te esperamos el <strong>{formatoFecha(confirmada.fecha, { weekday: 'long', day: 'numeric', month: 'long' })}</strong> a las <strong>{confirmada.hora}</strong> para{' '}
              {confirmada.personas} {confirmada.personas === 1 ? 'persona' : 'personas'}.
            </p>
            <p className="reserva-confirmada__codigo">
              Código de reserva <span>{confirmada.codigo}</span>
            </p>
            <p>
              Guarda el código para cualquier consulta con la casa.
              {usuario?.rol === ROLES.CLIENTE && (
                <>
                  {' '}También la encuentras en <Link to="/mi-cuenta?vista=reservas" className="enlace-subrayado">Mis reservas</Link>.
                </>
              )}
            </p>
            <button type="button" className="boton boton--secundario" onClick={() => setConfirmada(null)}>
              Hacer otra reserva
            </button>
          </div>
        ) : (
          <form className="formulario" onSubmit={enviar} noValidate>
            <div className="campo campo--ancho">
              <label htmlFor="reserva-nombre">Nombre completo</label>
              <input type="text" autoComplete="name" {...campo('nombre')} />
              {mensaje('nombre')}
            </div>
            <div className="campo">
              <label htmlFor="reserva-telefono">Teléfono</label>
              <input type="tel" autoComplete="tel" {...campo('telefono')} />
              {mensaje('telefono')}
            </div>
            <div className="campo">
              <label htmlFor="reserva-correo">Correo</label>
              <input type="email" autoComplete="email" {...campo('correo')} />
              {mensaje('correo')}
            </div>
            <div className="campo">
              <label htmlFor="reserva-fecha">Fecha</label>
              <input type="date" min={hoy()} {...campo('fecha')} />
              {mensaje('fecha')}
            </div>
            <div className="campo">
              <label htmlFor="reserva-hora">Hora</label>
              <select {...campo('hora')}>
                {horasReserva.map((hora) => (
                  <option key={hora} value={hora}>{hora}</option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label htmlFor="reserva-personas">Personas</label>
              <input type="number" min="1" max="12" inputMode="numeric" {...campo('personas')} />
              {mensaje('personas')}
            </div>
            <div className="campo">
              <label htmlFor="reserva-ocasion">Ocasión</label>
              <select {...campo('ocasion')}>
                {ocasiones.map((ocasion) => (
                  <option key={ocasion} value={ocasion}>{ocasion}</option>
                ))}
              </select>
            </div>
            <div className="campo campo--ancho">
              <label htmlFor="reserva-notas">Notas para la casa <span className="campo__opcional">(opcional)</span></label>
              <textarea rows="3" maxLength="300" placeholder="Una mesa tranquila, una sorpresa, algo que debamos saber" {...campo('notas')} />
            </div>
            {errorGeneral && <p className="formulario__error" role="alert">{errorGeneral}</p>}
            <button type="submit" className="boton boton--primario formulario__enviar" disabled={enviando}>
              {enviando ? 'Reservando...' : 'Reservar mesa'}
            </button>
          </form>
        )}
      </div>
    </section>
  )
}

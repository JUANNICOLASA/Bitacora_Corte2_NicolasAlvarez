import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { espacioPorRol, nombreDelRol, ROLES } from '../../context/roles'
import { mensajeDeError } from '../../api/axiosClient'
import Ornamento from '../../components/ui/Ornamento'
import Pestanas from '../../components/ui/Pestanas'

const modos = [
  { id: 'ingresar', nombre: 'Ingresar' },
  { id: 'registro', nombre: 'Crear cuenta' }
]

const correoValido = (correo) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo.trim())

const validarRegistro = (datos) => {
  const errores = {}
  if (datos.nombre.trim().length < 3) {
    errores.nombre = 'Escribe tu nombre completo.'
  }
  if (!correoValido(datos.email)) {
    errores.email = 'Revisa el correo: debe tener la forma nombre@dominio.com.'
  }
  if (datos.password.length < 8 || datos.password.length > 64) {
    errores.password = 'La contraseña debe tener entre 8 y 64 caracteres.'
  } else if (!/[a-z]/.test(datos.password) || !/[A-Z]/.test(datos.password) || !/\d/.test(datos.password) || !/[^A-Za-z0-9]/.test(datos.password)) {
    errores.password = 'Usa al menos una mayúscula, una minúscula, un número y un carácter especial.'
  }
  if (datos.confirmacion !== datos.password) {
    errores.confirmacion = 'Las contraseñas no coinciden.'
  }
  return errores
}

export default function AccesoPage() {
  const { usuario, sesionVencida, iniciarSesion, registrarse, cerrarSesion } = useAuth()
  const navegar = useNavigate()
  const { state } = useLocation()
  const [modo, setModo] = useState('ingresar')
  const [datos, setDatos] = useState({ nombre: '', email: '', password: '', confirmacion: '' })
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (e) => {
    const { name, value } = e.target
    setDatos((previo) => ({ ...previo, [name]: value }))
    if (errores[name]) {
      setErrores((previo) => ({ ...previo, [name]: undefined }))
    }
  }

  const continuar = (sesion) => {
    const destino = state?.desde ?? (sesion.rol === ROLES.CLIENTE ? '/carta' : espacioPorRol[sesion.rol].ruta)
    navegar(destino, { replace: true })
  }

  const enviar = async (e) => {
    e.preventDefault()
    setError(null)
    if (modo === 'ingresar') {
      if (!correoValido(datos.email) || datos.password.length === 0) {
        setError('Escribe tu correo y tu contraseña para ingresar.')
        return
      }
    } else {
      const encontrados = validarRegistro(datos)
      setErrores(encontrados)
      if (Object.keys(encontrados).length > 0) {
        return
      }
    }
    setEnviando(true)
    try {
      const sesion =
        modo === 'ingresar'
          ? await iniciarSesion(datos.email.trim(), datos.password)
          : await registrarse({ nombre: datos.nombre.trim(), email: datos.email.trim(), password: datos.password })
      continuar(sesion)
    } catch (err) {
      setError(err?.response?.status === 401 ? 'El correo o la contraseña no son correctos.' : mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  const campo = (nombre) => ({
    id: `acceso-${nombre}`,
    name: nombre,
    value: datos[nombre],
    onChange: cambiar,
    'aria-invalid': Boolean(errores[nombre]),
    'aria-describedby': errores[nombre] ? `acceso-${nombre}-error` : undefined
  })

  const mensaje = (nombre) => errores[nombre] && <p id={`acceso-${nombre}-error`} className="campo__error">{errores[nombre]}</p>

  if (usuario) {
    const espacio = espacioPorRol[usuario.rol]
    return (
      <section className="acceso">
        <div className="acceso__panel">
          <Ornamento />
          <h1 className="titulo-seccion">Hola, {usuario.nombre}</h1>
          <p>Ingresaste como {nombreDelRol[usuario.rol]}.</p>
          <div className="acceso__acciones">
            <Link to={espacio.ruta} className="boton boton--primario">Ir a {espacio.texto.toLowerCase()}</Link>
            <button type="button" className="boton boton--secundario" onClick={cerrarSesion}>Cerrar sesión</button>
          </div>
        </div>
      </section>
    )
  }

  return (
    <section className="acceso">
      <div className="acceso__panel">
        <Ornamento />
        <Pestanas
          opciones={modos}
          activa={modo}
          alCambiar={(nuevo) => {
            setModo(nuevo)
            setError(null)
            setErrores({})
          }}
          etiqueta="Ingreso"
          idPanel="acceso-formulario"
        />
        <p>{modo === 'ingresar' ? 'Para clientes frecuentes y personal de la casa.' : 'Crea tu cuenta de cliente para pedir desde la mesa y calificar la carta.'}</p>
        {sesionVencida && modo === 'ingresar' && (
          <p className="formulario__error acceso__aviso" role="status">Tu sesión se cerró por seguridad. Ingresa de nuevo para continuar.</p>
        )}
        <form id="acceso-formulario" className="formulario formulario--simple" onSubmit={enviar} noValidate>
          {modo === 'registro' && (
            <div className="campo">
              <label htmlFor="acceso-nombre">Nombre completo</label>
              <input type="text" autoComplete="name" maxLength="80" {...campo('nombre')} />
              {mensaje('nombre')}
            </div>
          )}
          <div className="campo">
            <label htmlFor="acceso-email">Correo</label>
            <input type="email" autoComplete="email" maxLength="120" {...campo('email')} />
            {mensaje('email')}
          </div>
          <div className="campo">
            <label htmlFor="acceso-password">Contraseña</label>
            <input type="password" autoComplete={modo === 'ingresar' ? 'current-password' : 'new-password'} maxLength="64" {...campo('password')} />
            {mensaje('password')}
          </div>
          {modo === 'registro' && (
            <div className="campo">
              <label htmlFor="acceso-confirmacion">Confirma la contraseña</label>
              <input type="password" autoComplete="new-password" maxLength="64" {...campo('confirmacion')} />
              {mensaje('confirmacion')}
            </div>
          )}
          {error && <p className="formulario__error" role="alert">{error}</p>}
          <button type="submit" className="boton boton--primario formulario__enviar" disabled={enviando}>
            {enviando ? (modo === 'ingresar' ? 'Ingresando...' : 'Creando la cuenta...') : modo === 'ingresar' ? 'Ingresar' : 'Crear cuenta'}
          </button>
        </form>
      </div>
    </section>
  )
}

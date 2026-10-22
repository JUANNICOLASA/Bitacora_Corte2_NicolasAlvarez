import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useComanda } from '../../context/ComandaContext'
import { espacioPorRol, puedePedir } from '../../context/roles'
import { IconoCerrar, IconoMenu } from '../ui/Iconos'

const enlaces = [
  { a: '/carta', texto: 'La carta' },
  { a: '/#el-bar', texto: 'El bar' },
  { a: '/#horario', texto: 'Horario' },
  { a: '/#ubicacion', texto: 'Ubicación' }
]

export default function Header() {
  const { usuario, cerrarSesion } = useAuth()
  const { unidades } = useComanda()
  const { pathname } = useLocation()
  const [conFondo, setConFondo] = useState(false)
  const [marcaVisible, setMarcaVisible] = useState(true)
  const [menuAbierto, setMenuAbierto] = useState(false)

  useEffect(() => {
    const revisar = () => {
      setConFondo(window.scrollY > 40)
      const tituloPortada = document.getElementById('hero-titulo')
      setMarcaVisible(!tituloPortada || tituloPortada.getBoundingClientRect().bottom < 80)
    }
    revisar()
    window.addEventListener('scroll', revisar, { passive: true })
    window.addEventListener('resize', revisar)
    return () => {
      window.removeEventListener('scroll', revisar)
      window.removeEventListener('resize', revisar)
    }
  }, [pathname])

  useEffect(() => {
    setMenuAbierto(false)
  }, [pathname])

  const solido = conFondo || pathname !== '/'
  const espacio = usuario ? espacioPorRol[usuario.rol] : null

  return (
    <header className={`cabecera ${solido ? 'cabecera--solida' : ''}`}>
      <div className="cabecera__contenido">
        <Link
          to="/"
          className={`marca cabecera__marca ${marcaVisible ? '' : 'cabecera__marca--oculta'}`}
          aria-label="Blue Velvet, inicio"
          aria-hidden={!marcaVisible}
          tabIndex={marcaVisible ? undefined : -1}
        >
          Blue Velvet
        </Link>

        <button
          type="button"
          className="cabecera__menu-boton"
          onClick={() => setMenuAbierto((abierto) => !abierto)}
          aria-expanded={menuAbierto}
          aria-controls="navegacion-principal"
          aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
        >
          {menuAbierto ? <IconoCerrar /> : <IconoMenu />}
        </button>

        <nav id="navegacion-principal" className={`navegacion ${menuAbierto ? 'navegacion--abierta' : ''}`} aria-label="Principal">
          <ul>
            {enlaces.map((enlace) => (
              <li key={enlace.a}>
                {enlace.a.startsWith('/#') ? (
                  <Link to={enlace.a} onClick={() => setMenuAbierto(false)}>{enlace.texto}</Link>
                ) : (
                  <NavLink to={enlace.a}>{enlace.texto}</NavLink>
                )}
              </li>
            ))}
            {espacio && (
              <li>
                <NavLink to={espacio.ruta}>
                  {espacio.texto}
                  {puedePedir(usuario) && unidades > 0 && (
                    <span className="navegacion__contador" aria-label={`${unidades} tragos en la comanda`}>{unidades}</span>
                  )}
                </NavLink>
              </li>
            )}
            <li>
              {usuario ? (
                <button type="button" className="navegacion__sesion" onClick={cerrarSesion}>
                  Salir ({usuario.nombre.split(' ')[0]})
                </button>
              ) : (
                <NavLink to="/acceso">Ingresar</NavLink>
              )}
            </li>
          </ul>
          <Link to="/#reservas" className="boton boton--primario navegacion__reservar" onClick={() => setMenuAbierto(false)}>
            Reservar mesa
          </Link>
        </nav>
      </div>
    </header>
  )
}

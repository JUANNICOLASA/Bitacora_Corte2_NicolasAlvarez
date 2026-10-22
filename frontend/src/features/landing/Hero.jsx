import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { contacto } from '../../data/bar'
import { IconoMensaje, IconoTelefono } from '../../components/ui/Iconos'

export default function Hero() {
  const [encendido, setEncendido] = useState(false)

  useEffect(() => {
    const temporizador = setTimeout(() => setEncendido(true), 250)
    return () => clearTimeout(temporizador)
  }, [])

  return (
    <section className={`hero ${encendido ? 'hero--abierto' : ''}`} aria-labelledby="hero-titulo">
      <div className="hero__cortina" aria-hidden="true" />
      <div className="hero__piso" aria-hidden="true" />

      <div className="hero__escena">
        <h1 id="hero-titulo" className="hero__titulo">Blue Velvet</h1>
        <svg className="hero__rubrica" viewBox="0 0 600 50" aria-hidden="true" focusable="false">
          <path d="M6 40 C 150 18, 300 12, 440 22 S 570 36, 594 10" />
        </svg>
        <p className="hero__subtitulo">Cócteles de autor a media luz</p>
        <p className="hero__texto">
          Una sala de terciopelo azul en el corazón de Bogotá. Reserva tu mesa y deja que la noche haga lo suyo.
        </p>
        <div className="hero__acciones">
          <Link to="/#reservas" className="boton boton--primario">Reservar mesa</Link>
          <Link to="/carta" className="boton boton--secundario">Abrir la carta</Link>
        </div>
        <ul className="hero__contacto">
          <li>
            <a href={contacto.whatsappLink} target="_blank" rel="noreferrer">
              <IconoMensaje /> WhatsApp
            </a>
          </li>
          <li>
            <a href={contacto.telefonoLink}>
              <IconoTelefono /> Llamar
            </a>
          </li>
        </ul>
      </div>
    </section>
  )
}

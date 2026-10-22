import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import Hero from '../features/landing/Hero'
import Galeria from '../features/landing/Galeria'
import ElBar from '../features/landing/ElBar'
import Exclusivos from '../features/landing/Exclusivos'
import Horario from '../features/landing/Horario'
import Reservas from '../features/landing/Reservas'
import Ubicacion from '../features/landing/Ubicacion'

export default function HomePage() {
  const { hash } = useLocation()

  useEffect(() => {
    if (!hash) {
      window.scrollTo(0, 0)
      return
    }
    const destino = document.getElementById(hash.slice(1))
    destino?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [hash])

  return (
    <>
      <Hero />
      <Galeria />
      <ElBar />
      <Exclusivos />
      <Horario />
      <Reservas />
      <Ubicacion />
    </>
  )
}

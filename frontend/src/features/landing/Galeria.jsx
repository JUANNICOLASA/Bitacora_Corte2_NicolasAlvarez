import { useEffect, useRef, useState } from 'react'
import ImagenConRespaldo from '../../components/ui/ImagenConRespaldo'

const fotos = [
  { src: '/img/ambiente/salon.jpg', alt: 'Salón principal con sofás de terciopelo y lámparas de mesa', pie: 'El salón', texto: 'Sofás de terciopelo, lámparas verdes y ventanales en arco.' },
  { src: '/img/ambiente/barra.jpg', alt: 'Barra curva tapizada en terciopelo azul con copas colgantes', pie: 'La barra', texto: 'Donde cada trago se prepara frente a ti.' },
  { src: '/img/ambiente/escenario.jpg', alt: 'Escenario con piano de cola bajo luces azules', pie: 'El escenario', texto: 'Piano los jueves y jazz en vivo los fines de semana.' }
]

const limitar = (valor) => Math.min(1, Math.max(0, valor))

const useMovimientoReducido = () => {
  const [reducido, setReducido] = useState(() => window.matchMedia('(prefers-reduced-motion: reduce)').matches)
  useEffect(() => {
    const consulta = window.matchMedia('(prefers-reduced-motion: reduce)')
    const cambiar = (e) => setReducido(e.matches)
    consulta.addEventListener('change', cambiar)
    return () => consulta.removeEventListener('change', cambiar)
  }, [])
  return reducido
}

export default function Galeria() {
  const contenedor = useRef(null)
  const [progreso, setProgreso] = useState(0)
  const reducido = useMovimientoReducido()

  useEffect(() => {
    if (reducido) {
      return undefined
    }
    let cuadro = null
    const medir = () => {
      cuadro = null
      const caja = contenedor.current?.getBoundingClientRect()
      if (!caja) {
        return
      }
      const recorrido = caja.height - window.innerHeight
      setProgreso(recorrido > 0 ? limitar(-caja.top / recorrido) : 0)
    }
    const programar = () => {
      if (cuadro === null) {
        cuadro = requestAnimationFrame(medir)
      }
    }
    medir()
    window.addEventListener('scroll', programar, { passive: true })
    window.addEventListener('resize', programar)
    return () => {
      window.removeEventListener('scroll', programar)
      window.removeEventListener('resize', programar)
      if (cuadro !== null) {
        cancelAnimationFrame(cuadro)
      }
    }
  }, [reducido])

  if (reducido) {
    return (
      <section className="galeria galeria--estatica" aria-label="Fotos del bar">
        {fotos.map((foto) => (
          <figure key={foto.src} className="galeria__estatica">
            <ImagenConRespaldo src={foto.src} alt={foto.alt} />
            <figcaption>{foto.pie}</figcaption>
          </figure>
        ))}
      </section>
    )
  }

  const tramos = fotos.length - 1
  const revelado = fotos.map((_, indice) => (indice === 0 ? 1 : limitar((progreso * tramos - (indice - 1) - 0.15) / 0.7)))
  const activa = revelado.reduce((ultima, valor, indice) => (valor > 0.5 ? indice : ultima), 0)

  return (
    <section ref={contenedor} className="galeria" style={{ '--tramos': fotos.length }} aria-label="Fotos del bar">
      <div className="galeria__escena">
        <div className="galeria__marco">
          {fotos.map((foto, indice) => {
            const valor = revelado[indice]
            const siguiente = revelado[indice + 1] ?? 0
            return (
              <figure
                key={foto.src}
                className="galeria__capa"
                aria-hidden={indice !== activa}
                style={{
                  clipPath: `inset(${(1 - valor) * 100}% 0 0 0)`,
                  zIndex: indice + 1
                }}
              >
                <div
                  className="galeria__imagen"
                  style={{
                    transform: `scale(${1.18 - valor * 0.18 + siguiente * 0.04})`,
                    filter: `brightness(${0.35 + valor * 0.65 - siguiente * 0.45})`
                  }}
                >
                  <ImagenConRespaldo src={foto.src} alt={foto.alt} />
                </div>
              </figure>
            )
          })}
          <div className="galeria__leyenda" aria-live="polite">
            <p className="galeria__pie">{fotos[activa].pie}</p>
            <p className="galeria__texto">{fotos[activa].texto}</p>
          </div>
        </div>

        <ol className="galeria__progreso" aria-hidden="true">
          {fotos.map((foto, indice) => (
            <li key={foto.src} className={indice === activa ? 'galeria__paso galeria__paso--activo' : 'galeria__paso'}>
              <span className="galeria__barra">
                <span style={{ transform: `scaleX(${revelado[indice]})` }} />
              </span>
              {foto.pie}
            </li>
          ))}
        </ol>
      </div>
    </section>
  )
}

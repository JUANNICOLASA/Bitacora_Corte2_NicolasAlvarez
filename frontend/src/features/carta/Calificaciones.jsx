import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { cartaService } from '../../services/cartaService'
import { mensajeDeError } from '../../api/axiosClient'
import { useAuth } from '../../context/AuthContext'
import { ROLES } from '../../context/roles'
import Estrellas from '../../components/ui/Estrellas'
import { formatoFecha } from '../../utils'

const textosEstrellas = ['', 'No me gustó', 'Regular', 'Bueno', 'Muy bueno', 'Excelente']

export default function Calificaciones({ producto }) {
  const { usuario } = useAuth()
  const ubicacion = useLocation()
  const [resenas, setResenas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [estrellas, setEstrellas] = useState(0)
  const [resaltadas, setResaltadas] = useState(0)
  const [comentario, setComentario] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [agradecimiento, setAgradecimiento] = useState(false)

  useEffect(() => {
    let activo = true
    setCargando(true)
    cartaService
      .listarResenas(producto)
      .then((lista) => activo && setResenas(lista))
      .catch(() => activo && setResenas([]))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [producto])

  const resumen = useMemo(() => {
    const distribucion = { ...producto.calificacion.distribucion }
    let suma = producto.calificacion.promedio * producto.calificacion.total
    let total = producto.calificacion.total
    resenas
      .filter((resena) => resena.nueva)
      .forEach((resena) => {
        distribucion[resena.estrellas] += 1
        suma += resena.estrellas
        total += 1
      })
    return { distribucion, total, promedio: total ? suma / total : 0 }
  }, [producto, resenas])

  const enviar = async (e) => {
    e.preventDefault()
    if (estrellas === 0) {
      setError('Elige de una a cinco estrellas antes de publicar.')
      return
    }
    setEnviando(true)
    setError(null)
    try {
      const nueva = await cartaService.calificar(producto, { estrellas, comentario: comentario.trim() || null })
      setResenas((previas) => [{ ...nueva, nueva: true }, ...previas])
      setEstrellas(0)
      setComentario('')
      setAgradecimiento(true)
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  const visibles = resaltadas || estrellas

  return (
    <section className="calificaciones" aria-labelledby={`calificaciones-${producto.slug}`}>
      <h3 id={`calificaciones-${producto.slug}`} className="detalle__subtitulo">Calificaciones</h3>

      <div className="calificaciones__resumen">
        <div className="calificaciones__promedio">
          <span className="calificaciones__numero">{resumen.promedio.toFixed(1)}</span>
          <Estrellas valor={resumen.promedio} tamano={18} />
          <span className="calificaciones__total">{resumen.total} opiniones</span>
        </div>
        <ul className="calificaciones__barras">
          {[5, 4, 3, 2, 1].map((nivel) => {
            const cantidad = resumen.distribucion[nivel] ?? 0
            const porcentaje = resumen.total ? Math.round((cantidad / resumen.total) * 100) : 0
            return (
              <li key={nivel}>
                <span className="calificaciones__nivel">{nivel} ★</span>
                <span className="calificaciones__barra" aria-hidden="true">
                  <span style={{ width: `${porcentaje}%` }} />
                </span>
                <span className="calificaciones__porcentaje">{porcentaje} %</span>
              </li>
            )
          })}
        </ul>
      </div>

      {usuario?.rol === ROLES.CLIENTE && (
        <form className="calificar" onSubmit={enviar}>
          <fieldset className="calificar__estrellas" onMouseLeave={() => setResaltadas(0)}>
            <legend>¿Ya lo probaste? Califícalo</legend>
            <div className="calificar__opciones">
              {[1, 2, 3, 4, 5].map((nivel) => (
                <label key={nivel} className={`calificar__estrella ${nivel <= visibles ? 'calificar__estrella--activa' : ''}`} onMouseEnter={() => setResaltadas(nivel)}>
                  <input
                    type="radio"
                    name={`estrellas-${producto.slug}`}
                    value={nivel}
                    checked={estrellas === nivel}
                    onChange={() => {
                      setEstrellas(nivel)
                      setAgradecimiento(false)
                    }}
                    className="sr-only"
                  />
                  <span aria-hidden="true">★</span>
                  <span className="sr-only">{nivel} {nivel === 1 ? 'estrella' : 'estrellas'}, {textosEstrellas[nivel]}</span>
                </label>
              ))}
              <span className="calificar__texto" aria-live="polite">{textosEstrellas[visibles]}</span>
            </div>
          </fieldset>
          <div className="calificar__campos calificar__campos--uno">
            <label className="sr-only" htmlFor={`comentario-${producto.slug}`}>Tu comentario</label>
            <textarea id={`comentario-${producto.slug}`} rows="2" value={comentario} onChange={(e) => setComentario(e.target.value)} placeholder="Cuéntanos qué te pareció (opcional)" maxLength="280" />
          </div>
          <p className="calificar__autor">Publicas como {usuario?.nombre}</p>
          {error && <p className="campo__error" role="alert">{error}</p>}
          {agradecimiento && <p className="calificar__gracias" role="status">Gracias, tu opinión ya está publicada.</p>}
          <button type="submit" className="boton boton--secundario" disabled={enviando}>
            {enviando ? 'Publicando...' : 'Publicar opinión'}
          </button>
        </form>
      )}
      {!usuario && (
        <p className="calificar calificar__aviso">
          ¿Ya lo probaste? <Link to="/acceso" state={{ desde: `${ubicacion.pathname}${ubicacion.search}` }} className="enlace-subrayado">Ingresa con tu cuenta</Link> para calificarlo.
        </p>
      )}

      <div className="resenas">
        {cargando && <p className="resenas__vacio">Cargando opiniones...</p>}
        {!cargando && resenas.length === 0 && <p className="resenas__vacio">Todavía no hay opiniones escritas. Sé el primero en contarnos.</p>}
        {!cargando && resenas.length > 0 && (
          <ul>
            {resenas.map((resena) => (
              <li key={resena.id} className="resena">
                <div className="resena__encabezado">
                  <span className="resena__autor">{resena.autor}</span>
                  <Estrellas valor={resena.estrellas} tamano={13} />
                </div>
                {resena.comentario && <p>{resena.comentario}</p>}
                <time dateTime={resena.fecha}>{formatoFecha(resena.fecha)}</time>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

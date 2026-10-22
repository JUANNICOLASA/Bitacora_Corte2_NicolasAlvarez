import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { cartaService } from '../../services/cartaService'
import { useAuth } from '../../context/AuthContext'
import { useComanda } from '../../context/ComandaContext'
import { espacioPorRol, puedePedir } from '../../context/roles'
import { IconoCandado } from '../../components/ui/Iconos'
import { formatoPrecio } from '../../utils'

export default function PedirCoctel({ coctel }) {
  const { usuario } = useAuth()
  const { agregar } = useComanda()
  const ubicacion = useLocation()
  const [modificadores, setModificadores] = useState([])
  const [disponibleAhora, setDisponibleAhora] = useState(coctel.disponible)
  const [cargando, setCargando] = useState(true)
  const [elegidos, setElegidos] = useState([])
  const [cantidad, setCantidad] = useState(1)
  const [destilado, setDestilado] = useState('')
  const [error, setError] = useState(null)
  const [agregado, setAgregado] = useState(false)

  const pide = puedePedir(usuario)
  const conAlcohol = coctel.tipoBebida === 'ALCOHOLICA'

  useEffect(() => {
    let activo = true
    cartaService
      .obtenerCoctel(coctel.id)
      .then((actual) => activo && setDisponibleAhora(actual.seleccionable))
      .catch(() => activo && setDisponibleAhora(coctel.disponible))
    cartaService
      .listarModificadores(coctel.id)
      .then((lista) => activo && setModificadores(lista))
      .catch(() => activo && setModificadores([]))
      .finally(() => activo && setCargando(false))
    return () => {
      activo = false
    }
  }, [coctel.id])

  const alternar = (id) => {
    setAgregado(false)
    setElegidos((previos) => (previos.includes(id) ? previos.filter((otro) => otro !== id) : [...previos, id]))
  }

  const agregarAComanda = (e) => {
    e.preventDefault()
    if (conAlcohol && destilado.trim().length < 2) {
      setError('Antes de enviarlo a la barra, escribe la marca o el tipo exacto del destilado.')
      document.getElementById(`destilado-${coctel.slug}`)?.focus()
      return
    }
    agregar({
      idCoctel: coctel.id,
      nombre: coctel.nombre,
      tipoBebida: coctel.tipoBebida,
      precio: coctel.precio,
      cantidad,
      destilado: conAlcohol ? destilado.trim() : null,
      modificadores: modificadores
        .filter((modificador) => elegidos.includes(modificador.id))
        .map(({ id, nombre, precioExtra }) => ({ id, nombre, precioExtra }))
    })
    setError(null)
    setAgregado(true)
    setElegidos([])
    setCantidad(1)
    setDestilado('')
  }

  return (
    <section className="pedir" aria-labelledby={`pedir-${coctel.slug}`}>
      <h3 id={`pedir-${coctel.slug}`} className="detalle__subtitulo">Personalízalo</h3>
      {cargando && <p className="resenas__vacio">Cargando adiciones...</p>}
      {!cargando && modificadores.length === 0 && <p className="resenas__vacio">Este cóctel se sirve tal como está en la carta.</p>}
      {!cargando && modificadores.length > 0 && (
        <ul className="pedir__modificadores">
          {modificadores.map((modificador) => {
            const seleccionable = pide && disponibleAhora && modificador.habilitado
            return (
              <li key={modificador.id} className={`pedir__modificador ${modificador.habilitado ? '' : 'pedir__modificador--bloqueado'}`}>
                <label>
                  {seleccionable ? (
                    <input type="checkbox" checked={elegidos.includes(modificador.id)} onChange={() => alternar(modificador.id)} />
                  ) : (
                    <span className="pedir__marca" aria-hidden="true">{modificador.habilitado ? '·' : <IconoCandado />}</span>
                  )}
                  <span className="pedir__nombre">{modificador.nombre}</span>
                  <span className="pedir__precio">{modificador.precioExtra > 0 ? `+ ${formatoPrecio(modificador.precioExtra)}` : 'Sin costo'}</span>
                </label>
                {!modificador.habilitado && <span className="pedir__etiqueta">{modificador.etiqueta}</span>}
              </li>
            )
          })}
        </ul>
      )}

      {pide && disponibleAhora && (
        <form className="calificar pedir__formulario" onSubmit={agregarAComanda} noValidate>
          {conAlcohol && (
            <div className="campo">
              <label htmlFor={`destilado-${coctel.slug}`}>Destilado que prefieres</label>
              <input
                id={`destilado-${coctel.slug}`}
                type="text"
                maxLength="60"
                value={destilado}
                onChange={(e) => {
                  setDestilado(e.target.value)
                  setAgregado(false)
                }}
                placeholder={`Marca o tipo exacto de ${coctel.destiladoBase?.toLowerCase() ?? 'destilado'}`}
                aria-invalid={Boolean(error)}
              />
            </div>
          )}
          <div className="pedir__fila">
            <div className="contador" role="group" aria-label="Cantidad">
              <button type="button" onClick={() => setCantidad((valor) => Math.max(1, valor - 1))} disabled={cantidad <= 1} aria-label="Quitar una unidad">−</button>
              <span aria-live="polite">{cantidad}</span>
              <button type="button" onClick={() => setCantidad((valor) => Math.min(20, valor + 1))} disabled={cantidad >= 20} aria-label="Agregar una unidad">+</button>
            </div>
            <button type="submit" className="boton boton--primario">Agregar a la comanda</button>
          </div>
          {error && <p className="campo__error" role="alert">{error}</p>}
          {agregado && (
            <p className="calificar__gracias" role="status">
              Va en tu comanda. <Link to={`${espacioPorRol[usuario.rol].ruta}`} className="enlace-subrayado">Revisar y enviar a la barra</Link>
            </p>
          )}
        </form>
      )}

      {coctel.disponible && !disponibleAhora && (
        <p className="etiqueta etiqueta--agotado etiqueta--en-linea">
          <IconoCandado /> Se acaba de agotar en barra
        </p>
      )}

      {!usuario && disponibleAhora && (
        <p className="resenas__vacio">
          Para pedirlo desde tu mesa, <Link to="/acceso" state={{ desde: `${ubicacion.pathname}${ubicacion.search}` }} className="enlace-subrayado">ingresa con tu cuenta</Link>.
        </p>
      )}
    </section>
  )
}

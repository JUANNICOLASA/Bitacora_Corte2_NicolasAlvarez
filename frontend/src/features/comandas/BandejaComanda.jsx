import { useState } from 'react'
import { Link } from 'react-router-dom'
import { subtotalDe, useComanda } from '../../context/ComandaContext'
import { pedidoService } from '../../services/pedidoService'
import { mensajeDeError } from '../../api/axiosClient'
import { IconoCerrar } from '../../components/ui/Iconos'
import { formatoPrecio } from '../../utils'

export default function BandejaComanda({ alEnviar }) {
  const { items, numeroMesa, total, cambiarCantidad, quitar, cambiarMesa, vaciar } = useComanda()
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [enviada, setEnviada] = useState(null)

  const enviar = async (e) => {
    e.preventDefault()
    const mesa = Number(numeroMesa)
    if (!Number.isInteger(mesa) || mesa < 1) {
      setError('Escribe el número de la mesa donde estás sentado.')
      document.getElementById('comanda-mesa')?.focus()
      return
    }
    setEnviando(true)
    setError(null)
    try {
      const comanda = await pedidoService.crear({
        numeroMesa: mesa,
        items: items.map((item) => ({
          idCoctel: item.idCoctel,
          cantidad: item.cantidad,
          destilado: item.destilado || null,
          idsModificadores: item.modificadores.map((modificador) => modificador.id)
        }))
      })
      vaciar()
      setEnviada(comanda)
      alEnviar?.(comanda)
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  if (items.length === 0) {
    return (
      <section className="bandeja bandeja--vacia" aria-labelledby="bandeja-titulo">
        <h2 id="bandeja-titulo" className="panel__subtitulo">Comanda en curso</h2>
        {enviada ? (
          <div className="reserva-confirmada" role="status">
            <p className="reserva-confirmada__titulo">La comanda #{enviada.id} ya está en la barra</p>
            <p>El bartender la verá en el tablero en orden de llegada. Puedes seguir su estado más abajo.</p>
          </div>
        ) : (
          <p className="panel__nota">Todavía no has agregado tragos. Elígelos en la carta y vuelve aquí para enviarlos a la barra.</p>
        )}
        <Link to="/carta" className="boton boton--secundario">Abrir la carta</Link>
      </section>
    )
  }

  return (
    <section className="bandeja" aria-labelledby="bandeja-titulo">
      <h2 id="bandeja-titulo" className="panel__subtitulo">Comanda en curso</h2>
      <ul className="bandeja__items">
        {items.map((item) => (
          <li key={item.clave} className="bandeja__item">
            <div className="comanda__linea">
              <span className="comanda__nombre">{item.nombre}</span>
              <span className="producto__puntos" aria-hidden="true" />
              <span className="precio">{formatoPrecio(subtotalDe(item))}</span>
            </div>
            {item.destilado && <p className="comanda__detalle">Destilado: {item.destilado}</p>}
            {item.modificadores.length > 0 && (
              <p className="comanda__detalle">Con {item.modificadores.map((modificador) => modificador.nombre).join(', ')}</p>
            )}
            <div className="bandeja__controles">
              <div className="contador" role="group" aria-label={`Cantidad de ${item.nombre}`}>
                <button type="button" onClick={() => cambiarCantidad(item.clave, item.cantidad - 1)} disabled={item.cantidad <= 1} aria-label="Quitar una unidad">−</button>
                <span aria-live="polite">{item.cantidad}</span>
                <button type="button" onClick={() => cambiarCantidad(item.clave, item.cantidad + 1)} disabled={item.cantidad >= 20} aria-label="Agregar una unidad">+</button>
              </div>
              <button type="button" className="enlace-boton" onClick={() => quitar(item.clave)}>
                <IconoCerrar width="16" height="16" /> Quitar
              </button>
            </div>
          </li>
        ))}
      </ul>

      <form className="bandeja__envio" onSubmit={enviar} noValidate>
        <div className="campo">
          <label htmlFor="comanda-mesa">Número de mesa</label>
          <input id="comanda-mesa" type="number" min="1" inputMode="numeric" value={numeroMesa} onChange={(e) => cambiarMesa(e.target.value)} />
        </div>
        <p className="bandeja__total">
          Total <span className="precio precio--grande">{formatoPrecio(total)}</span>
        </p>
        {error && <p className="formulario__error" role="alert">{error}</p>}
        <button type="submit" className="boton boton--primario" disabled={enviando}>
          {enviando ? 'Enviando a la barra...' : 'Enviar a la barra'}
        </button>
      </form>
    </section>
  )
}

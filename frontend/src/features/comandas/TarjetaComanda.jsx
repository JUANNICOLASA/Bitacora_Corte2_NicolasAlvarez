import { useState } from 'react'
import { pedidoService } from '../../services/pedidoService'
import { mensajeDeError } from '../../api/axiosClient'
import EstadoEtiqueta from '../../components/ui/EstadoEtiqueta'
import Confirmar from '../../components/ui/Confirmar'
import { formatoFechaHora, formatoPrecio } from '../../utils'

function CambioDeLicor({ comanda, item, alActualizar }) {
  const [abierto, setAbierto] = useState(false)
  const [destilado, setDestilado] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const guardar = async (e) => {
    e.preventDefault()
    if (destilado.trim().length < 2) {
      setError('Escribe la marca o el tipo exacto del nuevo destilado.')
      return
    }
    setEnviando(true)
    setError(null)
    try {
      alActualizar(await pedidoService.cambiarDestilado(comanda.id, item.id, destilado.trim()))
      setAbierto(false)
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  if (!abierto) {
    return (
      <button type="button" className="enlace-boton" onClick={() => setAbierto(true)}>
        Cambiar el licor (una sola vez)
      </button>
    )
  }

  return (
    <form className="cambio-licor" onSubmit={guardar}>
      <label className="sr-only" htmlFor={`licor-${item.id}`}>Nuevo destilado para {item.nombreCoctel}</label>
      <input id={`licor-${item.id}`} type="text" value={destilado} onChange={(e) => setDestilado(e.target.value)} placeholder="Nueva marca o tipo de destilado" maxLength="60" />
      <button type="submit" className="boton boton--primario boton--compacto" disabled={enviando}>
        {enviando ? 'Guardando...' : 'Guardar'}
      </button>
      <button type="button" className="boton boton--secundario boton--compacto" onClick={() => setAbierto(false)}>
        Volver
      </button>
      {error && <p className="campo__error" role="alert">{error}</p>}
    </form>
  )
}

export default function TarjetaComanda({ comanda, puedeCambiarLicor, puedeCancelar, accion, alActualizar, alCancelar }) {
  const [error, setError] = useState(null)
  const [trabajando, setTrabajando] = useState(false)
  const enRecibido = comanda.estado === 'RECIBIDO'

  const ejecutar = async (tarea) => {
    setTrabajando(true)
    setError(null)
    try {
      await tarea()
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setTrabajando(false)
    }
  }

  return (
    <article className={`comanda comanda--${comanda.estado.toLowerCase()}`} aria-labelledby={`comanda-${comanda.id}`}>
      <header className="comanda__encabezado">
        <div>
          <h3 id={`comanda-${comanda.id}`}>Comanda #{comanda.id}</h3>
          <p className="comanda__meta">
            Mesa {comanda.numeroMesa} · {formatoFechaHora(comanda.fechaCreacion)}
          </p>
        </div>
        <EstadoEtiqueta estado={comanda.estado} />
      </header>

      <ul className="comanda__items">
        {comanda.items.map((item) => (
          <li key={item.id} className="comanda__item">
            <div className="comanda__linea">
              <span className="comanda__nombre">
                {item.cantidad} × {item.nombreCoctel}
              </span>
              <span className="producto__puntos" aria-hidden="true" />
              <span className="precio">{formatoPrecio(item.subtotal)}</span>
            </div>
            {item.destilado && <p className="comanda__detalle">Destilado: {item.destilado}</p>}
            {item.tipo === 'MOCKTAIL' && <p className="comanda__detalle">Sin alcohol</p>}
            {item.modificadores.length > 0 && <p className="comanda__detalle">Con {item.modificadores.join(', ')}</p>}
            {puedeCambiarLicor && enRecibido && item.tipo === 'ALCOHOLICA' && item.cambiosDeLicor < 1 && (
              <CambioDeLicor comanda={comanda} item={item} alActualizar={alActualizar} />
            )}
            {item.cambiosDeLicor > 0 && <p className="comanda__detalle comanda__detalle--nota">El licor ya se cambió una vez.</p>}
          </li>
        ))}
      </ul>

      <footer className="comanda__pie">
        <span className="comanda__total">
          Total <span className="precio">{formatoPrecio(comanda.total)}</span>
        </span>
        <div className="comanda__acciones">
          {accion && (
            <button type="button" className="boton boton--primario boton--compacto" disabled={trabajando} onClick={() => ejecutar(accion.ejecutar)}>
              {trabajando ? 'Actualizando...' : accion.texto}
            </button>
          )}
          {puedeCancelar && enRecibido && (
            <Confirmar
              texto="Cancelar comanda"
              pregunta="¿Cancelar esta comanda?"
              deshabilitado={trabajando}
              alConfirmar={() =>
                ejecutar(async () => {
                  await pedidoService.cancelar(comanda.id)
                  alCancelar(comanda.id)
                })
              }
            />
          )}
        </div>
      </footer>
      {error && <p className="formulario__error" role="alert">{error}</p>}
    </article>
  )
}

import { useState } from 'react'
import { pedidoService } from '../../services/pedidoService'
import { mensajeDeError } from '../../api/axiosClient'
import { useConsulta } from '../../hooks/useConsulta'
import { textoDeEstado } from '../../components/ui/EstadoEtiqueta'
import TarjetaComanda from './TarjetaComanda'

const estados = ['TODAS', 'RECIBIDO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO']

export default function ListaComandas({ titulo, todas = false, puedeCambiarLicor, puedeCancelar, recarga = 0, vacio }) {
  const [estado, setEstado] = useState('TODAS')
  const [numero, setNumero] = useState('')
  const [buscada, setBuscada] = useState(null)
  const [errorBusqueda, setErrorBusqueda] = useState(null)
  const { datos, setDatos, cargando, error, recargar } = useConsulta(
    () => (todas ? pedidoService.listar(estado === 'TODAS' ? null : estado) : pedidoService.listarMias()),
    [todas, estado, recarga],
    30000
  )

  const reemplazar = (actualizada) => {
    setDatos((lista) => lista.map((comanda) => (comanda.id === actualizada.id ? actualizada : comanda)))
    setBuscada((actual) => (actual?.id === actualizada.id ? actualizada : actual))
  }
  const marcarCancelada = (id) => {
    setDatos((lista) => lista.map((comanda) => (comanda.id === id ? { ...comanda, estado: 'CANCELADO' } : comanda)))
    setBuscada((actual) => (actual?.id === id ? { ...actual, estado: 'CANCELADO' } : actual))
  }

  const buscar = async (e) => {
    e.preventDefault()
    const id = Number(numero)
    setBuscada(null)
    setErrorBusqueda(null)
    if (!Number.isInteger(id) || id < 1) {
      setErrorBusqueda('Escribe el número de la comanda.')
      return
    }
    try {
      setBuscada(await pedidoService.obtener(id))
    } catch (err) {
      setErrorBusqueda(err?.response?.status === 403 ? 'Esa comanda no es tuya.' : mensajeDeError(err))
    }
  }

  const tarjeta = (comanda) => (
    <TarjetaComanda
      comanda={comanda}
      puedeCambiarLicor={puedeCambiarLicor}
      puedeCancelar={puedeCancelar}
      alActualizar={reemplazar}
      alCancelar={marcarCancelada}
    />
  )

  return (
    <section className="panel__seccion" aria-labelledby="comandas-titulo">
      <div className="panel__encabezado">
        <h2 id="comandas-titulo" className="panel__subtitulo">{titulo}</h2>
        <button type="button" className="enlace-boton" onClick={() => recargar()}>Actualizar</button>
      </div>

      <form className="panel__busqueda" onSubmit={buscar}>
        <div className="campo">
          <label htmlFor={`buscar-comanda-${todas ? 'todas' : 'mias'}`}>Buscar por número</label>
          <input id={`buscar-comanda-${todas ? 'todas' : 'mias'}`} type="number" min="1" inputMode="numeric" value={numero} onChange={(e) => setNumero(e.target.value)} placeholder="Ej. 12" />
        </div>
        <button type="submit" className="boton boton--secundario">Buscar</button>
        {(buscada || errorBusqueda) && (
          <button
            type="button"
            className="enlace-boton"
            onClick={() => {
              setBuscada(null)
              setErrorBusqueda(null)
              setNumero('')
            }}
          >
            Ver todas
          </button>
        )}
      </form>
      {errorBusqueda && <p className="formulario__error" role="alert">{errorBusqueda}</p>}
      {buscada && <div className="panel__resultado">{tarjeta(buscada)}</div>}

      {todas && !buscada && (
        <ul className="filtros__categorias panel__filtros" aria-label="Filtrar por estado">
          {estados.map((opcion) => (
            <li key={opcion}>
              <button type="button" className={`chip ${estado === opcion ? 'chip--activo' : ''}`} aria-pressed={estado === opcion} onClick={() => setEstado(opcion)}>
                {opcion === 'TODAS' ? 'Todas' : textoDeEstado(opcion)}
              </button>
            </li>
          ))}
        </ul>
      )}

      {!buscada && cargando && <p className="panel__nota">Cargando comandas...</p>}
      {!buscada && error && <p className="formulario__error" role="alert">{error}</p>}
      {!buscada && !cargando && !error && datos?.length === 0 && <p className="panel__nota">{vacio}</p>}
      {!buscada && !cargando && datos?.length > 0 && (
        <ul className="panel__grilla">
          {datos.map((comanda) => (
            <li key={comanda.id}>{tarjeta(comanda)}</li>
          ))}
        </ul>
      )}
    </section>
  )
}

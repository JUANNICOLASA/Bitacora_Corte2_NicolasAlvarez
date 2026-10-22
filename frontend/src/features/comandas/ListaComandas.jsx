import { useState } from 'react'
import { pedidoService } from '../../services/pedidoService'
import { useConsulta } from '../../hooks/useConsulta'
import { textoDeEstado } from '../../components/ui/EstadoEtiqueta'
import TarjetaComanda from './TarjetaComanda'

const estados = ['TODAS', 'RECIBIDO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO']

export default function ListaComandas({ titulo, todas = false, puedeCambiarLicor, puedeCancelar, recarga = 0, vacio }) {
  const [estado, setEstado] = useState('TODAS')
  const { datos, setDatos, cargando, error, recargar } = useConsulta(
    () => (todas ? pedidoService.listar(estado === 'TODAS' ? null : estado) : pedidoService.listarMias()),
    [todas, estado, recarga],
    30000
  )

  const reemplazar = (actualizada) => setDatos((lista) => lista.map((comanda) => (comanda.id === actualizada.id ? actualizada : comanda)))
  const marcarCancelada = (id) => setDatos((lista) => lista.map((comanda) => (comanda.id === id ? { ...comanda, estado: 'CANCELADO' } : comanda)))

  return (
    <section className="panel__seccion" aria-labelledby="comandas-titulo">
      <div className="panel__encabezado">
        <h2 id="comandas-titulo" className="panel__subtitulo">{titulo}</h2>
        <button type="button" className="enlace-boton" onClick={() => recargar()}>Actualizar</button>
      </div>

      {todas && (
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

      {cargando && <p className="panel__nota">Cargando comandas...</p>}
      {error && <p className="formulario__error" role="alert">{error}</p>}
      {!cargando && !error && datos?.length === 0 && <p className="panel__nota">{vacio}</p>}
      {!cargando && datos?.length > 0 && (
        <ul className="panel__grilla">
          {datos.map((comanda) => (
            <li key={comanda.id}>
              <TarjetaComanda
                comanda={comanda}
                puedeCambiarLicor={puedeCambiarLicor}
                puedeCancelar={puedeCancelar}
                alActualizar={reemplazar}
                alCancelar={marcarCancelada}
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

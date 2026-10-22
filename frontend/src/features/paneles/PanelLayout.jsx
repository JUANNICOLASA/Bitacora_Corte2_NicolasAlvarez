import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import Ornamento from '../../components/ui/Ornamento'
import Pestanas from '../../components/ui/Pestanas'

export default function PanelLayout({ titulo, lema, vistas }) {
  const [parametros, setParametros] = useSearchParams()
  const pedida = parametros.get('vista')
  const activa = vistas.some((vista) => vista.id === pedida) ? pedida : vistas[0].id
  const vista = vistas.find((opcion) => opcion.id === activa)

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  const cambiar = (id) => {
    const siguientes = new URLSearchParams(parametros)
    siguientes.set('vista', id)
    setParametros(siguientes, { replace: true })
  }

  return (
    <div className="carta panel">
      <header className="carta__portada">
        <Ornamento />
        <h1 className="carta__titulo">{titulo}</h1>
        {lema && <p className="carta__lema">{lema}</p>}
      </header>
      {vistas.length > 1 && (
        <div className="filtros panel__pestanas">
          <Pestanas opciones={vistas} activa={activa} alCambiar={cambiar} etiqueta={`Secciones de ${titulo}`} idPanel="panel-contenido" />
        </div>
      )}
      <div id="panel-contenido" role="tabpanel" aria-labelledby={`pestana-${activa}`} className="panel__contenido">
        {vista.contenido}
      </div>
    </div>
  )
}

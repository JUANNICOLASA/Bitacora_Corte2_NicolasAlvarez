import { IconoLupa } from '../../components/ui/Iconos'

const secciones = [
  { id: 'cocteles', nombre: 'Cócteles' },
  { id: 'cocina', nombre: 'Cocina' }
]

export default function FiltrosCarta({ seccion, alCambiarSeccion, categorias, categoria, alCambiarCategoria, busqueda, alBuscar }) {
  return (
    <div className="filtros">
      <div className="filtros__secciones" role="tablist" aria-label="Secciones de la carta">
        {secciones.map((opcion) => (
          <button
            key={opcion.id}
            type="button"
            role="tab"
            id={`pestana-${opcion.id}`}
            aria-selected={seccion === opcion.id}
            aria-controls="carta-listado"
            className={`filtros__seccion ${seccion === opcion.id ? 'filtros__seccion--activa' : ''}`}
            onClick={() => alCambiarSeccion(opcion.id)}
          >
            {opcion.nombre}
          </button>
        ))}
      </div>

      <div className="filtros__fila">
        <ul className="filtros__categorias" aria-label="Categorías">
          {categorias.map((opcion) => (
            <li key={opcion.id}>
              <button
                type="button"
                className={`chip ${categoria === opcion.id ? 'chip--activo' : ''}`}
                aria-pressed={categoria === opcion.id}
                onClick={() => alCambiarCategoria(opcion.id)}
              >
                {opcion.nombre}
              </button>
            </li>
          ))}
        </ul>

        <label className="buscador">
          <IconoLupa />
          <span className="sr-only">Buscar en la carta</span>
          <input
            type="search"
            value={busqueda}
            onChange={(e) => alBuscar(e.target.value)}
            placeholder={seccion === 'cocteles' ? 'Busca por nombre o destilado' : 'Busca por nombre o ingrediente'}
          />
        </label>
      </div>
    </div>
  )
}

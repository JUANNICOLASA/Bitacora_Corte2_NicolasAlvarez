import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useCarta } from '../../hooks/useCarta'
import { categoriasCocteles } from '../../data/cocteles'
import { categoriasCocina } from '../../data/cocina'
import Ornamento from '../../components/ui/Ornamento'
import FiltrosCarta from './FiltrosCarta'
import ProductoCard from './ProductoCard'
import ProductoDetalle from './ProductoDetalle'

const normalizar = (texto) =>
  texto
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')

export default function CartaPage() {
  const { cocteles, cocina, cargando, error } = useCarta()
  const [parametros, setParametros] = useSearchParams()
  const [categoria, setCategoria] = useState('TODOS')
  const [busqueda, setBusqueda] = useState('')

  const seccion = parametros.get('seccion') === 'cocina' ? 'cocina' : 'cocteles'
  const slugAbierto = parametros.get('item')

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  const actualizarParametros = useCallback(
    (cambios) => {
      const siguientes = new URLSearchParams(parametros)
      Object.entries(cambios).forEach(([clave, valor]) => {
        if (valor) {
          siguientes.set(clave, valor)
        } else {
          siguientes.delete(clave)
        }
      })
      setParametros(siguientes, { replace: true })
    },
    [parametros, setParametros]
  )

  const cambiarSeccion = (nueva) => {
    setCategoria('TODOS')
    setBusqueda('')
    actualizarParametros({ seccion: nueva === 'cocina' ? 'cocina' : null })
  }

  const productos = seccion === 'cocteles' ? cocteles : cocina
  const categorias = seccion === 'cocteles' ? categoriasCocteles : categoriasCocina

  const filtrados = useMemo(() => {
    const termino = normalizar(busqueda.trim())
    return productos.filter((producto) => {
      const enCategoria = categoria === 'TODOS' || producto.categoria === categoria
      const texto = normalizar([producto.nombre, producto.descripcion, producto.destiladoBase ?? '', ...producto.ingredientes].join(' '))
      return enCategoria && (!termino || texto.includes(termino))
    })
  }, [productos, categoria, busqueda])

  const productoAbierto = useMemo(
    () => [...cocteles, ...cocina].find((producto) => producto.slug === slugAbierto) ?? null,
    [cocteles, cocina, slugAbierto]
  )

  const nombreCategoria = categorias.find((opcion) => opcion.id === categoria)?.nombre

  return (
    <div className="carta">
      <header className="carta__portada">
        <Ornamento />
        <h1 className="carta__titulo">La carta</h1>
        <p className="carta__lema">
          Cócteles de autor creados en la casa, los clásicos de siempre y una barra sin alcohol con el mismo cuidado. La cocina acompaña con tablas para compartir, platos fuertes y postres pensados para la noche.
        </p>
      </header>

      <FiltrosCarta
        seccion={seccion}
        alCambiarSeccion={cambiarSeccion}
        categorias={categorias}
        categoria={categoria}
        alCambiarCategoria={setCategoria}
        busqueda={busqueda}
        alBuscar={setBusqueda}
      />

      <div id="carta-listado" role="tabpanel" aria-labelledby={`pestana-${seccion}`} className="carta__listado">
        {cargando && (
          <ul className="carta__grilla" aria-busy="true" aria-label="Cargando la carta">
            {Array.from({ length: 6 }, (_, indice) => (
              <li key={indice} className="producto-cargando" />
            ))}
          </ul>
        )}

        {error && <p className="carta__aviso" role="alert">{error}</p>}

        {!cargando && !error && filtrados.length === 0 && (
          <div className="carta__aviso">
            <p>No encontramos nada con "{busqueda}" en {nombreCategoria?.toLowerCase()}.</p>
            <button
              type="button"
              className="boton boton--secundario"
              onClick={() => {
                setBusqueda('')
                setCategoria('TODOS')
              }}
            >
              Ver toda la carta
            </button>
          </div>
        )}

        {!cargando && !error && filtrados.length > 0 && (
          <>
            <p className="carta__conteo" aria-live="polite">
              {filtrados.length} {filtrados.length === 1 ? 'opción' : 'opciones'} en {nombreCategoria?.toLowerCase()}
            </p>
            <ul className="carta__grilla">
              {filtrados.map((producto) => (
                <li key={producto.slug}>
                  <ProductoCard producto={producto} alAbrir={(slug) => actualizarParametros({ item: slug })} />
                </li>
              ))}
            </ul>
          </>
        )}
      </div>

      {productoAbierto && (
        <ProductoDetalle key={productoAbierto.slug} producto={productoAbierto} alCerrar={() => actualizarParametros({ item: null })} />
      )}
    </div>
  )
}

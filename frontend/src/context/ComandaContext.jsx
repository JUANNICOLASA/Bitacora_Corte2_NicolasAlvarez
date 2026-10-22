import { createContext, useCallback, useContext, useEffect, useMemo, useReducer } from 'react'
import { useAuth } from './AuthContext'

const ComandaContext = createContext(null)
const CLAVE_COMANDA = 'bv_comanda'

const vacia = { items: [], numeroMesa: '' }

const leerComanda = () => {
  try {
    const guardada = localStorage.getItem(CLAVE_COMANDA)
    return guardada ? { ...vacia, ...JSON.parse(guardada) } : vacia
  } catch {
    return vacia
  }
}

const claveDe = (item) =>
  [item.idCoctel, (item.destilado ?? '').trim().toLowerCase(), [...item.modificadores.map((m) => m.id)].sort().join('-')].join('|')

function reducir(estado, accion) {
  switch (accion.tipo) {
    case 'agregar': {
      const clave = claveDe(accion.item)
      const existente = estado.items.find((item) => item.clave === clave)
      if (existente) {
        return {
          ...estado,
          items: estado.items.map((item) =>
            item.clave === clave ? { ...item, cantidad: Math.min(20, item.cantidad + accion.item.cantidad) } : item
          )
        }
      }
      return { ...estado, items: [...estado.items, { ...accion.item, clave }] }
    }
    case 'cantidad':
      return {
        ...estado,
        items: estado.items.map((item) =>
          item.clave === accion.clave ? { ...item, cantidad: Math.min(20, Math.max(1, accion.cantidad)) } : item
        )
      }
    case 'quitar':
      return { ...estado, items: estado.items.filter((item) => item.clave !== accion.clave) }
    case 'mesa':
      return { ...estado, numeroMesa: accion.numeroMesa }
    case 'vaciar':
      return { ...vacia, numeroMesa: estado.numeroMesa }
    case 'reiniciar':
      return vacia
    default:
      return estado
  }
}

export const subtotalDe = (item) =>
  (item.precio + item.modificadores.reduce((suma, modificador) => suma + (modificador.precioExtra ?? 0), 0)) * item.cantidad

export function ComandaProvider({ children }) {
  const { usuario } = useAuth()
  const [comanda, despachar] = useReducer(reducir, undefined, leerComanda)

  useEffect(() => {
    try {
      localStorage.setItem(CLAVE_COMANDA, JSON.stringify({ items: comanda.items, numeroMesa: comanda.numeroMesa }))
    } catch {
      return
    }
  }, [comanda])

  useEffect(() => {
    if (!usuario) {
      despachar({ tipo: 'reiniciar' })
    }
  }, [usuario])

  const agregar = useCallback((item) => despachar({ tipo: 'agregar', item }), [])
  const cambiarCantidad = useCallback((clave, cantidad) => despachar({ tipo: 'cantidad', clave, cantidad }), [])
  const quitar = useCallback((clave) => despachar({ tipo: 'quitar', clave }), [])
  const cambiarMesa = useCallback((numeroMesa) => despachar({ tipo: 'mesa', numeroMesa }), [])
  const vaciar = useCallback(() => despachar({ tipo: 'vaciar' }), [])

  const valor = useMemo(() => {
    const unidades = comanda.items.reduce((suma, item) => suma + item.cantidad, 0)
    const total = comanda.items.reduce((suma, item) => suma + subtotalDe(item), 0)
    return { ...comanda, unidades, total, agregar, cambiarCantidad, quitar, cambiarMesa, vaciar }
  }, [comanda, agregar, cambiarCantidad, quitar, cambiarMesa, vaciar])

  return <ComandaContext.Provider value={valor}>{children}</ComandaContext.Provider>
}

export const useComanda = () => useContext(ComandaContext)

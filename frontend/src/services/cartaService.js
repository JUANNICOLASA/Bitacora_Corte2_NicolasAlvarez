import axiosClient from '../api/axiosClient'
import { cocteles as fichasCocteles } from '../data/cocteles'
import { cocina as fichasCocina } from '../data/cocina'
import { normalizar, slugDe } from '../utils'

const fichaPorNombre = (fichas) => new Map(fichas.map((ficha) => [normalizar(ficha.nombre), ficha]))
const fichasDeCocteles = fichaPorNombre(fichasCocteles)
const fichasDePlatos = fichaPorNombre(fichasCocina)

const distribucionVacia = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }

const calificacionDe = (resumenes, tipo, id) => {
  const resumen = resumenes.find((item) => item.tipo === tipo && item.idProducto === id)
  if (!resumen) {
    return { promedio: 0, total: 0, distribucion: { ...distribucionVacia } }
  }
  return { promedio: resumen.promedio, total: resumen.total, distribucion: { ...distribucionVacia, ...resumen.distribucion } }
}

const aCoctel = (coctel, resumenes) => {
  const ficha = fichasDeCocteles.get(normalizar(coctel.nombre))
  return {
    id: coctel.id,
    slug: ficha?.slug ?? slugDe(coctel.nombre),
    tipo: 'coctel',
    tipoProducto: 'COCTEL',
    categoria: coctel.categoria,
    nombre: ficha?.nombre ?? coctel.nombre,
    descripcion: coctel.descripcion || ficha?.descripcion || '',
    historia: ficha?.historia ?? null,
    precio: coctel.precio,
    imagen: ficha?.imagen ?? null,
    tipoBebida: coctel.tipo,
    destiladoBase: coctel.destiladoBase,
    graduacion: ficha?.graduacion ?? null,
    disponible: coctel.disponible,
    ingredientes: ficha?.ingredientes ?? [],
    maridaje: ficha?.maridaje ?? null,
    calificacion: calificacionDe(resumenes, 'COCTEL', coctel.id)
  }
}

const aPlato = (plato, resumenes) => {
  const ficha = fichasDePlatos.get(normalizar(plato.nombre))
  return {
    id: plato.id,
    slug: ficha?.slug ?? slugDe(plato.nombre),
    tipo: 'cocina',
    tipoProducto: 'PLATO',
    categoria: plato.categoria,
    nombre: ficha?.nombre ?? plato.nombre,
    descripcion: plato.descripcion || ficha?.descripcion || '',
    historia: ficha?.historia ?? null,
    precio: plato.precio,
    imagen: ficha?.imagen ?? null,
    porcion: plato.porcion || ficha?.porcion || 'Individual',
    disponible: plato.disponible,
    ingredientes: ficha?.ingredientes ?? [],
    maridaje: ficha?.maridaje ?? null,
    calificacion: calificacionDe(resumenes, 'PLATO', plato.id)
  }
}

export const cartaService = {
  async listarResumenes() {
    try {
      const { data } = await axiosClient.get('/resenas/resumen')
      return data
    } catch {
      return []
    }
  },

  async listarCocteles(resumenes = []) {
    const { data } = await axiosClient.get('/menu')
    return data.map((coctel) => aCoctel(coctel, resumenes))
  },

  async listarCocina(resumenes = []) {
    const { data } = await axiosClient.get('/platos')
    return data.map((plato) => aPlato(plato, resumenes))
  },

  async obtenerCoctel(idCoctel) {
    const { data } = await axiosClient.get(`/menu/${idCoctel}`)
    return data
  },

  async idsDisponibles() {
    const { data } = await axiosClient.get('/menu/disponibles')
    return new Set(data.map((coctel) => coctel.id))
  },

  async listarModificadores(idCoctel) {
    const { data } = await axiosClient.get(`/menu/${idCoctel}/modificadores`)
    return data
  },

  async obtenerReceta(idCoctel) {
    const { data } = await axiosClient.get(`/menu/${idCoctel}/receta`)
    return data
  },

  async listarResenas(producto) {
    const { data } = await axiosClient.get('/resenas', { params: { tipo: producto.tipoProducto, idProducto: producto.id } })
    return data
  },

  async calificar(producto, resena) {
    const { data } = await axiosClient.post('/resenas', { tipo: producto.tipoProducto, idProducto: producto.id, ...resena })
    return data
  }
}

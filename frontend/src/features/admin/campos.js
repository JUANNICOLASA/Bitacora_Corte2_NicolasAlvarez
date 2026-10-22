export const categoriasDeCocteles = [
  { id: 'DE_AUTOR', nombre: 'De autor' },
  { id: 'CLASICO', nombre: 'Clásico' },
  { id: 'SOUR', nombre: 'Sour' },
  { id: 'TROPICAL', nombre: 'Tropical' },
  { id: 'SIN_ALCOHOL', nombre: 'Sin alcohol' }
]

export const categoriasDePlatos = [
  { id: 'PARA_COMPARTIR', nombre: 'Para compartir' },
  { id: 'PLATOS_FUERTES', nombre: 'Plato fuerte' },
  { id: 'POSTRES', nombre: 'Postre' }
]

export const nombreDe = (opciones, id) => opciones.find((opcion) => opcion.id === id)?.nombre ?? id

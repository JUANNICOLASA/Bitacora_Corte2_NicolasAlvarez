export const formatoPrecio = (valor) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor ?? 0)

export const formatoFecha = (fecha, opciones = { day: 'numeric', month: 'long', year: 'numeric' }) =>
  new Date(`${fecha.slice(0, 10)}T12:00:00`).toLocaleDateString('es-CO', opciones)

export const formatoFechaHora = (fechaHora) =>
  new Date(fechaHora).toLocaleString('es-CO', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })

export const formatoHora = (fechaHora) =>
  new Date(fechaHora).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })

export const hoy = () => {
  const fecha = new Date()
  fecha.setMinutes(fecha.getMinutes() - fecha.getTimezoneOffset())
  return fecha.toISOString().slice(0, 10)
}

export const normalizar = (texto = '') =>
  texto
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .trim()

export const slugDe = (texto) =>
  normalizar(texto)
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')

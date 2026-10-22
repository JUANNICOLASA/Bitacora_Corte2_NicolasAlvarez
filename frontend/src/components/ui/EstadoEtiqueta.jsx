const textos = {
  RECIBIDO: 'Recibida',
  EN_PREPARACION: 'En preparación',
  LISTO: 'Lista para entregar',
  ENTREGADO: 'Entregada',
  CANCELADO: 'Cancelada',
  CONFIRMADA: 'Confirmada',
  CUMPLIDA: 'Cumplida',
  CANCELADA: 'Cancelada'
}

export const textoDeEstado = (estado) => textos[estado] ?? estado

export default function EstadoEtiqueta({ estado }) {
  return <span className={`estado estado--${estado.toLowerCase()}`}>{textoDeEstado(estado)}</span>
}

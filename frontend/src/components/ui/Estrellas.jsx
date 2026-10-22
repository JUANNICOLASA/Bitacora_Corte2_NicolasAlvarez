export default function Estrellas({ valor, tamano = 16, etiqueta }) {
  const porcentaje = Math.max(0, Math.min(100, (valor / 5) * 100))
  return (
    <span
      className="estrellas"
      style={{ '--tamano': `${tamano}px`, '--relleno': `${porcentaje}%` }}
      role="img"
      aria-label={etiqueta ?? `${valor.toFixed(1)} de 5 estrellas`}
    >
      <span className="estrellas__fondo" aria-hidden="true">★★★★★</span>
      <span className="estrellas__relleno" aria-hidden="true">★★★★★</span>
    </span>
  )
}

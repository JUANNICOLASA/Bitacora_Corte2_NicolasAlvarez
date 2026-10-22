import { useState } from 'react'

const iniciales = (texto) =>
  texto
    .split(' ')
    .filter((palabra) => palabra.length > 2)
    .slice(0, 2)
    .map((palabra) => palabra[0])
    .join('')

export default function ImagenConRespaldo({ src, alt, tono = 'azul', className = '' }) {
  const [fallo, setFallo] = useState(false)

  if (fallo || !src) {
    return (
      <div className={`imagen-respaldo imagen-respaldo--${tono} ${className}`} role="img" aria-label={alt}>
        <span className="imagen-respaldo__inicial" aria-hidden="true">{iniciales(alt)}</span>
      </div>
    )
  }

  return <img className={className} src={src} alt={alt} loading="lazy" onError={() => setFallo(true)} />
}

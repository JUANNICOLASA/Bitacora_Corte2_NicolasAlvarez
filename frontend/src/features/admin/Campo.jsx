export default function Campo({ id, etiqueta, error, ancho, opcional, children }) {
  return (
    <div className={`campo ${ancho ? 'campo--ancho' : ''}`}>
      <label htmlFor={id}>
        {etiqueta} {opcional && <span className="campo__opcional">(opcional)</span>}
      </label>
      {children}
      {error && <p id={`${id}-error`} className="campo__error">{error}</p>}
    </div>
  )
}

export const propiedades = (prefijo, nombre, formulario, cambiar, errores) => ({
  id: `${prefijo}-${nombre}`,
  name: nombre,
  value: formulario[nombre] ?? '',
  onChange: cambiar,
  'aria-invalid': Boolean(errores[nombre]),
  'aria-describedby': errores[nombre] ? `${prefijo}-${nombre}-error` : undefined
})

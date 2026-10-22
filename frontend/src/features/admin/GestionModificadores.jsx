import { useState } from 'react'
import { modificadorService } from '../../services/inventarioService'
import { useConsulta } from '../../hooks/useConsulta'
import { mensajeDeError } from '../../api/axiosClient'
import Disponibilidad from '../../components/ui/Disponibilidad'
import Campo, { propiedades } from './Campo'
import { formatoPrecio } from '../../utils'

const vacio = { nombre: '', graduacionAlcoholica: '0', precioExtra: '0', disponible: true }

export default function GestionModificadores() {
  const { datos, setDatos, cargando, error } = useConsulta(() => modificadorService.listar())
  const [formulario, setFormulario] = useState(vacio)
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (e) => {
    const { name, value, type, checked } = e.target
    setFormulario((previo) => ({ ...previo, [name]: type === 'checkbox' ? checked : value }))
  }

  const guardar = async (e) => {
    e.preventDefault()
    const encontrados = {}
    if (formulario.nombre.trim().length < 2) {
      encontrados.nombre = 'Escribe el nombre del modificador.'
    }
    const graduacion = Number(formulario.graduacionAlcoholica)
    if (Number.isNaN(graduacion) || graduacion < 0 || graduacion > 100) {
      encontrados.graduacionAlcoholica = 'La graduación va de 0 a 100. Usa 0 si no tiene alcohol.'
    }
    if (!(Number(formulario.precioExtra) >= 0)) {
      encontrados.precioExtra = 'El precio extra no puede ser negativo.'
    }
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) {
      return
    }
    setEnviando(true)
    setErrorGeneral(null)
    try {
      const creado = await modificadorService.crear({
        nombre: formulario.nombre.trim(),
        graduacionAlcoholica: graduacion,
        precioExtra: Number(formulario.precioExtra),
        disponible: formulario.disponible
      })
      setDatos((lista) => [...lista, creado])
      setAviso(`${creado.nombre} ya se puede agregar a los cócteles.`)
      setFormulario(vacio)
    } catch (err) {
      setErrorGeneral(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  const cambiarDisponibilidad = async (id, disponible) => {
    const actualizado = await modificadorService.cambiarDisponibilidad(id, disponible)
    setDatos((lista) => lista.map((item) => (item.id === id ? actualizado : item)))
  }

  const p = (nombre) => propiedades('modificador', nombre, formulario, cambiar, errores)

  return (
    <div className="panel__columnas panel__columnas--gestion">
      <section className="panel__seccion" aria-labelledby="lista-modificadores">
        <h2 id="lista-modificadores" className="panel__subtitulo">Modificadores</h2>
        <p className="panel__nota">Los modificadores con alcohol quedan bloqueados en los mocktails.</p>
        {aviso && <p className="calificar__gracias" role="status">{aviso}</p>}
        {cargando && <p className="panel__nota">Cargando...</p>}
        {error && <p className="formulario__error" role="alert">{error}</p>}
        {datos && (
          <ul className="registro">
            {datos.map((item) => (
              <li key={item.id} className="registro__fila">
                <div className="registro__datos">
                  <span className="registro__nombre">{item.nombre}</span>
                  <span className="registro__detalle">
                    {item.graduacionAlcoholica > 0 ? `${item.graduacionAlcoholica} % vol.` : 'Sin alcohol'} · {formatoPrecio(item.precioExtra)}
                  </span>
                </div>
                <Disponibilidad nombre={item.nombre} disponible={item.disponible} alCambiar={(valor) => cambiarDisponibilidad(item.id, valor)} />
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel__seccion panel__tarjeta" aria-labelledby="nuevo-modificador">
        <h2 id="nuevo-modificador" className="panel__subtitulo">Nuevo modificador</h2>
        <form className="formulario" onSubmit={guardar} noValidate>
          <Campo id="modificador-nombre" etiqueta="Nombre" error={errores.nombre} ancho>
            <input type="text" maxLength="60" {...p('nombre')} />
          </Campo>
          <Campo id="modificador-graduacionAlcoholica" etiqueta="Graduación (% vol.)" error={errores.graduacionAlcoholica}>
            <input type="number" min="0" max="100" step="0.5" {...p('graduacionAlcoholica')} />
          </Campo>
          <Campo id="modificador-precioExtra" etiqueta="Precio extra (COP)" error={errores.precioExtra}>
            <input type="number" min="0" step="500" {...p('precioExtra')} />
          </Campo>
          <label className="casilla campo--ancho">
            <input type="checkbox" name="disponible" checked={formulario.disponible} onChange={cambiar} /> Disponible en barra
          </label>
          {errorGeneral && <p className="formulario__error" role="alert">{errorGeneral}</p>}
          <button type="submit" className="boton boton--primario formulario__enviar" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Crear modificador'}
          </button>
        </form>
      </section>
    </div>
  )
}

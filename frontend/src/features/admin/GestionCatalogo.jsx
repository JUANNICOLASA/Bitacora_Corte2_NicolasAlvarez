import { useState } from 'react'
import { useConsulta } from '../../hooks/useConsulta'
import { mensajeDeError } from '../../api/axiosClient'
import Disponibilidad from '../../components/ui/Disponibilidad'
import Confirmar from '../../components/ui/Confirmar'
import { formatoPrecio } from '../../utils'

export default function GestionCatalogo({ titulo, singular, servicio, vacio, validar, aPeticion, Campos, detalle, textoAgotado }) {
  const { datos, setDatos, cargando, error } = useConsulta(() => servicio.listar())
  const [editando, setEditando] = useState(null)
  const [formulario, setFormulario] = useState(vacio)
  const [errores, setErrores] = useState({})
  const [errorGeneral, setErrorGeneral] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (e) => {
    const { name, value, type, checked } = e.target
    setFormulario((previo) => ({ ...previo, [name]: type === 'checkbox' ? checked : value }))
    if (errores[name]) {
      setErrores((previo) => ({ ...previo, [name]: undefined }))
    }
  }

  const reiniciar = () => {
    setEditando(null)
    setFormulario(vacio)
    setErrores({})
    setErrorGeneral(null)
  }

  const editar = async (item) => {
    setErrores({})
    setAviso(null)
    setErrorGeneral(null)
    try {
      const actual = await servicio.obtener(item.id)
      setDatos((lista) => lista.map((otro) => (otro.id === actual.id ? actual : otro)))
      setEditando(actual.id)
      setFormulario(Object.fromEntries(Object.keys(vacio).map((clave) => [clave, actual[clave] ?? vacio[clave]])))
      document.getElementById(`formulario-${singular}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    } catch (err) {
      setErrorGeneral(mensajeDeError(err))
    }
  }

  const guardar = async (e) => {
    e.preventDefault()
    const encontrados = validar(formulario)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) {
      return
    }
    setEnviando(true)
    setErrorGeneral(null)
    try {
      const peticion = aPeticion(formulario)
      if (editando) {
        const actualizado = await servicio.actualizar(editando, peticion)
        setDatos((lista) => lista.map((item) => (item.id === editando ? actualizado : item)))
        setAviso(`${actualizado.nombre} quedó actualizado.`)
      } else {
        const creado = await servicio.crear(peticion)
        setDatos((lista) => [...lista, creado])
        setAviso(`${creado.nombre} ya está en la carta.`)
      }
      reiniciar()
    } catch (err) {
      setErrorGeneral(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
  }

  const eliminar = async (item) => {
    setAviso(null)
    try {
      await servicio.eliminar(item.id)
      setDatos((lista) => lista.filter((otro) => otro.id !== item.id))
      setAviso(`${item.nombre} salió de la carta.`)
      if (editando === item.id) {
        reiniciar()
      }
    } catch (err) {
      setErrorGeneral(mensajeDeError(err))
    }
  }

  const cambiarDisponibilidad = async (id, disponible) => {
    const actualizado = await servicio.cambiarDisponibilidad(id, disponible)
    setDatos((lista) => lista.map((item) => (item.id === id ? actualizado : item)))
  }

  return (
    <div className="panel__columnas panel__columnas--gestion">
      <section className="panel__seccion" aria-labelledby={`lista-${singular}`}>
        <h2 id={`lista-${singular}`} className="panel__subtitulo">{titulo}</h2>
        {aviso && <p className="calificar__gracias" role="status">{aviso}</p>}
        {cargando && <p className="panel__nota">Cargando...</p>}
        {error && <p className="formulario__error" role="alert">{error}</p>}
        {datos && (
          <ul className="registro">
            {datos.map((item) => (
              <li key={item.id} className={`registro__fila ${editando === item.id ? 'registro__fila--activa' : ''}`}>
                <div className="registro__datos">
                  <span className="registro__nombre">{item.nombre}</span>
                  <span className="registro__detalle">{detalle(item)} · {formatoPrecio(item.precio)}</span>
                  <Disponibilidad nombre={item.nombre} disponible={item.disponible} textoAgotado={textoAgotado} alCambiar={(valor) => cambiarDisponibilidad(item.id, valor)} />
                </div>
                <div className="registro__acciones">
                  <button type="button" className="boton boton--secundario boton--compacto" onClick={() => editar(item)}>Editar</button>
                  <Confirmar texto="Eliminar" pregunta={`¿Eliminar ${item.nombre}?`} alConfirmar={() => eliminar(item)} />
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section id={`formulario-${singular}`} className="panel__seccion panel__tarjeta" aria-labelledby={`titulo-formulario-${singular}`}>
        <h2 id={`titulo-formulario-${singular}`} className="panel__subtitulo">
          {editando ? `Editar ${singular}` : `Nuevo ${singular}`}
        </h2>
        <form className="formulario" onSubmit={guardar} noValidate>
          <Campos formulario={formulario} cambiar={cambiar} errores={errores} />
          {errorGeneral && <p className="formulario__error" role="alert">{errorGeneral}</p>}
          <div className="formulario__enviar panel__botones">
            <button type="submit" className="boton boton--primario" disabled={enviando}>
              {enviando ? 'Guardando...' : editando ? 'Guardar cambios' : `Crear ${singular}`}
            </button>
            {editando && (
              <button type="button" className="boton boton--secundario" onClick={reiniciar}>Cancelar edición</button>
            )}
          </div>
        </form>
      </section>
    </div>
  )
}

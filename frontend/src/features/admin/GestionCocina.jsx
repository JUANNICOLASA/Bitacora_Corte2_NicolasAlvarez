import { platoService } from '../../services/inventarioService'
import GestionCatalogo from './GestionCatalogo'
import Campo, { propiedades } from './Campo'
import { categoriasDePlatos, nombreDe } from './campos'

const vacio = { nombre: '', descripcion: '', precio: '', categoria: 'PARA_COMPARTIR', porcion: '', disponible: true }

const validar = (datos) => {
  const errores = {}
  if (datos.nombre.trim().length < 2) {
    errores.nombre = 'Escribe el nombre del plato.'
  }
  if (!(Number(datos.precio) > 0)) {
    errores.precio = 'El precio debe ser mayor a cero.'
  }
  return errores
}

const aPeticion = (datos) => ({
  nombre: datos.nombre.trim(),
  descripcion: datos.descripcion.trim() || null,
  precio: Number(datos.precio),
  categoria: datos.categoria,
  porcion: datos.porcion.trim() || null,
  disponible: datos.disponible
})

function Campos({ formulario, cambiar, errores }) {
  const p = (nombre) => propiedades('plato', nombre, formulario, cambiar, errores)
  return (
    <>
      <Campo id="plato-nombre" etiqueta="Nombre" error={errores.nombre} ancho>
        <input type="text" maxLength="60" {...p('nombre')} />
      </Campo>
      <Campo id="plato-descripcion" etiqueta="Descripción para la carta" ancho opcional>
        <textarea rows="2" maxLength="250" {...p('descripcion')} />
      </Campo>
      <Campo id="plato-precio" etiqueta="Precio (COP)" error={errores.precio}>
        <input type="number" min="1" step="500" inputMode="numeric" {...p('precio')} />
      </Campo>
      <Campo id="plato-categoria" etiqueta="Categoría">
        <select {...p('categoria')}>
          {categoriasDePlatos.map((opcion) => (
            <option key={opcion.id} value={opcion.id}>{opcion.nombre}</option>
          ))}
        </select>
      </Campo>
      <Campo id="plato-porcion" etiqueta="Porción" ancho opcional>
        <input type="text" maxLength="60" placeholder="Individual, para compartir, 300 g..." {...p('porcion')} />
      </Campo>
      <label className="casilla campo--ancho">
        <input type="checkbox" name="disponible" checked={formulario.disponible} onChange={cambiar} /> Disponible en la cocina
      </label>
    </>
  )
}

export default function GestionCocina() {
  return (
    <GestionCatalogo
      titulo="Platos de la cocina"
      singular="plato"
      servicio={platoService}
      vacio={vacio}
      validar={validar}
      aPeticion={aPeticion}
      Campos={Campos}
      textoAgotado="AGOTADO EN COCINA"
      detalle={(plato) => `${nombreDe(categoriasDePlatos, plato.categoria)}${plato.porcion ? ` · ${plato.porcion}` : ''}`}
    />
  )
}

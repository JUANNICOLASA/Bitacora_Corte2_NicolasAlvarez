import { coctelService } from '../../services/inventarioService'
import GestionCatalogo from './GestionCatalogo'
import Campo, { propiedades } from './Campo'
import { categoriasDeCocteles, nombreDe } from './campos'

const vacio = { nombre: '', descripcion: '', precio: '', categoria: 'DE_AUTOR', tipo: 'ALCOHOLICA', destiladoBase: '', disponible: true }

const validar = (datos) => {
  const errores = {}
  if (datos.nombre.trim().length < 2) {
    errores.nombre = 'Escribe el nombre del cóctel.'
  }
  if (!(Number(datos.precio) > 0)) {
    errores.precio = 'El precio debe ser mayor a cero.'
  }
  if (datos.tipo === 'ALCOHOLICA' && datos.destiladoBase.trim().length === 0) {
    errores.destiladoBase = 'Un cóctel con alcohol debe indicar su destilado base.'
  }
  return errores
}

const aPeticion = (datos) => ({
  nombre: datos.nombre.trim(),
  descripcion: datos.descripcion.trim() || null,
  precio: Number(datos.precio),
  categoria: datos.categoria,
  tipo: datos.tipo,
  destiladoBase: datos.tipo === 'MOCKTAIL' ? null : datos.destiladoBase.trim(),
  disponible: datos.disponible
})

function Campos({ formulario, cambiar, errores }) {
  const p = (nombre) => propiedades('coctel', nombre, formulario, cambiar, errores)
  return (
    <>
      <Campo id="coctel-nombre" etiqueta="Nombre" error={errores.nombre} ancho>
        <input type="text" maxLength="60" {...p('nombre')} />
      </Campo>
      <Campo id="coctel-descripcion" etiqueta="Descripción para la carta" ancho opcional>
        <textarea rows="2" maxLength="250" {...p('descripcion')} />
      </Campo>
      <Campo id="coctel-precio" etiqueta="Precio (COP)" error={errores.precio}>
        <input type="number" min="1" step="500" inputMode="numeric" {...p('precio')} />
      </Campo>
      <Campo id="coctel-categoria" etiqueta="Categoría">
        <select {...p('categoria')}>
          {categoriasDeCocteles.map((opcion) => (
            <option key={opcion.id} value={opcion.id}>{opcion.nombre}</option>
          ))}
        </select>
      </Campo>
      <Campo id="coctel-tipo" etiqueta="Tipo de bebida">
        <select {...p('tipo')}>
          <option value="ALCOHOLICA">Con alcohol</option>
          <option value="MOCKTAIL">Sin alcohol (mocktail)</option>
        </select>
      </Campo>
      <Campo id="coctel-destiladoBase" etiqueta="Destilado base" error={errores.destiladoBase}>
        <input type="text" maxLength="40" disabled={formulario.tipo === 'MOCKTAIL'} placeholder={formulario.tipo === 'MOCKTAIL' ? 'No aplica' : 'Ginebra, ron, whisky...'} {...p('destiladoBase')} />
      </Campo>
      <label className="casilla campo--ancho">
        <input type="checkbox" name="disponible" checked={formulario.disponible} onChange={cambiar} /> Disponible en barra
      </label>
    </>
  )
}

export default function GestionCocteles() {
  return (
    <GestionCatalogo
      titulo="Cócteles de la carta"
      singular="cóctel"
      servicio={coctelService}
      vacio={vacio}
      validar={validar}
      aPeticion={aPeticion}
      Campos={Campos}
      detalle={(coctel) => `${nombreDe(categoriasDeCocteles, coctel.categoria)} · ${coctel.tipo === 'MOCKTAIL' ? 'Sin alcohol' : coctel.destiladoBase}`}
    />
  )
}

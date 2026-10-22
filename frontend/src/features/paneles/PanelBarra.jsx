import TableroBarra from '../comandas/TableroBarra'
import Inventario from '../inventario/Inventario'
import PanelLayout from './PanelLayout'

export default function PanelBarra() {
  return (
    <PanelLayout
      titulo="La barra"
      lema="Las comandas llegan en orden. Avanza cada una a medida que la preparas y marca lo que se agote."
      vistas={[
        { id: 'tablero', nombre: 'Tablero', contenido: <TableroBarra puedeAvanzar /> },
        { id: 'inventario', nombre: 'Inventario', contenido: <Inventario /> }
      ]}
    />
  )
}

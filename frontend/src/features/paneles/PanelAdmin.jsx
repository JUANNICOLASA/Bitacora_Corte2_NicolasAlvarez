import TableroBarra from '../comandas/TableroBarra'
import ListaComandas from '../comandas/ListaComandas'
import AgendaReservas from '../reservas/AgendaReservas'
import GestionCocteles from '../admin/GestionCocteles'
import GestionCocina from '../admin/GestionCocina'
import GestionModificadores from '../admin/GestionModificadores'
import HistorialEventos from '../admin/HistorialEventos'
import PanelLayout from './PanelLayout'

export default function PanelAdmin() {
  return (
    <PanelLayout
      titulo="Administración"
      lema="La carta, la barra, la agenda y el historial de la casa en un solo lugar."
      vistas={[
        { id: 'cocteles', nombre: 'Cócteles', contenido: <GestionCocteles /> },
        { id: 'cocina', nombre: 'Cocina', contenido: <GestionCocina /> },
        { id: 'modificadores', nombre: 'Modificadores', contenido: <GestionModificadores /> },
        {
          id: 'comandas',
          nombre: 'Comandas',
          contenido: (
            <>
              <TableroBarra puedeAvanzar={false} />
              <ListaComandas titulo="Todas las comandas" todas puedeCancelar vacio="No hay comandas con este estado." />
            </>
          )
        },
        { id: 'agenda', nombre: 'Agenda', contenido: <AgendaReservas /> },
        { id: 'historial', nombre: 'Historial', contenido: <HistorialEventos /> }
      ]}
    />
  )
}

import { useState } from 'react'
import BandejaComanda from '../comandas/BandejaComanda'
import ListaComandas from '../comandas/ListaComandas'
import AgendaReservas from '../reservas/AgendaReservas'
import PanelLayout from './PanelLayout'

export default function PanelMesero() {
  const [enviadas, setEnviadas] = useState(0)

  return (
    <PanelLayout
      titulo="Servicio"
      lema="Toma las comandas en la mesa, cambia el licor antes de que la barra empiece y revisa la agenda de reservas."
      vistas={[
        {
          id: 'comandas',
          nombre: 'Comandas',
          contenido: (
            <div className="panel__columnas">
              <BandejaComanda alEnviar={() => setEnviadas((valor) => valor + 1)} />
              <ListaComandas titulo="Comandas del salón" todas puedeCambiarLicor puedeCancelar recarga={enviadas} vacio="No hay comandas con este estado." />
            </div>
          )
        },
        { id: 'agenda', nombre: 'Agenda', contenido: <AgendaReservas /> }
      ]}
    />
  )
}

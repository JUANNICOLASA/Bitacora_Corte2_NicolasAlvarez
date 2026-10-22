import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import BandejaComanda from '../comandas/BandejaComanda'
import ListaComandas from '../comandas/ListaComandas'
import MisReservas from '../reservas/MisReservas'
import PanelLayout from './PanelLayout'

export default function PanelCliente() {
  const { usuario } = useAuth()
  const [enviadas, setEnviadas] = useState(0)

  return (
    <PanelLayout
      titulo="Mi cuenta"
      lema={`Hola, ${usuario.nombre}. Desde aquí envías tu comanda a la barra, sigues su preparación y revisas tus reservas.`}
      vistas={[
        {
          id: 'comanda',
          nombre: 'Mi comanda',
          contenido: (
            <div className="panel__columnas">
              <BandejaComanda alEnviar={() => setEnviadas((valor) => valor + 1)} />
              <ListaComandas titulo="Mis comandas" puedeCambiarLicor recarga={enviadas} vacio="Aún no has enviado comandas." />
            </div>
          )
        },
        { id: 'reservas', nombre: 'Mis reservas', contenido: <MisReservas /> }
      ]}
    />
  )
}

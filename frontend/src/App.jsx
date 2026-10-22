import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ComandaProvider } from './context/ComandaContext'
import { ROLES } from './context/roles'
import Header from './components/layout/Header'
import Footer from './components/layout/Footer'
import RutaProtegida from './components/routing/RutaProtegida'
import HomePage from './pages/HomePage'
import NotFoundPage from './pages/NotFoundPage'
import CartaPage from './features/carta/CartaPage'
import AccesoPage from './features/auth/AccesoPage'
import PanelCliente from './features/paneles/PanelCliente'
import PanelMesero from './features/paneles/PanelMesero'
import PanelBarra from './features/paneles/PanelBarra'
import PanelAdmin from './features/paneles/PanelAdmin'

export default function App() {
  return (
    <AuthProvider>
      <ComandaProvider>
        <BrowserRouter>
          <a href="#contenido" className="saltar-contenido">Saltar al contenido</a>
          <Header />
          <main id="contenido">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/carta" element={<CartaPage />} />
              <Route path="/acceso" element={<AccesoPage />} />
              <Route path="/mi-cuenta" element={<RutaProtegida roles={[ROLES.CLIENTE]}><PanelCliente /></RutaProtegida>} />
              <Route path="/servicio" element={<RutaProtegida roles={[ROLES.MESERO]}><PanelMesero /></RutaProtegida>} />
              <Route path="/barra" element={<RutaProtegida roles={[ROLES.BARTENDER]}><PanelBarra /></RutaProtegida>} />
              <Route path="/administracion" element={<RutaProtegida roles={[ROLES.ADMIN]}><PanelAdmin /></RutaProtegida>} />
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </main>
          <Footer />
        </BrowserRouter>
      </ComandaProvider>
    </AuthProvider>
  )
}

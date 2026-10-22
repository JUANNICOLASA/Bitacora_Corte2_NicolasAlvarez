import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { espacioPorRol } from '../../context/roles'

export default function RutaProtegida({ roles, children }) {
  const { usuario } = useAuth()
  const ubicacion = useLocation()

  if (!usuario) {
    return <Navigate to="/acceso" replace state={{ desde: `${ubicacion.pathname}${ubicacion.search}` }} />
  }
  if (!roles.includes(usuario.rol)) {
    return <Navigate to={espacioPorRol[usuario.rol]?.ruta ?? '/'} replace />
  }
  return children
}

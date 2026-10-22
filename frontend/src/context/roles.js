export const ROLES = {
  ADMIN: 'ADMIN',
  BARTENDER: 'BARTENDER',
  MESERO: 'MESERO',
  CLIENTE: 'CLIENTE'
}

export const espacioPorRol = {
  CLIENTE: { ruta: '/mi-cuenta', texto: 'Mi cuenta' },
  MESERO: { ruta: '/servicio', texto: 'Servicio' },
  BARTENDER: { ruta: '/barra', texto: 'La barra' },
  ADMIN: { ruta: '/administracion', texto: 'Administración' }
}

export const nombreDelRol = {
  CLIENTE: 'cliente',
  MESERO: 'mesero',
  BARTENDER: 'bartender',
  ADMIN: 'administrador'
}

export const puedePedir = (usuario) => usuario?.rol === ROLES.CLIENTE || usuario?.rol === ROLES.MESERO

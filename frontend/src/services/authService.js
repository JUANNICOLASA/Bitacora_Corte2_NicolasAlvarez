import axiosClient from '../api/axiosClient'

export const authService = {
  async login(email, password) {
    const { data } = await axiosClient.post('/auth/login', { email, password })
    return data
  },

  async registrar(datos) {
    const { data } = await axiosClient.post('/auth/registro', datos)
    return data
  },

  async perfil(token) {
    const { data } = await axiosClient.get('/auth/me', { headers: { Authorization: `Bearer ${token}` } })
    return data
  }
}

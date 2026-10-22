import axiosClient from '../api/axiosClient'

export const reservaService = {
  async crear(reserva) {
    const { data } = await axiosClient.post('/reservas', reserva)
    return data
  },

  async listar(fecha) {
    const { data } = await axiosClient.get('/reservas', { params: fecha ? { fecha } : {} })
    return data
  },

  async listarMias() {
    const { data } = await axiosClient.get('/reservas/mias')
    return data
  },

  async cambiarEstado(id, estado) {
    const { data } = await axiosClient.patch(`/reservas/${id}/estado`, { estado })
    return data
  },

  async cancelar(id) {
    await axiosClient.delete(`/reservas/${id}`)
  }
}

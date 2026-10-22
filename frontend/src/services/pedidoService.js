import axiosClient from '../api/axiosClient'

export const pedidoService = {
  async crear(comanda) {
    const { data } = await axiosClient.post('/pedidos', comanda)
    return data
  },

  async listarMias() {
    const { data } = await axiosClient.get('/pedidos/mias')
    return data
  },

  async listar(estado) {
    const { data } = await axiosClient.get('/pedidos', { params: estado ? { estado } : {} })
    return data
  },

  async obtener(id) {
    const { data } = await axiosClient.get(`/pedidos/${id}`)
    return data
  },

  async listarActivas() {
    const { data } = await axiosClient.get('/pedidos/activos')
    return data
  },

  async cambiarEstado(id, estado) {
    const { data } = await axiosClient.patch(`/pedidos/${id}/estado`, { estado })
    return data
  },

  async cambiarDestilado(id, idItem, destilado) {
    const { data } = await axiosClient.patch(`/pedidos/${id}/items/${idItem}/destilado`, { destilado })
    return data
  },

  async cancelar(id) {
    await axiosClient.delete(`/pedidos/${id}`)
  }
}

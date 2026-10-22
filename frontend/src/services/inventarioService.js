import axiosClient from '../api/axiosClient'

export const coctelService = {
  async listar() {
    const { data } = await axiosClient.get('/cocteles')
    return data
  },

  async crear(coctel) {
    const { data } = await axiosClient.post('/cocteles', coctel)
    return data
  },

  async actualizar(id, coctel) {
    const { data } = await axiosClient.put(`/cocteles/${id}`, coctel)
    return data
  },

  async cambiarDisponibilidad(id, disponible) {
    const { data } = await axiosClient.patch(`/cocteles/${id}/disponibilidad`, { disponible })
    return data
  },

  async eliminar(id) {
    await axiosClient.delete(`/cocteles/${id}`)
  }
}

export const platoService = {
  async listar() {
    const { data } = await axiosClient.get('/platos')
    return data
  },

  async crear(plato) {
    const { data } = await axiosClient.post('/platos', plato)
    return data
  },

  async actualizar(id, plato) {
    const { data } = await axiosClient.put(`/platos/${id}`, plato)
    return data
  },

  async cambiarDisponibilidad(id, disponible) {
    const { data } = await axiosClient.patch(`/platos/${id}/disponibilidad`, { disponible })
    return data
  },

  async eliminar(id) {
    await axiosClient.delete(`/platos/${id}`)
  }
}

export const modificadorService = {
  async listar() {
    const { data } = await axiosClient.get('/modificadores')
    return data
  },

  async crear(modificador) {
    const { data } = await axiosClient.post('/modificadores', modificador)
    return data
  },

  async cambiarDisponibilidad(id, disponible) {
    const { data } = await axiosClient.patch(`/modificadores/${id}/disponibilidad`, { disponible })
    return data
  }
}

export const eventoService = {
  async listar(idPedido) {
    const { data } = await axiosClient.get('/eventos', { params: idPedido ? { idPedido } : {} })
    return data
  }
}

import client from './client'

export const getCarts       = (owner)       => client.get('/shopping-carts', { params: { owner } })
export const getCart        = (id)          => client.get(`/shopping-carts/${id}`)
export const createCart     = (owner, name) => client.post('/shopping-carts', { owner, name })
export const updateCart     = (id, data)    => client.put(`/shopping-carts/${id}`, data)
export const deleteCart     = (id)          => client.delete(`/shopping-carts/${id}`)
export const getOwners      = ()            => client.get('/shopping-carts/owners')

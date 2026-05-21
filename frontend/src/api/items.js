import client from './client'

export const getItems = () => client.get('/items')

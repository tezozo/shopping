import client from './client'

export const getCategoryDiscounts = () => client.get('/category-discounts')

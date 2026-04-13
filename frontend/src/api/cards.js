import { api } from './auth'

export const cardsApi = {
  getAll: () => 
    api.get('/cards'),
  
  getById: (id) => 
    api.get(`/cards/${id}`),
  
  getByType: (type) => 
    api.get(`/cards/type/${type}`),
  
  getByElement: (element) => 
    api.get(`/cards/element/${element}`),
  
  getByRarity: (rarity) => 
    api.get(`/cards/rarity/${rarity}`),
  
  search: (keyword, page = 0, size = 20) => 
    api.get('/cards/search', { params: { keyword, page, size } }),
  
  getFiltered: (filters) => 
    api.get('/cards/filter', { params: filters }),
  
  openPack: (packSize = 5) => 
    api.post('/cards/pack', { packSize }),
  
  openSingleCard: () => 
    api.get('/cards/pack/single')
}

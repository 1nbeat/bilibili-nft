import { request } from './http'

export function getSeriesList(params = {}) {
  const search = new URLSearchParams()
  if (params.itemId) {
    search.set('itemId', params.itemId)
  }
  if (params.itemName) {
    search.set('itemName', params.itemName)
  }
  const query = search.toString()
  return request(`/api/bili-nft-series${query ? `?${query}` : ''}`)
}

export function getSeriesById(itemId) {
  return request(`/api/bili-nft-series/${itemId}`)
}

export function getSeriesImages(itemId) {
  return request(`/api/bili-nft-series/${itemId}/images`)
}

export function getSeriesImagesBatch(itemIds, limit = 3) {
  const search = new URLSearchParams()
  itemIds.forEach((itemId) => {
    search.append('itemIds', itemId)
  })
  search.set('limit', limit)
  return request(`/api/bili-nft-series/images?${search.toString()}`)
}

export function getNftListByItemId(itemId, pageNum = 1, pageSize = 20) {
  return request(`/api/bili-nft-details/page/item/${itemId}?pageNum=${pageNum}&pageSize=${pageSize}`)
}

export function getNftItemDetail(nftId) {
  return request(`/api/bili-nft-item-details/${encodeURIComponent(nftId)}`)
}

export function getNftAttributeFacets(itemId) {
  return request(`/api/bili-nft-item-details/facets?itemId=${encodeURIComponent(itemId)}`)
}

export function searchNftItemsByAttributes({ itemId, pageNum = 1, pageSize = 20, filters = [] }) {
  return request('/api/bili-nft-item-details/search', {
    method: 'POST',
    body: JSON.stringify({
      itemId: Number(itemId),
      pageNum,
      pageSize,
      filters
    })
  })
}

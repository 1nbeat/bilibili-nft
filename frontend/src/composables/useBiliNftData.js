import { computed, ref, watch } from 'vue'
import {
  getNftAttributeFacets,
  getNftItemDetail,
  getNftListByItemId,
  getSeriesById,
  getSeriesList,
  searchNftItemsByAttributes
} from '../api/bili-nft'

export function useBiliNftData(route) {
  const loading = ref(false)
  const nftListLoading = ref(false)
  const errorMessage = ref('')
  const seriesList = ref([])
  const currentSeries = ref(null)
  const nftPage = ref({ total: 0, pageNum: 1, pageSize: 20, records: [] })
  const nftDetail = ref(null)
  const attributeFacets = ref([])
  const selectedAttributeFilters = ref([])
  const activeSeriesItemId = ref('')

  const pageTitle = computed(() => {
    if (route.value.name === 'series-detail') {
      return currentSeries.value?.itemName ?? '数字藏品系列'
    }
    if (route.value.name === 'nft-detail') {
      return nftDetail.value?.nftName ?? '数字藏品详情'
    }
    return '数字藏品系列'
  })

  const totalPages = computed(() => {
    const total = nftPage.value?.total ?? 0
    const pageSize = nftPage.value?.pageSize ?? 20
    return Math.max(1, Math.ceil(total / pageSize))
  })

  const visiblePages = computed(() => {
    const current = nftPage.value?.pageNum ?? 1
    const total = totalPages.value
    const start = Math.max(1, current - 2)
    const end = Math.min(total, start + 4)
    const pages = []
    for (let i = start; i <= end; i += 1) {
      pages.push(i)
    }
    return pages
  })

  const hasAttributeFilters = computed(() => selectedAttributeFilters.value.length > 0)

  function loadNftPageByFilters(itemId, pageNum = 1) {
    if (hasAttributeFilters.value) {
      return searchNftItemsByAttributes({
        itemId,
        pageNum,
        pageSize: 20,
        filters: selectedAttributeFilters.value
      })
    }
    return getNftListByItemId(itemId, pageNum, 20)
  }

  async function loadSeriesDetailPage(itemId, pageNum = 1) {
    const [series, facets, page] = await Promise.all([
      getSeriesById(itemId),
      getNftAttributeFacets(itemId),
      loadNftPageByFilters(itemId, pageNum)
    ])

    currentSeries.value = series
    attributeFacets.value = facets ?? []
    nftPage.value = page
  }

  async function loadPage() {
    loading.value = true
    errorMessage.value = ''

    try {
      if (route.value.name === 'series-list') {
        currentSeries.value = null
        nftDetail.value = null
        attributeFacets.value = []
        selectedAttributeFilters.value = []
        activeSeriesItemId.value = ''
        seriesList.value = await getSeriesList({
          itemId: route.value.itemId,
          itemName: route.value.itemName
        })
        return
      }

      if (route.value.name === 'series-detail') {
        nftDetail.value = null
        const itemId = route.value.itemId
        if (activeSeriesItemId.value !== itemId) {
          selectedAttributeFilters.value = []
          activeSeriesItemId.value = itemId
        }
        await loadSeriesDetailPage(itemId, route.value.pageNum ?? 1)
        return
      }

      if (route.value.name === 'nft-detail') {
        currentSeries.value = null
        attributeFacets.value = []
        nftDetail.value = await getNftItemDetail(route.value.nftId)
      }
    } catch (error) {
      errorMessage.value = error.message || '加载失败'
    } finally {
      loading.value = false
    }
  }

  watch(route, loadPage, { immediate: true, deep: true })

  function setAttributeFilters(filters) {
    selectedAttributeFilters.value = filters
  }

  async function applyAttributeFilters(filters) {
    if (route.value.name !== 'series-detail') {
      return
    }

    selectedAttributeFilters.value = filters
    nftListLoading.value = true
    errorMessage.value = ''

    try {
      nftPage.value = await loadNftPageByFilters(route.value.itemId, 1)
    } catch (error) {
      errorMessage.value = error.message || '加载失败'
    } finally {
      nftListLoading.value = false
    }
  }

  async function applyNftPage(pageNum) {
    if (route.value.name !== 'series-detail') {
      return
    }

    nftListLoading.value = true
    errorMessage.value = ''

    try {
      nftPage.value = await loadNftPageByFilters(route.value.itemId, pageNum)
    } catch (error) {
      errorMessage.value = error.message || '加载失败'
    } finally {
      nftListLoading.value = false
    }
  }

  return {
    loading,
    nftListLoading,
    errorMessage,
    seriesList,
    currentSeries,
    nftPage,
    nftDetail,
    attributeFacets,
    selectedAttributeFilters,
    pageTitle,
    totalPages,
    visiblePages,
    setAttributeFilters,
    applyAttributeFilters,
    applyNftPage,
    loadPage
  }
}

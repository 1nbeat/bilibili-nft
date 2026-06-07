<script setup>
import { computed, ref, watch } from 'vue'
import AppHeader from './components/AppHeader.vue'
import PageHero from './components/PageHero.vue'
import StateCard from './components/StateCard.vue'
import { useAppRouter } from './composables/useAppRouter'
import { useBiliNftData } from './composables/useBiliNftData'
import NftDetailView from './views/NftDetailView.vue'
import SeriesDetailView from './views/SeriesDetailView.vue'
import SeriesListView from './views/SeriesListView.vue'

const { route, navigateTo, goHome, goBack } = useAppRouter()
const {
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
  applyAttributeFilters,
  applyNftPage
} = useBiliNftData(route)

const detailPalette = ref({
  primary: 'rgba(0, 161, 214, 0.28)',
  secondary: 'rgba(251, 114, 153, 0.2)'
})
const appShellClass = computed(() => ({
  'app-shell--detail': route.value.name === 'nft-detail'
}))
const appShellStyle = computed(() => ({
  '--detail-bg-primary': detailPalette.value.primary,
  '--detail-bg-secondary': detailPalette.value.secondary
}))

watch(
  () => route.value.name,
  (name) => {
    if (name !== 'nft-detail') {
      detailPalette.value = {
        primary: 'rgba(0, 161, 214, 0.28)',
        secondary: 'rgba(251, 114, 153, 0.2)'
      }
    }
  }
)

function openSeries(itemId) {
  openInNewTab(`/series/${itemId}`)
}

function openSeriesPage(pageNum) {
  applyNftPage(pageNum)
}

function openNftDetail(nftId) {
  openInNewTab(`/nft/${encodeURIComponent(nftId)}`)
}

function openInNewTab(path) {
  const url = `${window.location.origin}${window.location.pathname}#${path}`
  window.open(url, '_blank', 'noopener,noreferrer')
}

function changeAttributeFilters(filters) {
  applyAttributeFilters(filters)
}

function searchSeries(filters) {
  const search = new URLSearchParams()
  if (filters.itemId) {
    search.set('itemId', filters.itemId)
  }
  if (filters.itemName) {
    search.set('itemName', filters.itemName)
  }
  const query = search.toString()
  navigateTo(`/${query ? `?${query}` : ''}`)
}

function resetSeriesSearch() {
  navigateTo('/')
}

function updateDetailPalette(palette) {
  detailPalette.value = palette
}
</script>

<template>
  <div class="app-shell" :class="appShellClass" :style="appShellStyle">
    <AppHeader @home="goHome" />

    <main class="page">
      <PageHero :title="pageTitle" description="" />

      <StateCard v-if="loading" message="正在加载数据..." />
      <StateCard v-else-if="errorMessage" :message="errorMessage" error />

      <template v-else>
        <SeriesListView
          v-if="route.name === 'series-list'"
          :series-list="seriesList"
          :search-item-id="route.itemId || ''"
          :search-item-name="route.itemName || ''"
          @open-series="openSeries"
          @search-series="searchSeries"
          @reset-series-search="resetSeriesSearch"
        />

        <SeriesDetailView
          v-else-if="route.name === 'series-detail'"
          :current-series="currentSeries"
          :nft-page="nftPage"
          :nft-list-loading="nftListLoading"
          :total-pages="totalPages"
          :visible-pages="visiblePages"
          :attribute-facets="attributeFacets"
          :selected-attribute-filters="selectedAttributeFilters"
          @open-nft="openNftDetail"
          @change-page="openSeriesPage"
          @change-attribute-filters="changeAttributeFilters"
        />

        <NftDetailView
          v-else-if="route.name === 'nft-detail' && nftDetail"
          :nft-detail="nftDetail"
          @palette-change="updateDetailPalette"
        />
      </template>
    </main>
  </div>
</template>

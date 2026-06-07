<script setup>
import { computed, ref, watch } from 'vue'
import { getSeriesImagesBatch } from '../api/bili-nft'
import PaginationBar from '../components/PaginationBar.vue'
import SeriesCard from '../components/SeriesCard.vue'
import SeriesSearchBar from '../components/SeriesSearchBar.vue'

const props = defineProps({
  seriesList: {
    type: Array,
    required: true
  },
  searchItemId: {
    type: String,
    default: ''
  },
  searchItemName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['open-series', 'search-series', 'reset-series-search'])

const PAGE_SIZE = 12
const currentPage = ref(1)
const seriesImages = ref({})
const seriesImagesLoading = ref(false)
let seriesImagesRequestId = 0

const totalPages = computed(() => Math.max(1, Math.ceil(props.seriesList.length / PAGE_SIZE)))
const visiblePages = computed(() => {
  const total = totalPages.value
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(total, start + 4)
  const pages = []
  for (let page = start; page <= end; page += 1) {
    pages.push(page)
  }
  return pages
})

const visibleSeries = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return props.seriesList.slice(start, start + PAGE_SIZE)
})
const visibleSeriesItemIds = computed(() => [
  ...new Set(visibleSeries.value.map((series) => series.itemId).filter(Boolean))
])

watch(
  () => props.seriesList,
  () => {
    currentPage.value = 1
  },
  { immediate: true }
)

watch(
  visibleSeriesItemIds,
  async (itemIds) => {
    const requestId = seriesImagesRequestId + 1
    seriesImagesRequestId = requestId
    seriesImages.value = {}

    if (!itemIds.length) {
      seriesImagesLoading.value = false
      return
    }

    seriesImagesLoading.value = true
    try {
      const result = await getSeriesImagesBatch(itemIds, 3)
      if (requestId !== seriesImagesRequestId) {
        return
      }
      seriesImages.value = result ?? {}
    } catch (error) {
      if (requestId === seriesImagesRequestId) {
        seriesImages.value = {}
      }
    } finally {
      if (requestId === seriesImagesRequestId) {
        seriesImagesLoading.value = false
      }
    }
  },
  { immediate: true }
)

function changePage(pageNum) {
  currentPage.value = pageNum
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <section class="series-list-view">
    <SeriesSearchBar
      :item-id="searchItemId"
      :item-name="searchItemName"
      @search="emit('search-series', $event)"
      @reset="emit('reset-series-search')"
    />

    <section class="series-grid">
      <SeriesCard
        v-for="series in visibleSeries"
        :key="series.itemId"
        :series="series"
        :images="seriesImages[series.itemId] || []"
        :images-loading="seriesImagesLoading"
        @open="emit('open-series', $event)"
      />
    </section>

    <PaginationBar
      v-if="seriesList.length > PAGE_SIZE"
      :page-num="currentPage"
      :total="seriesList.length"
      :page-size="PAGE_SIZE"
      :total-pages="totalPages"
      :visible-pages="visiblePages"
      @change="changePage"
    />
  </section>
</template>

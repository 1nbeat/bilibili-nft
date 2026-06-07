<script setup>
import { computed, nextTick, ref } from 'vue'
import AttributeFilterPanel from '../components/AttributeFilterPanel.vue'
import NftCard from '../components/NftCard.vue'
import PaginationBar from '../components/PaginationBar.vue'

const props = defineProps({
  currentSeries: {
    type: Object,
    default: null
  },
  nftPage: {
    type: Object,
    required: true
  },
  nftListLoading: {
    type: Boolean,
    default: false
  },
  totalPages: {
    type: Number,
    required: true
  },
  visiblePages: {
    type: Array,
    required: true
  },
  attributeFacets: {
    type: Array,
    default: () => []
  },
  selectedAttributeFilters: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['open-nft', 'change-page', 'change-attribute-filters'])

const hasSelectedFilters = computed(() => props.selectedAttributeFilters.length > 0)
const hasAttributeFacets = computed(() => props.attributeFacets.length > 0)
const hasRecords = computed(() => (props.nftPage.records?.length ?? 0) > 0)
const contentPanel = ref(null)

function removeAttributeFilter(filter) {
  emit(
    'change-attribute-filters',
    props.selectedAttributeFilters.filter(
      (item) => item.traitType !== filter.traitType || item.traitValue !== filter.traitValue
    )
  )
}

async function changePage(pageNum) {
  if (pageNum === props.nftPage.pageNum) {
    return
  }

  emit('change-page', pageNum)
  await nextTick()
  requestAnimationFrame(() => {
    const topbarHeight = document.querySelector('.topbar')?.offsetHeight ?? 0
    const targetTop = (contentPanel.value?.getBoundingClientRect().top ?? 0)
      + window.scrollY
      - topbarHeight
      - 12
    window.scrollTo({ top: Math.max(0, targetTop), behavior: 'smooth' })
  })
}
</script>

<template>
  <template v-if="currentSeries">
    <section class="series-banner">
      <div>
        <p class="series-banner__label">系列信息</p>
        <h2>{{ currentSeries.itemName }}</h2>
        <p>{{ currentSeries.issuerName || '未知发行方' }}</p>
      </div>
      <div class="series-banner__stats">
        <div>
          <strong>{{ currentSeries.itemId }}</strong>
          <span>系列ID</span>
        </div>
        <div>
          <strong>{{ currentSeries.collectionTotal ?? '--' }}</strong>
          <span>系列总量</span>
        </div>
      </div>
    </section>

    <section
      class="series-detail-layout"
      :class="{ 'series-detail-layout--single': !hasAttributeFacets }"
    >
      <aside v-if="hasAttributeFacets" class="series-detail-layout__filters">
        <AttributeFilterPanel
          :facets="attributeFacets"
          :selected-filters="selectedAttributeFilters"
          @change="emit('change-attribute-filters', $event)"
        />
      </aside>

      <div ref="contentPanel" class="series-detail-layout__content">
        <div v-if="hasSelectedFilters" class="selected-filter-bar">
          <span class="selected-filter-bar__label">已选</span>
          <button
            v-for="filter in selectedAttributeFilters"
            :key="`${filter.traitType}-${filter.traitValue}`"
            class="selected-filter-bar__chip"
            type="button"
            @click="removeAttributeFilter(filter)"
          >
            {{ filter.traitType }}：{{ filter.traitValue }}
            <span>×</span>
          </button>
        </div>

        <section v-if="nftListLoading" class="state-card series-detail-layout__state">
          正在刷新藏品列表...
        </section>

        <section v-else-if="hasRecords" class="nft-grid">
          <NftCard
            v-for="nft in nftPage.records"
            :key="nft.nftId"
            :nft="nft"
            @open="emit('open-nft', $event)"
          />
        </section>

        <section v-else class="state-card series-detail-layout__state">
          暂无符合条件的藏品
        </section>

        <PaginationBar
          v-if="hasRecords && !nftListLoading"
          :page-num="nftPage.pageNum"
          :total="nftPage.total"
          :page-size="nftPage.pageSize"
          :total-pages="totalPages"
          :visible-pages="visiblePages"
          @change="changePage"
        />
      </div>
    </section>
  </template>
</template>

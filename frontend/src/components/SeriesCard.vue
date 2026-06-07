<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { getProxyImageUrl, normalizeImageUrl } from '../utils/media'

const props = defineProps({
  series: {
    type: Object,
    required: true
  },
  images: {
    type: Array,
    default: () => []
  },
  imagesLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['open'])

const images = ref([])
const activeIndex = ref(0)
const loadingImages = ref(true)
let timer = null

const carouselImages = computed(() =>
  images.value
    .map((item, index) => ({
      index,
      url: item?.failed ? '' : item?.displayUrl
    }))
    .filter((item) => item.url)
)

const activeImageCount = computed(() => carouselImages.value.length)

function startAutoplay() {
  stopAutoplay()
  if (activeImageCount.value <= 1) {
    return
  }

  timer = window.setInterval(() => {
    activeIndex.value = (activeIndex.value + 1) % activeImageCount.value
  }, 2400)
}

function stopAutoplay() {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

function applyImages(seriesImages) {
  images.value = []
  activeIndex.value = 0
  stopAutoplay()

  const urls = (seriesImages ?? [])
    .map((item) => item?.imageUrl)
    .filter(Boolean)

  if (!urls.length) {
    loadingImages.value = props.imagesLoading
    return
  }

  loadingImages.value = true
  images.value = urls.map((url) => ({
    originalUrl: url,
    displayUrl: normalizeImageUrl(url),
    loaded: false,
    failed: false,
    fallbackUsed: false
  }))
}

function handleImageLoad(index) {
  const image = images.value[index]
  if (!image) {
    return
  }

  image.loaded = true
  loadingImages.value = false
  startAutoplay()
}

function handleImageError(index) {
  const image = images.value[index]
  if (!image || image.fallbackUsed) {
    if (image) {
      image.failed = true
    }
    if (!images.value.some((item) => item.loaded)) {
      loadingImages.value = images.value.some((item) => !item.failed)
    }
    if (activeIndex.value >= activeImageCount.value) {
      activeIndex.value = 0
    }
    return
  }

  const proxyUrl = getProxyImageUrl(image.originalUrl)
  image.fallbackUsed = true
  if (proxyUrl && proxyUrl !== image.displayUrl) {
    image.displayUrl = proxyUrl
  } else {
    image.failed = true
    if (!images.value.some((item) => item.loaded)) {
      loadingImages.value = images.value.some((item) => !item.failed)
    }
  }
}

watch(
  () => props.imagesLoading,
  (isLoading) => {
    if (isLoading && !images.value.length) {
      loadingImages.value = true
    } else if (!isLoading && !images.value.length) {
      loadingImages.value = false
    }
  },
  { immediate: true }
)

watch(
  () => props.images,
  (seriesImages) => {
    applyImages(seriesImages)
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  stopAutoplay()
})
</script>

<template>
  <article class="series-card" @click="emit('open', series.itemId)">
    <div class="series-card__cover">
      <div v-if="carouselImages.length" class="series-card__carousel">
        <img
          v-for="(image, carouselIndex) in carouselImages"
          :key="`${series.itemId}-${image.index}`"
          class="series-card__image"
          :class="{ 'series-card__image--active': carouselIndex === activeIndex }"
          :src="image.url"
          :alt="series.itemName"
          loading="lazy"
          referrerpolicy="no-referrer"
          @load="handleImageLoad(image.index)"
          @error="handleImageError(image.index)"
        />
      </div>
      <div v-else-if="loadingImages" class="series-card__loading"></div>
      <div v-else-if="!loadingImages" class="series-card__fallback"></div>
      <div class="series-card__overlay"></div>
      <span class="series-card__badge">Series</span>
    </div>
    <div class="series-card__body">
      <h2>{{ series.itemName }}</h2>
      <p class="series-card__issuer">{{ series.issuerName || '未知发行方' }}</p>
      <div class="series-card__meta">
        <span>系列ID {{ series.itemId }}</span>
        <span>总量 {{ series.collectionTotal ?? '--' }}</span>
      </div>
    </div>
  </article>
</template>

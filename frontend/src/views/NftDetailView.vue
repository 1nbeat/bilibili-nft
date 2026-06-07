<script setup>
import { computed, ref, watch } from 'vue'
import { getProxyImageUrl, normalizeImageUrl } from '../utils/media'
import { extractImagePalette } from '../utils/palette'

const props = defineProps({
  nftDetail: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['palette-change'])
const previewVisible = ref(false)
const downloading = ref(false)
const imageUrl = ref('')
const avatarUrl = ref('')
const DEFAULT_PRIMARY = 'rgba(0, 161, 214, 0.28)'
const DEFAULT_SECONDARY = 'rgba(251, 114, 153, 0.2)'
const detailPalette = ref({
  primary: DEFAULT_PRIMARY,
  secondary: DEFAULT_SECONDARY
})

const detailStyle = computed(() => ({
  '--detail-bg-primary': detailPalette.value.primary,
  '--detail-bg-secondary': detailPalette.value.secondary
}))
const seriesTotal = computed(() => Number(props.nftDetail.serialTotal) || 0)
const attributes = computed(() => {
  if (!props.nftDetail.attributesJson) {
    return []
  }

  try {
    const parsed = JSON.parse(props.nftDetail.attributesJson)
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed.map((item) => ({
      traitType: item?.traitType ?? item?.trait_type ?? '',
      value: item?.value ?? '',
      traitCount: item?.traitCount ?? item?.trait_count
    }))
  } catch (error) {
    return []
  }
})

watch(
  () => props.nftDetail.imageUrl,
  (url) => {
    imageUrl.value = normalizeImageUrl(url)
    detailPalette.value = {
      primary: DEFAULT_PRIMARY,
      secondary: DEFAULT_SECONDARY
    }
  },
  { immediate: true }
)

watch(
  () => props.nftDetail.ownerAvatarUrl,
  (url) => {
    avatarUrl.value = normalizeImageUrl(url)
  },
  { immediate: true }
)

function openPreview() {
  if (!imageUrl.value) {
    return
  }
  previewVisible.value = true
}

function closePreview() {
  previewVisible.value = false
}

function getDownloadFileName() {
  const nftId = props.nftDetail.nftId || 'nft-image'
  const imagePath = props.nftDetail.imageUrl?.split('?')[0] ?? ''
  const extension = imagePath.match(/\.(png|jpg|jpeg|webp|gif)$/i)?.[1] ?? 'jpg'
  return `${nftId}.${extension.toLowerCase()}`
}

async function downloadImage() {
  if (downloading.value || !props.nftDetail.imageUrl) {
    return
  }

  downloading.value = true
  try {
    const response = await fetch(getProxyImageUrl(props.nftDetail.imageUrl))
    if (!response.ok) {
      throw new Error(`Download failed: ${response.status}`)
    }

    const blob = await response.blob()
    const objectUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = getDownloadFileName()
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(objectUrl)
  } finally {
    downloading.value = false
  }
}

function applyPalette(palette) {
  detailPalette.value = palette
  emit('palette-change', palette)
}

function updatePalette(event) {
  const palette = extractImagePalette(event.target)
  const isDefaultPalette = palette.primary === DEFAULT_PRIMARY && palette.secondary === DEFAULT_SECONDARY
  if (!isDefaultPalette) {
    applyPalette(palette)
  }
}

function fallbackAvatarToProxy() {
  const proxyUrl = getProxyImageUrl(props.nftDetail.ownerAvatarUrl)
  if (proxyUrl && avatarUrl.value !== proxyUrl) {
    avatarUrl.value = proxyUrl
  }
}

function getRarityRatio(traitCount) {
  if (!traitCount || !seriesTotal.value) {
    return null
  }

  return Number(traitCount) / seriesTotal.value
}

function formatRarity(traitCount) {
  const ratio = getRarityRatio(traitCount)
  if (ratio == null) {
    return '--'
  }

  return `${(ratio * 100).toFixed(2)}%`
}

function isGoldRarity(traitCount) {
  const ratio = getRarityRatio(traitCount)
  return ratio != null && ratio < 0.1
}

function formatDateTime(value) {
  if (!value) {
    return '--'
  }

  const text = String(value).trim()
  if (!text) {
    return '--'
  }

  return text.replace('T', ' ')
}
</script>

<template>
  <section class="detail-page" :style="detailStyle">
    <section class="detail-layout">
      <div class="detail-media">
        <div class="detail-visual">
          <button class="detail-visual__button" type="button" @click="openPreview">
            <img
              :src="imageUrl"
              :alt="nftDetail.nftName"
              crossorigin="anonymous"
              referrerpolicy="no-referrer"
              @load="updatePalette"
            />
          </button>
        </div>
        <section v-if="attributes.length" class="detail-attributes">
          <div class="detail-attributes__head">
            <h3>藏品属性</h3>
            <span>{{ attributes.length }} 项</span>
          </div>
          <div class="detail-attributes__grid">
            <article
              v-for="(attribute, index) in attributes"
              :key="`${attribute.traitType || 'attr'}-${index}`"
              class="detail-attributes__card"
            >
              <span class="detail-attributes__label">{{ attribute.traitType || '属性' }}</span>
              <strong class="detail-attributes__value">{{ attribute.value || '--' }}</strong>
              <div v-if="attribute.traitCount != null" class="detail-attributes__meta">
                <small class="detail-attributes__count">出现次数 {{ attribute.traitCount }}</small>
                <small
                  class="detail-attributes__count"
                  :class="{ 'detail-attributes__count--rare': isGoldRarity(attribute.traitCount) }"
                >
                  稀有度 {{ formatRarity(attribute.traitCount) }}
                </small>
              </div>
            </article>
          </div>
        </section>
      </div>
      <div class="detail-panel">
        <p class="detail-panel__serial">{{ nftDetail.serialNumber }}</p>
        <h2>{{ nftDetail.nftName }}</h2>
        <div class="detail-actions">
          <button
            v-if="nftDetail.imageUrl"
            class="detail-actions__btn detail-actions__btn--primary"
            type="button"
            :disabled="downloading"
            @click="downloadImage"
          >
            {{ downloading ? '下载中...' : '下载图片' }}
          </button>
        </div>

        <section class="detail-info-section detail-info-section--owner">
          <h3 class="detail-info-section__title">持有人信息</h3>
          <div class="detail-owner">
            <div class="detail-owner__avatar-wrap">
              <img
                v-if="nftDetail.ownerAvatarUrl"
                class="detail-owner__avatar"
                :src="avatarUrl"
                :alt="nftDetail.ownerName || '持有人头像'"
                referrerpolicy="no-referrer"
                @error="fallbackAvatarToProxy"
              />
              <div v-else class="detail-owner__avatar detail-owner__avatar--placeholder">
                {{ (nftDetail.ownerName || '?').slice(0, 1) }}
              </div>
            </div>
            <div class="detail-owner__info">
              <span>持有人</span>
              <strong>{{ nftDetail.ownerName || '匿名持有人' }}</strong>
              <small>交易时间 {{ formatDateTime(nftDetail.ownerTxTime) }}</small>
            </div>
          </div>
        </section>

        <section class="detail-info-section detail-info-section--plain">
          <h3 class="detail-info-section__title">系列信息</h3>
          <div class="detail-info-grid">
            <div class="detail-panel__group">
              <span>系列ID</span>
              <strong>{{ nftDetail.itemId }}</strong>
            </div>
            <div class="detail-panel__group">
              <span>发行方</span>
              <strong>{{ nftDetail.issueIssuer || '--' }}</strong>
            </div>
            <div class="detail-panel__group">
              <span>作者</span>
              <strong>{{ nftDetail.issueAuthor || '--' }}</strong>
            </div>
            <div class="detail-panel__group">
              <span>发行时间</span>
              <strong>{{ formatDateTime(nftDetail.issueTime) }}</strong>
            </div>
          </div>
        </section>

        <section class="detail-info-section detail-info-section--plain">
          <h3 class="detail-info-section__title">链上信息</h3>
          <div class="detail-info-grid detail-info-grid--single">
            <div class="detail-panel__group">
              <span>NFT ID</span>
              <strong class="detail-panel__mono">{{ nftDetail.nftId }}</strong>
            </div>
            <div class="detail-panel__group">
              <span>链上地址</span>
              <strong class="detail-panel__mono">{{ nftDetail.nftAddress || '--' }}</strong>
            </div>
          </div>
        </section>

        <div class="detail-panel__desc">
          <h3>藏品描述</h3>
          <p>{{ nftDetail.description || '暂无描述' }}</p>
        </div>
      </div>
    </section>
  </section>

  <teleport to="body">
    <div v-if="previewVisible" class="image-preview" @click.self="closePreview">
      <button class="image-preview__close" type="button" @click="closePreview">关闭</button>
      <div class="image-preview__content">
        <img
          :src="imageUrl"
          :alt="nftDetail.nftName"
          referrerpolicy="no-referrer"
        />
      </div>
    </div>
  </teleport>
</template>

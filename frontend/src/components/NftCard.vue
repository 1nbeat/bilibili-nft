<script setup>
import { computed, ref, watch } from 'vue'
import { getProxyImageUrl, normalizeImageUrl } from '../utils/media'

const props = defineProps({
  nft: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['open'])

const displayName = computed(() => props.nft.itemName || props.nft.nftName || '未命名藏品')
const ownerName = computed(() => props.nft.username || props.nft.ownerName || '匿名持有人')
const imageUrl = ref('')

watch(
  () => props.nft.imageUrl,
  (url) => {
    imageUrl.value = normalizeImageUrl(url)
  },
  { immediate: true }
)

function fallbackToProxy() {
  const proxyUrl = getProxyImageUrl(props.nft.imageUrl)
  if (proxyUrl && imageUrl.value !== proxyUrl) {
    imageUrl.value = proxyUrl
  }
}
</script>

<template>
  <article class="nft-card" @click="emit('open', nft.nftId)">
    <div class="nft-card__media">
      <img
        :src="imageUrl"
        :alt="displayName"
        loading="lazy"
        referrerpolicy="no-referrer"
        @error="fallbackToProxy"
      />
    </div>
    <div class="nft-card__body">
      <p class="nft-card__serial">{{ nft.serialNumber }}</p>
      <h3>{{ displayName }}</h3>
      <p class="nft-card__owner">{{ ownerName }}</p>
      <div class="nft-card__meta">
        <span>NFT ID</span>
        <strong>{{ nft.nftId }}</strong>
      </div>
    </div>
  </article>
</template>

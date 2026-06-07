<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { getSeriesList } from '../api/bili-nft'

const SUGGESTION_LIMIT = 8
const SUGGESTION_DELAY = 260

const props = defineProps({
  itemId: {
    type: String,
    default: ''
  },
  itemName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['search', 'reset'])

const form = reactive({
  itemId: props.itemId,
  itemName: props.itemName
})
const suggestions = ref([])
const suggestionLoading = ref(false)
const showSuggestions = ref(false)
let suggestionTimer = null
let suggestionRequestId = 0

const visibleSuggestions = computed(() => suggestions.value.slice(0, SUGGESTION_LIMIT))

watch(
  () => [props.itemId, props.itemName],
  ([itemId, itemName]) => {
    form.itemId = itemId
    form.itemName = itemName
  }
)

function handleSubmit() {
  showSuggestions.value = false
  emit('search', {
    itemId: form.itemId.trim(),
    itemName: form.itemName.trim()
  })
}

function handleReset() {
  form.itemId = ''
  form.itemName = ''
  suggestions.value = []
  showSuggestions.value = false
  emit('reset')
}

function scheduleSuggestions() {
  window.clearTimeout(suggestionTimer)
  const keyword = form.itemName.trim()
  if (!keyword) {
    suggestions.value = []
    showSuggestions.value = false
    suggestionLoading.value = false
    return
  }

  suggestionTimer = window.setTimeout(() => {
    loadSuggestions(keyword)
  }, SUGGESTION_DELAY)
}

async function loadSuggestions(keyword) {
  const requestId = suggestionRequestId + 1
  suggestionRequestId = requestId
  suggestionLoading.value = true
  showSuggestions.value = true

  try {
    const result = await getSeriesList({ itemName: keyword })
    if (requestId !== suggestionRequestId) {
      return
    }
    suggestions.value = result ?? []
  } catch (error) {
    if (requestId === suggestionRequestId) {
      suggestions.value = []
    }
  } finally {
    if (requestId === suggestionRequestId) {
      suggestionLoading.value = false
    }
  }
}

function selectSuggestion(series) {
  form.itemId = ''
  form.itemName = series.itemName || ''
  showSuggestions.value = false
  emit('search', {
    itemId: '',
    itemName: form.itemName.trim()
  })
}

function hideSuggestionsLater() {
  window.setTimeout(() => {
    showSuggestions.value = false
  }, 160)
}

onBeforeUnmount(() => {
  window.clearTimeout(suggestionTimer)
})

watch(
  () => form.itemName,
  () => {
    scheduleSuggestions()
  }
)
</script>

<template>
  <section class="series-search">
    <div class="series-search__fields">
      <label class="series-search__field">
        <span>系列ID</span>
        <input v-model="form.itemId" type="text" placeholder="输入系列ID" />
      </label>
      <label class="series-search__field series-search__field--suggest">
        <span>系列名称</span>
        <input
          v-model="form.itemName"
          type="text"
          placeholder="输入系列名称"
          autocomplete="off"
          @focus="form.itemName.trim() && (showSuggestions = true)"
          @blur="hideSuggestionsLater"
          @keydown.enter.prevent="handleSubmit"
        />
        <div v-if="showSuggestions" class="series-search__suggestions">
          <div v-if="suggestionLoading" class="series-search__suggestion-state">
            正在查询...
          </div>
          <template v-else-if="visibleSuggestions.length">
            <button
              v-for="series in visibleSuggestions"
              :key="series.itemId"
              class="series-search__suggestion"
              type="button"
              @mousedown.prevent="selectSuggestion(series)"
            >
              <strong>{{ series.itemName }}</strong>
              <span>{{ series.issuerName || '未知发行方' }} · ID {{ series.itemId }}</span>
            </button>
          </template>
          <div v-else class="series-search__suggestion-state">
            暂无相关系列
          </div>
        </div>
      </label>
    </div>
    <div class="series-search__actions">
      <button class="ghost-btn" type="button" @click="handleReset">重置</button>
      <button class="series-search__submit" type="button" @click="handleSubmit">查询</button>
    </div>
  </section>
</template>

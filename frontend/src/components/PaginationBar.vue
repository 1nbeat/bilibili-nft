<script setup>
import { computed } from 'vue'

const props = defineProps({
  pageNum: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 20
  },
  totalPages: {
    type: Number,
    required: true
  },
  visiblePages: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['change'])

const middlePages = computed(() => (
  props.visiblePages.filter((page) => page > 1 && page < props.totalPages)
))

const showLeadingEllipsis = computed(() => middlePages.value[0] > 2)
const showTrailingEllipsis = computed(() => {
  const lastMiddlePage = middlePages.value[middlePages.value.length - 1]
  return lastMiddlePage < props.totalPages - 1
})

const startRecord = computed(() => {
  if (props.total <= 0) {
    return 0
  }
  return (props.pageNum - 1) * props.pageSize + 1
})

const endRecord = computed(() => Math.min(props.pageNum * props.pageSize, props.total))
</script>

<template>
  <section class="pager" v-if="total > 0">
    <div class="pager__summary">
      共 {{ total }} 条，当前 {{ startRecord }}-{{ endRecord }} 条
    </div>

    <div v-if="totalPages > 1" class="pager__controls">
      <button
        class="pager__btn"
        type="button"
        :disabled="pageNum <= 1"
        @click="emit('change', pageNum - 1)"
      >
        上一页
      </button>

      <button
        class="pager__btn"
        :class="{ 'pager__btn--active': pageNum === 1 }"
        type="button"
        @click="emit('change', 1)"
      >
        1
      </button>

      <span v-if="showLeadingEllipsis" class="pager__ellipsis">...</span>

      <button
        v-for="page in middlePages"
        :key="page"
        class="pager__btn"
        :class="{ 'pager__btn--active': page === pageNum }"
        type="button"
        @click="emit('change', page)"
      >
        {{ page }}
      </button>

      <span v-if="showTrailingEllipsis" class="pager__ellipsis">...</span>

      <button
        v-if="totalPages > 1"
        class="pager__btn"
        :class="{ 'pager__btn--active': pageNum === totalPages }"
        type="button"
        @click="emit('change', totalPages)"
      >
        {{ totalPages }}
      </button>

      <button
        class="pager__btn"
        type="button"
        :disabled="pageNum >= totalPages"
        @click="emit('change', pageNum + 1)"
      >
        下一页
      </button>
    </div>
  </section>
</template>

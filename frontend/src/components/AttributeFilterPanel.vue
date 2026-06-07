<script setup>
import { computed, ref } from 'vue'

const OPTION_LIMIT = 8

const props = defineProps({
  facets: {
    type: Array,
    default: () => []
  },
  selectedFilters: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['change'])
const expandedGroups = ref(new Set())

const selectedMap = computed(() => {
  const map = new Map()
  props.selectedFilters.forEach((filter) => {
    map.set(filter.traitType, filter.traitValue)
  })
  return map
})

function isSelected(traitType, traitValue) {
  return selectedMap.value.get(traitType) === traitValue
}

function isExpanded(traitType) {
  return expandedGroups.value.has(traitType)
}

function visibleOptions(facet) {
  const options = facet.options ?? []
  if (isExpanded(facet.traitType) || options.length <= OPTION_LIMIT) {
    return options
  }
  return options.slice(0, OPTION_LIMIT)
}

function toggleExpanded(traitType) {
  const next = new Set(expandedGroups.value)
  if (next.has(traitType)) {
    next.delete(traitType)
  } else {
    next.add(traitType)
  }
  expandedGroups.value = next
}

function toggleOption(traitType, traitValue) {
  const next = props.selectedFilters.filter((filter) => filter.traitType !== traitType)
  if (!isSelected(traitType, traitValue)) {
    next.push({ traitType, traitValue })
  }
  emit('change', next)
}

function clearFilters() {
  emit('change', [])
}
</script>

<template>
  <section v-if="facets.length" class="attribute-filter">
    <div class="attribute-filter__head">
      <div>
        <h3>属性筛选</h3>
        <p>已选 {{ selectedFilters.length }} 项</p>
      </div>
      <button
        v-if="selectedFilters.length"
        class="attribute-filter__clear"
        type="button"
        @click="clearFilters"
      >
        清空
      </button>
    </div>

    <div class="attribute-filter__groups">
      <div v-for="facet in facets" :key="facet.traitType" class="attribute-filter__group">
        <div class="attribute-filter__title">{{ facet.traitType }}</div>
        <div class="attribute-filter__options">
          <button
            v-for="option in visibleOptions(facet)"
            :key="`${facet.traitType}-${option.traitValue}`"
            class="attribute-filter__option"
            :class="{ 'attribute-filter__option--active': isSelected(facet.traitType, option.traitValue) }"
            type="button"
            @click="toggleOption(facet.traitType, option.traitValue)"
          >
            <span>{{ option.traitValue }}</span>
            <small>{{ option.count }}</small>
          </button>
        </div>
        <button
          v-if="facet.options?.length > OPTION_LIMIT"
          class="attribute-filter__more"
          type="button"
          @click="toggleExpanded(facet.traitType)"
        >
          {{ isExpanded(facet.traitType) ? '收起' : `展开更多 ${facet.options.length - OPTION_LIMIT} 项` }}
        </button>
      </div>
    </div>
  </section>
</template>

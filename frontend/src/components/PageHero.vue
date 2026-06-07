<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <section class="hero">
    <h1 class="hero__title">{{ title }}</h1>
    <p v-if="description" class="hero__desc">{{ description }}</p>
  </section>
</template>

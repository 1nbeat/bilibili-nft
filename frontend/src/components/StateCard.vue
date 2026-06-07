<script setup>
defineProps({
  message: {
    type: String,
    required: true
  },
  error: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <section class="state-card" :class="{ 'state-card--error': error }">
    <p>{{ message }}</p>
  </section>
</template>


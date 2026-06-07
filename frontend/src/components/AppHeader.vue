<script setup>
import logoUrl from '../assets/bilibili-nft-logo.png'

const emit = defineEmits(['home'])
</script>

<template>
  <header class="topbar">
    <div class="topbar__inner">
      <button class="brand" type="button" @click="emit('home')">
        <img class="brand__image" :src="logoUrl" alt="bilibili NFT" />
      </button>
    </div>
  </header>
</template>

import { onBeforeUnmount, onMounted, ref } from 'vue'

function parseRoute() {
  const hashPath = window.location.hash.startsWith('#')
    ? window.location.hash.slice(1)
    : ''
  const normalizedHashPath = hashPath
    ? (hashPath.startsWith('/') ? hashPath : `/${hashPath}`)
    : ''
  const [hashRoutePath, hashQuery = ''] = normalizedHashPath.split('?')
  const path = hashRoutePath || window.location.pathname
  const search = new URLSearchParams(hashQuery || window.location.search)

  if (path.startsWith('/series/')) {
    return {
      name: 'series-detail',
      itemId: path.split('/')[2],
      pageNum: Number(search.get('page')) || 1
    }
  }

  if (path.startsWith('/nft/')) {
    return {
      name: 'nft-detail',
      nftId: decodeURIComponent(path.split('/')[2] ?? '')
    }
  }

  return {
    name: 'series-list',
    itemId: search.get('itemId') || '',
    itemName: search.get('itemName') || ''
  }
}

export function useAppRouter() {
  const route = ref(parseRoute())

  function syncRoute() {
    route.value = parseRoute()
  }

  function navigateTo(path) {
    window.history.pushState({}, '', `#${path}`)
    syncRoute()
  }

  function goHome() {
    navigateTo('/')
  }

  function goBack() {
    window.history.back()
  }

  onMounted(() => {
    window.addEventListener('hashchange', syncRoute)
    window.addEventListener('popstate', syncRoute)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('hashchange', syncRoute)
    window.removeEventListener('popstate', syncRoute)
  })

  return {
    route,
    navigateTo,
    goHome,
    goBack
  }
}

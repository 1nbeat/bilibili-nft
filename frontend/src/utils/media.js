const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

function buildApiUrl(path) {
  if (!API_BASE_URL) {
    return path
  }

  return `${API_BASE_URL.replace(/\/$/, '')}${path.startsWith('/') ? path : `/${path}`}`
}

export function normalizeImageUrl(url) {
  if (!url) {
    return ''
  }

  if (url.startsWith('/api/media/image?url=') || url.includes('/api/media/image?url=')) {
    return url
  }

  return url.startsWith('http://')
    ? `https://${url.slice('http://'.length)}`
    : url
}

export function getProxyImageUrl(url) {
  const normalized = normalizeImageUrl(url)
  if (!normalized) {
    return ''
  }

  return buildApiUrl(`/api/media/image?url=${encodeURIComponent(normalized)}`)
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

function buildUrl(url) {
  if (!API_BASE_URL || url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  return `${API_BASE_URL.replace(/\/$/, '')}${url.startsWith('/') ? url : `/${url}`}`
}

export async function request(url, options = {}) {
  const headers = {
    ...(options.headers ?? {})
  }

  if (options.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(buildUrl(url), {
    headers,
    ...options
  })

  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`)
  }

  const result = await response.json()
  if (result?.code !== 0) {
    throw new Error(result?.message ?? 'Request error')
  }

  return result.data
}

const DEFAULT_PALETTE = {
  primary: 'rgba(0, 161, 214, 0.28)',
  secondary: 'rgba(251, 114, 153, 0.2)'
}

function toRgba(color, alpha) {
  return `rgba(${color.r}, ${color.g}, ${color.b}, ${alpha})`
}

function isUsablePixel(r, g, b, a) {
  if (a < 180) {
    return false
  }

  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const brightness = (r + g + b) / 3

  if (brightness > 238 || brightness < 24) {
    return false
  }

  return max - min > 18
}

function bucketColor(r, g, b) {
  return {
    r: Math.round(r / 24) * 24,
    g: Math.round(g / 24) * 24,
    b: Math.round(b / 24) * 24
  }
}

export function extractImagePalette(imageElement) {
  if (!imageElement?.naturalWidth || !imageElement?.naturalHeight) {
    return DEFAULT_PALETTE
  }

  const canvas = document.createElement('canvas')
  const size = 56
  canvas.width = size
  canvas.height = size

  const context = canvas.getContext('2d', { willReadFrequently: true })
  if (!context) {
    return DEFAULT_PALETTE
  }

  try {
    context.drawImage(imageElement, 0, 0, size, size)
    const { data } = context.getImageData(0, 0, size, size)
    const buckets = new Map()

    for (let i = 0; i < data.length; i += 16) {
      const r = data[i]
      const g = data[i + 1]
      const b = data[i + 2]
      const a = data[i + 3]

      if (!isUsablePixel(r, g, b, a)) {
        continue
      }

      const color = bucketColor(r, g, b)
      const key = `${color.r}-${color.g}-${color.b}`
      const current = buckets.get(key) ?? { ...color, count: 0 }
      current.count += 1
      buckets.set(key, current)
    }

    const colors = [...buckets.values()].sort((a, b) => b.count - a.count)
    if (!colors.length) {
      return DEFAULT_PALETTE
    }

    const primary = colors[0]
    const secondary = colors.find((color) => {
      const distance = Math.abs(color.r - primary.r)
        + Math.abs(color.g - primary.g)
        + Math.abs(color.b - primary.b)
      return distance > 80
    }) ?? colors[1] ?? primary

    return {
      primary: toRgba(primary, 0.28),
      secondary: toRgba(secondary, 0.22)
    }
  } catch (error) {
    return DEFAULT_PALETTE
  }
}

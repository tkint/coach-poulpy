import { ref } from 'vue'

// The browser width web mockups are drawn at, shared by every frame like the mockup theme: a
// number of pixels, or "full" for the column's whole width. Remembered in this browser, and
// followed by frames in other tabs.
export type MockupWidth = number | 'full'

const key = 'coach-poulpy-mockup-width'
const width = ref<MockupWidth>(1280)
let started = false

function parse(value: string | null): MockupWidth | undefined {
  if (value === 'full') return 'full'
  const pixels = Number(value)
  return value && Number.isFinite(pixels) && pixels > 0 ? pixels : undefined
}

function read() {
  try {
    width.value = parse(localStorage.getItem(key)) ?? width.value
  } catch {}
}

export function useMockupWidth() {
  if (!started && typeof window !== 'undefined') {
    started = true
    read()
    addEventListener('storage', (event) => {
      if (event.key === key || event.key === null) read()
    })
  }
  function set(value: MockupWidth) {
    width.value = value
    try {
      localStorage.setItem(key, String(value))
    } catch {}
  }
  return { width, set }
}

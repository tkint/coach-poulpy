import { onBeforeUnmount, onMounted, ref } from 'vue'

// The theme the component samples are drawn in, apart from the docs site's own light or dark choice
// (like the mockups'): one cookie, read by every samples page. With no choice, they follow the system.
const cookieName = 'coach-poulpy-sample-theme'
const mode = ref<'light' | 'dark'>('light')

function read(): 'light' | 'dark' {
  const saved = document.cookie.match(new RegExp(`(?:^|; )${cookieName}=(light|dark)`))?.[1]
  if (saved) return saved as 'light' | 'dark'
  return matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function useSampleTheme() {
  const sync = () => (mode.value = read())
  onMounted(() => {
    sync()
    addEventListener('focus', sync)
  })
  onBeforeUnmount(() => removeEventListener('focus', sync))
  const toggle = () => {
    mode.value = mode.value === 'dark' ? 'light' : 'dark'
    document.cookie = `${cookieName}=${mode.value}; path=/; max-age=31536000; SameSite=Lax`
  }
  return { mode, toggle }
}

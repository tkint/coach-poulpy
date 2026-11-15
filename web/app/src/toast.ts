// The confirmation in the corner (CpToast): said once, then gone by itself after a few seconds. It lives outside
// any screen, so one that is said as a screen closes (a saved exercise sends the person back to the catalog) is still there on the next.
import { ref } from 'vue'

const SECONDS = 4

export const toast = ref<string>()
let timer: ReturnType<typeof setTimeout> | undefined

export function showToast(message: string) {
  clearTimeout(timer)
  toast.value = message
  timer = setTimeout(() => (toast.value = undefined), SECONDS * 1000)
}

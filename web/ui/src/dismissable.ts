// Every dropdown of the design system (select, multi-select, menu) is a `details` that closes the same
// way: Escape, a press anywhere outside it, or its own `close()`. Opening another one is a press outside the
// first, so only one is open at a time. The panels are absolutely positioned, so opening or closing one never
// moves the page.
import { onBeforeUnmount, onMounted, type Ref } from 'vue'

export function useDismissable(root: Ref<HTMLDetailsElement | undefined>) {
  const close = () => {
    if (root.value) root.value.open = false
  }
  const outside = (event: Event) => {
    if (root.value?.open && !root.value.contains(event.target as Node)) close()
  }
  const escape = (event: KeyboardEvent) => {
    if (event.key === 'Escape' && root.value?.open) {
      close()
      root.value.querySelector('summary')?.focus()
    }
  }
  onMounted(() => {
    document.addEventListener('pointerdown', outside)
    document.addEventListener('keydown', escape)
  })
  onBeforeUnmount(() => {
    document.removeEventListener('pointerdown', outside)
    document.removeEventListener('keydown', escape)
  })
  return { close }
}

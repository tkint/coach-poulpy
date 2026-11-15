<script setup lang="ts">
// A button that opens a short menu of actions only while it is used. Put `li` entries (each a `button`) in the
// default slot and the button's content (an icon, a count) in `trigger`; `label` names the button for assistive
// technology. Choosing an entry, Escape, a click elsewhere or scrolling closes the menu.
// The panel is fixed under its button, so a table's own overflow never clips it or adds a scrollbar to a small table.
import { onBeforeUnmount, ref } from 'vue'
import { useDismissable } from '../dismissable'

defineProps<{ label: string }>()
const root = ref<HTMLDetailsElement>()
const { close } = useDismissable(root)

const PANEL_WIDTH = 240
const MARGIN = 8
const position = ref<{ top: string; left: string }>()

function place() {
  const box = root.value?.querySelector('summary')?.getBoundingClientRect()
  if (!box) return
  position.value = {
    top: `${box.bottom + MARGIN}px`,
    left: `${Math.max(MARGIN, Math.min(box.left, window.innerWidth - PANEL_WIDTH - MARGIN))}px`,
  }
}
// The page moving under an open menu would leave it behind, so it closes.
const scrolled = () => close()
function toggled() {
  if (root.value?.open) {
    place()
    window.addEventListener('scroll', scrolled, { capture: true, once: true })
    window.addEventListener('resize', scrolled, { once: true })
  }
}
onBeforeUnmount(() => {
  window.removeEventListener('scroll', scrolled, { capture: true })
  window.removeEventListener('resize', scrolled)
})
</script>

<template>
  <details ref="root" class="relative inline-block" data-component="dropdown-menu" @toggle="toggled">
    <summary class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm relative list-none" :aria-label="label"><slot name="trigger" /></summary>
    <ul class="cp-menu fixed z-20 w-60 rounded-box border border-base-300 bg-base-100 p-2 text-start shadow-lg" :style="position" @click="close"><slot /></ul>
  </details>
</template>

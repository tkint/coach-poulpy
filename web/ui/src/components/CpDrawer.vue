<script setup lang="ts">
// The admin's page frame: a full-height sidebar from a large screen up, which the button in the top
// bar folds to icons only; on a smaller screen the sidebar slides over the page. `open` is the
// expanded state. Items in the sidebar hide their text with the `cp-is-drawer-close:hidden` variant
// and show a tooltip with `cp-is-drawer-close:cp-tooltip`, as the Admin mockup does.
import { onBeforeUnmount, onMounted, useId } from 'vue'

defineProps<{ toggleLabel: string; closeLabel: string }>()
const open = defineModel<boolean>('open', { default: false })
const id = useId()

// Expanded on a large screen and closed below it, following the window.
let large: MediaQueryList | undefined
const sync = () => {
  open.value = large?.matches ?? false
}
onMounted(() => {
  large = matchMedia('(min-width: 1024px)')
  large.addEventListener('change', sync)
  sync()
})
onBeforeUnmount(() => large?.removeEventListener('change', sync))
</script>

<template>
  <div class="cp-drawer lg:cp-drawer-open" data-component="drawer">
    <input :id="id" v-model="open" type="checkbox" class="cp-drawer-toggle" :aria-label="toggleLabel">
    <div class="cp-drawer-content flex flex-col">
      <nav class="cp-navbar w-full bg-base-100 px-2 shadow-sm md:px-4">
        <label :for="id" :aria-label="toggleLabel" class="cp-btn cp-btn-square cp-btn-ghost"><slot name="toggle-icon" /></label>
        <div class="px-2 font-bold"><slot name="title" /></div>
      </nav>
      <slot />
    </div>
    <div class="cp-drawer-side cp-is-drawer-close:overflow-visible">
      <label :for="id" :aria-label="closeLabel" class="cp-drawer-overlay" />
      <div class="flex min-h-full flex-col items-start border-r border-base-300 bg-base-100 cp-is-drawer-close:w-16 cp-is-drawer-open:w-64">
        <slot name="sidebar" />
      </div>
    </div>
  </div>
</template>

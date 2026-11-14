<script setup lang="ts">
// A button that opens a short menu of actions only while it is used. Put `li` entries (each a `button`) in the
// default slot and the button's content (an icon, a count) in `trigger`; `label` names the button for assistive
// technology. Choosing an entry, Escape or a click elsewhere closes the menu.
import { ref } from 'vue'
import { useDismissable } from '../dismissable'

defineProps<{ label: string }>()
const root = ref<HTMLDetailsElement>()
const { close } = useDismissable(root)
</script>

<template>
  <details ref="root" class="relative inline-block" data-component="dropdown-menu">
    <summary class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm relative list-none" :aria-label="label"><slot name="trigger" /></summary>
    <ul class="cp-menu absolute left-0 z-20 mt-2 w-60 rounded-box border border-base-300 bg-base-100 p-2 text-start shadow-lg" @click="close"><slot /></ul>
  </details>
</template>

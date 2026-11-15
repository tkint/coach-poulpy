<script setup lang="ts">
// A sheet over the screen: a dialog that holds what the slot gives it (a handle, a title, rows, actions).
// Its `aria-label`, `id`, `aria-modal` and `style` go through to the section.
</script>

<template>
  <section class="sheet" data-component="sheet" role="dialog"><slot /></section>
</template>

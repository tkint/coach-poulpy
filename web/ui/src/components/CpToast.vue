<script setup lang="ts">
import CpIcon from './CpIcon.vue'
// A short confirmation in the bottom corner that goes by itself, such as "Exercise saved": a status, not an
// alert, so it never takes the focus. Whoever shows it decides how long it stays; an error that needs reading
// again belongs in a CpErrorNote on the screen.
import { Check } from '@lucide/vue'
</script>

<template>
  <div class="cp-toast cp-toast-end cp-toast-bottom z-50" data-component="toast">
    <div class="cp-alert cp-alert-success shadow-lg" role="status">
      <CpIcon :icon="Check" />
      <span><slot /></span>
    </div>
  </div>
</template>

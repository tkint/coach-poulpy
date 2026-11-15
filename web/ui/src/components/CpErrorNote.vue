<script setup lang="ts">
import CpIcon from './CpIcon.vue'
// An error: crimson, always with an icon and words. Never ember, which only means a session slipped.
import { CircleExclamationPoint } from '@lucide/vue'
</script>

<template>
  <div class="cp-alert cp-alert-error cp-error-note" role="alert" data-component="error-note">
    <CpIcon :icon="CircleExclamationPoint" />
    <span><slot /></span>
  </div>
</template>

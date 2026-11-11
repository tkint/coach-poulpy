<script setup lang="ts">
// An error: crimson, always with an icon and words. Never ember, which only means a session slipped.
import { CircleExclamationPoint } from '@lucide/vue'
</script>

<template>
  <div class="cp-alert cp-alert-error cp-error-note" role="alert" data-component="error-note">
    <CircleExclamationPoint :size="20" aria-hidden="true" />
    <span><slot /></span>
  </div>
</template>

<script setup lang="ts">
// An error: crimson, always with an icon and words. Never ember, which only means a session slipped.
</script>

<template>
  <div class="cp-alert cp-alert-error cp-error-note" role="alert" data-component="error-note">
    <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="9" /><path d="M12 7v6M12 16.5v.5" /></svg>
    <span><slot /></span>
  </div>
</template>

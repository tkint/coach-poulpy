<script setup lang="ts">
// Something went through, said in words with a check: a test email sent. The counterpart of CpErrorNote;
// a person reads it once, so it is a status, not an alert.
</script>

<template>
  <div class="cp-alert cp-alert-success" role="status" data-component="success-note">
    <svg class="size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6 9 17l-5-5" /></svg>
    <span><slot /></span>
  </div>
</template>

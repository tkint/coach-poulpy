<script setup lang="ts">
// Something went through, said in words with a check: a test email sent. The counterpart of CpErrorNote;
// a person reads it once, so it is a status, not an alert.
import { Check } from '@lucide/vue'
</script>

<template>
  <div class="cp-alert cp-alert-success" role="status" data-component="success-note">
    <Check class="size-5 shrink-0" aria-hidden="true" />
    <span><slot /></span>
  </div>
</template>

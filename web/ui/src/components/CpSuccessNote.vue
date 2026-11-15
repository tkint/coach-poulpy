<script setup lang="ts">
import CpIcon from './CpIcon.vue'
// Something went through, said in words with a check: a test email sent. The counterpart of CpErrorNote;
// a person reads it once, so it is a status, not an alert.
import { Check } from '@lucide/vue'
</script>

<template>
  <div class="cp-alert cp-alert-success" role="status" data-component="success-note">
    <CpIcon :icon="Check" />
    <span><slot /></span>
  </div>
</template>

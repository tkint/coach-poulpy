<script setup lang="ts">
// The public status, /status (design/mockups/agreed/web/app-status.html and app-status-down.html, #340): up or down,
// nothing about how the server is configured. A server that can't be reached is down too.
import { onMounted, ref } from 'vue'
import { CpErrorNote, CpSuccessNote } from '@coach-poulpy/ui'
import SitePage from '../SitePage.vue'
import { serverStatus } from '../api'
import { t } from '../i18n'

const status = ref<'checking' | 'up' | 'down'>('checking')
onMounted(async () => (status.value = await serverStatus()))
</script>

<template>
  <SitePage drawing="mood_happy">
    <h1 class="cp-type-display">{{ t('status.title') }}</h1>
    <p v-if="status === 'checking'">{{ t('status.checking') }}</p>
    <template v-else-if="status === 'up'">
      <CpSuccessNote>{{ t('status.up') }}</CpSuccessNote>
      <p>{{ t('status.upBody') }}</p>
    </template>
    <template v-else>
      <CpErrorNote>{{ t('status.down') }}</CpErrorNote>
      <p>{{ t('status.downBody') }}</p>
    </template>
  </SitePage>
</template>

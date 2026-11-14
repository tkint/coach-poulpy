<script setup lang="ts">
// The profile (design/mockups/agreed/web/app-profile.html): the account, a password-reset email, and where the data lives.
import { ref } from 'vue'
import { Check } from '@lucide/vue'
import { CpAvatar, CpBadge, CpButton, CpCard, CpErrorNote } from '@coach-poulpy/ui'
import { requestPasswordReset } from '../api'
import type { Me } from '../session'
import { t } from '../i18n'

const props = defineProps<{ me: Me }>()
const reset = ref<Awaited<ReturnType<typeof requestPasswordReset>> | 'sending'>()

async function sendResetLink() {
  reset.value = 'sending'
  reset.value = await requestPasswordReset(props.me.email)
}
</script>

<template>
  <main class="flex flex-col gap-6 p-4 md:p-8">
    <div class="flex flex-col gap-1">
      <h1 class="cp-type-display">{{ t('app.profileTitle') }}</h1>
      <p class="text-base-content/70">{{ t('app.profileIntro') }}</p>
    </div>
    <CpCard>
      <div class="flex items-center gap-4">
        <CpAvatar :initials="me.email.charAt(0).toUpperCase()" tone="primary" size="lg" />
        <div class="flex min-w-0 flex-col gap-1">
          <h2 class="cp-card-title truncate">{{ me.email }}</h2>
          <CpBadge class="gap-1"><Check class="size-3 shrink-0" aria-hidden="true" />{{ t('app.emailConfirmed') }}</CpBadge>
        </div>
      </div>
    </CpCard>
    <CpCard :title="t('app.passwordTitle')">
      <p class="text-base-content/70">{{ t('app.passwordBody') }}</p>
      <CpErrorNote v-if="reset === 'offline'">{{ t('admin.errorOffline') }}</CpErrorNote>
      <p v-else-if="reset === 'done'" role="status">{{ t('app.resetLinkSent') }}</p>
      <div class="cp-card-actions">
        <CpButton variant="outline" :disabled="reset === 'sending'" @click="sendResetLink">{{ t('app.sendResetLink') }}</CpButton>
      </div>
    </CpCard>
    <CpCard :title="t('app.dataTitle')">
      <p class="text-base-content/70">{{ t('app.dataBody') }}</p>
    </CpCard>
  </main>
</template>

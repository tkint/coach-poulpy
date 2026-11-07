<script setup lang="ts">
// The link in the confirmation email when it opens in a browser: /confirm-email#<token>.
import { onMounted, ref } from 'vue'
import { CpButton, CpErrorNote } from '@coach-poulpy/ui'
import SitePage from '../SitePage.vue'
import { confirmEmail, type LinkAnswer } from '../api'
import { t } from '../i18n'

const props = defineProps<{ token: string }>()
const answer = ref<LinkAnswer | 'checking'>('checking')

async function confirm() {
  answer.value = 'checking'
  answer.value = props.token ? await confirmEmail(props.token) : 'expired'
}
onMounted(confirm)
</script>

<template>
  <SitePage v-if="answer === 'done'" drawing="pose_champion">
    <div class="flex flex-col gap-2">
      <h1 class="cp-type-display">{{ t('account.confirmedTitle') }}</h1>
      <p>{{ t('account.confirmedBody') }}</p>
    </div>
    <p class="font-bold text-primary">{{ t('account.confirmedPoulpy') }}</p>
  </SitePage>
  <SitePage v-else-if="answer === 'expired' || answer === 'invalid-password'" drawing="pose_thinking">
    <div class="flex flex-col gap-2">
      <h1 class="cp-type-display">{{ t('account.linkExpiredTitle') }}</h1>
      <p>{{ t('account.confirmExpiredBody') }}</p>
    </div>
  </SitePage>
  <SitePage v-else drawing="pose_thinking">
    <p v-if="answer === 'checking'">{{ t('account.checking') }}</p>
    <template v-else>
      <CpErrorNote>{{ t('account.errorOffline') }}</CpErrorNote>
      <CpButton @click="confirm">{{ t('account.tryAgain') }}</CpButton>
    </template>
  </SitePage>
</template>

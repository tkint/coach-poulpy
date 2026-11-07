<script setup lang="ts">
// The link in the password reset email when it opens in a browser: /reset-password#<token>.
import { ref } from 'vue'
import { CpButton, CpErrorNote, CpPasswordField } from '@coach-poulpy/ui'
import SitePage from '../SitePage.vue'
import { setNewPassword, type LinkAnswer } from '../api'
import { t } from '../i18n'

const props = defineProps<{ token: string }>()
const password = ref('')
const answer = ref<LinkAnswer | 'too-short' | 'sending' | undefined>(props.token ? undefined : 'expired')

async function save() {
  // The same rule as the server and the apps: 8 characters or more.
  if (password.value.length < 8) {
    answer.value = 'too-short'
    return
  }
  answer.value = 'sending'
  const result = await setNewPassword(props.token, password.value)
  answer.value = result === 'invalid-password' ? 'too-short' : result
}
</script>

<template>
  <SitePage v-if="answer === 'done'" drawing="pose_standing_tall">
    <div class="flex flex-col gap-2">
      <h1 class="cp-type-display">{{ t('account.savedTitle') }}</h1>
      <p>{{ t('account.savedBody') }}</p>
    </div>
  </SitePage>
  <SitePage v-else-if="answer === 'expired'" drawing="pose_thinking">
    <div class="flex flex-col gap-2">
      <h1 class="cp-type-display">{{ t('account.linkExpiredTitle') }}</h1>
      <p>{{ t('account.resetExpiredBody') }}</p>
    </div>
  </SitePage>
  <SitePage v-else :drawing="answer === 'too-short' ? 'pose_thinking' : 'pose_standing_tall'">
    <div class="flex flex-col gap-2">
      <h1 class="cp-type-display">{{ t('account.resetTitle') }}</h1>
      <p>{{ t('account.resetIntro') }}</p>
    </div>
    <form class="flex flex-col gap-6" novalidate @submit.prevent="save">
      <CpPasswordField
        v-model="password"
        :label="t('account.newPassword')"
        :hint="t('account.passwordHint')"
        :show-label="t('account.showPassword')"
        :hide-label="t('account.hidePassword')"
        :invalid="answer === 'too-short'"
        autocomplete="new-password"
      />
      <CpErrorNote v-if="answer === 'too-short'">{{ t('account.errorPasswordShort') }}</CpErrorNote>
      <CpErrorNote v-else-if="answer === 'offline'">{{ t('account.errorOffline') }}</CpErrorNote>
      <CpButton type="submit" class="w-full" :disabled="answer === 'sending'">{{ t('account.savePassword') }}</CpButton>
    </form>
  </SitePage>
</template>

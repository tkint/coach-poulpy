<script setup lang="ts">
// The admin's sign-in (design/mockups/agreed/web/admin-sign-in.html). The cookies the server sets are
// the session; this page only says whether it worked.
import { ref } from 'vue'
import { CpButton, CpErrorNote, CpPasswordField, CpTextField } from '@coach-poulpy/ui'
import SitePage from '../SitePage.vue'
import { signIn, type SignInAnswer } from '../session'
import { t } from '../i18n'

const emit = defineEmits<{ signedIn: [] }>()
const email = ref('')
const password = ref('')
const failed = ref<Exclude<SignInAnswer, 'signedIn'> | 'sending'>()

async function submit() {
  failed.value = 'sending'
  const answer = await signIn(email.value.trim(), password.value, 'admin')
  if (answer === 'signedIn') emit('signedIn')
  else failed.value = answer
}
</script>

<template>
  <SitePage :drawing="failed === 'refused' ? 'mood_happy' : 'pose_standing_tall'">
    <div class="flex flex-col gap-2">
      <h1 class="cp-type-display">{{ t('admin.signInTitle') }}</h1>
      <p>{{ t('admin.signInIntro') }}</p>
    </div>
    <form class="flex flex-col gap-6" novalidate @submit.prevent="submit">
      <CpTextField v-model="email" type="email" :label="t('admin.email')" :invalid="failed === 'refused'" autocomplete="username" />
      <CpPasswordField
        v-model="password"
        :label="t('admin.password')"
        :show-label="t('admin.showPassword')"
        :hide-label="t('admin.hidePassword')"
        :invalid="failed === 'refused'"
        autocomplete="current-password"
      />
      <CpErrorNote v-if="failed === 'refused'">{{ t('admin.errorSignIn') }}</CpErrorNote>
      <CpErrorNote v-else-if="failed === 'throttled'">{{ t('admin.errorTooMany') }}</CpErrorNote>
      <CpErrorNote v-else-if="failed === 'offline'">{{ t('admin.errorOffline') }}</CpErrorNote>
      <CpButton type="submit" class="w-full" :disabled="failed === 'sending'">{{ t('admin.signIn') }}</CpButton>
    </form>
  </SitePage>
</template>

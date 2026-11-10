<script setup lang="ts">
// The settings screen of the admin (design/mockups/agreed/web/admin-settings-mail*.html), at /admin/settings:
// the from-address of the emails, saved without a deploy, and a test email to the admin's own address with the
// saved settings (#339). The mail server itself (host, port, user, password) stays in the server's environment.
// Nothing here is a view to bookmark beyond the path: the test's result is the answer to a click.
import { Send } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import { CpButton, CpCard, CpDivider, CpErrorNote, CpSuccessNote, CpTextField } from '@coach-poulpy/ui'
import { getSettings, saveMailFrom, sendTestEmail, type TestEmailAnswer } from '../session'
import { t } from '../i18n'

const props = defineProps<{ email: string }>()

const state = ref<'loading' | 'ready' | 'failed'>('loading')
const mailFrom = ref('')
const invalid = ref(false)
const saved = ref(false)
const saveFailed = ref(false)
const saving = ref(false)
const testing = ref(false)
const test = ref<TestEmailAnswer | 'offline'>()
/** What the mail server said when it refused the test email. */
const refusal = computed(() => (test.value && test.value !== 'offline' && !test.value.sent ? test.value.error : undefined))

async function load() {
  state.value = 'loading'
  const settings = await getSettings()
  if (!settings) {
    state.value = 'failed'
    return
  }
  mailFrom.value = settings.mailFrom ?? ''
  state.value = 'ready'
}

onMounted(load)

// Typing again clears what the last save said.
watch(mailFrom, () => {
  invalid.value = false
  saved.value = false
  saveFailed.value = false
})

async function save() {
  saving.value = true
  const answer = await saveMailFrom(mailFrom.value.trim())
  saving.value = false
  invalid.value = answer === 'invalid'
  saveFailed.value = answer === 'failed'
  saved.value = answer === 'saved'
}

async function sendTest() {
  testing.value = true
  test.value = undefined
  test.value = (await sendTestEmail()) ?? 'offline'
  testing.value = false
}
</script>

<template>
  <main class="flex flex-col gap-6 p-4 md:p-8">
    <div class="flex flex-col gap-1">
      <h1 class="cp-type-display">{{ t('admin.settingsTitle') }}</h1>
      <p class="text-base-content/70">{{ t('admin.settingsIntro') }}</p>
    </div>
    <CpErrorNote v-if="state === 'failed'">{{ t('admin.settingsError') }}</CpErrorNote>
    <CpCard v-else-if="state === 'ready'" :title="t('admin.mailSenderTitle')">
      <form class="flex flex-col gap-4" novalidate @submit.prevent="save">
        <CpTextField
          v-model="mailFrom"
          :label="t('admin.mailFromLabel')"
          type="email"
          autocomplete="off"
          :hint="t('admin.mailFromHint')"
          :error="invalid ? t('admin.mailFromInvalid') : undefined"
        />
        <CpSuccessNote v-if="saved">{{ t('admin.mailFromSaved') }}</CpSuccessNote>
        <CpErrorNote v-if="saveFailed">{{ t('admin.saveError') }}</CpErrorNote>
        <div class="flex flex-wrap justify-end gap-2">
          <CpButton type="submit" size="md" :disabled="saving">{{ t('admin.save') }}</CpButton>
        </div>
      </form>
      <CpDivider />
      <div class="flex flex-col gap-3">
        <div class="flex flex-col gap-1">
          <h3 class="cp-type-label">{{ t('admin.testEmailTitle') }}</h3>
          <p class="text-sm text-base-content/70">{{ t('admin.testEmailHint', { email: props.email }) }}</p>
        </div>
        <div>
          <CpButton variant="outline" size="md" :disabled="testing" @click="sendTest">
            <Send class="size-5 shrink-0" aria-hidden="true" />
            {{ t('admin.testEmailSend') }}
          </CpButton>
        </div>
        <CpSuccessNote v-if="test && test !== 'offline' && test.sent">{{ t('admin.testEmailSent', { email: props.email }) }}</CpSuccessNote>
        <CpErrorNote v-else-if="refusal !== undefined">{{ t('admin.testEmailFailed', { error: refusal }) }}</CpErrorNote>
        <CpErrorNote v-else-if="test === 'offline'">{{ t('admin.errorOffline') }}</CpErrorNote>
      </div>
      <p class="text-sm text-base-content/70">{{ t('admin.mailServerNote') }}</p>
    </CpCard>
  </main>
</template>

<script setup lang="ts">
// The status screen of the admin (design/mockups/agreed/web/admin-status*.html), at /admin/status (#340): whether
// the server has a mail server and how the last email went, then the accounts that haven't confirmed their email.
// It only reads; the mail server is set in the server's environment and no email is sent from here.
import { computed, onMounted, ref } from 'vue'
import { CpBadge, CpCard, CpErrorNote, CpTable } from '@coach-poulpy/ui'
import { getStatus, pendingRegistrations, type AdminStatus, type LastSend, type PendingRegistration } from '../session'
import { locale, t } from '../i18n'

const status = ref<AdminStatus | 'loading' | 'failed'>('loading')
const pending = ref<PendingRegistration[] | 'loading' | 'failed'>('loading')

onMounted(async () => {
  const [loadedStatus, loadedPending] = await Promise.all([getStatus(), pendingRegistrations()])
  status.value = loadedStatus ?? 'failed'
  pending.value = loadedPending ?? 'failed'
})

const moment = new Intl.DateTimeFormat(locale, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
const time = (at: string) => moment.format(new Date(at))
const kinds: Record<LastSend['kind'], string> = {
  confirmation: 'admin.kindConfirmation',
  'password-reset': 'admin.kindPasswordReset',
  test: 'admin.kindTest',
}

const mail = computed(() => (typeof status.value === 'object' ? status.value.mail : undefined))
/** "Last send: sent, 9 Nov, 08:41 (a confirmation email).", split around the word in bold. */
const lastSent = computed(() => {
  const last = mail.value?.lastSend
  if (!last?.sent) return undefined
  const [before, after = ''] = t('admin.lastSendSent', { when: time(last.at), kind: t(kinds[last.kind]) }).split('{status}')
  return { before, after }
})
</script>

<template>
  <main class="flex flex-col gap-6 p-4 md:p-8">
    <div class="flex flex-col gap-1">
      <h1 class="cp-type-display">{{ t('admin.statusTitle') }}</h1>
      <p class="text-base-content/70">{{ t('admin.statusIntro') }}</p>
    </div>
    <CpErrorNote v-if="status === 'failed'">{{ t('admin.statusError') }}</CpErrorNote>
    <CpCard v-else-if="mail" :title="t('admin.mailTitle')">
      <template #badge>
        <CpBadge v-if="mail.configured" tone="success">{{ t('admin.mailConfigured') }}</CpBadge>
        <CpBadge v-else tone="error">{{ t('admin.mailNotConfigured') }}</CpBadge>
      </template>
      <template v-if="mail.configured">
        <p class="text-base-content/70">{{ t('admin.mailConfiguredBody') }}</p>
        <CpErrorNote v-if="mail.lastSend && !mail.lastSend.sent">
          {{ t('admin.lastSendFailed', { when: time(mail.lastSend.at), error: mail.lastSend.error ?? '' }) }}
        </CpErrorNote>
        <p v-else-if="lastSent" class="text-sm">{{ lastSent.before }}<strong>{{ t('admin.lastSendStatusSent') }}</strong>{{ lastSent.after }}</p>
        <p v-else class="text-sm">{{ t('admin.lastSendNone') }}</p>
      </template>
      <template v-else>
        <CpErrorNote>{{ t('admin.mailMissing') }}</CpErrorNote>
        <p class="text-sm text-base-content/70">{{ t('admin.mailMissingNote') }}</p>
      </template>
    </CpCard>
    <CpErrorNote v-if="pending === 'failed'">{{ t('admin.pendingError') }}</CpErrorNote>
    <CpCard v-else-if="pending !== 'loading'" :title="t('admin.pendingTitle')" :description="t('admin.pendingIntro')">
      <p v-if="!pending.length">{{ t('admin.pendingNone') }}</p>
      <CpTable v-else>
        <thead>
          <tr><th>{{ t('admin.colEmail') }}</th><th>{{ t('admin.colSignedUp') }}</th><th>{{ t('admin.colLinkExpires') }}</th></tr>
        </thead>
        <tbody>
          <tr v-for="registration in pending" :key="registration.email">
            <td class="break-all">{{ registration.email }}</td>
            <td class="whitespace-nowrap">{{ time(registration.signedUpAt) }}</td>
            <td class="whitespace-nowrap">{{ registration.linkExpiresAt ? time(registration.linkExpiresAt) : t('admin.linkNone') }}</td>
          </tr>
        </tbody>
      </CpTable>
    </CpCard>
  </main>
</template>

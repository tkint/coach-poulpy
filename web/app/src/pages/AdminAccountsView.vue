<script setup lang="ts">
// The accounts screen of the admin (design/mockups/agreed/web/admin-accounts.html): find an account by its
// email, see its state, disable or enable it, or delete it after typing its email again (#336).
// It never shows a workout or any health data.
import { computed, ref } from 'vue'
import { CpAvatar, CpBadge, CpButton, CpCard, CpErrorNote, CpPoulpyCard, CpSheet, CpTextField } from '@coach-poulpy/ui'
import { deleteAccount, findAccount, setAccountDisabled, type AdminAccount } from '../session'
import { locale, t } from '../i18n'

const props = defineProps<{ ownAccountId: string }>()

const email = ref('')
const account = ref<AdminAccount>()
const state = ref<'idle' | 'none' | 'failed'>('idle')
const busy = ref(false)
const deleting = ref(false)
const typed = ref('')

const day = new Intl.DateTimeFormat(locale, { day: 'numeric', month: 'short', year: 'numeric' })
const moment = new Intl.DateTimeFormat(locale, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
const isOwn = computed(() => account.value?.id === props.ownAccountId)
const confirmed = computed(() => typed.value.trim().toLowerCase() === account.value?.email.toLowerCase())

async function find() {
  if (!email.value.trim()) return
  busy.value = true
  const found = await findAccount(email.value)
  busy.value = false
  account.value = typeof found === 'object' ? found.account : undefined
  state.value = typeof found === 'object' ? 'idle' : found
}

async function toggle() {
  if (!account.value) return
  busy.value = true
  const done = await setAccountDisabled(account.value.id, !account.value.disabledAt)
  busy.value = false
  if (done) await find()
  else state.value = 'failed'
}

function askDelete() {
  typed.value = ''
  deleting.value = true
}

async function remove() {
  if (!account.value || !confirmed.value) return
  busy.value = true
  const done = await deleteAccount(account.value.id, typed.value)
  busy.value = false
  deleting.value = false
  if (done) {
    account.value = undefined
    state.value = 'none'
  } else {
    state.value = 'failed'
  }
}
</script>

<template>
  <main class="flex flex-col gap-6 p-4 md:p-8">
    <div class="flex flex-col gap-1">
      <h1 class="cp-type-display">{{ t('admin.accountsTitle') }}</h1>
      <p class="text-base-content/70">{{ t('admin.accountsIntro') }}</p>
    </div>
    <form class="flex flex-col gap-1 sm:flex-row sm:items-end" role="search" @submit.prevent="find">
      <CpTextField v-model="email" class="grow" type="search" :label="t('admin.fieldEmail')" autocomplete="off" />
      <CpButton type="submit" size="md" :disabled="busy">{{ t('admin.accountsFind') }}</CpButton>
    </form>
    <CpErrorNote v-if="state === 'failed'">{{ t('admin.accountsError') }}</CpErrorNote>
    <CpPoulpyCard v-else-if="state === 'none'" :title="t('admin.accountsNone')">{{ t('admin.accountsNoneHint') }}</CpPoulpyCard>
    <CpCard v-else-if="account">
      <div class="flex flex-wrap items-center gap-3">
        <CpAvatar :initials="account.email.charAt(0).toUpperCase()" />
        <h2 class="cp-card-title min-w-0 break-all">{{ account.email }}</h2>
        <CpBadge :tone="account.disabledAt ? 'outline' : 'primary'">{{ account.disabledAt ? t('admin.accountDisabled') : t('admin.accountActive') }}</CpBadge>
      </div>
      <dl class="grid gap-x-8 gap-y-3 sm:grid-cols-2">
        <div><dt class="cp-type-label">{{ t('admin.fieldEmail') }}</dt><dd>{{ account.confirmed ? t('admin.emailConfirmed') : t('admin.emailNotConfirmed') }}</dd></div>
        <div><dt class="cp-type-label">{{ t('admin.fieldRole') }}</dt><dd>{{ account.role === 'admin' ? t('admin.roleAdmin') : t('admin.roleMember') }}</dd></div>
        <div><dt class="cp-type-label">{{ t('admin.fieldCreated') }}</dt><dd>{{ day.format(new Date(account.createdAt)) }}</dd></div>
        <div>
          <dt class="cp-type-label">{{ t('admin.fieldSignIn') }}</dt>
          <dd>{{ account.disabledAt ? t('admin.signInBlocked', { when: moment.format(new Date(account.disabledAt)) }) : t('admin.signInAllowed') }}</dd>
        </div>
      </dl>
      <p v-if="isOwn" class="text-sm text-base-content/70">{{ t('admin.ownAccount') }}</p>
      <template v-else>
        <p v-if="!account.disabledAt" class="text-sm text-base-content/70">{{ t('admin.disableHint') }}</p>
        <div class="cp-card-actions flex-wrap justify-end">
          <CpButton :variant="account.disabledAt ? 'primary' : 'outline'" size="md" :disabled="busy" @click="toggle">
            {{ account.disabledAt ? t('admin.enable') : t('admin.disable') }}
          </CpButton>
          <CpButton variant="danger" size="md" :disabled="busy" @click="askDelete">{{ t('admin.delete') }}</CpButton>
        </div>
      </template>
    </CpCard>
    <CpSheet v-if="account" v-model:open="deleting" :title="t('admin.deleteTitle')" :close-label="t('admin.deleteClose')">
      <p class="py-2">{{ t('admin.deleteBody', { email: account.email }) }}</p>
      <CpTextField v-model="typed" :label="t('admin.deleteConfirmLabel')" autocomplete="off" />
      <template #actions>
        <CpButton variant="outline" size="md" @click="deleting = false">{{ t('admin.deleteCancel') }}</CpButton>
        <CpButton variant="danger" size="md" :disabled="!confirmed || busy" @click="remove">{{ t('admin.deleteConfirm') }}</CpButton>
      </template>
    </CpSheet>
  </main>
</template>

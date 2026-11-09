<script setup lang="ts">
// The accounts screen of the admin (design/mockups/agreed/web/admin-accounts*.html): a paged table of every
// account, sorted and filtered on the server, with the account picked in a panel floating on the right where it is
// disabled, enabled or deleted after typing its email again (#336). It never shows a workout or health data.
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import {
  CpAvatar,
  CpBadge,
  CpButton,
  CpCard,
  CpErrorNote,
  CpPagination,
  CpPoulpyCard,
  CpSelectField,
  CpSheet,
  CpSidePanel,
  CpTable,
  CpTextField,
} from '@coach-poulpy/ui'
import { deleteAccount, listAccounts, setAccountDisabled, type AccountsQuery, type AdminAccount } from '../session'
import { locale, t } from '../i18n'

const props = defineProps<{ ownAccountId: string }>()

const PAGE_SIZE = 8
type Sort = AccountsQuery['sort']

const query = reactive<AccountsQuery>({ q: '', role: '', confirmed: '', signIn: '', sort: 'created', order: 'desc', page: 1, size: PAGE_SIZE })
const accounts = ref<AdminAccount[]>([])
const total = ref(0)
const state = ref<'loading' | 'ready' | 'failed'>('loading')
const selected = ref<AdminAccount>()
const busy = ref(false)
const deleting = ref(false)
const typed = ref('')

const day = new Intl.DateTimeFormat(locale, { day: 'numeric', month: 'short', year: 'numeric' })
const moment = new Intl.DateTimeFormat(locale, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
const pages = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))
const showing = computed(() =>
  t('admin.showing', {
    from: String((query.page - 1) * PAGE_SIZE + 1),
    to: String(Math.min(query.page * PAGE_SIZE, total.value)),
    total: String(total.value),
  }),
)
const isOwn = computed(() => selected.value?.id === props.ownAccountId)
const confirmed = computed(() => typed.value.trim().toLowerCase() === selected.value?.email.toLowerCase())

const columns: { sort: Sort; label: string }[] = [
  { sort: 'email', label: 'admin.fieldEmail' },
  { sort: 'role', label: 'admin.fieldRole' },
  { sort: 'confirmed', label: 'admin.colEmailStatus' },
  { sort: 'signIn', label: 'admin.fieldSignIn' },
  { sort: 'created', label: 'admin.fieldCreated' },
]
const roleOptions = computed(() => [
  { value: '', text: t('admin.filterAll') },
  { value: 'member', text: t('admin.roleMember') },
  { value: 'admin', text: t('admin.roleAdmin') },
])
const confirmedOptions = computed(() => [
  { value: '', text: t('admin.filterAll') },
  { value: 'yes', text: t('admin.emailConfirmed') },
  { value: 'no', text: t('admin.emailNotConfirmed') },
])
const signInOptions = computed(() => [
  { value: '', text: t('admin.filterAll') },
  { value: 'active', text: t('admin.accountActive') },
  { value: 'disabled', text: t('admin.accountDisabled') },
])

const ariaSort = (sort: Sort) => (query.sort !== sort ? 'none' : query.order === 'asc' ? 'ascending' : 'descending')

/** A new column sorts ascending first; the same column flips its direction. */
function sortBy(sort: Sort) {
  query.order = query.sort === sort && query.order === 'asc' ? 'desc' : 'asc'
  query.sort = sort
  query.page = 1
}

let latest = 0
async function load() {
  const asked = ++latest
  const page = await listAccounts(query)
  if (asked !== latest) return // a newer question was asked meanwhile
  if (!page) {
    state.value = 'failed'
    return
  }
  // The last page of a list that shrank (a deletion, a filter) is gone: go back to the new last one.
  if (page.accounts.length === 0 && page.total > 0 && query.page > 1) {
    query.page = Math.ceil(page.total / PAGE_SIZE)
    return
  }
  accounts.value = page.accounts
  total.value = page.total
  state.value = 'ready'
  if (selected.value) selected.value = page.accounts.find((a) => a.id === selected.value?.id) ?? selected.value
}

// Typing in the email filter waits for a pause; every other change loads at once, from the first page.
let pause: ReturnType<typeof setTimeout> | undefined
watch(
  () => [query.role, query.confirmed, query.signIn],
  () => {
    query.page = 1
  },
)
watch(
  () => query.q,
  () => {
    clearTimeout(pause)
    pause = setTimeout(() => {
      query.page = 1
      void load()
    }, 300)
  },
)
watch(() => [query.role, query.confirmed, query.signIn, query.sort, query.order, query.page], load)
onMounted(load)
onBeforeUnmount(() => clearTimeout(pause))

async function toggle() {
  const account = selected.value
  if (!account) return
  busy.value = true
  const done = await setAccountDisabled(account.id, !account.disabledAt)
  busy.value = false
  if (done) await load()
  else state.value = 'failed'
}

function askDelete() {
  typed.value = ''
  deleting.value = true
}

async function remove() {
  const account = selected.value
  if (!account || !confirmed.value) return
  busy.value = true
  const done = await deleteAccount(account.id, typed.value)
  busy.value = false
  deleting.value = false
  if (done) {
    selected.value = undefined
    await load()
  } else {
    state.value = 'failed'
  }
}
</script>

<template>
  <main class="flex min-h-full">
    <div class="flex min-w-0 grow flex-col gap-6 p-4 md:p-8">
      <div class="flex flex-col gap-1">
        <h1 class="cp-type-display">{{ t('admin.accountsTitle') }}</h1>
        <p class="text-base-content/70">{{ t('admin.accountsIntro') }}</p>
      </div>
      <form class="cp-card bg-base-100 shadow-sm" role="search" :aria-label="t('admin.filtersLabel')" @submit.prevent>
        <div class="cp-card-body grid gap-4 p-4 sm:grid-cols-2 lg:grid-cols-4 md:p-6">
          <CpTextField v-model="query.q" class="sm:col-span-2 lg:col-span-1" type="search" :label="t('admin.filterEmail')" autocomplete="off" />
          <CpSelectField v-model="query.role" :label="t('admin.fieldRole')" :options="roleOptions" />
          <CpSelectField v-model="query.confirmed" :label="t('admin.fieldEmail')" :options="confirmedOptions" />
          <CpSelectField v-model="query.signIn" :label="t('admin.fieldSignIn')" :options="signInOptions" />
        </div>
      </form>
      <CpErrorNote v-if="state === 'failed'">{{ t('admin.accountsError') }}</CpErrorNote>
      <CpPoulpyCard v-else-if="state === 'ready' && total === 0" :title="t('admin.accountsNone')">{{ t('admin.accountsNoneHint') }}</CpPoulpyCard>
      <CpCard v-else-if="state === 'ready'">
        <CpTable>
          <thead>
              <tr>
                <th v-for="column in columns" :key="column.sort" :aria-sort="ariaSort(column.sort)">
                  <button type="button" class="cp-btn cp-btn-ghost cp-btn-sm -ms-3 gap-1 whitespace-nowrap" @click="sortBy(column.sort)">
                    {{ t(column.label) }}
                    <svg class="size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <path v-if="query.sort !== column.sort" d="m7 15 5 5 5-5" />
                      <path v-if="query.sort !== column.sort" d="m7 9 5-5 5 5" />
                      <path v-else-if="query.order === 'asc'" d="m18 15-6-6-6 6" />
                      <path v-else d="m6 9 6 6 6-6" />
                    </svg>
                  </button>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="account in accounts" :key="account.id" class="cp-hover" :class="{ 'bg-base-200': account.id === selected?.id }" :aria-selected="account.id === selected?.id ? 'true' : undefined">
                <td>
                  <div class="flex items-center gap-3">
                    <CpAvatar :initials="account.email.charAt(0).toUpperCase()" />
                    <button type="button" class="cp-link cp-link-hover whitespace-nowrap text-start font-semibold" @click="selected = account">{{ account.email }}</button>
                  </div>
                </td>
                <td><CpBadge :tone="account.role === 'admin' ? 'primary' : 'neutral'" >{{ account.role === 'admin' ? t('admin.roleAdmin') : t('admin.roleMember') }}</CpBadge></td>
                <td><CpBadge :tone="account.confirmed ? 'success' : 'outline'" >{{ account.confirmed ? t('admin.emailConfirmed') : t('admin.emailNotConfirmed') }}</CpBadge></td>
                <td><CpBadge :tone="account.disabledAt ? 'outline' : 'success'" >{{ account.disabledAt ? t('admin.accountDisabled') : t('admin.accountActive') }}</CpBadge></td>
                <td class="whitespace-nowrap">{{ day.format(new Date(account.createdAt)) }}</td>
              </tr>
            </tbody>
        </CpTable>
        <div class="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <p class="text-sm text-base-content/70">{{ showing }}</p>
          <CpPagination v-model:page="query.page" :pages="pages" :nav-label="t('admin.pages')" :previous-label="t('admin.previousPage')" :next-label="t('admin.nextPage')" />
        </div>
      </CpCard>
    </div>
    <CpSidePanel v-if="selected" :title="selected.email" :close-label="t('admin.detailsClose')" @close="selected = undefined">
      <template #avatar><CpAvatar :initials="selected.email.charAt(0).toUpperCase()" size="md" /></template>
      <template #subtitle>
        <CpBadge :tone="selected.disabledAt ? 'outline' : 'success'" class="w-fit">{{ selected.disabledAt ? t('admin.accountDisabled') : t('admin.accountActive') }}</CpBadge>
      </template>
      <dl class="grid gap-3">
        <div><dt class="cp-type-label">{{ t('admin.fieldEmail') }}</dt><dd>{{ selected.confirmed ? t('admin.emailConfirmed') : t('admin.emailNotConfirmed') }}</dd></div>
        <div><dt class="cp-type-label">{{ t('admin.fieldRole') }}</dt><dd>{{ selected.role === 'admin' ? t('admin.roleAdmin') : t('admin.roleMember') }}</dd></div>
        <div><dt class="cp-type-label">{{ t('admin.fieldCreated') }}</dt><dd>{{ day.format(new Date(selected.createdAt)) }}</dd></div>
        <div>
          <dt class="cp-type-label">{{ t('admin.fieldSignIn') }}</dt>
          <dd>{{ selected.disabledAt ? t('admin.signInBlocked', { when: moment.format(new Date(selected.disabledAt)) }) : t('admin.signInAllowed') }}</dd>
        </div>
      </dl>
      <p v-if="isOwn" class="text-sm text-base-content/70">{{ t('admin.ownAccount') }}</p>
      <template v-else>
        <p v-if="!selected.disabledAt" class="text-sm text-base-content/70">{{ t('admin.disableHint') }}</p>
        <div class="flex flex-wrap gap-2">
          <CpButton :variant="selected.disabledAt ? 'primary' : 'outline'" size="md" :disabled="busy" @click="toggle">
            {{ selected.disabledAt ? t('admin.enable') : t('admin.disable') }}
          </CpButton>
          <CpButton variant="danger" size="md" :disabled="busy" @click="askDelete">{{ t('admin.delete') }}</CpButton>
        </div>
      </template>
    </CpSidePanel>
    <CpSheet v-if="selected" v-model:open="deleting" :title="t('admin.deleteTitle')" :close-label="t('admin.deleteClose')">
      <p class="py-2">{{ t('admin.deleteBody', { email: selected.email }) }}</p>
      <CpTextField v-model="typed" :label="t('admin.deleteConfirmLabel')" autocomplete="off" />
      <template #actions>
        <CpButton variant="outline" size="md" @click="deleting = false">{{ t('admin.deleteCancel') }}</CpButton>
        <CpButton variant="danger" size="md" :disabled="!confirmed || busy" @click="remove">{{ t('admin.deleteConfirm') }}</CpButton>
      </template>
    </CpSheet>
  </main>
</template>

<script setup lang="ts">
// The accounts screen of the admin (design/mockups/agreed/web/admin-accounts*.html): a paged table of every
// account, sorted and filtered on the server, with the account picked in a panel floating on the right where it
// is disabled, enabled or deleted after typing its email again (#336). It never shows a workout or health data.
// The table's state lives in the URL (route.ts): /admin/accounts?q=lea&role=admin&sort=email&order=asc&page=2&account=<id>.
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ChevronDown, ChevronUp, ChevronsUpDown } from '@lucide/vue'
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
import { deleteAccount, getAccount, listAccounts, setAccountDisabled, type AccountsQuery, type AdminAccount } from '../session'
import { navigate, search, withParams } from '../route'
import { locale, t } from '../i18n'

const props = defineProps<{ ownAccountId: string }>()

const PAGE_SIZE = 8
const BASE = '/admin/accounts'
type Sort = AccountsQuery['sort']
const SORTS: Sort[] = ['email', 'role', 'confirmed', 'signIn', 'created']
/** What each URL parameter is when it is absent; a default isn't written to the address. */
const DEFAULTS = { q: '', role: '', confirmed: '', signIn: '', sort: 'created', order: 'desc', page: '1', account: '' }
type Param = keyof typeof DEFAULTS

const param = (name: Param) => search.value.get(name) ?? DEFAULTS[name]
const query = computed<AccountsQuery>(() => ({
  q: param('q'),
  role: param('role'),
  confirmed: param('confirmed'),
  signIn: param('signIn'),
  sort: SORTS.find((sort) => sort === param('sort')) ?? 'created',
  order: param('order') === 'asc' ? 'asc' : 'desc',
  page: Math.max(1, Number.parseInt(param('page'), 10) || 1),
  size: PAGE_SIZE,
}))
const accountId = computed(() => param('account'))

/** Moves to the table with [patch] applied to the address; `replace` for typing, which shouldn't fill the history. */
function go(patch: Partial<Record<Param, string>>, mode: 'push' | 'replace' = 'push') {
  const current = Object.fromEntries(Object.keys(DEFAULTS).map((name) => [name, search.value.get(name) ?? ''])) as Record<Param, string>
  navigate(withParams(BASE, { ...current, ...patch }, DEFAULTS), mode)
}

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
    from: String((query.value.page - 1) * PAGE_SIZE + 1),
    to: String(Math.min(query.value.page * PAGE_SIZE, total.value)),
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

/** A select bound to a filter in the address; changing it goes back to the first page. */
const filter = (name: 'role' | 'confirmed' | 'signIn') => computed({ get: () => param(name), set: (value: string) => go({ [name]: value, page: '' }) })
const role = filter('role')
const emailStatus = filter('confirmed')
const signIn = filter('signIn')
const page = computed({ get: () => query.value.page, set: (value: number) => go({ page: String(value) }) })

// The email filter is typed in a draft and reaches the address after a pause, replacing the entry.
const draft = ref(param('q'))
let pause: ReturnType<typeof setTimeout> | undefined
watch(draft, (text) => {
  clearTimeout(pause)
  pause = setTimeout(() => {
    if (text !== param('q')) go({ q: text, page: '' }, 'replace')
  }, 300)
})
watch(
  () => param('q'),
  (text) => {
    if (text !== draft.value) draft.value = text
  },
)
onBeforeUnmount(() => clearTimeout(pause))

const ariaSort = (sort: Sort) => (query.value.sort !== sort ? 'none' : query.value.order === 'asc' ? 'ascending' : 'descending')

/** A new column sorts ascending first; the same column flips its direction. */
function sortBy(sort: Sort) {
  go({ sort, order: query.value.sort === sort && query.value.order === 'asc' ? 'desc' : 'asc', page: '' })
}

const href = (id: string) => withParams(BASE, { ...Object.fromEntries(Object.keys(DEFAULTS).map((name) => [name, search.value.get(name) ?? ''])), account: id }, DEFAULTS)

let latest = 0
async function load() {
  const asked = ++latest
  const found = await listAccounts(query.value)
  if (asked !== latest) return // a newer question was asked meanwhile
  if (!found) {
    state.value = 'failed'
    return
  }
  // The last page of a list that shrank (a deletion, a filter) is gone: go to the new last one.
  if (found.accounts.length === 0 && found.total > 0 && query.value.page > 1) {
    go({ page: String(Math.ceil(found.total / PAGE_SIZE)) }, 'replace')
    return
  }
  accounts.value = found.accounts
  total.value = found.total
  state.value = 'ready'
  await syncSelected()
}

/** The open account is the one the address names: on the page, or fetched when a link opens one that isn't. */
async function syncSelected() {
  const id = accountId.value
  if (!id) {
    selected.value = undefined
    return
  }
  const found = accounts.value.find((account) => account.id === id) ?? (await getAccount(id))
  if (id !== accountId.value) return
  if (found) selected.value = found
  else go({ account: '' }, 'replace') // an id that is no account: leave it out of the address
}

// Everything but the open account asks the server again.
watch(() => JSON.stringify(query.value), load, { immediate: true })
watch(accountId, syncSelected)

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
    go({ account: '' }, 'replace')
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
          <CpTextField v-model="draft" class="sm:col-span-2 lg:col-span-1" type="search" :label="t('admin.filterEmail')" autocomplete="off" />
          <CpSelectField v-model="role" :label="t('admin.fieldRole')" :options="roleOptions" />
          <CpSelectField v-model="emailStatus" :label="t('admin.fieldEmail')" :options="confirmedOptions" />
          <CpSelectField v-model="signIn" :label="t('admin.fieldSignIn')" :options="signInOptions" />
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
                    <ChevronsUpDown v-if="query.sort !== column.sort" class="size-4 shrink-0" aria-hidden="true" />
                    <ChevronUp v-else-if="query.order === 'asc'" class="size-4 shrink-0" aria-hidden="true" />
                    <ChevronDown v-else class="size-4 shrink-0" aria-hidden="true" />
                  </button>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="account in accounts" :key="account.id" class="cp-hover" :class="{ 'bg-base-200': account.id === selected?.id }" :aria-selected="account.id === selected?.id ? 'true' : undefined">
                <td>
                  <div class="flex items-center gap-3">
                    <CpAvatar :initials="account.email.charAt(0).toUpperCase()" />
                    <a :href="href(account.id)" class="cp-link cp-link-hover whitespace-nowrap text-start font-semibold" @click.prevent="go({ account: account.id })">{{ account.email }}</a>
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
          <CpPagination v-model:page="page" :pages="pages" :nav-label="t('admin.pages')" :previous-label="t('admin.previousPage')" :next-label="t('admin.nextPage')" />
        </div>
      </CpCard>
    </div>
    <CpSidePanel v-if="selected" :title="selected.email" :close-label="t('admin.detailsClose')" @close="go({ account: '' })">
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

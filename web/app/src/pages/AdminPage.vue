<script setup lang="ts">
// /admin: the sign-in until the cookies sign an admin in, then the admin shell
// (design/mockups/agreed/web/admin-shell.html) with the audit log and the accounts. A link at
// the bottom of the sidebar goes back to the web app.
import { computed, onMounted, ref } from 'vue'
import { ArrowLeft, LogOut, PanelLeft, RotateCcwClock, Users } from '@lucide/vue'
import { CpAvatar, CpBadge, CpCard, CpDrawer, CpDrawerItem, CpErrorNote, CpNavMenu, CpStat, CpStats, CpTable, poulpyDrawings } from '@coach-poulpy/ui'
import AdminAccountsView from './AdminAccountsView.vue'
import AdminSignInPage from './AdminSignInPage.vue'
import { auditLog, currentAccount, signOut, type AuditEntry, type Me } from '../session'
import { navigate, path } from '../route'
import { locale, t } from '../i18n'

const me = ref<Me>()
const checked = ref(false)
const open = ref(false)
// The screen is the address: /admin is the audit log, /admin/accounts the accounts table.
const view = computed<'audit' | 'accounts'>(() => (path.value === '/admin/accounts' ? 'accounts' : 'audit'))
const entries = ref<AuditEntry[]>()
const loadFailed = ref(false)

async function load() {
  loadFailed.value = false
  entries.value = await auditLog()
  loadFailed.value = entries.value === undefined
}

async function enter(account: Me | undefined) {
  me.value = account?.role === 'admin' ? account : undefined
  if (me.value) await load()
}

onMounted(async () => {
  try {
    await enter(await currentAccount())
  } catch {
    me.value = undefined
  }
  checked.value = true
})

async function leave() {
  await signOut()
  me.value = undefined
  entries.value = undefined
}

const when = new Intl.DateTimeFormat(locale, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
const time = (at: string) => when.format(new Date(at))
const initial = (email: string | null) => (email ?? t('admin.theServer')).charAt(0).toUpperCase()
const who = (email: string | null) => email ?? t('admin.theServer')
// The actions the server writes; a code the website doesn't know yet shows as it is.
const actions: Record<string, string> = {
  'admin.promote': 'admin.actionPromote',
  'admin.account.disable': 'admin.actionDisable',
  'admin.account.enable': 'admin.actionEnable',
  'admin.account.delete': 'admin.actionDelete',
}
const what = (action: string) => (action in actions ? t(actions[action]) : action)
</script>

<template>
  <p v-if="!checked" class="p-8" role="status">{{ t('admin.checking') }}</p>
  <AdminSignInPage v-else-if="!me" @signed-in="async () => enter(await currentAccount())" />
  <CpDrawer v-else v-model:open="open" :toggle-label="t('admin.toggleNavigation')" :close-label="t('admin.closeNavigation')" class="bg-base-200 text-base-content">
    <template #toggle-icon>
      <PanelLeft class="size-5 shrink-0" aria-hidden="true" />
    </template>
    <template #title>{{ view === 'audit' ? t('admin.auditTitle') : t('admin.accountsTitle') }}</template>
    <AdminAccountsView v-if="view === 'accounts'" :own-account-id="me.accountId" />
    <main v-else class="flex flex-col gap-6 p-4 md:p-8">
      <div class="flex flex-col gap-1">
        <h1 class="cp-type-display">{{ t('admin.auditTitle') }}</h1>
        <p class="text-base-content/70">{{ t('admin.auditIntro') }}</p>
      </div>
      <CpErrorNote v-if="loadFailed">{{ t('admin.auditError') }}</CpErrorNote>
      <template v-else-if="entries">
        <CpStats>
          <CpStat :title="t('admin.statChanges')" :value="String(entries.length)" :description="t('admin.statChangesSince')" accent />
          <CpStat
            :title="t('admin.statLast')"
            :value="entries[0] ? new Date(entries[0].at).toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' }) : '–'"
            :description="entries[0] ? `${time(entries[0].at)}, ${who(entries[0].actorEmail)}` : t('admin.statLastNone')"
          />
        </CpStats>
        <CpCard :title="t('admin.auditLatest')">
          <p v-if="!entries.length">{{ t('admin.auditEmpty') }}</p>
          <CpTable v-else>
            <thead>
              <tr><th>{{ t('admin.colWhen') }}</th><th>{{ t('admin.colWho') }}</th><th>{{ t('admin.colWhat') }}</th><th>{{ t('admin.colTarget') }}</th></tr>
            </thead>
            <tbody>
              <tr v-for="entry in entries" :key="entry.at + entry.action + entry.target">
                <td class="whitespace-nowrap">{{ time(entry.at) }}</td>
                <td>
                  <div class="flex items-center gap-3"><CpAvatar :initials="initial(entry.actorEmail)" /><span class="whitespace-nowrap">{{ who(entry.actorEmail) }}</span></div>
                </td>
                <td><CpBadge :tone="actions[entry.action] ? undefined : 'outline'">{{ what(entry.action) }}</CpBadge></td>
                <td class="font-mono text-sm">{{ entry.target }}</td>
              </tr>
            </tbody>
          </CpTable>
        </CpCard>
      </template>
    </main>
    <template #sidebar>
      <div class="flex min-h-16 w-full items-center gap-2 px-4 py-2">
        <img :src="poulpyDrawings.mood_happy" alt="" width="32" height="32" class="shrink-0" />
        <div class="flex flex-col items-start cp-is-drawer-close:hidden">
          <span class="cp-type-headline">Coach <span class="text-primary">poulpy</span></span>
          <CpBadge>{{ t('admin.adminBadge') }}</CpBadge>
        </div>
      </div>
      <div class="w-full grow px-2"><CpNavMenu>
        <CpDrawerItem :label="t('admin.auditTitle')" href="/admin" :active="view === 'audit'" @click.prevent="navigate('/admin'); load()">
          <template #icon><RotateCcwClock class="size-5 shrink-0" aria-hidden="true" /></template>
        </CpDrawerItem>
        <CpDrawerItem :label="t('admin.navAccounts')" href="/admin/accounts" :active="view === 'accounts'" @click.prevent="navigate('/admin/accounts')">
          <template #icon><Users class="size-5 shrink-0" aria-hidden="true" /></template>
        </CpDrawerItem>
      </CpNavMenu></div>
      <div class="flex w-full flex-col gap-2 p-2">
        <CpNavMenu>
          <CpDrawerItem :label="t('admin.appLink')" href="/"><template #icon><ArrowLeft class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
        </CpNavMenu>
        <div class="flex items-center gap-3 px-2">
          <CpAvatar :initials="initial(me.email)" tone="primary" size="md" />
          <span class="truncate text-sm cp-is-drawer-close:hidden">{{ me.email }}</span>
        </div>
        <CpNavMenu>
          <CpDrawerItem :label="t('admin.signOut')" @click="leave">
            <template #icon><LogOut class="size-5 shrink-0" aria-hidden="true" /></template>
          </CpDrawerItem>
        </CpNavMenu>
      </div>
    </template>
  </CpDrawer>
</template>

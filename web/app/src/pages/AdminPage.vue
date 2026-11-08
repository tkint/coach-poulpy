<script setup lang="ts">
// /admin: the sign-in until the cookies sign an admin in, then the admin shell
// (design/mockups/agreed/web/admin-shell.html) with the audit log, the only screen so far. A link at
// the bottom of the sidebar goes back to the web app.
import { onMounted, ref } from 'vue'
import { ArrowLeft } from 'lucide-vue-next'
import { CpAvatar, CpBadge, CpCard, CpDrawer, CpDrawerItem, CpErrorNote, CpNavMenu, CpStat, CpStats, CpTable, poulpyDrawings } from '@coach-poulpy/ui'
import AdminSignInPage from './AdminSignInPage.vue'
import { auditLog, currentAccount, signOut, type AuditEntry, type Me } from '../session'
import { locale, t } from '../i18n'

const me = ref<Me>()
const checked = ref(false)
const open = ref(false)
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
const actions: Record<string, string> = { 'admin.promote': 'admin.actionPromote' }
const what = (action: string) => (action in actions ? t(actions[action]) : action)
</script>

<template>
  <p v-if="!checked" class="p-8" role="status">{{ t('admin.checking') }}</p>
  <AdminSignInPage v-else-if="!me" @signed-in="async () => enter(await currentAccount())" />
  <CpDrawer v-else v-model:open="open" :toggle-label="t('admin.toggleNavigation')" :close-label="t('admin.closeNavigation')" class="bg-base-200 text-base-content">
    <template #toggle-icon>
      <svg class="size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect width="18" height="18" x="3" y="3" rx="2" /><path d="M9 3v18" /></svg>
    </template>
    <template #title>{{ t('admin.auditTitle') }}</template>
    <main class="flex flex-col gap-6 p-4 md:p-8">
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
      <div class="flex h-16 w-full items-center gap-2 px-4">
        <img :src="poulpyDrawings.mood_happy" alt="" width="32" height="32" class="shrink-0" />
        <span class="cp-type-headline cp-is-drawer-close:hidden">Coach <span class="text-primary">poulpy</span></span>
      </div>
      <div class="w-full grow px-2"><CpNavMenu>
        <CpDrawerItem :label="t('admin.auditTitle')" active>
          <template #icon><svg class="size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" /><path d="M3 3v5h5" /><path d="M12 7v5l4 2" /></svg></template>
        </CpDrawerItem>
      </CpNavMenu></div>
      <div class="flex w-full flex-col gap-2 p-2">
        <CpNavMenu>
          <CpDrawerItem :label="t('admin.appLink')" href="/"><template #icon><ArrowLeft class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
        </CpNavMenu>
        <CpBadge class="cp-is-drawer-close:hidden">{{ t('admin.adminBadge') }}</CpBadge>
        <div class="flex items-center gap-3 px-2">
          <CpAvatar :initials="initial(me.email)" tone="primary" size="md" />
          <span class="truncate text-sm cp-is-drawer-close:hidden">{{ me.email }}</span>
        </div>
        <CpNavMenu>
          <CpDrawerItem :label="t('admin.signOut')" @click="leave">
            <template #icon><svg class="size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" x2="9" y1="12" y2="12" /></svg></template>
          </CpDrawerItem>
        </CpNavMenu>
      </div>
    </template>
  </CpDrawer>
</template>

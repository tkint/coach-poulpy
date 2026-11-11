<script setup lang="ts">
// The web app (design/mockups/agreed/web/app-profile.html): the sign-in until the cookies sign
// someone in, then the sidebar shell with the profile, the only screen so far. Admins get a link to
// /admin at the bottom of the sidebar, just above themselves.
import { onMounted, ref } from 'vue'
import { Check, LogOut, PanelLeft, ShieldCheck, UserRound } from '@lucide/vue'
import { CpAvatar, CpBadge, CpButton, CpCard, CpDrawer, CpDrawerItem, CpErrorNote, CpNavMenu, poulpyDrawings } from '@coach-poulpy/ui'
import AppSignInPage from './AppSignInPage.vue'
import { requestPasswordReset } from '../api'
import { currentAccount, signOut, type Me } from '../session'
import { t } from '../i18n'

const me = ref<Me>()
const checked = ref(false)
const open = ref(false)
const reset = ref<Awaited<ReturnType<typeof requestPasswordReset>> | 'sending'>()

onMounted(async () => {
  try {
    me.value = await currentAccount()
  } catch {
    me.value = undefined
  }
  checked.value = true
})

async function leave() {
  await signOut()
  me.value = undefined
}

async function sendResetLink() {
  reset.value = 'sending'
  reset.value = await requestPasswordReset(me.value!.email)
}
</script>

<template>
  <p v-if="!checked" class="p-8" role="status">{{ t('admin.checking') }}</p>
  <AppSignInPage v-else-if="!me" @signed-in="async () => (me = await currentAccount())" />
  <CpDrawer v-else v-model:open="open" :toggle-label="t('admin.toggleNavigation')" :close-label="t('admin.closeNavigation')" class="bg-base-200 text-base-content">
    <template #toggle-icon><PanelLeft class="size-5 shrink-0" aria-hidden="true" /></template>
    <template #title>{{ t('app.profileTitle') }}</template>
    <main class="flex flex-col gap-6 p-4 md:p-8">
      <div class="flex flex-col gap-1">
        <h1 class="cp-type-display">{{ t('app.profileTitle') }}</h1>
        <p class="text-base-content/70">{{ t('app.profileIntro') }}</p>
      </div>
      <CpCard>
        <div class="flex items-center gap-4">
          <CpAvatar :initials="me.email.charAt(0).toUpperCase()" tone="primary" size="lg" />
          <div class="flex min-w-0 flex-col gap-1">
            <h2 class="cp-card-title truncate">{{ me.email }}</h2>
            <CpBadge class="gap-1"><Check class="size-3 shrink-0" aria-hidden="true" />{{ t('app.emailConfirmed') }}</CpBadge>
          </div>
        </div>
      </CpCard>
      <CpCard :title="t('app.passwordTitle')">
        <p class="text-base-content/70">{{ t('app.passwordBody') }}</p>
        <CpErrorNote v-if="reset === 'offline'">{{ t('admin.errorOffline') }}</CpErrorNote>
        <p v-else-if="reset === 'done'" role="status">{{ t('app.resetLinkSent') }}</p>
        <div class="cp-card-actions">
          <CpButton variant="outline" :disabled="reset === 'sending'" @click="sendResetLink">{{ t('app.sendResetLink') }}</CpButton>
        </div>
      </CpCard>
      <CpCard :title="t('app.dataTitle')">
        <p class="text-base-content/70">{{ t('app.dataBody') }}</p>
      </CpCard>
    </main>
    <template #sidebar>
      <div class="flex h-16 w-full items-center gap-2 px-4">
        <img :src="poulpyDrawings.mood_happy" alt="" width="32" height="32" class="shrink-0" />
        <span class="cp-type-headline cp-is-drawer-close:hidden">Coach <span class="text-primary">poulpy</span></span>
      </div>
      <div class="w-full grow px-2">
        <CpNavMenu>
          <CpDrawerItem :label="t('app.profileTitle')" active><template #icon><UserRound class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
        </CpNavMenu>
      </div>
      <div class="flex w-full flex-col gap-2 p-2">
        <CpNavMenu v-if="me.role === 'admin'">
          <CpDrawerItem :label="t('app.adminLink')" href="/admin"><template #icon><ShieldCheck class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
        </CpNavMenu>
        <div class="flex items-center gap-3 px-2">
          <CpAvatar :initials="me.email.charAt(0).toUpperCase()" tone="primary" size="md" />
          <span class="truncate text-sm font-bold cp-is-drawer-close:hidden">{{ me.email }}</span>
        </div>
        <CpNavMenu>
          <CpDrawerItem :label="t('admin.signOut')" @click="leave"><template #icon><LogOut class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
        </CpNavMenu>
      </div>
    </template>
  </CpDrawer>
</template>

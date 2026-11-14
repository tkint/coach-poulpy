<script setup lang="ts">
// The web app: the sign-in until the cookies sign someone in, then the sidebar shell with the workouts
// (design/mockups/agreed/web/app-workouts.html, app-workout.html) and the profile (app-profile.html). The screen is the
// address: /workouts, /workouts/<id>, /profile; anything else goes to /workouts. Admins get a link to /admin at the
// bottom of the sidebar, just above themselves.
import { computed, onMounted, ref, watch } from 'vue'
import { List, LogOut, PanelLeft, ShieldCheck, UserRound } from '@lucide/vue'
import { CpAvatar, CpDrawer, CpDrawerItem, CpNavMenu, poulpyDrawings } from '@coach-poulpy/ui'
import AppProfileView from './AppProfileView.vue'
import AppSignInPage from './AppSignInPage.vue'
import WorkoutView from './WorkoutView.vue'
import WorkoutsView from './WorkoutsView.vue'
import { currentAccount, signOut, type Me } from '../session'
import { navigate, path } from '../route'
import { t } from '../i18n'

const me = ref<Me>()
const checked = ref(false)
const open = ref(false)

const WORKOUTS = '/workouts'
const PROFILE = '/profile'
const view = computed<'workouts' | 'workout' | 'profile' | undefined>(() =>
  path.value === WORKOUTS ? 'workouts' : path.value.startsWith(`${WORKOUTS}/`) ? 'workout' : path.value === PROFILE ? 'profile' : undefined,
)
const workoutId = computed(() => decodeURIComponent(path.value.slice(WORKOUTS.length + 1)))
const title = computed(() => (view.value === 'profile' ? t('app.profileTitle') : t('app.workoutsTitle')))
// An address that is no screen, such as the plain one, opens the workouts.
watch([me, view], () => {
  if (me.value && !view.value) navigate(WORKOUTS, 'replace')
})

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
</script>

<template>
  <p v-if="!checked" class="p-8" role="status">{{ t('admin.checking') }}</p>
  <AppSignInPage v-else-if="!me" @signed-in="async () => (me = await currentAccount())" />
  <CpDrawer v-else v-model:open="open" :toggle-label="t('admin.toggleNavigation')" :close-label="t('admin.closeNavigation')" class="bg-base-200 text-base-content">
    <template #toggle-icon><PanelLeft class="size-5 shrink-0" aria-hidden="true" /></template>
    <template #title>{{ title }}</template>
    <WorkoutsView v-if="view === 'workouts'" />
    <WorkoutView v-else-if="view === 'workout'" :id="workoutId" />
    <AppProfileView v-else-if="view === 'profile'" :me="me" />
    <template #sidebar>
      <div class="flex h-16 w-full items-center gap-2 px-4">
        <img :src="poulpyDrawings.mood_happy" alt="" width="32" height="32" class="shrink-0" />
        <span class="cp-type-headline cp-is-drawer-close:hidden">Coach <span class="text-primary">poulpy</span></span>
      </div>
      <div class="w-full grow px-2">
        <CpNavMenu>
          <CpDrawerItem :label="t('app.workoutsTitle')" :href="WORKOUTS" :active="view === 'workouts' || view === 'workout'" @click.prevent="navigate(WORKOUTS)"><template #icon><List class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
          <CpDrawerItem :label="t('app.profileTitle')" :href="PROFILE" :active="view === 'profile'" @click.prevent="navigate(PROFILE)"><template #icon><UserRound class="size-5 shrink-0" aria-hidden="true" /></template></CpDrawerItem>
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

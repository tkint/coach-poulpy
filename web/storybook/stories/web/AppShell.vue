<script setup lang="ts">
import { CpIcon } from '@coach-poulpy/ui'
// The web app's page frame: the sidebar with Workouts and Profile, the admin link, and the signed-in account (ADR 0029:
// composed from the Cp components, as the web app's AppPage does).
import { List, LogOut, PanelLeft, ShieldCheck, UserRound } from '@lucide/vue'
import { CpAvatar, CpDrawer, CpDrawerItem, CpNavMenu, poulpyDrawings } from '@coach-poulpy/ui'

defineProps<{ title: string; active: 'workouts' | 'profile' }>()
</script>

<template>
  <CpDrawer class="min-h-screen bg-base-200 text-base-content">
    <template #toggle-icon><CpIcon :icon="PanelLeft" /></template>
    <template #title>{{ title }}</template>
    <slot />
    <template #sidebar>
      <div class="flex h-16 w-full items-center gap-2 px-4">
        <img :src="poulpyDrawings.mood_happy" alt="" width="32" height="32" class="shrink-0" />
        <span class="cp-type-headline cp-is-drawer-close:hidden">Coach <span class="text-primary">poulpy</span></span>
      </div>
      <div class="w-full grow px-2">
        <CpNavMenu>
          <CpDrawerItem label="Workouts" :active="active === 'workouts'"><template #icon><CpIcon :icon="List" /></template></CpDrawerItem>
          <CpDrawerItem label="Profile" :active="active === 'profile'"><template #icon><CpIcon :icon="UserRound" /></template></CpDrawerItem>
        </CpNavMenu>
      </div>
      <div class="flex w-full flex-col gap-2 p-2">
        <CpNavMenu>
          <CpDrawerItem label="Admin" href="#"><template #icon><CpIcon :icon="ShieldCheck" /></template></CpDrawerItem>
        </CpNavMenu>
        <div class="flex items-center gap-3 px-2">
          <CpAvatar initials="A" tone="primary" size="md" />
          <span class="truncate text-sm font-bold cp-is-drawer-close:hidden">ana@example.com</span>
        </div>
        <CpNavMenu>
          <CpDrawerItem label="Sign out"><template #icon><CpIcon :icon="LogOut" /></template></CpDrawerItem>
        </CpNavMenu>
      </div>
    </template>
  </CpDrawer>
</template>

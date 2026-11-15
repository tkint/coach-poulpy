<script setup lang="ts">
import { CpIcon } from '@coach-poulpy/ui'
// The admin's page frame: the sidebar with the five screens, the app link and the signed-in admin, around a screen
// (ADR 0029: composed from the Cp components, as the web app's AdminPage does).
import { Activity, ArrowLeft, BookOpen, LogOut, PanelLeft, RotateCcwClock, ShieldCheck, SlidersHorizontal, Users } from '@lucide/vue'
import { CpAvatar, CpBadge, CpDrawer, CpDrawerItem, CpNavMenu, poulpyDrawings } from '@coach-poulpy/ui'

defineProps<{ title: string; active: 'audit' | 'accounts' | 'catalog' | 'settings' | 'status' }>()
</script>

<template>
  <CpDrawer class="min-h-screen bg-base-200 text-base-content">
    <template #toggle-icon><CpIcon :icon="PanelLeft" /></template>
    <template #title>{{ title }}</template>
    <slot />
    <template #sidebar>
      <div class="flex min-h-16 w-full items-center gap-2 px-4 py-2">
        <img :src="poulpyDrawings.mood_happy" alt="" width="32" height="32" class="shrink-0" />
        <div class="flex flex-col items-start cp-is-drawer-close:hidden">
          <span class="cp-type-headline">Coach <span class="text-primary">poulpy</span></span>
          <CpBadge small class="w-fit gap-1"><CpIcon :icon="ShieldCheck" size="xs" />Admin</CpBadge>
        </div>
      </div>
      <div class="w-full grow p-2">
        <CpNavMenu>
          <CpDrawerItem label="Audit log" :active="active === 'audit'"><template #icon><CpIcon :icon="RotateCcwClock" /></template></CpDrawerItem>
          <CpDrawerItem label="Accounts" :active="active === 'accounts'"><template #icon><CpIcon :icon="Users" /></template></CpDrawerItem>
          <CpDrawerItem label="Catalog" :active="active === 'catalog'"><template #icon><CpIcon :icon="BookOpen" /></template></CpDrawerItem>
          <CpDrawerItem label="Settings" :active="active === 'settings'"><template #icon><CpIcon :icon="SlidersHorizontal" /></template></CpDrawerItem>
          <CpDrawerItem label="Status" :active="active === 'status'"><template #icon><CpIcon :icon="Activity" /></template></CpDrawerItem>
        </CpNavMenu>
      </div>
      <div class="flex w-full flex-col gap-2 p-2">
        <CpNavMenu>
          <CpDrawerItem label="Coach Poulpy app" href="#"><template #icon><CpIcon :icon="ArrowLeft" /></template></CpDrawerItem>
        </CpNavMenu>
        <div class="flex items-center gap-3 px-2">
          <CpAvatar initials="A" tone="primary" size="md" />
          <div class="flex min-w-0 flex-1 flex-col text-sm cp-is-drawer-close:hidden">
            <span class="font-bold">Ana</span>
            <span class="truncate text-base-content/70">ana@example.com</span>
          </div>
        </div>
        <CpNavMenu>
          <CpDrawerItem label="Sign out"><template #icon><CpIcon :icon="LogOut" /></template></CpDrawerItem>
        </CpNavMenu>
      </div>
    </template>
  </CpDrawer>
</template>

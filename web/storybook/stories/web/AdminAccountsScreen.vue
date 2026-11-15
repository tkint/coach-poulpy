<script setup lang="ts">
import { CpIcon } from '@coach-poulpy/ui'
// The admin's accounts screen: filters, the table, and the details beside it (ADR 0029: composed from the Cp components,
// as the web app's AdminAccountsView does, with static accounts).
import { ref } from 'vue'
import { Ban, ChevronDown, ChevronsUpDown, ChevronUp, RotateCcw, Trash } from '@lucide/vue'
import { CpAvatar, CpBadge, CpButton, CpCard, CpPagination, CpPoulpyCard, CpSelectField, CpSheet, CpSidePanel, CpTable, CpTextField } from '@coach-poulpy/ui'
import AdminShell from './AdminShell.vue'

export interface Account { email: string; role: 'Member' | 'Admin'; confirmed: boolean; disabled: boolean; created: string }

const props = withDefaults(defineProps<{
  accounts: Account[]
  showing?: string
  pages?: number
  signIn?: string
  search?: string
  sort?: 'email' | 'created'
  order?: 'asc' | 'desc'
  selected?: string
  disabledSince?: string
  deleting?: boolean
  typed?: string
}>(), { pages: 3, signIn: '', search: '', sort: 'created', order: 'desc', typed: '' })

const query = ref(props.search)
const signIn = ref(props.signIn)
const role = ref('')
const email = ref('')
const typed = ref(props.typed)
const page = ref(1)
const open = ref(true)
const account = () => props.accounts.find((a) => a.email === props.selected)
const columns = [{ key: 'email', label: 'Email' }, { key: 'role', label: 'Role' }, { key: 'status', label: 'Email status' }, { key: 'signin', label: 'Sign-in' }, { key: 'created', label: 'Created' }]
const ariaSort = (key: string) => (key === props.sort ? (props.order === 'asc' ? 'ascending' : 'descending') : 'none')
</script>

<template>
  <AdminShell title="Accounts" active="accounts">
    <main class="flex min-h-full">
      <div class="flex min-w-0 grow flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Accounts</h1>
          <p class="text-base-content/70">Every account, with its state and never its workouts or health data. Open one to disable or delete it.</p>
        </div>
        <form class="cp-card bg-base-100 shadow-sm" role="search" aria-label="Filter the accounts" @submit.prevent>
          <div class="cp-card-body grid gap-4 p-4 sm:grid-cols-2 lg:grid-cols-4 md:p-6">
            <CpTextField v-model="query" class="sm:col-span-2 lg:col-span-1" type="search" label="Email contains" autocomplete="off" />
            <CpSelectField v-model="role" label="Role" :options="[{ value: '', text: 'All' }, { value: 'member', text: 'Member' }, { value: 'admin', text: 'Admin' }]" />
            <CpSelectField v-model="email" label="Email" :options="[{ value: '', text: 'All' }, { value: 'yes', text: 'Confirmed' }, { value: 'no', text: 'Not confirmed' }]" />
            <CpSelectField v-model="signIn" label="Sign-in" :options="[{ value: '', text: 'All' }, { value: 'active', text: 'Active' }, { value: 'disabled', text: 'Disabled' }]" />
          </div>
        </form>
        <CpPoulpyCard v-if="!accounts.length" title="No account matches">Change or clear the filters to see more accounts.</CpPoulpyCard>
        <CpCard v-else>
          <CpTable>
            <thead>
              <tr>
                <th v-for="column in columns" :key="column.key" :aria-sort="ariaSort(column.key)">
                  <button type="button" class="cp-btn cp-btn-ghost cp-btn-sm -ms-3 gap-1 whitespace-nowrap">
                    {{ column.label }}
                    <CpIcon :icon="ChevronsUpDown" size="sm" v-if="column.key !== sort" />
                    <CpIcon :icon="ChevronUp" size="sm" v-else-if="order === 'asc'" />
                    <CpIcon :icon="ChevronDown" size="sm" v-else />
                  </button>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in accounts" :key="a.email" class="cp-hover" :class="{ 'bg-base-200': a.email === selected }" :aria-selected="a.email === selected ? 'true' : undefined">
                <td>
                  <div class="flex items-center gap-3">
                    <CpAvatar :initials="a.email.charAt(0).toUpperCase()" />
                    <a href="#" class="cp-link cp-link-hover whitespace-nowrap text-start font-semibold" @click.prevent>{{ a.email }}</a>
                  </div>
                </td>
                <td><CpBadge :tone="a.role === 'Admin' ? 'primary' : 'neutral'">{{ a.role }}</CpBadge></td>
                <td><CpBadge :tone="a.confirmed ? 'success' : 'outline'">{{ a.confirmed ? 'Confirmed' : 'Not confirmed' }}</CpBadge></td>
                <td><CpBadge :tone="a.disabled ? 'outline' : 'success'">{{ a.disabled ? 'Disabled' : 'Active' }}</CpBadge></td>
                <td class="whitespace-nowrap">{{ a.created }}</td>
              </tr>
            </tbody>
          </CpTable>
          <div class="flex flex-col items-center justify-between gap-3 sm:flex-row">
            <p class="text-sm text-base-content/70">{{ showing }}</p>
            <CpPagination size="sm" v-model:page="page" :pages="pages" />
          </div>
        </CpCard>
      </div>
      <CpSidePanel v-if="account()" :title="account()!.email" close-label="Close the details">
        <template #avatar><CpAvatar :initials="account()!.email.charAt(0).toUpperCase()" size="md" /></template>
        <template #subtitle><CpBadge :tone="account()!.disabled ? 'outline' : 'success'" class="w-fit">{{ account()!.disabled ? 'Disabled' : 'Active' }}</CpBadge></template>
        <dl class="grid gap-3">
          <div><dt class="cp-type-label">Email</dt><dd>{{ account()!.confirmed ? 'Confirmed' : 'Not confirmed' }}</dd></div>
          <div><dt class="cp-type-label">Role</dt><dd>{{ account()!.role }}</dd></div>
          <div><dt class="cp-type-label">Created</dt><dd>{{ account()!.created }}</dd></div>
          <div><dt class="cp-type-label">Sign-in</dt><dd>{{ account()!.disabled ? `Blocked since ${disabledSince}` : 'Allowed' }}</dd></div>
        </dl>
        <p v-if="!account()!.disabled" class="text-sm text-base-content/70">Disabling signs this account out everywhere and blocks sign-in until you enable it again. Nothing is deleted.</p>
        <div class="flex flex-wrap gap-2">
          <CpButton v-if="account()!.disabled" size="md" class="gap-2"><CpIcon :icon="RotateCcw" size="sm" />Enable account</CpButton>
          <CpButton v-else variant="outline" size="md" class="gap-2"><CpIcon :icon="Ban" size="sm" />Disable account</CpButton>
          <CpButton variant="danger" size="md" class="gap-2"><CpIcon :icon="Trash" size="sm" />Delete account</CpButton>
        </div>
      </CpSidePanel>
      <CpSheet v-if="account() && deleting" v-model:open="open" title="Delete this account?">
        <p class="py-2">This removes <strong class="break-all">{{ account()!.email }}</strong> and everything saved for it, for good. It is the same deletion the user can ask for in the app. It cannot be undone.</p>
        <CpTextField v-model="typed" label="Type the email to confirm" autocomplete="off" />
        <template #actions>
          <CpButton variant="ghost" size="md">Cancel</CpButton>
          <CpButton variant="danger" size="md" class="gap-2" :disabled="typed !== account()!.email"><CpIcon :icon="Trash" size="sm" />Delete for good</CpButton>
        </template>
      </CpSheet>
    </main>
  </AdminShell>
</template>

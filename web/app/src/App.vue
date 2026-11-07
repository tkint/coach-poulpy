<script setup lang="ts">
import { CpPoulpy } from '@coach-poulpy/ui'
import ConfirmEmailPage from './pages/ConfirmEmailPage.vue'
import ResetPasswordPage from './pages/ResetPasswordPage.vue'
import { locale } from './i18n'

// The account email links, /confirm-email#<token> and /reset-password#<token>, open here when
// they don't open the app. The token stays in the fragment, which never reaches a server log, and
// is taken off the address so a reload or the history doesn't keep it.
const page = location.pathname.replace(/\/+$/, '')
const token = location.hash.slice(1)
if (token) history.replaceState(null, '', location.pathname + location.search)
document.documentElement.lang = locale
</script>

<template>
  <ConfirmEmailPage v-if="page === '/confirm-email'" :token="token" />
  <ResetPasswordPage v-else-if="page === '/reset-password'" :token="token" />
  <main v-else class="mx-auto my-16 max-w-xl px-4">
    <CpPoulpy drawing="mood_happy" :size="96" />
    <h1 class="cp-type-display">Coach <span class="text-primary">poulpy</span></h1>
    <p>The website arrives with accounts.</p>
  </main>
</template>

<style>
html,
body {
  min-height: 100%;
  background: var(--cp-background);
  color: var(--cp-on-background);
}
</style>

<script setup lang="ts">
import AdminPage from './pages/AdminPage.vue'
import AppPage from './pages/AppPage.vue'
import ConfirmEmailPage from './pages/ConfirmEmailPage.vue'
import ResetPasswordPage from './pages/ResetPasswordPage.vue'
import StatusPage from './pages/StatusPage.vue'
import { locale } from './i18n'

// The web app is everything else: its sign-in first, then the profile. /status says whether Coach Poulpy is up.
// The account email links, /confirm-email#<token> and /reset-password#<token>, open here when
// they don't open the app. The token stays in the fragment, which never reaches a server log, and
// is taken off the address so a reload or the history doesn't keep it.
const page = location.pathname.replace(/\/+$/, '')
const token = location.hash.slice(1)
if (token) history.replaceState(null, '', location.pathname + location.search)
document.documentElement.lang = locale
</script>

<template>
  <AdminPage v-if="page === '/admin' || page.startsWith('/admin/')" />
  <ConfirmEmailPage v-else-if="page === '/confirm-email'" :token="token" />
  <ResetPasswordPage v-else-if="page === '/reset-password'" :token="token" />
  <StatusPage v-else-if="page === '/status'" />
  <AppPage v-else />
</template>

<style>
html,
body {
  min-height: 100%;
  background: var(--cp-background);
  color: var(--cp-on-background);
}
</style>

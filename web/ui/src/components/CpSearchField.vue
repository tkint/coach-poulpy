<script setup lang="ts">
// The search field: a pill with a search icon, primary while it holds text, and a round clear button. With `compact`
// it is one control of a filter bar: fixed in width instead of filling its row (give it `size="sm"` to be as high as a
// small button). `size` is the websites' scale (xs, sm, md, lg, xl, xxl); md when none is given, and a list's own search is `lg`.
import { Search, X } from '@lucide/vue'
import { uiText } from '../i18n'
import CpIcon from './CpIcon.vue'
import { buttonSize, controlIcon, inputSize, type CpSize } from './sizes'
const { size = 'md' } = defineProps<{ label: string; clearLabel?: string; placeholder?: string; compact?: boolean; size?: CpSize }>()
const query = defineModel<string>({ default: '' })
</script>

<template>
  <label class="cp-input cp-search-field" :class="[inputSize[size], { 'cp-search-field-compact': compact }]" data-component="search-field">
    <CpIcon :icon="Search" :size="controlIcon[size]" />
    <input v-model="query" type="search" autocomplete="off" class="grow" :aria-label="label" :placeholder="placeholder ?? ' '" />
    <button v-if="query" type="button" class="cp-btn cp-btn-circle cp-btn-ghost" :class="buttonSize.xs" :aria-label="clearLabel ?? uiText('ui.clearSearch')" @click="query = ''">
      <CpIcon :icon="X" size="sm" />
    </button>
  </label>
</template>

<script setup lang="ts">
// CLEANUP: drawn with the classes of the mockup until the deletion of an exercise is built as a Cp component.
import { Trash2 } from '@lucide/vue'
defineProps<{ slug: string; typed?: string; published?: boolean }>()
</script>

<template>
  <dialog class="cp-modal cp-modal-open cp-modal-bottom sm:cp-modal-middle cp-sheet" open aria-labelledby="delete-title">
    <div class="cp-modal-box">
      <h2 id="delete-title" class="cp-type-headline">Delete {{ slug }}?</h2>
      <p class="py-2">This removes the exercise and its translations from the catalog for good. It cannot be undone.</p>
      <p v-if="published" class="cp-alert cp-alert-error mb-2" role="alert">{{ slug }} is published. Workouts that use it will lose it.</p>
      <label class="cp-form-control flex flex-col gap-1">
        <span class="cp-label-text">Type <strong>{{ slug }}</strong> to confirm</span>
        <input class="cp-input w-full" type="text" autocomplete="off" :value="typed" />
      </label>
      <div class="cp-modal-action">
        <button type="button" class="cp-btn cp-btn-ghost">Cancel</button>
        <button type="button" class="cp-btn cp-btn-error gap-2" :disabled="typed !== slug"><Trash2 class="size-5 shrink-0" aria-hidden="true" />Delete exercise</button>
      </div>
    </div>
    <div class="cp-modal-backdrop"><button type="button" tabindex="-1">Close</button></div>
  </dialog>
</template>

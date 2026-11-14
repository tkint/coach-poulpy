<script setup lang="ts">
// CLEANUP: drawn with the classes of the mockup until the bulk retirement of #390 is built as a Cp component.
import { CpBadge } from '@coach-poulpy/ui'
import { X } from '@lucide/vue'
</script>

<template>
  <dialog class="cp-modal cp-modal-open cp-modal-bottom sm:cp-modal-middle" open aria-labelledby="bulk-retire-title">
    <div class="cp-modal-box flex max-h-[90vh] w-11/12 max-w-2xl flex-col gap-4 overflow-y-auto">
      <div class="flex items-start justify-between gap-4">
        <div class="flex flex-col gap-1">
          <h2 id="bulk-retire-title" class="cp-type-headline">Retire 3 selected exercises?</h2>
          <p class="text-sm text-base-content/70">These exercises stay available until you publish the changes. Existing workouts keep working and retain their history.</p>
        </div>
        <button type="button" class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm" aria-label="Close retirement review"><X class="size-4 shrink-0" aria-hidden="true" /></button>
      </div>
      <div class="overflow-x-auto rounded-box border border-base-300">
        <table class="cp-table bg-base-100">
          <thead><tr><th>Exercise</th><th>Current state</th><th>Pending content</th></tr></thead>
          <tbody>
            <tr><td class="font-semibold">Goblet squat</td><td><CpBadge tone="success">Offered</CpBadge></td><td>Edited</td></tr>
            <tr><td class="font-semibold">Glute bridge</td><td><CpBadge tone="success">Offered</CpBadge></td><td>Unchanged</td></tr>
            <tr><td class="font-semibold">Romanian deadlift</td><td><CpBadge tone="success">Offered</CpBadge></td><td>Edited</td></tr>
          </tbody>
        </table>
      </div>
      <p class="text-sm text-base-content/70">Retirement removes an exercise from the catalog and exercise builder after publication. It does not delete exercise data or workout history. You can offer it again later.</p>
      <div class="flex flex-wrap justify-end gap-2 border-t border-base-300 pt-4">
        <button type="button" class="cp-btn cp-btn-ghost">Cancel</button>
        <button type="button" class="cp-btn cp-btn-outline">Retire 3 exercises</button>
      </div>
    </div>
    <div class="cp-modal-backdrop"><button type="button" tabindex="-1">Close retirement review</button></div>
  </dialog>
</template>

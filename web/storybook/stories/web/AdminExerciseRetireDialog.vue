<script setup lang="ts">
import { CpIcon } from '@coach-poulpy/ui'
// CLEANUP: drawn with the classes of the mockup until the retirement of an exercise (#389) is built as a Cp component.
import { Archive } from '@lucide/vue'
</script>

<template>
  <dialog class="cp-modal cp-modal-open cp-modal-bottom sm:cp-modal-middle cp-sheet" open aria-labelledby="retire-title">
    <div class="cp-modal-box">
      <h2 id="retire-title" class="cp-type-headline">Prepare retirement for Wall sit?</h2>
      <p class="py-2">Wall sit stays active in the published catalog until you publish this change. Existing workouts keep working and retain their history. You can cancel this pending action before publication or offer the exercise again after retirement.</p>
      <div class="cp-modal-action">
        <button type="button" class="cp-btn cp-btn-ghost">Cancel</button>
        <button type="button" class="cp-btn cp-btn-outline gap-2"><CpIcon :icon="Archive" />Retire</button>
      </div>
    </div>
    <div class="cp-modal-backdrop"><button type="button" tabindex="-1">Close</button></div>
  </dialog>
</template>

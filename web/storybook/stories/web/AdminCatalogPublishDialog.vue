<script setup lang="ts">
// CLEANUP: drawn with the classes of the mockup until the batch publication of #389 is built as a Cp component.
import { CloudUpload, X } from '@lucide/vue'
</script>

<template>
  <dialog class="cp-modal cp-modal-open" open aria-labelledby="publish-title" data-component="publish-dialog">
    <div class="cp-modal-box flex max-h-[90vh] w-11/12 max-w-4xl flex-col gap-4 overflow-y-auto p-4 md:p-6">
      <div class="flex items-start justify-between gap-4">
        <div class="flex flex-col gap-1">
          <h2 class="cp-type-headline" id="publish-title">Publish catalog changes</h2>
          <p class="text-sm text-base-content/70">Review the changes before publishing. Phones download the updated catalog the next time they open the app, guests included.</p>
        </div>
        <button type="button" class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm" aria-label="Close publication review"><X class="size-4 shrink-0" aria-hidden="true" /></button>
      </div>
      <div class="overflow-x-auto rounded-box border border-base-300">
        <table class="cp-table bg-base-100">
          <thead>
            <tr><th>Exercise</th><th>Status</th><th>Change</th></tr>
          </thead>
          <tbody>
            <tr>
              <td><div class="font-semibold">Pike push-up</div><div class="text-sm text-base-content/70">pike-push-up</div></td>
              <td><span class="cp-badge cp-badge-error cp-badge-soft whitespace-nowrap">Missing translations · French</span></td>
              <td><span class="cp-badge cp-badge-primary cp-badge-soft">Creation</span></td>
            </tr>
            <tr>
              <td><div class="font-semibold">Goblet squat</div><div class="text-sm text-base-content/70">goblet-squat</div></td>
              <td><span class="cp-badge cp-badge-success cp-badge-soft">Complete</span></td>
              <td><span class="cp-badge cp-badge-neutral cp-badge-soft">Edition</span></td>
            </tr>
            <tr class="bg-base-200/50">
              <td colspan="3" class="!py-2">
                <div class="flex flex-col gap-2 border-s-2 border-base-300 ps-3">
                  <div class="grid grid-cols-[minmax(7rem,auto)_1fr] gap-x-3 gap-y-1 text-sm">
                    <span class="font-semibold text-base-content/70">French name</span>
                    <div class="grid grid-cols-[1fr_auto_1fr] items-start gap-2">
                      <span class="rounded bg-error/10 px-2 py-1 text-error line-through">Squat avec haltère</span><span class="text-base-content/50">→</span><span class="rounded bg-success/10 px-2 py-1 text-success">Squat gobelet</span>
                    </div>
                    <span class="font-semibold text-base-content/70">Muscles worked</span>
                    <div class="grid grid-cols-[1fr_auto_1fr] items-start gap-2">
                      <span class="rounded bg-error/10 px-2 py-1 text-error line-through">Quadriceps</span><span class="text-base-content/50">→</span><span class="rounded bg-success/10 px-2 py-1 text-success">Quadriceps, glutes</span>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
            <tr>
              <td><div class="font-semibold">Romanian deadlift</div><div class="text-sm text-base-content/70">romanian-deadlift</div></td>
              <td><span class="cp-badge cp-badge-success cp-badge-soft">Complete</span></td>
              <td><span class="cp-badge cp-badge-neutral cp-badge-soft">Edition</span></td>
            </tr>
            <tr class="bg-base-200/50">
              <td colspan="3" class="!py-2">
                <div class="flex flex-col gap-2 border-s-2 border-base-300 ps-3">
                  <div class="grid grid-cols-[minmax(7rem,auto)_1fr] gap-x-3 gap-y-1 text-sm">
                    <span class="font-semibold text-base-content/70">Step 3 · English</span>
                    <div class="grid grid-cols-[1fr_auto_1fr] items-start gap-2">
                      <span class="rounded bg-error/10 px-2 py-1 text-error line-through">Keep your back straight.</span><span class="text-base-content/50">→</span><span class="rounded bg-success/10 px-2 py-1 text-success">Keep your spine neutral and push your hips back.</span>
                    </div>
                    <span class="font-semibold text-base-content/70">Required equipment</span>
                    <div class="grid grid-cols-[1fr_auto_1fr] items-start gap-2">
                      <span class="rounded bg-error/10 px-2 py-1 text-error line-through">Barbell</span><span class="text-base-content/50">→</span><span class="rounded bg-success/10 px-2 py-1 text-success">Barbell, weight plates</span>
                    </div>
                    <span class="font-semibold text-base-content/70">French search aliases</span>
                    <div class="grid grid-cols-[1fr_auto_1fr] items-start gap-2">
                      <span class="rounded bg-error/10 px-2 py-1 text-error line-through">soulevé roumain</span><span class="text-base-content/50">→</span><span class="rounded bg-success/10 px-2 py-1 text-success">soulevé roumain, SDT roumain</span>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
            <tr>
              <td><div class="font-semibold">Wall sit</div><div class="text-sm text-base-content/70">wall-sit</div></td>
              <td><span class="cp-badge cp-badge-success cp-badge-soft">Complete</span></td>
              <td><span class="cp-badge cp-badge-neutral cp-badge-soft">Retirement</span></td>
            </tr>
            <tr>
              <td><div class="font-semibold">Kettlebell swing</div><div class="text-sm text-base-content/70">kettlebell-swing</div></td>
              <td><span class="cp-badge cp-badge-success cp-badge-soft">Complete</span></td>
              <td><span class="cp-badge cp-badge-success cp-badge-soft">Offered again</span></td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="text-sm text-base-content/70">Only changed fields are listed. Publishing makes these changes available to everyone on their next app open.</p>
      <div class="flex flex-wrap justify-end gap-2 border-t border-base-300 pt-4">
        <button type="button" class="cp-btn cp-btn-ghost">Cancel</button>
        <button type="button" class="cp-btn cp-btn-primary gap-2"><CloudUpload class="size-5 shrink-0" aria-hidden="true" />Publish changes</button>
      </div>
    </div>
    <div class="cp-modal-backdrop"><button type="button" tabindex="-1">Close publication review</button></div>
  </dialog>
</template>

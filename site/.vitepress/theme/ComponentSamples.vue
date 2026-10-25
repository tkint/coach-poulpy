<script setup lang="ts">
// The design system's components drawn with the tokens' own values, in light and dark.
import tokens from '../../../design/tokens.json'

const theme = tokens.theme as Record<'light' | 'dark', Record<string, string>>
const vars = (mode: 'light' | 'dark') =>
  Object.fromEntries(Object.entries(theme[mode]).map(([name, hex]) => [`--${name}`, hex]))
</script>

<template>
  <div class="samples">
    <div v-for="mode in ['light', 'dark'] as const" :key="mode" class="panel" :style="vars(mode)">
      <span class="eyebrow">{{ mode }}</span>
      <div class="row">
        <button type="button" class="btn primary">Start session</button>
        <button type="button" class="btn tonal">Edit</button>
        <button type="button" class="btn outline">Cancel</button>
        <button type="button" class="btn grumpy">Catch up</button>
      </div>
      <div class="row">
        <button type="button" class="chip on" aria-pressed="true">Legs</button>
        <button type="button" class="chip" aria-pressed="false">Core</button>
        <button type="button" class="chip" aria-pressed="false">Dumbbell</button>
      </div>
      <div class="row">
        <div class="card">
          <span class="badge" aria-hidden="true">SQ</span>
          <span class="card-text"><strong>Goblet squat</strong><span>4 × 8 at 16 kg</span></span>
        </div>
        <div class="stepper">
          <span class="eyebrow">Rest</span>
          <div class="stepper-row">
            <button type="button" aria-label="Less rest">−</button>
            <span class="value">90 s</span>
            <button type="button" aria-label="More rest">+</button>
          </div>
        </div>
      </div>
      <div class="error" role="note">
        <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="9" /><path d="M12 7v6M12 16.5v.5" /></svg>
        The treadmill stopped answering. Recording resumes when it's back.
      </div>
    </div>
  </div>
</template>

<style scoped>
.samples { display: grid; gap: 16px; margin: 16px 0; }
.panel { background: var(--background); color: var(--onBackground); border-radius: 24px; padding: 20px; display: flex; flex-direction: column; gap: 16px; font-family: Nunito, sans-serif; border: 1px solid var(--outline); }
.eyebrow { font-size: 12.5px; font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase; color: var(--onSurfaceVariant); }
.row { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.btn { height: 56px; padding: 0 24px; border-radius: 999px; border: 0; font: inherit; font-size: 17px; font-weight: 800; cursor: pointer; }
.primary { background: var(--primary); color: var(--onPrimary); }
.tonal { background: var(--primaryContainer); color: var(--onPrimaryContainer); }
.outline { background: var(--surface); color: var(--onSurface); border: 2px solid var(--outline); }
.grumpy { background: var(--grumpy); color: var(--onGrumpy); }
.chip { height: 36px; padding: 0 16px; border-radius: 999px; font: inherit; font-size: 14px; font-weight: 700; background: var(--surface); color: var(--onSurfaceVariant); border: 1.5px solid var(--outline); cursor: pointer; }
.chip.on { background: var(--primary); color: var(--onPrimary); border-color: var(--primary); }
.card { background: var(--surface); border: 1px solid var(--outline); border-radius: 16px; padding: 14px; display: flex; gap: 12px; align-items: center; min-width: 220px; }
.badge { width: 44px; height: 44px; border-radius: 999px; background: var(--primaryContainer); color: var(--onPrimaryContainer); display: flex; align-items: center; justify-content: center; font-family: 'Baloo 2', sans-serif; font-weight: 800; }
.card-text { display: flex; flex-direction: column; }
.card-text span { color: var(--onSurfaceVariant); font-size: 14px; }
.stepper { background: var(--surfaceVariant); border-radius: 12px; padding: 6px 8px; display: flex; flex-direction: column; align-items: center; min-width: 180px; }
.stepper-row { display: flex; align-items: center; justify-content: space-between; width: 100%; }
.stepper button { width: 44px; height: 44px; border-radius: 999px; border: 0; background: var(--surface); color: var(--primary); font-size: 22px; font-weight: 800; cursor: pointer; }
.value { font-size: 22px; font-weight: 800; font-variant-numeric: tabular-nums; }
.error { display: flex; gap: 10px; align-items: center; border-radius: 16px; padding: 12px 14px; font-size: 14px; font-weight: 700; color: var(--error); border: 1.5px solid var(--error); background: var(--surface); }
</style>

<script setup lang="ts">
// The design system's base fields drawn with the tokens' own values, in light and dark.
// Each sample has an id, so the "Base fields" table in the design system links straight to it.
import tokens from '../../../design/tokens.json'

const theme = tokens.theme as Record<'light' | 'dark', Record<string, string>>
const vars = (mode: 'light' | 'dark') =>
  Object.fromEntries(Object.entries(theme[mode]).map(([name, hex]) => [`--${name}`, hex]))

const grid = [0, 25, 50, 75, 100]
const points = '8,62 60,56 112,58 164,44 216,40 268,30'
</script>

<template>
  <div class="samples">
    <div v-for="mode in ['light', 'dark'] as const" :key="mode" class="panel" :style="vars(mode)">
      <span class="eyebrow">{{ mode }}</span>

      <section :id="`text-field-${mode}`" class="sample">
        <span class="name">Text field</span>
        <label class="label">Name</label>
        <div class="box"><input value="Alex" aria-label="Name" /></div>
        <span class="note">Shown to you only, on this phone.</span>
      </section>

      <section :id="`select-field-${mode}`" class="sample">
        <span class="name">Select field</span>
        <button type="button" class="box select"><span><span class="label">Experience</span><strong>Getting going</strong></span><span aria-hidden="true">›</span></button>
        <button type="button" class="box select"><span><span class="label">Sex</span><strong class="muted">Not set</strong></span><span aria-hidden="true">›</span></button>
      </section>

      <section :id="`option-sheet-${mode}`" class="sample">
        <span class="name">Option sheet</span>
        <div class="sheet">
          <strong class="sheet-title">Pronouns</strong>
          <div class="option"><span class="radio on" aria-hidden="true"></span>They / them</div>
          <div class="option"><span class="radio" aria-hidden="true"></span>She / her</div>
          <div class="option"><span class="radio" aria-hidden="true"></span>Custom</div>
          <div class="box"><input value="" placeholder="Custom pronouns" aria-label="Custom pronouns" /></div>
          <button type="button" class="btn">Save</button>
        </div>
      </section>

      <section :id="`number-field-${mode}`" class="sample">
        <span class="name">Number field</span>
        <div class="box number"><input inputmode="decimal" value="64" aria-label="Resting heart rate" /><span class="unit">bpm</span></div>
        <div class="entry">
          <button type="button" aria-label="Less">−</button>
          <div class="box number"><input inputmode="numeric" value="8" aria-label="Reps" /><span class="unit">reps</span></div>
          <button type="button" aria-label="More">+</button>
        </div>
      </section>

      <section :id="`step-slider-${mode}`" class="sample">
        <span class="name">Step slider</span>
        <strong>Getting going</strong>
        <div class="slider" aria-hidden="true">
          <span class="dot" /><span class="bar" /><span class="dot" /><span class="bar on" /><span class="dot on" /><span class="bar off" /><span class="dot off" /><span class="bar off" /><span class="dot off" />
        </div>
        <div class="ends"><span>Just starting</span><span>Very experienced</span></div>
      </section>

      <section :id="`line-chart-${mode}`" class="sample">
        <span class="name">Line chart</span>
        <svg viewBox="0 0 290 100" class="chart" role="img" aria-label="Weight from 72 kg to 70 kg over three months">
          <text x="0" y="8" class="tick">kg</text>
          <g v-for="line in grid" :key="line">
            <line :x1="28" :x2="290" :y1="12 + line * 0.64" :y2="12 + line * 0.64" class="grid" />
            <text x="0" :y="16 + line * 0.64" class="tick">{{ 74 - line * 0.06 }}</text>
          </g>
          <polyline :points="points" class="line" transform="translate(28 0)" />
        </svg>
        <div class="ends"><span>1 Aug</span><span>1 Nov</span></div>
      </section>

      <section :id="`empty-chart-card-${mode}`" class="sample">
        <span class="name">Empty chart card</span>
        <div class="empty">
          <strong>No weight yet</strong>
          <span>Log it once to see your chart. It stays on this phone, and only you see it.</span>
          <button type="button" class="btn">Log your weight</button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.samples { display: grid; gap: 16px; margin: 16px 0; }
.panel { background: var(--background); color: var(--onBackground); border-radius: 24px; padding: 20px; display: flex; flex-direction: column; gap: 20px; font-family: Nunito, sans-serif; border: 1px solid var(--outline); }
.eyebrow, .name { font-size: 12.5px; font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase; color: var(--onSurfaceVariant); }
.sample { display: flex; flex-direction: column; gap: 8px; scroll-margin-top: 80px; }
.label { font-size: 13px; color: var(--onSurfaceVariant); padding: 0 4px; display: block; }
.note { font-size: 13px; color: var(--onSurfaceVariant); padding: 0 4px; }
.box { height: 52px; border-radius: 10px; border: 1.5px solid var(--outline); background: var(--surface); padding: 0 16px; display: flex; align-items: center; gap: 8px; }
.box:focus-within { border-color: var(--primary); }
.box input { flex: 1; min-width: 0; width: 100%; background: transparent; border: 0; outline: 0; font: inherit; font-size: 17px; font-weight: 800; color: var(--onSurface); }
.select { width: 100%; justify-content: space-between; text-align: left; font: inherit; color: var(--onSurface); cursor: pointer; }
.select strong { display: block; }
.muted { color: var(--onSurfaceVariant); }
.unit { font-weight: 800; color: var(--onSurfaceVariant); }
.entry { display: flex; align-items: center; gap: 8px; }
.entry .box { flex: 1; }
.entry button { width: 48px; height: 48px; flex: none; border-radius: 999px; border: 0; background: var(--surfaceVariant); color: var(--primary); font-size: 24px; font-weight: 800; cursor: pointer; }
.sheet { background: var(--surface); border: 1px solid var(--outline); border-radius: 24px; padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.sheet-title { font-size: 18px; }
.option { display: flex; align-items: center; gap: 12px; min-height: 44px; font-weight: 700; }
.radio { width: 20px; height: 20px; border-radius: 999px; border: 2px solid var(--outline); }
.radio.on { border: 6px solid var(--primary); }
.btn { height: 48px; border-radius: 999px; border: 0; background: var(--primary); color: var(--onPrimary); font: inherit; font-size: 16px; font-weight: 800; padding: 0 24px; cursor: pointer; }
.slider { display: flex; align-items: center; }
.dot { width: 14px; height: 14px; border-radius: 999px; background: var(--primary); flex: none; }
.dot.off { background: var(--outline); }
.bar { flex: 1; height: 4px; background: var(--primary); }
.bar.off { background: var(--outline); }
.ends { display: flex; justify-content: space-between; font-size: 13px; color: var(--onSurfaceVariant); }
.chart { width: 100%; max-width: 420px; }
.grid { stroke: var(--outline); stroke-width: 1; }
.tick { font-size: 8px; fill: var(--onSurfaceVariant); }
.line { fill: none; stroke: var(--primary); stroke-width: 2.5; stroke-linecap: round; stroke-linejoin: round; }
.empty { background: var(--surface); border: 1px solid var(--outline); border-radius: 16px; padding: 16px; display: flex; flex-direction: column; gap: 8px; align-items: flex-start; }
.empty span { color: var(--onSurfaceVariant); font-size: 14px; }
</style>

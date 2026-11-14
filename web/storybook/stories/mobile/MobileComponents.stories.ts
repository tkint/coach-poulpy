import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CircleExclamationPoint, ChevronRight } from '@lucide/vue'

// The Android and iOS components, drawn on the shared tokens with the isolated mobile stylesheet (ADR 0029).
// Each story id is linked from the tables in docs/design-system.md.
const meta = {
  title: 'Mobile/Components',
  parameters: { docs: { description: { component: 'The Android and iOS components, drawn with the values of `design/tokens.json`. The shape and notes of each one are in the Components and Base fields tables of the design system. Their classes live in `stories/mobile/mobile.css`, under `.cp-mobile`, and use only the `--cp-*` tokens.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

/** The four button shapes. One primary button per screen; the grumpy button is only the catch-up action. */
export const Button = sample(`<div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div>`)

/** Filters, with `aria-pressed`. On is `primary`, off is a 1.5 px `outline`. */
export const Chip = sample(`<div class="row">
  <button type="button" class="chip on" aria-pressed="true">Legs</button>
  <button type="button" class="chip" aria-pressed="false">Core</button>
  <button type="button" class="chip" aria-pressed="false">Dumbbell</button></div>`)

/** A row with a pattern badge: the exercise and workout lists are made of these. */
export const Card = sample(`<div class="card"><span class="badge" aria-hidden="true">SQ</span>
  <span class="card-text"><strong>Goblet squat</strong><span>4 × 8 at 16 kg</span></span></div>`)

/** Sets, reps, load and rest. */
export const Stepper = sample(`<div class="stepper"><span class="eyebrow">Rest</span><div class="stepper-row">
  <button type="button" aria-label="Less rest">−</button><span class="value">90 s</span>
  <button type="button" aria-label="More rest">+</button></div></div>`)

/** The error banner: `error` tint, an icon and text, never ember. */
export const Alert = sample(`<div class="alert" role="note"><CircleExclamationPoint :size="20" aria-hidden="true" />
  The treadmill stopped answering. Recording resumes when it's back.</div>`, { CircleExclamationPoint })

export const TextField = sample(`<div class="sample"><label class="label" for="name">Name</label>
  <div class="box"><input id="name" value="Alex" /></div>
  <span class="note">Shown to you only, on this phone.</span></div>`)

export const SelectField = sample(`<div class="sample">
  <button type="button" class="box select"><span><span class="label">Experience</span><strong>Getting going</strong></span><ChevronRight :size="20" aria-hidden="true" /></button>
  <button type="button" class="box select"><span><span class="label">Sex</span><strong class="muted">Not set</strong></span><ChevronRight :size="20" aria-hidden="true" /></button></div>`, { ChevronRight })

export const OptionSheet = sample(`<div class="sheet"><strong class="sheet-title">Pronouns</strong>
  <div class="option"><span class="radio on" aria-hidden="true"></span>They / them</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>She / her</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>Custom</div>
  <div class="box"><input value="" placeholder="Custom pronouns" aria-label="Custom pronouns" /></div>
  <button type="button" class="btn primary sm">Save</button></div>`)

export const NumberField = sample(`<div class="sample">
  <div class="box"><input inputmode="decimal" value="64" aria-label="Resting heart rate" /><span class="unit">bpm</span></div>
  <div class="entry"><button type="button" aria-label="Less">−</button>
    <div class="box"><input inputmode="numeric" value="8" aria-label="Reps" /><span class="unit">reps</span></div>
    <button type="button" aria-label="More">+</button></div></div>`)

export const StepSlider = sample(`<div class="sample"><strong>Getting going</strong>
  <div class="slider" aria-hidden="true"><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar off"></span><span class="dot off"></span><span class="bar off"></span><span class="dot off"></span></div>
  <div class="ends"><span>Just starting</span><span>Very experienced</span></div></div>`)

export const LineChart: Story = {
  render: () => ({
    setup: () => ({ grid: [0, 25, 50, 75, 100], points: '8,62 60,56 112,58 164,44 216,40 268,30' }),
    template: `<div class="cp-mobile"><div class="sample">
      <svg viewBox="0 0 290 100" class="chart" role="img" aria-label="Weight from 72 kg to 70 kg over three months">
        <text x="0" y="8" class="tick">kg</text>
        <g v-for="line in grid" :key="line">
          <line :x1="28" :x2="290" :y1="12 + line * 0.64" :y2="12 + line * 0.64" class="grid" />
          <text x="0" :y="16 + line * 0.64" class="tick">{{ 74 - line * 0.06 }}</text>
        </g>
        <polyline :points="points" class="line" transform="translate(28 0)" />
      </svg>
      <div class="ends"><span>1 Aug</span><span>1 Nov</span></div></div></div>`,
  }),
}

/** Shown instead of the chart when nothing is logged. */
export const EmptyChartCard = sample(`<div class="empty"><strong>No weight yet</strong>
  <span>Log it once to see your chart. It stays on this phone, and only you see it.</span>
  <button type="button" class="btn primary sm">Log your weight</button></div>`)

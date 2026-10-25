<script setup lang="ts">
// Read straight from design/tokens.json at build time, so this page can never drift from the apps.
import tokens from '../../../design/tokens.json'

const palettes = Object.entries(tokens.color) as [string, Record<string, string>][]
const moods = Object.entries(tokens.mood) as [string, string][]
const roles = Object.keys(tokens.theme.light).filter((name) => !name.startsWith('on'))
const onRole = (role: string) => {
  const name = 'on' + role[0].toUpperCase() + role.slice(1)
  return name in tokens.theme.light ? name : null
}
const theme = tokens.theme as Record<'light' | 'dark', Record<string, string>>
const typeRoles = Object.entries(tokens.type) as [string, any][]
const family = (font: string) => (font === 'display' ? "'Baloo 2', sans-serif" : 'Nunito, sans-serif')
const space = Object.entries(tokens.space) as [string, number][]
const radius = Object.entries(tokens.radius) as [string, number][]
const motion = Object.entries(tokens.motion) as [string, any][]
</script>

<template>
  <div class="tokens">
    <h2 id="roles">Colour roles</h2>
    <p>Each role with its text colour, in light and dark.</p>
    <div class="themes">
      <div v-for="mode in ['light', 'dark']" :key="mode" class="theme" :style="{ background: theme[mode as 'light'].background }">
        <strong :style="{ color: theme[mode as 'light'].onBackground }">{{ mode }}</strong>
        <div class="roles">
          <div
            v-for="role in roles"
            :key="role"
            class="role"
            :style="{
              background: theme[mode as 'light'][role],
              color: onRole(role) ? theme[mode as 'light'][onRole(role)!] : theme[mode as 'light'].onBackground,
              borderColor: theme[mode as 'light'].outline,
            }"
          >
            <span class="name">{{ role }}</span>
            <code>{{ theme[mode as 'light'][role] }}</code>
          </div>
        </div>
      </div>
    </div>

    <h2 id="moods">Mood spectrum</h2>
    <div class="spectrum">
      <div v-for="[name, hex] in moods" :key="name" class="mood">
        <span class="chip" :style="{ background: hex }" />
        <span class="name">{{ name }}</span>
        <code>{{ hex }}</code>
      </div>
    </div>

    <h2 id="palette">Palette</h2>
    <div v-for="[name, steps] in palettes" :key="name" class="palette">
      <span class="label">{{ name }}</span>
      <div class="steps">
        <div v-for="(hex, step) in steps" :key="step" class="step">
          <span class="chip" :style="{ background: hex }" />
          <span class="name">{{ step }}</span>
          <code>{{ hex }}</code>
        </div>
      </div>
    </div>

    <h2 id="type">Type</h2>
    <div v-for="[name, role] in typeRoles" :key="name" class="type-row">
      <span class="label">{{ name }}</span>
      <span
        :style="{
          fontFamily: family(role.font),
          fontSize: role.size + 'px',
          lineHeight: role.lineHeight + 'px',
          fontWeight: role.weight,
          letterSpacing: role.tracking ? role.tracking + 'em' : undefined,
          textTransform: name === 'label' ? 'uppercase' : undefined,
          fontVariantNumeric: role.tabular ? 'tabular-nums' : undefined,
        }"
        >{{ name === 'timer' ? '01:27' : 'Leg day, Poulpy approves' }}</span
      >
      <code>{{ role.size }} / {{ role.lineHeight }} · {{ role.weight }}</code>
    </div>

    <h2 id="space">Space and radius</h2>
    <div class="bars">
      <div v-for="[name, px] in space" :key="name" class="bar-row">
        <span class="label">{{ name }}</span>
        <span class="bar" :style="{ width: px * 3 + 'px' }" />
        <code>{{ px }}</code>
      </div>
    </div>
    <div class="radii">
      <div v-for="[name, px] in radius" :key="name" class="radius">
        <span class="box" :style="{ borderRadius: Math.min(px, 40) + 'px' }" />
        <span class="name">{{ name }} · {{ px }}</span>
      </div>
    </div>

    <h2 id="motion">Motion</h2>
    <table>
      <thead><tr><th>Token</th><th>Duration</th><th>Easing</th></tr></thead>
      <tbody>
        <tr v-for="[name, m] in motion" :key="name">
          <td>{{ name }}</td>
          <td>{{ m.duration }} ms</td>
          <td><code>{{ m.easing ? `cubic-bezier(${m.easing.join(', ')})` : 'standard' }}</code></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.themes { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; }
.theme { border-radius: 16px; padding: 16px; border: 1px solid var(--vp-c-divider); text-transform: capitalize; }
.roles { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 8px; margin-top: 12px; }
.role { border-radius: 12px; padding: 10px; min-height: 64px; border: 1px solid; display: flex; flex-direction: column; justify-content: space-between; text-transform: none; }
.role .name { font-weight: 800; font-size: 13px; }
.role code { background: none; padding: 0; color: inherit; font-size: 12px; }
.spectrum { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 8px; }
.spectrum .chip { display: block; height: 56px; border-radius: 12px; }
.mood, .step { display: flex; flex-direction: column; gap: 2px; font-size: 13px; }
.name { font-weight: 800; text-transform: capitalize; }
.palette { margin-bottom: 16px; }
.label { display: inline-block; min-width: 90px; font-size: 12.5px; font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase; color: var(--vp-c-text-2); }
.steps { display: grid; grid-template-columns: repeat(auto-fill, minmax(72px, 1fr)); gap: 8px; margin-top: 6px; }
.steps .chip { display: block; height: 40px; border-radius: 10px; border: 1px solid var(--vp-c-divider); }
.type-row { display: grid; grid-template-columns: 90px 1fr auto; align-items: baseline; gap: 16px; padding: 8px 0; border-bottom: 1px solid var(--vp-c-divider); overflow-x: auto; }
.bars { display: flex; flex-direction: column; gap: 6px; }
.bar-row { display: flex; align-items: center; gap: 12px; }
.bar { height: 12px; border-radius: 6px; background: var(--vp-c-brand-1); }
.radii { display: flex; flex-wrap: wrap; gap: 16px; margin-top: 16px; }
.radius { display: flex; flex-direction: column; align-items: center; gap: 6px; font-size: 13px; }
.radius .box { width: 80px; height: 80px; background: var(--vp-c-brand-soft); border: 2px solid var(--vp-c-brand-1); }
@media (max-width: 640px) { .spectrum { grid-template-columns: repeat(3, minmax(0, 1fr)); } .type-row { grid-template-columns: 1fr; } }
</style>

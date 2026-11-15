<script setup lang="ts">
// The body map: the front and back figure, drawn from the apps' shapes (design/body-map.json). `heat` says which areas are
// lit and `injury` which hurt; both take the same object, an area by its name ('biceps', 'lower-back'...) with its side
// ('left', 'right' or 'both', the person's own left and right) or `{ side, heat }`. An area is drawn on the figure that has it.
// `heat` in the object runs from 1 (a little) to 3 (most) and a bare side means 3; an injury is drawn in the pain colour, its
// `heat` how much it hurts. `description` says in words what the picture shows, since the figure alone is not readable by a screen reader.
// `layout` shows the front, the back or both; `legend` puts the Less to More scale on top, on the right or hides it.
// Every area has a title, its name as a tooltip. With `editing` (`heat` or `injury`), an area is a button (`selectable` narrows which; `pick` says its area and side, `aria-pressed` says whether it is
// lit) and a pick updates `v-model:heat` or `v-model:injury`, whichever `editing` says; without it the map is read-only; `interactive-mode` says whether a tap takes
// the side tapped or both sides. A screen that does this writes its own legend, or none, with the legend `hidden`.
import { computed } from 'vue'
import { bodyAreaText, uiText } from '../i18n'
import shapes from '../../../../design/body-map.json'

export type BodySide = 'left' | 'right' | 'both'
export type BodyHeat = 1 | 2 | 3
export type BodyAreaValue = BodySide | { side: BodySide; heat?: BodyHeat }
export type BodyAreas = Record<string, BodyAreaValue>

type Region = { key: string | null; d: string; skin: boolean }
const props = defineProps<{
  /** What is lit; `v-model:heat` in a form. */
  heat?: BodyAreas
  /** What hurts; `v-model:injury` in a form. */
  injury?: BodyAreas
  /** What a pick edits: the `heat` or the `injury`. Left out or false, the map is read-only. */
  editing?: false | 'heat' | 'injury'
  /** Wider shoulders (the default) or Wider hips. */
  figure?: 'shoulders' | 'hips'
  description: string
  /** Which figures: `both` (the default), `front` or `back`. */
  layout?: 'front' | 'back' | 'both'
  /** Where the Less to More legend goes: `hidden` (the default), `top` or `right`; its words are the shared ones. */
  legend?: 'hidden' | 'top' | 'right'
  /** What a tap selects with `editing`: the `individual` side tapped (the default) or `both` sides of the area. */
  interactiveMode?: 'individual' | 'both'
  /** The areas a tap can pick with `editing`; every area when left out. */
  selectable?: string[]
}>()
const emit = defineEmits<{ pick: [area: string, side: 'left' | 'right']; 'update:heat': [areas: BodyAreas]; 'update:injury': [areas: BodyAreas] }>()

/** The areas with [side] of [area] toggled (left and right make both), or both sides at once with `interactive-mode="both"`: an area on one side only then becomes both. A `heat` already on the area is kept. */
function toggled(areas: BodyAreas, area: string, side: 'left' | 'right'): BodyAreas {
  const value = areas[area]
  const given = typeof value === 'string' ? value : value?.side
  const { [area]: _, ...rest } = areas
  const heat = typeof value === 'object' ? value.heat : undefined
  const next: BodySide[] =
    props.interactiveMode === 'both' ? (given === 'both' ? [] : ['both']) : (() => {
      const sides: BodySide[] = given === 'both' ? ['left', 'right'] : given ? [given] : []
      return sides.includes(side) ? sides.filter((one) => one !== side) : [...sides, side]
    })()
  const named = next.length === 2 ? 'both' : next[0]
  return named ? { ...rest, [area]: heat ? { side: named, heat } : named } : rest
}

/** Picking says `pick`, and with `v-model:heat` or `v-model:injury`, whichever `editing` names, it also hands over the new areas. */
function pick(area: string, side: 'left' | 'right') {
  emit('pick', area, side)
  if (props.editing === 'injury') {
    if (props.injury) emit('update:injury', toggled(props.injury, area, side))
  } else if (props.heat) emit('update:heat', toggled(props.heat, area, side))
}

const split = (key: string): { area: string; side?: 'left' | 'right' } => {
  const match = /^(.*)-(left|right)$/.exec(key)
  return match ? { area: match[1], side: match[2] as 'left' | 'right' } : { area: key }
}
/** The heat an area has on one side, or nothing when that side is not in [areas]. */
const heatOf = (areas: BodyAreas | undefined, area: string, side?: 'left' | 'right'): BodyHeat | undefined => {
  const value = areas?.[area]
  if (!value) return undefined
  const given = typeof value === 'string' ? value : value.side
  if (side && given !== 'both' && given !== side) return undefined
  return (typeof value === 'string' ? undefined : value.heat) ?? 3
}

const views = computed(() => {
  const regions = shapes[props.figure ?? 'shoulders'] as Record<'front' | 'back', Region[]>
  const view = (name: 'front' | 'back') => ({
    name,
    caption: uiText(`ui.${name}`),
    paths: regions[name].map((region) => {
      if (region.skin || !region.key) return { d: region.d, outline: region.skin }
      const { area, side } = split(region.key)
      const pickable = !!props.editing && !!side && (!props.selectable || props.selectable.includes(area))
      const label = bodyAreaText(area, side)
      return { d: region.d, area, side, label, pickable, heat: heatOf(props.heat, area, side), pain: heatOf(props.injury, area, side) }
    }),
  })
  const shown = [view('front'), view('back')]
  return props.layout === 'front' || props.layout === 'back' ? shown.filter((one) => one.name === props.layout) : shown
})
const showLegend = computed(() => !!props.legend && props.legend !== 'hidden')
</script>
<template>
  <div class="flex items-center justify-center gap-4" :class="legend === 'right' ? 'flex-row' : 'flex-col'" data-component="body-map">
    <p v-if="showLegend && legend === 'top'" class="cp-heat-legend order-first" aria-hidden="true">
      {{ uiText('ui.less') }}
      <svg v-for="heat in [1, 2, 3]" :key="heat" viewBox="0 0 14 14"><rect class="cp-body-part" :data-heat="heat" x="1" y="1" width="12" height="12" rx="3" /></svg>
      {{ uiText('ui.more') }}
    </p>
    <div class="cp-body-map" :role="editing ? 'group' : 'img'" :aria-label="description">
      <figure v-for="view in views" :key="view.name" class="cp-body-view">
        <svg viewBox="0 0 120 268" class="cp-body-figure" :aria-hidden="editing ? undefined : 'true'">
          <template v-for="(path, i) in view.paths" :key="i">
            <path v-if="path.outline" class="cp-body-outline" :d="path.d" />
            <path
              v-else
              class="cp-body-part"
              :class="{ 'cp-body-part-pick': path.pickable }"
              :data-heat="path.heat"
              :data-pain="path.pain"
              :d="path.d"
              :role="path.pickable ? 'button' : undefined"
              :tabindex="path.pickable ? 0 : undefined"
              :aria-label="path.pickable ? path.label : undefined"
              :aria-pressed="path.pickable ? path.heat !== undefined || path.pain !== undefined : undefined"
              @click="path.pickable && pick(path.area!, path.side!)"
              @keydown.enter.prevent="path.pickable && pick(path.area!, path.side!)"
              @keydown.space.prevent="path.pickable && pick(path.area!, path.side!)"
            >
              <title>{{ path.label }}</title>
            </path>
          </template>
        </svg>
        <figcaption>{{ view.caption }}</figcaption>
      </figure>
    </div>
    <p v-if="showLegend && legend === 'right'" class="cp-heat-legend cp-heat-legend-right" aria-hidden="true">
      {{ uiText('ui.more') }}
      <svg v-for="heat in [3, 2, 1]" :key="heat" viewBox="0 0 14 14"><rect class="cp-body-part" :data-heat="heat" x="1" y="1" width="12" height="12" rx="3" /></svg>
      {{ uiText('ui.less') }}
    </p>
  </div>
</template>

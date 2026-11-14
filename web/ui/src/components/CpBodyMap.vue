<script setup lang="ts">
// The body map: the front and back figure, drawn from the apps' shapes (design/body-map.json). `front` and `back` say which
// areas are lit, `front-injury` and `back-injury` which hurt; all four take the same object, an area by its name ('biceps',
// 'lower-back'...) with its side ('left', 'right' or 'both', the person's own left and right) or `{ side, heat }`.
// `heat` runs from 1 (a little) to 3 (most) and a bare side means 3; an injury is drawn in the pain colour, its `heat` how much it
// hurts. `description` says in words what the picture shows, since the figure alone is not readable by a screen reader.
// `layout` shows the front, the back or both; `legend` puts the Less to More scale on top, on the right or hides it.
// With `interactive`, an area `areaLabel` names is a button (`pick` says its area and side, `aria-pressed` says whether it is
// lit), so the picture is a field: a screen that does this writes its own legend, or none, by leaving `less` and `more` out.
import { computed } from 'vue'
import shapes from '../../../../design/body-map.json'

export type BodySide = 'left' | 'right' | 'both'
export type BodyHeat = 1 | 2 | 3
export type BodyAreaValue = BodySide | { side: BodySide; heat?: BodyHeat }
export type BodyAreas = Record<string, BodyAreaValue>

type Region = { key: string | null; d: string; skin: boolean }
const props = defineProps<{
  front?: BodyAreas
  back?: BodyAreas
  frontInjury?: BodyAreas
  backInjury?: BodyAreas
  /** Wider shoulders (the default) or Wider hips. */
  figure?: 'shoulders' | 'hips'
  captions: { front: string; back: string }
  description: string
  less?: string
  more?: string
  /** Which figures: `both` (the default), `front` or `back`. */
  layout?: 'front' | 'back' | 'both'
  /** Where the Less to More legend goes: `hidden` (the default), `top` or `right`; it says its words with `less` and `more`. */
  legend?: 'hidden' | 'top' | 'right'
  interactive?: boolean
  /** The name of an area as a button, or nothing for an area that cannot be picked. */
  areaLabel?: (area: string) => string | undefined
}>()
const emit = defineEmits<{ pick: [area: string, side: 'left' | 'right'] }>()

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
  const view = (name: 'front' | 'back', lit?: BodyAreas, hurt?: BodyAreas) => ({
    name,
    caption: props.captions[name],
    paths: regions[name].map((region) => {
      if (region.skin || !region.key) return { d: region.d, outline: region.skin }
      const { area, side } = split(region.key)
      const label = props.interactive && side ? props.areaLabel?.(area) : undefined
      return { d: region.d, area, side, label, heat: heatOf(lit, area, side), pain: heatOf(hurt, area, side) }
    }),
  })
  const shown = [view('front', props.front, props.frontInjury), view('back', props.back, props.backInjury)]
  return props.layout === 'front' || props.layout === 'back' ? shown.filter((one) => one.name === props.layout) : shown
})
const showLegend = computed(() => !!props.legend && props.legend !== 'hidden' && !!props.less && !!props.more)
</script>
<template>
  <div class="flex items-center gap-4" :class="legend === 'right' ? 'flex-row' : 'flex-col'" data-component="body-map">
    <p v-if="showLegend && legend === 'top'" class="cp-heat-legend order-first" aria-hidden="true">
      {{ less }}
      <svg v-for="heat in [1, 2, 3]" :key="heat" viewBox="0 0 14 14"><rect class="cp-body-part" :data-heat="heat" x="1" y="1" width="12" height="12" rx="3" /></svg>
      {{ more }}
    </p>
    <div class="cp-body-map" :role="interactive ? 'group' : 'img'" :aria-label="description">
      <figure v-for="view in views" :key="view.name" class="cp-body-view">
        <svg viewBox="0 0 120 268" class="cp-body-figure" :aria-hidden="interactive ? undefined : 'true'">
          <template v-for="(path, i) in view.paths" :key="i">
            <path v-if="path.outline" class="cp-body-outline" :d="path.d" />
            <path
              v-else-if="path.label"
              class="cp-body-part cp-body-part-pick"
              :data-heat="path.heat"
              :data-pain="path.pain"
              :d="path.d"
              role="button"
              tabindex="0"
              :aria-label="path.label"
              :aria-pressed="path.heat !== undefined || path.pain !== undefined"
              @click="emit('pick', path.area!, path.side!)"
              @keydown.enter.prevent="emit('pick', path.area!, path.side!)"
              @keydown.space.prevent="emit('pick', path.area!, path.side!)"
            />
            <path v-else class="cp-body-part" :data-heat="path.heat" :data-pain="path.pain" :d="path.d" />
          </template>
        </svg>
        <figcaption>{{ view.caption }}</figcaption>
      </figure>
    </div>
    <p v-if="showLegend && legend === 'right'" class="cp-heat-legend cp-heat-legend-right" aria-hidden="true">
      {{ more }}
      <svg v-for="heat in [3, 2, 1]" :key="heat" viewBox="0 0 14 14"><rect class="cp-body-part" :data-heat="heat" x="1" y="1" width="12" height="12" rx="3" /></svg>
      {{ less }}
    </p>
  </div>
</template>

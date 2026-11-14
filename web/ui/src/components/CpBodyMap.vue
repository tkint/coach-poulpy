<script setup lang="ts">
// The muscles worked: the front and back figure, areas lit from heat 1 (a little) to 3 (most), and
// a Less to More legend. `figures` come from the generated body map shapes; `description` says in
// words what the picture shows, since the figure alone is not readable by a screen reader.
// With `interactive`, an area that has a `key` and a `label` is a button (`pick` says its key, `aria-pressed` says
// whether it is lit), so the picture is a field: a screen that does this writes its own legend, or none, by
// leaving `less` and `more` out.
export type BodyMapPath = { d: string; heat?: 1 | 2 | 3; outline?: boolean; key?: string; label?: string }
export type BodyMapFigure = { caption: string; paths: BodyMapPath[] }
const props = defineProps<{ figures: BodyMapFigure[]; description: string; less?: string; more?: string; interactive?: boolean }>()
const emit = defineEmits<{ pick: [key: string] }>()
const pickable = (path: BodyMapPath) => props.interactive && !path.outline && !!path.key && !!path.label
</script>
<template>
  <div class="flex flex-col items-center gap-4" data-component="body-map">
    <div class="cp-body-map" :role="interactive ? 'group' : 'img'" :aria-label="description">
      <figure v-for="figure in figures" :key="figure.caption" class="cp-body-view">
        <svg viewBox="0 0 120 268" class="cp-body-figure" :aria-hidden="interactive ? undefined : 'true'">
          <template v-for="(path, i) in figure.paths" :key="i">
            <path
              v-if="pickable(path)"
              class="cp-body-part cp-body-part-pick"
              :data-heat="path.heat"
              :d="path.d"
              role="button"
              tabindex="0"
              :aria-label="path.label"
              :aria-pressed="path.heat !== undefined"
              @click="emit('pick', path.key!)"
              @keydown.enter.prevent="emit('pick', path.key!)"
              @keydown.space.prevent="emit('pick', path.key!)"
            />
            <path v-else :class="path.outline ? 'cp-body-outline' : 'cp-body-part'" :data-heat="path.heat" :d="path.d" />
          </template>
        </svg>
        <figcaption>{{ figure.caption }}</figcaption>
      </figure>
    </div>
    <p v-if="less && more" class="cp-heat-legend" aria-hidden="true">
      {{ less }}
      <svg v-for="heat in [1, 2, 3]" :key="heat" viewBox="0 0 14 14"><rect class="cp-body-part" :data-heat="heat" x="1" y="1" width="12" height="12" rx="3" /></svg>
      {{ more }}
    </p>
  </div>
</template>

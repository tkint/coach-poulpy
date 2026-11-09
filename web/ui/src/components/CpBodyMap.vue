<script setup lang="ts">
// The muscles worked: the front and back figure, areas lit from heat 1 (a little) to 3 (most), and
// a Less to More legend. `figures` come from the generated body map shapes; `description` says in
// words what the picture shows, since the figure alone is not readable by a screen reader.
export type BodyMapPath = { d: string; heat?: 1 | 2 | 3; outline?: boolean }
export type BodyMapFigure = { caption: string; paths: BodyMapPath[] }
defineProps<{ figures: BodyMapFigure[]; description: string; less: string; more: string }>()
</script>
<template>
  <div class="flex flex-col items-center gap-4" data-component="body-map">
    <div class="cp-body-map" role="img" :aria-label="description">
      <figure v-for="figure in figures" :key="figure.caption" class="cp-body-view">
        <svg viewBox="0 0 120 268" class="cp-body-figure" aria-hidden="true">
          <path v-for="(path, i) in figure.paths" :key="i" :class="path.outline ? 'cp-body-outline' : 'cp-body-part'" :data-heat="path.heat" :d="path.d" />
        </svg>
        <figcaption>{{ figure.caption }}</figcaption>
      </figure>
    </div>
    <p class="cp-heat-legend" aria-hidden="true">
      {{ less }}
      <svg v-for="heat in [1, 2, 3]" :key="heat" viewBox="0 0 14 14"><rect class="cp-body-part" :data-heat="heat" x="1" y="1" width="12" height="12" rx="3" /></svg>
      {{ more }}
    </p>
  </div>
</template>

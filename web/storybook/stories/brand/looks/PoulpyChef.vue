<script setup lang="ts">
// Poulpy the cook (design/poulpy-3d/looks/chef.mjs), live in PoulpyLookViewer.vue, running the cook's idle (CHEF in
// motion.mjs): his chin goes up and his moustache twitches. Stopped, he stands as modelled.
import PoulpyLookViewer, { type LiveLook, type PoulpyScene } from './PoulpyLookViewer.vue'

async function start(scene: PoulpyScene): Promise<LiveLook> {
  const [{ buildChef }, { createChef }, tokens] = await Promise.all([
    import('../../../../../design/poulpy-3d/looks/chef.mjs'),
    import('../../../../../design/poulpy-3d/motion.mjs'),
    import('../../../../../design/tokens.json').then((m) => m.default),
  ])
  const colours = tokens.color.poulpy, tones = tokens.mood
  const idle = createChef()
  return {
    // His face is drawn apart, at each step of a twitch, so the idle can twitch his moustache.
    show: (outline) => scene.show({ model: buildChef({ face: false, tones, colours }), faces: (t) => buildChef({ twitch: t, tones, colours, faceOnly: true }) }, outline),
    advance: (seconds) => scene.pose(idle.advance(seconds)),
    stopped: () => scene.pose(undefined),
  }
}
</script>

<template>
  <!-- Framed for his toque: looked at a little higher than his middle, from a little further away. -->
  <PoulpyLookViewer
    label="Poulpy the cook in a tall white toque and a curled moustache: eyes wide, brows up and a small confident grin."
    :framing="{ tilt: 14, distance: 10.4, centre: 1.62 }"
    move-label="Idle"
    :start="start"
  />
</template>

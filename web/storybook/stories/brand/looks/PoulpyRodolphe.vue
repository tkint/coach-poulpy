<script setup lang="ts">
// Rodolphe, Poulpy's red-nosed reindeer look (design/poulpy-3d/looks/rodolphe.mjs), live in PoulpyLookViewer.vue: he sways to a
// tune, his nose glowing on the beat. The glow is light, so it is drawn here, not in the model.
import type * as Three from 'three'
import tokens from '../../../../../design/tokens.json'
import { buildRodolphe, COLOURS, createRodolpheIdle, noseBulb, scarfKnot } from '../../../../../design/poulpy-3d/looks/rodolphe.mjs'
import PoulpyLookViewer, { type LiveLook, type PoulpyScene } from './PoulpyLookViewer.vue'

// A soft round glow, warm in the middle and gone at its edge: the light of his nose on the air in front of his face.
function glowTexture(three: typeof Three) {
  const size = 128, paint = document.createElement('canvas')
  paint.width = paint.height = size
  const ink = paint.getContext('2d')!, fade = ink.createRadialGradient(size / 2, size / 2, 0, size / 2, size / 2, size / 2)
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(COLOURS.glow.slice(i, i + 2), 16))
  fade.addColorStop(0, `rgba(${r},${g},${b},0.8)`)
  fade.addColorStop(0.3, `rgba(${r},${g},${b},0.35)`)
  fade.addColorStop(0.65, `rgba(${r},${g},${b},0.08)`)
  fade.addColorStop(1, `rgba(${r},${g},${b},0)`)
  ink.fillStyle = fade
  ink.fillRect(0, 0, size, size)
  return new three.CanvasTexture(paint)
}

async function start(scene: PoulpyScene): Promise<LiveLook> {
  const three = await import('three')
  const move = createRodolpheIdle()
  // What the idle moves on its own: the glow in front of his nose, his nose, and the scarf's ends around their knot.
  let glow: Three.Sprite | undefined
  let nose: Three.Mesh[] = []
  let bulb: { material: Three.MeshToonMaterial; colour: Three.Color; lit: Three.Color } | undefined
  let ends: Three.Mesh[] = []
  const forget = () => { glow?.material.map?.dispose(); glow?.material.dispose() }

  function pose() {
    const now = move.pose()
    bulb?.material.emissive.copy(bulb.colour).lerp(bulb.lit, now.glow * 0.55)
    for (const part of nose) part.scale.setScalar(1 + 0.06 * now.glow)
    if (glow) {
      glow.scale.setScalar(noseBulb.radius * (3 + 2.2 * now.glow))
      glow.material.opacity = 0.25 + 0.75 * now.glow
    }
    for (const end of ends) end.rotation.y = now.ends
    scene.pose(now)
  }

  return {
    show(outline) {
      forget()
      scene.show({ model: buildRodolphe({ colours: tokens.color.poulpy, tone: tokens.mood.happy }) }, outline)
      // His nose swells a little as it glows, around its own middle, and lights up towards a paler red.
      const middle = new three.Vector3().fromArray(noseBulb.at)
      nose = scene.parts('nose')
      for (const part of nose) { part.geometry.translate(-middle.x, -middle.y, -middle.z); part.position.copy(middle) }
      const material = nose.find((part) => part.name === 'nose')?.material as Three.MeshToonMaterial | undefined
      bulb = material && { material, colour: material.emissive.clone(), lit: new three.Color(COLOURS.lit) }
      // The ends swing around the upright line through the knot: each is moved so the knot is where it turns.
      const knot = new three.Vector3().fromArray(scarfKnot)
      ends = scene.parts('scarf_end')
      for (const end of ends) { end.geometry.translate(-knot.x, -knot.y, -knot.z); end.position.copy(knot) }
      // The glow rides with his head, a little in front of his nose.
      glow = new three.Sprite(new three.SpriteMaterial({ map: glowTexture(three), transparent: true, depthWrite: false }))
      glow.position.copy(middle).add(new three.Vector3(0, 0, noseBulb.radius * 1.2))
      glow.renderOrder = 1
      nose[0]?.parent?.add(glow)
      pose()
    },
    advance(seconds) {
      move.advance(seconds)
      pose()
    },
    dispose: forget,
  }
}
</script>

<template>
  <PoulpyLookViewer
    label="Poulpy in 3D as Rodolphe, a red-nosed reindeer with antlers and a striped scarf, swaying to a tune as his nose glows."
    :framing="{ tilt: 18, distance: 9.6 }"
    move-label="Dance"
    :start="start"
  />
</template>

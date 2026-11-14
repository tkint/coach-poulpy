// Draws the Poulpy of design/poulpy-3d/model.mjs with three.js: flat cartoon shading, a light that stays
// in the room while he turns, the gleam that follows it and the optional outline. Poulpy3d.vue owns
// the controls; this file owns the canvas and everything WebGL, and gives it all back on dispose().
import * as THREE from 'three'
import tokens from '../../../design/tokens.json'
import { buildPoulpy3d, gleamPatch, LOOK, type PoulpySide } from '../../../design/poulpy-3d/model.mjs'

/** Where the viewer stands: degrees around Poulpy, degrees above (or below) him, and how far away. */
export interface PoulpyView { turn: number; tilt: number; distance: number }

export function createPoulpyScene(canvas: HTMLCanvasElement) {
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  const scene = new THREE.Scene()
  const camera = new THREE.PerspectiveCamera(28, 1, 0.1, 100)

  // One shadow band: the lit colour is the token colour, the shaded one is LOOK.shade of it.
  const ramp = new THREE.DataTexture(new Uint8Array([0, 0, 0, 255, 255, 255, 255, 255]), 2, 1, THREE.RGBAFormat)
  ramp.minFilter = ramp.magFilter = THREE.NearestFilter
  ramp.needsUpdate = true
  const light = new THREE.DirectionalLight(0xffffff, Math.PI * (1 - LOOK.shade))
  scene.add(light, light.target)

  // A soft round shadow on the floor, in the palette's deepest blue.
  const blot = document.createElement('canvas')
  blot.width = blot.height = 256
  const ink = blot.getContext('2d')!, fade = ink.createRadialGradient(128, 128, 0, 128, 128, 128)
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(tokens.color.lagoon[900].slice(i, i + 2), 16))
  fade.addColorStop(0, `rgba(${r},${g},${b},0.38)`)
  fade.addColorStop(0.55, `rgba(${r},${g},${b},0.16)`)
  fade.addColorStop(1, `rgba(${r},${g},${b},0)`)
  ink.fillStyle = fade
  ink.fillRect(0, 0, 256, 256)
  const shadow = new THREE.Mesh(new THREE.PlaneGeometry(4, 4), new THREE.MeshBasicMaterial({ map: new THREE.CanvasTexture(blot), transparent: true, depthWrite: false }))
  shadow.rotation.x = -Math.PI / 2
  shadow.position.y = 0.005
  scene.add(shadow)

  let poulpy: THREE.Group | undefined
  let gleam: THREE.BufferGeometry | undefined
  let centre = 1
  let view: PoulpyView = { turn: 0, tilt: 17, distance: 9.6 }

  const geometry = (shape: { positions: number[]; normals: number[]; indices: number[] }) => {
    const geo = new THREE.BufferGeometry()
    geo.setAttribute('position', new THREE.Float32BufferAttribute(shape.positions, 3))
    geo.setAttribute('normal', new THREE.Float32BufferAttribute(shape.normals, 3))
    geo.setIndex(shape.indices)
    return geo
  }
  const clear = () => {
    if (!poulpy) return
    scene.remove(poulpy)
    poulpy.traverse((node) => {
      if (!(node instanceof THREE.Mesh)) return
      node.geometry.dispose()
      node.material.dispose()
    })
  }

  function render() {
    const turn = THREE.MathUtils.degToRad(view.turn), tilt = THREE.MathUtils.degToRad(view.tilt)
    camera.position.set(view.distance * Math.sin(turn) * Math.cos(tilt), centre + view.distance * Math.sin(tilt), view.distance * Math.cos(turn) * Math.cos(tilt))
    camera.lookAt(0, centre, 0)
    camera.updateMatrixWorld()
    // The light and the gleam are placed from where the viewer stands, so they stay put in the room.
    light.target.position.set(0, centre, 0)
    light.position.set(...(LOOK.light as [number, number, number])).normalize().transformDirection(camera.matrixWorld).multiplyScalar(10).add(light.target.position)
    if (gleam) {
      const towards = new THREE.Vector3(...(LOOK.gleam.towards as [number, number, number])).normalize().transformDirection(camera.matrixWorld)
      const patch = gleamPatch(towards.toArray())
      gleam.setAttribute('position', new THREE.Float32BufferAttribute(patch.positions, 3))
      gleam.setAttribute('normal', new THREE.Float32BufferAttribute(patch.normals, 3))
    }
    renderer.render(scene, camera)
  }

  /** Builds Poulpy again for a side of the plush, with or without the outline. */
  function show(side: PoulpySide, outline: boolean) {
    clear()
    const model = buildPoulpy3d({ side, colours: tokens.color.poulpy })
    centre = model.height / 2
    poulpy = new THREE.Group()
    for (const part of model.parts) {
      const { colour, flat, outline: edge } = model.materials[part.material]
      const tint = new THREE.Color().setRGB(colour[0], colour[1], colour[2])
      const mesh = new THREE.Mesh(
        geometry(part),
        flat ? new THREE.MeshBasicMaterial({ color: tint }) : new THREE.MeshToonMaterial({ color: tint, emissive: tint.clone().multiplyScalar(LOOK.shade), gradientMap: ramp }),
      )
      mesh.name = part.name
      poulpy.add(mesh)
      if (part.name === 'gleam') gleam = mesh.geometry
      if (outline && edge) {
        // The outline is the same shape, a little bigger, seen from inside.
        const hull = geometry({ ...part, positions: part.positions.map((p, i) => p + part.normals[i] * LOOK.outline.width) })
        poulpy.add(new THREE.Mesh(hull, new THREE.MeshBasicMaterial({ color: new THREE.Color().setRGB(edge[0], edge[1], edge[2]), side: THREE.BackSide })))
      }
    }
    scene.add(poulpy)
    render()
  }

  function look(next: PoulpyView) {
    view = next
    render()
  }

  const fit = () => {
    const box = canvas.parentElement!.getBoundingClientRect()
    if (!box.width || !box.height) return
    renderer.setSize(box.width, box.height, false)
    camera.aspect = box.width / box.height
    camera.updateProjectionMatrix()
    render()
  }
  const resizing = new ResizeObserver(fit)
  resizing.observe(canvas.parentElement!)
  fit()

  function dispose() {
    resizing.disconnect()
    clear()
    shadow.geometry.dispose()
    shadow.material.map?.dispose()
    shadow.material.dispose()
    ramp.dispose()
    renderer.dispose()
  }

  return { show, look, dispose }
}

<script setup>
import { withBase } from 'vitepress'
</script>

# Poulpy in 3D

The plush as a solid you can turn around: a bubble of a head tucked into eight puffy arms, drawn with flat cartoon shading. One file, [`design/poulpy-3d/model.mjs`](design/poulpy-3d/model.mjs), describes his shape; this page draws it live and [`design/build-poulpy-3d.mjs`](design/build-poulpy-3d.mjs) writes it as glTF files. He is not in the apps yet: this is the reference to build from.

<Poulpy3d />

## The rules of the shape

These were settled with the owner on the first model; the reasons are in [Brand decisions](/docs/brand-decisions#_14-poulpy-in-3d-a-bubble-plush-in-flat-shading).

- **Chibi, never realistic.** A big round head, big eyes, short arms. The face is the 2D drawing's, pressed onto the head, so every [drawing](/drawings) and the model show the same Poulpy.
- **The head is tucked in.** It sinks into the arms, which wrap around its base, instead of resting on top of them.
- **Eight arms, always down.** They lie on the floor as on the plush; a mood shows on his face and in his colour, never by lifting an arm.
- **The other side is underneath.** Each arm is one smooth shape, the side's colour on top and the other side's colour below a low line, with no ridge or shelf between them. The base of the head is cut at the same height, so the whole underside is one colour: ember under the blue side, blue under the ember side.
- **Flat, like a drawing.** Plain token colours, one soft shadow band and one faint gleam. No gloss, no reflections, no fabric texture.
- **The light belongs to the room.** It comes from the viewer's upper left and stays there: the shade and the gleam slide over Poulpy as he turns, they are never painted on him.
- **The outline is optional and off by default.** It is there for small sizes or busy backgrounds.

## Using the model

| File | What it is |
| --- | --- |
| <a :href="withBase('/design/poulpy-3d/poulpy_blue.glb')" download>poulpy_blue.glb</a> | The blue, smiling side, as binary glTF |
| <a :href="withBase('/design/poulpy-3d/poulpy_ember.glb')" download>poulpy_ember.glb</a> | The ember, grumpy side, as binary glTF |
| [`design/poulpy-3d/model.mjs`](design/poulpy-3d/model.mjs) | The shape itself, as plain JavaScript with no dependency |

The glTF files open in any 3D tool or engine. Poulpy stands on the floor (y = 0, y up) and faces +z; his head is about one unit from centre to edge. Each part is its own named mesh (`head`, `head_lining`, `arm_1` to `arm_8` and their `_lining`, `eye_left`, `eye_left_shine`, `cheek_left`, `mouth`, `gleam`, plus `brow_left` and `brow_right` on the ember side), so a part can be recoloured, hidden or animated on its own.

A glTF file only carries shapes and plain matte colours. The look is the renderer's job, and `LOOK` in `model.mjs` gives its numbers: the light's direction as the viewer sees it, the shadow band's strength, where the gleam sits, and the outline's width and tint. A renderer that wants the gleam to follow the light drops the `gleam` mesh from the file and asks `gleamPatch(direction)` for a new one whenever Poulpy or the camera turns. [`poulpy3dScene.ts`](site/.vitepress/theme/poulpy3dScene.ts), which draws the preview above with three.js, is the worked example.

## Changing the design

Everything that defines him is at the top of `model.mjs`, in three plainly named objects:

- `SHAPE`: the size of the head and how deep it sits, the number, size and reach of the arms, the height of the line between the two colours, and where the face sits.
- `FACES`: the eyes, cheeks, mouth and brows of each side, in the same units as the 2D drawings of `design/build-poulpy.mjs`.
- `LOOK`: the light, the shadow band, the gleam and the outline.

His colours are the `color.poulpy` tokens of [`design/tokens.json`](design/tokens.json), never written into the model.

Edit, then regenerate and look at this page (`npm run dev` in `site/`):

```sh
node design/build-poulpy-3d.mjs
```

CI runs it with `--check` and fails when the committed glTF files no longer match `model.mjs`. A new mood or pose in 3D starts as a new entry in `FACES`, or a new part in `buildPoulpy3d`, and follows the [brand rules](/docs/brand#character-rules) like any drawing.

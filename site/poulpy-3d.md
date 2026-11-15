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

## Turning in short steps

His idle move: Poulpy likes to turn around on the spot, a short step of 20 to 30 degrees at a time, stopping after each one. Fifteen steps bring him all the way around, in about 17 seconds, and then he goes again.

<Poulpy3d motion="turn" />

His arms are not stiff. Each one bends at three places, and every bend is a small spring: when he steps the arms trail behind, when he stops they swing a little past him and settle, the tips last. **Soft arms** above loosens or stiffens those springs to try other feels; the middle of the slider is the design, and the far left is the stiff plush with no springs at all.

- **The arms slide, they never lift.** Every bend turns flat on the floor, so the move keeps the plush's shape as the [brand rules](/docs/brand#character-rules) ask.
- **He winds up, then goes.** A small lean back before each step, a quick turn, and a slight rock past the mark as he stops.
- **The head dips as he turns** and bounces back, by a few hundredths of its height, widening to keep its volume. His face rides along with it.
- **No two steps or arms are quite alike.** The steps vary between 20 and 30 degrees and the pauses between 0.45 and 0.75 seconds, and each arm's springs are a little stiffer or softer than its neighbours', so the arms fall out of step with each other.
- **Reduced motion keeps him still.** The preview starts paused for anyone who asked their device for less motion, and an app must do the same.

The move is one file, [`design/poulpy-3d/motion.mjs`](design/poulpy-3d/motion.mjs), next to the shape. `TURN` at its top holds every number: the steps and pauses, the wind-up, how eager and how bouncy his body is, how soft each length of arm is, how much the floor holds the arms back and how far the head dips. `createTurn()` runs it live, as this page does, and `bakeTurn()` writes it down frame by frame for the glTF files.

## Using the model

| File | What it is |
| --- | --- |
| <a :href="withBase('/design/poulpy-3d/poulpy_blue.glb')" download>poulpy_blue.glb</a> | The blue, smiling side, as binary glTF |
| <a :href="withBase('/design/poulpy-3d/poulpy_ember.glb')" download>poulpy_ember.glb</a> | The ember, grumpy side, as binary glTF |
| <a :href="withBase('/design/poulpy-3d/poulpy_blue_turn.glb')" download>poulpy_blue_turn.glb</a> | The blue side with the animation `turn`: one full turn in short steps, made to loop |
| <a :href="withBase('/design/poulpy-3d/poulpy_ember_turn.glb')" download>poulpy_ember_turn.glb</a> | The ember side with the same animation |
| [`design/poulpy-3d/model.mjs`](design/poulpy-3d/model.mjs) | The shape itself, as plain JavaScript with no dependency |
| [`design/poulpy-3d/motion.mjs`](design/poulpy-3d/motion.mjs) | How he moves, the same way |

The glTF files open in any 3D tool or engine. Poulpy stands on the floor (y = 0, y up) and faces +z; his head is about one unit from centre to edge. In the two files that stand still, each part is its own named mesh (`head`, `head_lining`, `arm_1` to `arm_8` and their `_lining`, `eye_left`, `eye_left_shine`, `cheek_left`, `mouth`, `gleam`, plus `brow_left` and `brow_right` on the ember side), so a part can be recoloured, hidden or animated on its own.

The two `_turn` files hold the same meshes, rigged. The node `turn` faces him and `squash` under it carries his head and face; each arm is skinned to `turn` and to its own chain of three joints, `arm_1_bend_1` to `arm_1_bend_3` from the head outwards. The animation `turn` plays one full turn at 30 frames a second and ends where it starts, so any player can loop it. It is a recording of the springs at their design values: an engine that wants them live, to change the softness or to react to something, runs `createTurn()` from `motion.mjs` on the same joints, as the preview above does.

A glTF file only carries shapes and plain matte colours. The look is the renderer's job, and `LOOK` in `model.mjs` gives its numbers: the light's direction as the viewer sees it, the shadow band's strength, where the gleam sits, and the outline's width and tint. A renderer that wants the gleam to follow the light drops the `gleam` mesh from the file and asks `gleamPatch(direction)` for a new one whenever Poulpy or the camera turns. [`poulpy3dScene.ts`](site/.vitepress/theme/poulpy3dScene.ts), which draws the preview above with three.js, is the worked example.

## Changing the design

Everything that defines him is at the top of `model.mjs`, in three plainly named objects:

- `SHAPE`: the size of the head and how deep it sits, the number, size and reach of the arms, the height of the line between the two colours, and where the face sits.
- `FACES`: the eyes, cheeks, mouth and brows of each side, in the same units as the 2D drawings of `design/build-poulpy.mjs`.
- `LOOK`: the light, the shadow band, the gleam and the outline.

How he moves is in `motion.mjs`: `TURN` for the move and `RIG` for where the arms bend.

His colours are the `color.poulpy` tokens of [`design/tokens.json`](design/tokens.json), never written into the model.

Edit, then regenerate and look at this page (`npm run dev` in `site/`):

```sh
node design/build-poulpy-3d.mjs
```

CI runs it with `--check` and fails when the committed glTF files no longer match `model.mjs` and `motion.mjs`. A new mood or pose in 3D starts as a new entry in `FACES`, or a new part in `buildPoulpy3d`; a new move starts next to `TURN` in `motion.mjs`. Both follow the [brand rules](/docs/brand#character-rules) like any drawing.

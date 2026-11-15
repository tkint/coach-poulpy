# Poulpy in 3D

The plush as a solid you can turn around: a bubble of a head tucked into eight puffy arms, drawn with flat cartoon shading. One file, [`design/poulpy-3d/model.mjs`](../design/poulpy-3d/model.mjs), describes his shape; the [Poulpy in 3D](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/brand-poulpy-in-3d--docs) page of the component workshop draws it live and [`design/build-poulpy-3d.mjs`](../design/build-poulpy-3d.mjs) writes it as glTF files. He is not in the apps yet: this is the reference to build from.

<!-- live: Model -->

## The rules of the shape

These were settled with the owner on the first model; the reasons are in [Brand decisions](./brand-decisions.md#14-poulpy-in-3d-a-bubble-plush-in-flat-shading).

- **Chibi, never realistic.** A big round head, big eyes, short arms. The face is the 2D drawing's, pressed onto the head, so every [drawing](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/brand-drawings--docs) and the model show the same Poulpy.
- **The head is tucked in.** It sinks into the arms, which wrap around its base, instead of resting on top of them.
- **Eight arms, always down.** They lie on the floor as on the plush; a mood shows on his face and in his colour, never by lifting an arm. When he [jumps](#little-jumps) they hang under him.
- **The other side is underneath.** Each arm is one smooth shape, the side's colour on top and the other side's colour below a low line, with no ridge or shelf between them. The base of the head is cut at the same height, so the whole underside is one colour: ember under the blue side, blue under the ember side.
- **Flat, like a drawing.** Plain token colours, one soft shadow band and one faint gleam. No gloss, no reflections, no fabric texture.
- **The light belongs to the room.** It comes from the viewer's upper left and stays there: the shade and the gleam slide over Poulpy as he turns, they are never painted on him.
- **The outline is optional and off by default.** It is there for small sizes or busy backgrounds.

## Turning in short steps

His idle move: Poulpy likes to turn around on the spot, a short step of 20 to 30 degrees at a time, stopping after each one. Fifteen steps bring him all the way around, in about 17 seconds, and then he goes again.

<!-- live: Turning -->

His arms are not stiff. Each one bends at three places, and every bend is a small spring: when he steps the arms trail behind, when he stops they swing a little past him and settle, the tips last. The controls of the turning model are there to try other feels, and each shows its value so a setting can be quoted and written into the design. **Step** sets how far he turns each time, from 1 to 180 degrees, keeping the small differences between one step and the next; 24 is the design. **Soft arms** loosens or stiffens those springs: 1 is the design, 0 is the stiff plush with no springs at all and 4 is very floppy. **Speed** plays the whole move faster or slower, 1 being the design. The two arrows pick the direction, seen from above; the design turns anticlockwise, to his left.

- **The arms slide, they never lift.** Every bend turns flat on the floor, so the move keeps the plush's shape as the [brand rules](./brand.md#character-rules) ask.
- **He winds up, then goes.** A small lean back before each step, a quick turn, and a slight rock past the mark as he stops.
- **The head dips as he turns** and bounces back, by a few hundredths of its height, widening to keep its volume. His face rides along with it.
- **No two steps or arms are quite alike.** The steps vary between 20 and 30 degrees and the pauses between 0.45 and 0.75 seconds, and each arm's springs are a little stiffer or softer than its neighbours', so the arms fall out of step with each other.
- **Reduced motion keeps him still.** The preview starts paused for anyone who asked their device for less motion, and an app must do the same.

The moves are one file, [`design/poulpy-3d/motion.mjs`](../design/poulpy-3d/motion.mjs), next to the shape. `TURN` at its top holds every number: the steps and pauses, the wind-up, how eager and how bouncy his body is, how soft each length of arm is, how much the floor holds the arms back and how far the head dips. `createTurn()` runs it live, as the workshop's page does, and `bakeTurn()` writes it down frame by frame for the glTF files.

## Little jumps

Poulpy can also jump on the spot. He tucks his head down into his arms, then goes up head first: his arms follow a little, just enough for him to leave the floor. In the air he tilts to one side, the other side at the next jump, and as he lands his head goes a little down into his arms before he stands again.

<!-- live: Jumping -->

**The jump is a preview, still being tuned.** It runs live here and is not in the glTF files yet: its values are written into them once the owner has settled them on this page. Every control shows its value so a setting can be quoted. Heights are in the model's units, where his head is about 1 from centre to edge.

| Control | What it sets | Design |
| --- | --- | --- |
| **Height** | How far his head goes up | 0.36 |
| **Lift** | How far he leaves the floor: the base of his arms follows his head by this much | 0.10 |
| **Tuck** | How far his head goes down into his arms before the jump | 0.16 |
| **Landing** | How far his head goes down into his arms as he lands | 0.10 |
| **Lean** | How far he tilts to the side in the air, in degrees | 7 |
| **Pause** | Seconds he stands between two jumps, on average | 0.55 |
| **Soft arms** | How loosely his arms hang and trail, as for the turn | 1 |
| **Speed** | Plays the whole move faster or slower | 1 |

- **His head leads, his arms follow.** His head is pulled along the jump by a stiff spring and the base of his arms by a softer one, so the arms leave the floor after his head and land after it. In between his head comes a little out of them.
- **The arms hang, he never raises them.** Up and down, each arm bends at the same three places as when he turns. A spring holds each length level with the one before it, its weight pulls it down and the floor stops it: the tips leave the floor last, hang a little in the air and touch down first. On the side he tilts to, the arms lie flat against the floor.
- **His head squashes and stretches.** It widens as it tucks in and as he lands, and thins a little while it moves fast, keeping its volume.
- **Reduced motion keeps him still**, as for the turn.

### Jumping around

The same jump can turn him: each jump takes him one step around, the steps being those of his turn. He winds up while he tucks, turns in the air and his arms trail behind as they do on the floor.

<!-- live: JumpingAround -->

**Step** and the two arrows work as for the turn; every other control is the jump's.

`JUMP` in `motion.mjs` holds the jump's numbers, next to `TURN`, and `createJump()` runs it live. Given a `stride`, it turns him as `createTurn()` does.

## Using the model

| File | What it is |
| --- | --- |
| [poulpy_blue.glb](../design/poulpy-3d/poulpy_blue.glb) | The blue, smiling side, as binary glTF |
| [poulpy_ember.glb](../design/poulpy-3d/poulpy_ember.glb) | The ember, grumpy side, as binary glTF |
| [poulpy_blue_turn.glb](../design/poulpy-3d/poulpy_blue_turn.glb) | The blue side with the animation `turn`: one full turn in short steps, made to loop |
| [poulpy_ember_turn.glb](../design/poulpy-3d/poulpy_ember_turn.glb) | The ember side with the same animation |
| [`design/poulpy-3d/model.mjs`](../design/poulpy-3d/model.mjs) | The shape itself, as plain JavaScript with no dependency |
| [`design/poulpy-3d/motion.mjs`](../design/poulpy-3d/motion.mjs) | How he moves, the same way |

The glTF files open in any 3D tool or engine. Poulpy stands on the floor (y = 0, y up) and faces +z; his head is about one unit from centre to edge. In the two files that stand still, each part is its own named mesh (`head`, `head_lining`, `arm_1` to `arm_8` and their `_lining`, `eye_left`, `eye_left_shine`, `cheek_left`, `mouth`, `gleam`, plus `brow_left` and `brow_right` on the ember side), so a part can be recoloured, hidden or animated on its own.

The two `_turn` files hold the same meshes, rigged. The node `turn` faces him and `squash` under it carries his head and face; each arm is skinned to `turn` and to its own chain of three joints, `arm_1_bend_1` to `arm_1_bend_3` from the head outwards. The animation `turn` plays one full turn at 30 frames a second and ends where it starts, so any player can loop it. It is a recording of the springs at their design values: an engine that wants them live, to change the softness or to react to something, runs `createTurn()` from `motion.mjs` on the same joints, as the workshop's preview does.

A glTF file only carries shapes and plain matte colours. The look is the renderer's job, and `LOOK` in `model.mjs` gives its numbers: the light's direction as the viewer sees it, the shadow band's strength, where the gleam sits, and the outline's width and tint. A renderer that wants the gleam to follow the light drops the `gleam` mesh from the file and asks `gleamPatch(direction)` for a new one whenever Poulpy or the camera turns. [`poulpy3dScene.ts`](../web/storybook/stories/brand/poulpy3dScene.ts), which draws the workshop's preview with three.js, is the worked example.

## Changing the design

Everything that defines him is at the top of `model.mjs`, in three plainly named objects:

- `SHAPE`: the size of the head and how deep it sits, the number, size and reach of the arms, the height of the line between the two colours, and where the face sits.
- `FACES`: the eyes, cheeks, mouth and brows of each side, in the same units as the 2D drawings of `design/build-poulpy.mjs`.
- `LOOK`: the light, the shadow band, the gleam and the outline.

How he moves is in `motion.mjs`: `TURN` for his turn, `JUMP` for his jump and `RIG` for where the arms bend.

His colours are the `color.poulpy` tokens of [`design/tokens.json`](../design/tokens.json), never written into the model.

Edit, then regenerate and look at the workshop's page (`npm run storybook` in `web/`):

```sh
node design/build-poulpy-3d.mjs
```

CI runs it with `--check` and fails when the committed glTF files no longer match `model.mjs` and `motion.mjs`. A new mood or pose in 3D starts as a new entry in `FACES`, or a new part in `buildPoulpy3d`; a new move starts next to `TURN` and `JUMP` in `motion.mjs`. Both follow the [brand rules](./brand.md#character-rules) like any drawing.

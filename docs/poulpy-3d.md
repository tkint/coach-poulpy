# Poulpy in 3D

The plush as a solid you can turn around: a bubble of a head tucked into eight puffy arms, drawn with flat cartoon shading. One file, [`design/poulpy-3d/model.mjs`](../design/poulpy-3d/model.mjs), describes his shape; the [Poulpy in 3D](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/brand-poulpy-in-3d--docs) page of the component workshop draws it live, with a page for each of [his moves](#his-moves), and [`design/build-poulpy-3d.mjs`](../design/build-poulpy-3d.mjs) writes it as glTF files. He is not in the apps yet: this is the reference to build from.

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

## His six moods

The model makes the six [moods](./brand.md#moods) of the drawings: the same faces, pressed onto the head feature by feature, and the same shades, from the `mood` tokens. The side of the plush follows the mood, so the three ember moods show blue underneath.

<!-- live: Moods -->

- **The faces are the drawings'.** Each face in `model.mjs` is written in the 2D drawing's own coordinates, so the two can be read side by side and a change to one is copied to the other.
- **A mood changes his face and his shade, never his shape.** Heavy lids, brows, a smirk or clenched teeth lie on the head like the eyes do; his arms stay down.

## His twelve poses

The model also takes the twelve [poses](./brand.md#when-each-drawing-shows) of the drawings, each in the shade of its mood. They are in [`design/poulpy-3d/poses.mjs`](../design/poulpy-3d/poses.mjs), built on the shape and the faces of `model.mjs`.

<!-- live: Poses -->

- **What he wears is solid.** The sweatband goes all the way around his head, the nightcap sits on it, the medal rests on his front arm and the dumbbell lies on the floor beside him: turn him and they turn with him.
- **What is on him is on his skin.** The plasters and the vein are laid against his head where the drawing has them and wrapped around it; sweat and tears are part of his face. Turn him and they stay where they are.
- **The comic effects stay flat and face the viewer.** Sparks, confetti, puffs, bubbles and lettering are the drawing's own, standing in front of him or behind him, and they keep facing whoever looks while he turns.
- **Cocky has no halo and no "PR!" badge in 3D.** The owner dropped them; his sparks stay, and become solid stars when he [winks](#the-cocky-one-winks).
- **He still never lifts an arm.** Standing tall stretches his arms taller and lifts his head with them; Side glance leans his head. Nothing else moves.
- **The loader is his outline from above, with loose tentacles.** He is seen from straight overhead and only the edge of his shape is drawn, in the brand blue and see-through, as the old loader was: the flower of his eight tentacles round his head with no line inside, rounded, and a line bold enough for the bases of neighbouring tentacles to touch. The picture is one tentacle, drawn eight times. He swings half a turn one way and then half a turn the other way (from -180° to 180°), forever, once every 3.6 s (once every 12 s with reduced motion), easing at each end, and each tentacle hangs loose: it bends about its base behind the turn, by most when he turns fastest, and each follows a little later than the one before. The other version only turns clockwise, a full turn every 3.6 s, with the tentacles trailing behind at a steady lag (`clockwise` on the phones, `spin="clockwise"` on the web). The motion is `LoaderSwing` in the shared Kotlin, and `PoulpyLoader` on Android and iOS, `MobilePoulpyLoader` and `CpPoulpyLoader` in the workshop all follow it.

## His pictures

Where the apps and the website do not need him live, they take a picture of him: one for each of the six moods and twelve poses, painted from the model, and one more for the loader. They are in [`design/poulpy-3d/pictures`](../design/poulpy-3d/pictures), one PNG each at 744 by 678 pixels, the same frame as the drawings they replace so a picture can take a drawing's place without moving anything. **They are transparent around him**, with no background and no tile, so they sit on a light or a dark screen alike; the preview below shows them on the page's own background.

<!-- live: Pictures -->

- **They are painted by our own script.** [`design/poulpy-3d/picture.mjs`](../design/poulpy-3d/picture.mjs) draws what `model.mjs` and `poses.mjs` build, pixel by pixel, in plain JavaScript with the look of `LOOK`: the same flat shading, gleam and floor shadow as the live view. No browser or graphics card is involved, so every machine writes the same pixels, as for the app icons.
- **Every picture is taken from the same place.** He is seen from a little above and face on, close enough to fill the frame, and the same distance for all of them so he is the same size from one to the next.
- **The floor shadow is part of the picture**: a see-through dark blue that fades to nothing, so it shows on a light screen and disappears on a dark one.
- **Generated, never edited.** `node design/build-poulpy-pictures.mjs` writes them and CI runs it with `--check`. Change the model, a face or a pose, then regenerate.

## His moves

He moves in 3D too. Each move has its own page in the component workshop, so only one runs at a time:

- [Turning in short steps](#turning-in-short-steps): his idle.
- [Little jumps](#little-jumps) on the spot.
- [Jumping around](#jumping-around): the same jumps, each turning him.
- [The champion jumps for joy](#the-champion-jumps-for-joy), his medal bouncing and confetti flying.
- [The cocky one winks](#the-cocky-one-winks), and stars pop up around him.

All of them are one file, [`design/poulpy-3d/motion.mjs`](../design/poulpy-3d/motion.mjs), next to the shape. A preview only moves while it is on screen, and starts paused for anyone who asked their device for less motion; an app must do the same.

## Turning in short steps

His idle move: Poulpy likes to turn around on the spot, a short step of 20 to 30 degrees at a time, stopping after each one. Fifteen steps bring him all the way around, in about 17 seconds, and then he goes again.

<!-- live: Turning -->

His arms are not stiff. Each one bends at three places, and every bend is a small spring: when he steps the arms trail behind, when he stops they swing a little past him and settle, the tips last. The controls of the turning model are there to try other feels, and each shows its value so a setting can be quoted and written into the design. **Step** sets how far he turns each time, from 1 to 180 degrees, keeping the small differences between one step and the next; 24 is the design. **Soft arms** loosens or stiffens those springs: 1 is the design, 0 is the stiff plush with no springs at all and 4 is very floppy. **Speed** plays the whole move faster or slower, 1 being the design. The two arrows pick the direction, seen from above; the design turns anticlockwise, to his left.

- **The arms slide, they never lift.** Every bend turns flat on the floor, so the move keeps the plush's shape as the [brand rules](./brand.md#character-rules) ask.
- **He winds up, then goes.** A small lean back before each step, a quick turn, and a slight rock past the mark as he stops.
- **The head dips as he turns** and bounces back, by a few hundredths of its height, widening to keep its volume. His face rides along with it.
- **No two steps or arms are quite alike.** The steps vary between 20 and 30 degrees and the pauses between 0.45 and 0.75 seconds, and each arm's springs are a little stiffer or softer than its neighbours', so the arms fall out of step with each other.
- **Reduced motion keeps him still.** The preview starts paused for anyone who asked their device for less motion, and an app must do the same.

`TURN` at the top of `motion.mjs` holds every number: the steps and pauses, the wind-up, how eager and how bouncy his body is, how soft each length of arm is, how much the floor holds the arms back and how far the head dips. `createTurn()` runs it live, as the workshop's page does, and `bakeTurn()` writes it down frame by frame for the glTF files.

## Little jumps

Poulpy can also jump on the spot. He tucks his head down into his arms, which widen flat on the floor, then he goes up. His arms are part of him: their base goes up with his head and their tips hang a little lower, the last to leave the floor. In the air he tilts to one side, the other side at the next jump, and as he lands his head goes a little down into his arms before he stands again.

<!-- live: Jumping -->

**The jump is a preview, still being tuned.** It runs live here and is not in the glTF files yet: its values are written into them once the owner has settled them on this page. Every control shows its value so a setting can be quoted. Heights are in the model's units, where his head is about 1 from centre to edge.

| Control | What it sets | Design |
| --- | --- | --- |
| **Height** | How far he goes up | 0.34 |
| **Tuck** | How far his head goes down into his arms before the jump | 0.16 |
| **Landing** | How far his head goes down into his arms as he lands | 0.10 |
| **Spread** | How far his arms slide outwards when his head is tucked all the way in | 0.14 |
| **Hang** | How heavy his arms are, so how low their tips hang in the air | 1 |
| **Lean** | How far he tilts to the side in the air, in degrees | 7 |
| **Pause** | Seconds he stands between two jumps, on average | 0.55 |
| **Soft arms** | How loosely his arms hang and trail, as for the turn | 1 |
| **Speed** | Plays the whole move faster or slower | 1 |

- **His head never leaves his arms.** It goes down into them, to tuck and to land, and never up out of them: when he goes up they go with him.
- **His arms widen as his head comes down.** They stay flat on the floor and slide outwards, as a plush does when it is pressed.
- **The arms hang, he never raises them.** Up and down, each arm bends at the same three places as when he turns. A spring holds each length level with the one before it, its weight pulls it down and the floor stops it: the tips leave the floor last, hang under him in the air and touch down first. A length may hang nearly straight down when it has to. On the side he tilts to, the arms lie flat against the floor.
- **His head squashes and stretches.** It widens as it tucks in and as he lands, and thins a little while it moves fast, keeping its volume.
- **Reduced motion keeps him still**, as for the turn.

`JUMP` in `motion.mjs` holds the jump's numbers, next to `TURN`, and `createJump()` runs it live.

## Jumping around

The same [jump](#little-jumps) can turn him: each jump takes him one step around, the steps being those of his turn. He winds up while he tucks, turns in the air and his arms trail behind as they do on the floor.

<!-- live: JumpingAround -->

**Step** and the two arrows work as for the turn, and here Step goes up to 720 degrees: at 360 a jump is a whole turn, at 720 two. The steps keep their small differences, so he does not land facing quite the same way each time. Every other control is the jump's.

Given a `stride`, `createJump()` turns him as `createTurn()` does.

## The champion jumps for joy

The Champion pose, moving: he cannot hold still. His jumps are the [little jumps](#little-jumps), higher, leaning more and closer together, and two things answer them.

<!-- live: Cheering -->

- **His medal has its own weight.** It hangs on its ribbon from under his head and rests on his front arm. When he drops it floats up off him, then falls back on his arm with a small bounce; when he tilts it slides to that side and wobbles back. Nothing is drawn by hand: it is a weight on a hinge, pulled down and stopped by his arm.
- **Confetti flies at each jump.** A burst leaves the floor around him as he does, goes up past his head and flutters down, tumbling. It lands flat and stays on the floor until it flies again, so the floor fills up as he goes on. The pieces are solid here and belong to the room: turn the view and they stay where they are. The still pose keeps its flat confetti.
- **Every run is the same.** Where each piece starts and how it tumbles comes from a fixed list, not from chance, so the move can be written into a file later.
- **He still never lifts an arm.** His joy is in the jump, his face, the medal and the confetti.

The controls are the jump's, starting from the champion's own values: Height 0.50, Lean 9 and Pause 0.26. `CHEER` in `motion.mjs` holds those, the medal's weight, bounce and reach and the confetti's numbers, and `createCheer()` runs it live. Like the jump it is a preview, not in the glTF files yet.

## The cocky one winks

The Cocky pose, moving. He looks at you with both eyes wide open, then shuts one eye (his right, unless told otherwise), lifts the other brow and his mouth slides into the cocky smirk. He holds it, opens his eye and starts again.

<!-- live: Winking -->

- **His face moves, feature by feature.** The eye narrows to a line and the line bends into the wink; the brow and the mouth go from plain to cocky with it. `winkFace()` in `poses.mjs` gives his face at any point between the two, in the drawing's coordinates like every face.
- **His head goes with it.** It leans a little to the side of the eye he shuts and dips, on a spring, then comes back.
- **The stars come with the wink.** There are none while his eyes are open. As the eye shuts they pop up one after the other, each a little past its size and back, and they leave when he opens it.
- **The stars are solid and they twinkle.** Each is a four-point star cut like a gem, in the room around him where the drawing has its sparks. It grows and shrinks, turns on itself and rocks, and no two keep the same time. Turn the view and they stay where they are.
- **No halo, no badge.** The drawing's glow and its "PR!" are not part of him in 3D.

The switch picks the eye he shuts, named for him: his right eye, the design, is on your left. His brow, his smirk and the lean of his head go to the other side with it.

`WINK` in `motion.mjs` holds the timing, the lean of his head and the stars' numbers, and `createWink()` runs it live. It is a preview, not in the glTF files yet.

## Using the model

| File | What it is |
| --- | --- |
| [poulpy_blue.glb](../design/poulpy-3d/poulpy_blue.glb) | The blue, smiling side, as binary glTF |
| [poulpy_ember.glb](../design/poulpy-3d/poulpy_ember.glb) | The ember, grumpy side, as binary glTF |
| [poulpy_blue_turn.glb](../design/poulpy-3d/poulpy_blue_turn.glb) | The blue side with the animation `turn`: one full turn in short steps, made to loop |
| [poulpy_ember_turn.glb](../design/poulpy-3d/poulpy_ember_turn.glb) | The ember side with the same animation |
| [`design/poulpy-3d/model.mjs`](../design/poulpy-3d/model.mjs) | The shape itself, as plain JavaScript with no dependency |
| [`design/poulpy-3d/poses.mjs`](../design/poulpy-3d/poses.mjs) | His twelve poses, the same way |
| [`design/poulpy-3d/motion.mjs`](../design/poulpy-3d/motion.mjs) | How he moves, the same way |

The glTF files open in any 3D tool or engine. Poulpy stands on the floor (y = 0, y up) and faces +z; his head is about one unit from centre to edge. In the two files that stand still, each part is its own named mesh (`head`, `head_lining`, `arm_1` to `arm_8` and their `_lining`, `eye_left`, `eye_left_shine`, `cheek_left`, `mouth`, `gleam`, plus `brow_left` and `brow_right` on the ember side; left and right are Poulpy's own), so a part can be recoloured, hidden or animated on its own.

The two `_turn` files hold the same meshes, rigged. The node `turn` faces him and `squash` under it carries his head and face; each arm is skinned to `turn` and to its own chain of three joints, `arm_1_bend_1` to `arm_1_bend_3` from the head outwards. The animation `turn` plays one full turn at 30 frames a second and ends where it starts, so any player can loop it. It is a recording of the springs at their design values: an engine that wants them live, to change the softness or to react to something, runs `createTurn()` from `motion.mjs` on the same joints, as the workshop's preview does.

A glTF file only carries shapes and plain matte colours. The look is the renderer's job, and `LOOK` in `model.mjs` gives its numbers: the light's direction as the viewer sees it, the shadow band's strength, where the gleam sits, and the outline's width and tint. A renderer that wants the gleam to follow the light drops the `gleam` mesh from the file and asks `gleamPatch(direction)` for a new one whenever Poulpy or the camera turns. [`poulpy3dScene.ts`](../web/storybook/stories/brand/poulpy3dScene.ts), which draws the workshop's preview with three.js, is the worked example.

## Changing the design

Everything that defines him is at the top of `model.mjs`, in three plainly named objects:

- `SHAPE`: the size of the head and how deep it sits, the number, size and reach of the arms, the height of the line between the two colours, and where the face sits.
- `FACES`: the eyes, lids, brows, mouth and cheeks of each face, feature by feature, in the coordinates of the 2D drawings of `design/build-poulpy.mjs`.
- `MOODS`: for each of his six moods, the side of the plush and the face he makes. `buildPoulpy3d({ mood, tone })` builds him in one, tinted with that mood's `mood` token.
- `LOOK`: the light, the shadow band, the gleam and the outline.

His poses are in `poses.mjs`: `POSES` gives each one its mood, its face, what he wears, what lies on his head and the effects around him, all in the drawing's coordinates, and `buildPoulpyPose({ pose })` builds it.

How he moves is in `motion.mjs`: `TURN` for his turn, `JUMP` for his jump, `CHEER` for the champion's, `WINK` for the cocky one's and `RIG` for where the arms bend.

His colours are the `color.poulpy` tokens of [`design/tokens.json`](../design/tokens.json), never written into the model.

Edit, then regenerate and look at the workshop's page (`npm run storybook` in `web/`):

```sh
node design/build-poulpy-3d.mjs
```

CI runs it with `--check` and fails when the committed glTF files no longer match `model.mjs` and `motion.mjs`. A new mood in 3D starts as a new entry in `FACES` and `MOODS`, a new pose as a new entry in `POSES`; a new move starts next to `TURN`, `JUMP`, `CHEER` and `WINK` in `motion.mjs`, with a page of its own in the workshop. Both follow the [brand rules](./brand.md#character-rules) like any drawing.

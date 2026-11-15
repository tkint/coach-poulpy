# Brand decisions

What was decided about Poulpy and the brand, why, and when. The [brand page](./brand.md) and the brand book say what Poulpy is today; this page keeps the reasons, so a later change can see what it overturns. Architecture decisions live in [`docs/adr/`](./adr/) instead.

Add a decision here in the same PR as the brand change it explains, newest at the bottom. When a decision is overturned, keep it, mark it superseded and link the one that replaces it.

## 1. Logo A: the plush as he is

**Accepted, 24 October 2026.** The logo is a faithful front view of the plush: tall dome, five visible arms and the ember lining peeking out underneath. The logo and the in-app mascot are the same drawing.

**Why:** it is Poulpy himself, the one real plush the app is built around, and one drawing for both keeps him recognisable everywhere.

**Set aside:** B, Poulpy split down the middle with one happy and one grumpy half; C, a letter P made from his head and a curled arm.

## 2. Baloo 2 for headings, Nunito for text

**Accepted, 24 October 2026.** Baloo 2 for headings, Poulpy's speech and big moments; Nunito for body text, labels and numbers.

**Why:** both are round like the plush, and both are free (SIL Open Font License), so they ship inside the apps and load from Google Fonts on the web. Nunito has tabular figures, so timers and rep counts don't jitter while they tick.

## 3. The grumpy side is ember, not pink

**Accepted, 24 October 2026.** The real plush's grumpy side is pink; in the brand it is ember, from burnt orange to red, and the lining under the blue side follows it. Errors move to a crimson (`#C8264A`) and always carry an icon and text.

**Why:** the owner wanted a grumpy side that reads redder and less pink. Crimson errors with an icon keep a cross Poulpy from ever being mistaken for an error.

## 4. Burnt orange is the ember

**Accepted, 24 October 2026.** The everyday grumpy shade is burnt orange, `#D9622B`. Buttons use the deeper ember 600 (`#B4471C`), because the plush ember is too light for white text.

**Why:** warm, playful and clearly not an error, and it has room to deepen into red for Fuming.

**Set aside:** brick (`#C2452D`), moodier and with less room to go redder; vermilion (`#E4572E`), loud and the closest to a warning colour.

## 5. The side is the status, the shade is the feeling

**Accepted, 24 October 2026.** Which side shows says whether you are keeping your plan; the shade on that side moves with what just happened.

**Why:** the owner asked for shades that change with accomplishments and emotions, and real octopuses change colour with their mood.

## 6. Six moods, three blues, no grey

**Accepted, 24 October 2026.** The spectrum is Proud, Happy and Recovering on the blue side; Annoyed, Grumpy and Fuming on the ember side. Tired, Calm and rest merged into one light blue, Recovering. Proud sits just next to Happy.

**Why:** the first spectrum's grey Tired felt uneasy, even sick, and there were too many blues. Proud read too far from Poulpy's own colour.

## 7. No hearts; a grace period instead

**Accepted, 24 October 2026.** The "three hearts a month" idea, which let you miss sessions without Poulpy turning grumpy, is dropped. Instead a session still due by the evening only makes Poulpy Annoyed, and he turns Grumpy 24 hours after its day.

**Why:** the owner kept every game idea except the hearts. The grace period gives the same slack without something to count down.

## 8. Poulpy never lifts his tentacles

**Accepted, 24 October 2026.** Every pose keeps the plush's shape and shows the mood with his face, shade, props and small effects. The poses that needed arms were renamed for what they show: Flexing became Cocky, Lifting became Straining, Hiding his eyes became Shy, Head scratch became Thinking and Arms crossed became Pouting.

**Why:** Poulpy has eight tentacles and no arms, so the first poses with added arms were wrong. Lifting his own tentacles was tried next, first long, then cut to the plush's real length, and still didn't look like him.

## 9. Poses follow the real plush

**Accepted, 24 October 2026.** The pose sheet stays close to how the plush actually sits and moves:

- Standing tall, Side glance, Shy, Thinking and Spinning come from photos of the plush.
- Standing tall is the usual plush with the base of his tentacles slightly stretched, with no gaps.
- Spinning, the loading indicator, is drawn comic-style with motion lines, and only the front arc moves.
- Champion has no arms, Sleeping has a short upright nightcap and no bed, Patched up has no bubble, and Exhausted has no puddle.

**Why:** each was a correction from the owner. The first Standing tall showed gaps around his body, and the first Spinning flattened him like a card instead of turning him.

## 10. Colours generated from one tokens file

**Accepted, 24 October 2026.** Once the colours were validated, they went into `design/tokens.json` and are generated into each platform's theme. See [ADR 0012](./adr/0012-design-tokens-generated-from-one-file.md).

## 11. Tentacles never reset

**Accepted, 24 October 2026.** Each finished session lights one of the eight tentacles, and a lit tentacle never goes dark. The eighth makes a full octopus and the next session starts a new ring. This replaces "eight tentacles a week", where the ring emptied every Monday.

**Why:** eight sessions a week is a lot, and a weekly reset punishes the people who train least. A ring that only fills keeps Poulpy positive for them.

## 12. The ember-side rules

**Accepted, 26 October 2026**, for [#17](https://github.com/tkint/coach-poulpy/issues/17). The [brand page](./brand.md#the-ember-side) sets when Poulpy turns ember. Annoyed runs from 18:00 on an entry's day to the end of the next day; the entry is missed after that, which makes him Grumpy, and two missed entries make him Fuming. Only plan calendar entries count, never planned workouts placed by hand. Skipped entries never count. Any finished session forgives every entry dated on or before its day. Entries inside a 72 h pain or rehab window never count. The catch-up is the plan's shortest workout and leaves missed entries as they are.

**Why:** decision 7 promised a grace period "until 24 hours after its day", and reading it as the whole next day gives a person who trains in the morning a real second chance. Counting only what is still planned keeps the ember side for sessions left hanging, not for honest choices: a skip, a move, a rest day or pain. Forgiving by date rather than by matching each entry keeps "any finished session flips him straight back" literally true.

**Set aside:** a skip counting as missed, which makes skipping feel like a confession; the catch-up marking a missed entry done, which rewrites History.

## 13. Inclusive by default

**Accepted, 1 November 2026.** The owner wants the app to be LGBTQ+ friendly as part of the brand. The [brand page](./brand.md#inclusive-by-default) turns that into rules: optional fields with a stated reason, **Sex assigned at birth** asked only where an estimate needs it, pronouns as a separate field, neutral wording, no body judgement.

**Why:** "sex assigned at birth" is the wording most inclusive-forms guides recommend for a physiological question, because it describes what was recorded rather than who someone is, and it keeps gender out of it; "biological sex" and "birth sex" read as defining a person and are the terms trans and intersex people most often object to. The app also needs no gender at all: sex assigned at birth drives estimates, and pronouns drive wording.

**Set aside:** one "Gender" field with Male, Female and Other (which mixes the two questions and makes "Other" the label for everyone outside two boxes); free-text sex (cannot feed an estimate); asking nothing (the estimates become less accurate for people who are happy to say). The wording was chosen from published guides and has **not yet been read by LGBTQ+ people**: that read is still to do, and the wording changes if it asks for it.

## 14. Poulpy in 3D: a bubble plush in flat shading

**Accepted, 14 November 2026.** Poulpy's 3D form is a round head tucked into eight puffy arms, drawn with flat cartoon shading. The rules of the shape are on the [Poulpy in 3D](./poulpy-3d.md) page, and the shape is one file, [`design/poulpy-3d/model.mjs`](../design/poulpy-3d/model.mjs), so it can be reused and improved like the 2D drawings.

**Why:** the owner wanted him chibi, natural and simple, "a bit like the mascot of Duolingo", so the model had to read like the 2D drawings turned into a solid, not like a rendered toy. Each rule was a correction from the owner on a model he could turn: the head sinks into the arms because resting on top of them looked stacked; the two colours share one smooth surface because a separate lining made a shelf; the whole underside is the other side's colour because a patch of the top colour showed between the arms; and the light stays in the room because shade painted on the model turned with him.

**Set aside:** a dome head on puffy arms, the 2D drawing extruded (the head he preferred was rounder); a glossy vinyl ball on tiny feet (too photorealistic, arms too short); an egg head on long curling arms (less chibi, and too close to a real octopus); a one-piece bell with a scalloped hem. A dark outline is kept as an option, off by default.

## 15. Poulpy jumps, and his tentacles hang

**Accepted, 15 November 2026.** In 3D Poulpy can do little jumps, on the spot or turning a step at each. He tucks his head into his tentacles, which widen flat on the floor, then goes up; his tentacles stay joined to his head and their tips hang a little lower, barely off the floor. He tilts to one side in the air, and his head goes a little down into his tentacles as he lands. The move and its controls are on the [Poulpy in 3D](./poulpy-3d.md#little-jumps) page.

This narrows [decision 8](#8-poulpy-never-lifts-his-tentacles) without overturning it: Poulpy still never raises a tentacle himself, as an arm would. In a jump they leave the floor only because he does, hanging soft under him, tips last.

**Why:** the owner asked for the jump and described it. The first preview let his head come up out of his tentacles; he corrected it: the head is always joined to them, and they may hang much steeper instead. Turning, one jump may be a whole turn, or two. It is kept small so he stays the plush: a hop that barely clears the floor, not a leap.

**Still open:** the jump's values (how high, how deep the tuck and the landing, how far the tentacles widen and hang, how much tilt, how long between jumps). It runs as a live preview with a control for each, and is written into the glTF files once the owner has settled them.

## 16. The 3D Poulpy replaces the drawings

**Accepted, 15 November 2026.** The 2D drawings (six mood portraits, twelve poses, the loader and the app icons) are to be replaced by the 3D Poulpy. Everywhere a drawing shows today, a picture rendered from the model takes its place, generated like the drawings are; on the key screens, Home and the end of a session, he is live and animated instead.

It is done in steps, each reviewed on the [Poulpy in 3D](./poulpy-3d.md) page before the apps change:

1. The six moods on the model: faces and shades. Done with this decision.
2. The twelve poses and their props (done), then the loader and the app icons.
3. Pictures rendered from the model, generated for Android, iOS and the website in place of the drawings. The pictures are generated and shown on the page, and the website, the docs and both apps use them: Android takes PNGs under the same `poulpy_<name>` names, iOS takes 3x imagesets under the same asset names.
4. Live 3D on Home and on the end-of-session screen, each from an agreed mockup.

`design/build-poulpy.mjs` stays the source of the loader and the app icons, which are still drawings.

**Why:** the owner asked for it once the model could turn and jump, and chose pictures everywhere with live 3D on key screens over a 3D renderer on every screen, which would weigh on the apps for little gain on small portraits.

**Settled:** the pictures are painted by our own script in plain JavaScript (`design/poulpy-3d/picture.mjs`), so every machine writes the same pixels and CI can check them, as for the app icons. The owner chose it over photographing the workshop in a browser, whose pixels differ from machine to machine, and over tracing vector pictures, which would take far longer and flatten the shading.

## 17. In 3D, what is on Poulpy stays on him, the champion cheers and the cocky one winks

**Accepted, 15 November 2026.** Two rules for the 3D poses, and two moves:

- Sweat and tears are part of his face, and plasters or a vein are wrapped onto his head. They stay on his skin when he turns. Only what is around him (sparks, puffs, bubbles, lettering) is flat and faces the viewer.
- The champion has a move: he jumps for joy, his medal swings on its ribbon as a weight would and confetti flies at each jump, then lies on the floor. It is on [its own page](./poulpy-3d.md#the-champion-jumps-for-joy).
- The cocky one has a move: he starts with both eyes wide open, then winks with his slight cocky smile. His stars appear with the wink, solid and twinkling. In 3D he has no halo and no "PR!" badge. It is on [its own page](./poulpy-3d.md#the-cocky-one-winks).
- Each move has a page of its own in the component workshop, and a preview only moves while it is on screen.

**Why:** the owner asked for each on seeing the twelve poses in 3D. Drops that faced the viewer slid over him as he turned, where sweat should stick; the plasters were pressed on straight from the front and dipped under his skin near the edge of his head; and several moves running on one page made it lag. For the cocky one he asked for the wink, for stars that move in 3D instead of a still drawing, and to drop the badge and the halo.

**Still open:** the cheer's and the wink's values, like the jump's. They are live previews, not in the glTF files yet.

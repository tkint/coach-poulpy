# Brand

Poulpy is a reversible plush octopus: blue and smiling on one side, grumpy on the other. In the app the grumpy side is ember, from burnt orange to red. **The side is the status** (are you keeping your plan?) **and the shade is the feeling** (what just happened). The full brand book v6, with the logo, colour choices and every drawing, is on the site's Brand book page.

## Character rules

- **Grumpy is comic, never guilt.** No "you failed", no streak-loss alarms. Even Fuming is a cartoon tantrum.
- **Pain and rehab always win.** Someone who logged pain gets the Patched up pose and the blue side for 72 h, never ember.
- **One way back, always short.** While Poulpy is ember, the catch-up offered is the easiest session in the plan, and any finished session flips him straight back.
- **Nothing promised, nothing missed.** With no plan, Poulpy stays on the blue side and only reacts to when you last trained.
- **Poulpy never lifts his tentacles.** Every pose keeps the plush's shape and shows the mood with his face, shade, props and small effects.
- **At most one notification a day** in Poulpy's voice, and turning them off keeps the mascot.
- **Every line in French and English** from the start.

## Moods

| Mood | Side | When |
| --- | --- | --- |
| Proud | Blue | A personal record, or a full octopus |
| Happy | Blue | A session done, plan on track; the default |
| Recovering | Blue | Rest days, the hours after a hard session, pain or rehab days |
| Annoyed | Ember | Today's planned session isn't done and the evening is coming (the grace period) |
| Grumpy | Ember | A planned session still not done 24 h after its day |
| Fuming | Ember | Two planned sessions missed |

## When each drawing shows

| What happens | Mood | Drawing |
| --- | --- | --- |
| You finish any session | Happy | Flip back to blue with the flip animation; the flip is the reward |
| You set a record | Proud | Cocky, until the end of the day |
| The eighth tentacle lights | Proud | Champion, on the end-of-session screen and Home |
| A hard session ends (effort 8 or more) | Recovering | Exhausted or Side glance, for the rest of the day |
| A planned rest day | Recovering | Sleeping |
| Pain logged, or a rehab session marked too hard | Recovering | Patched up |
| Mid-set, heavy sets | Current | Straining |
| A new goal set | Current | Standing tall |
| Back after a break | Current | Shy |
| Picking a plan, an unknown exercise | Current | Thinking |
| Today's session not done by the evening | Annoyed | Annoyed portrait |
| A session overdue by 24 h | Grumpy | Pouting, with the catch-up offer |
| Two sessions missed | Fuming | Fuming, with the same catch-up offer |
| No plan, 7 days without training | Happy | Sleeping |
| Loading | | The loader, Poulpy from above |

Poses marked Current keep whatever mood Poulpy is in. The shared `PoulpyCoach` decides which drawing shows; `PoulpyDrawing` names every drawing. Rules that depend on plans (Annoyed, Grumpy, Fuming, rest days) wait for training plans.

## Tentacles

Poulpy's ring has eight tentacles. Each finished session lights one, and **a lit tentacle never goes dark**: there is no weekly reset, so a quiet month takes nothing away. The eighth makes a **full octopus** (Champion), and the next session starts a new ring. This replaces the brand book's "eight tentacles a week".

## Voice

Poulpy speaks in short first-person lines, warm and a little cheeky: "6 of 8 tentacles lit. One more and I'm doing a little dance." or "Thursday's run is still waiting. 20 minutes and I flip back, promise." He jokes about himself, never about the person.

## Logo and type

The logo is the same drawing as the in-app Poulpy: a front view of the plush with five visible arms and the ember lining peeking out underneath. Headings use Baloo 2, body text Nunito; see the [design system](./design-system.md).

## Ideas for later

- **Shells in the den**: achievements as shells Poulpy collects (first 5 km, 10 sessions, a squat record).
- **Colourways**: cosmetic seasonal skins unlocked by milestones.
- **Flip the real one**: a widget or morning nudge saying which side the real plush should show today.

# Brand

Poulpy is a reversible plush octopus: blue and smiling on one side, grumpy on the other. In the app the grumpy side is ember, from burnt orange to red. **The side is the status** (are you keeping your plan?) **and the shade is the feeling** (what just happened). The full brand book, with the logo, colours and every drawing, is on the site's Brand book page, and [Brand decisions](./brand-decisions.md) traces what was decided and why.

## Character rules

- **Grumpy is comic, never guilt.** No "you failed", no streak-loss alarms. Even Fuming is a cartoon tantrum.
- **Pain and rehab always win.** Someone who logged pain gets the Patched up pose and the blue side for 72 h, never ember.
- **One way back, always short.** While Poulpy is ember, the catch-up offered is the shortest workout in the plan, and any finished session flips him straight back.
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
| The eighth tentacle lights | Proud | Champion, on the end-of-session screen, on the share card and Home, with its own heading ("Champion!") and quote (“Eight tentacles. No regrets.”) |
| You share a finished session | Happy | Exhausted with a sweatband, under “Well done!” and the quote “Worth it. Probably.”, on the end-of-session screen and on the share card, whether shared straight away or from the history; the card leaves the sets-done line off; never an ember or guilty drawing |
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

Poses marked Current keep whatever mood Poulpy is in. The shared `PoulpyCoach` decides which drawing shows; `PoulpyDrawing` names every drawing.

## The ember side

Only a plan can turn Poulpy ember, through its calendar entries. A planned workout placed by hand is a reminder, not a promise, and never counts. See [Brand decisions](./brand-decisions.md), decision 12.

- **Annoyed (the grace period).** From 18:00 on an entry's day, while it is still planned, until the end of the next day. The entry can still be done the day after: finishing it then marks it done.
- **Grumpy.** An entry still planned once the day after its day is over: it is missed.
- **Fuming.** Two or more missed entries.
- **Any finished session flips him back.** Entries dated on or before the day of the last finished session never count, whatever their status. Finishing the catch-up, a free session or another entry all work.
- **Only planned entries count.** Done never counts; a moved entry counts on its new date only; a skipped entry never counts, because skipping is an honest choice and the ember side is for sessions left hanging.
- **Rest days never count.** A plan day with no workout has no entry. On a rest day Poulpy sleeps if he is blue and stays ember if he is ember: a rest day doesn't erase a missed session.
- **Pain and rehab win.** For 72 h after pain is logged or a rehab session is marked too hard, Poulpy is blue and Patched up, and entries dated inside those 72 h never count, even afterwards.
- **No enrollment, no ember.** Without an active enrollment the blue-side rules apply.

When several apply, the first wins: pain or rehab, then Fuming, Grumpy, Annoyed, then the blue-side rules.

**The catch-up** is offered with Grumpy and Fuming, and nowhere on the blue side. It is the workout with the shortest estimated duration in the enrolled plan version, the earliest in the plan on a tie. Finishing it flips Poulpy back like any session; it leaves the missed entries as they are, for the person to skip or move. While Annoyed there is no catch-up: today's entry is the way back.

| Mood | Title | Line |
| --- | --- | --- |
| Annoyed | Tick tock / Tic tac | "Today's {workout} is still waiting. I'm turning a little orange." / « {workout} t’attend encore aujourd’hui. Je commence à virer à l’orange. » |
| Grumpy | Hmph / Pff | "{workout} has been waiting since {day}. {minutes} minutes and I flip back, promise." / « {workout} t’attend depuis {day}. {minutes} minutes et je me retourne, promis. » |
| Fuming | Full lava / Mode lave | "Sessions slipped by and I'm boiling like a kettle. {minutes} minutes and I'm blue again." / « Des séances ont filé et je bous comme une bouilloire. {minutes} minutes et je redeviens bleu. » |
| Recovering, rest day | Rest day / Jour de repos | "No training today. I'm napping too." / « Pas d’entraînement aujourd’hui. Moi aussi, je fais la sieste. » |

The catch-up button reads "Catch up: {workout}, {minutes} min" / « Rattrapage : {workout}, {minutes} min », with a non-breaking space before the colon. In the Grumpy and Fuming lines, `{minutes}` is the catch-up's duration, an ICU plural in the translation files so one minute reads "1 minute". In the Grumpy line, `{workout}` and `{day}` are the oldest missed entry's: `{day}` is its weekday while it is less than a week old ("Thursday" / « jeudi »), its date after that ("12 October" / « le 12 octobre »).

## Tentacles

Poulpy's ring has eight tentacles. Each finished session lights one, and **a lit tentacle never goes dark**: there is no weekly reset, so a quiet month takes nothing away. The eighth makes a **full octopus** (Champion), and the next session starts a new ring. This replaced "eight tentacles a week"; see [Brand decisions](./brand-decisions.md), decision 11.

## Voice

Poulpy speaks in short first-person lines, warm and a little cheeky: "6 of 8 tentacles lit. One more and I'm doing a little dance." or "Thursday's run is still waiting. 20 minutes and I flip back, promise." He jokes about himself, never about the person. In French he says *tu*, like a friend, while the rest of the app says *vous*.

## Logo and type

The logo is the same drawing as the in-app Poulpy: a front view of the plush with five visible arms and the ember lining peeking out underneath. The app icon is that logo, happy and blue, on Foam (Deep sea for the iOS dark icon), generated by `design/build-poulpy.mjs`. Headings use Baloo 2, body text Nunito; see the [design system](./design-system.md).

## Ideas for later

- **Shells in the den**: achievements as shells Poulpy collects (first 5 km, 10 sessions, a squat record).
- **Colourways**: cosmetic seasonal skins unlocked by milestones.
- **Flip the real one**: a widget or morning nudge saying which side the real plush should show today.

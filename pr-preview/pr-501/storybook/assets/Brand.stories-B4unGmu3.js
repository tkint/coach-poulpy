const __vite__mapDeps=(i,m=__vite__mapDeps,d=(m.f||(m.f=["./motion-DoAzzIG7.js","./rolldown-runtime-sJBDdmOt.js","./model-Bz7IKTvt.js","./poses-BljLuoRA.js"])))=>i.map(i=>d[i]);
import{n as e,r as t}from"./rolldown-runtime-sJBDdmOt.js";import{n,t as r}from"./preload-helper-Dt5pTa-y.js";import{A as i,E as a,F as o,I as s,J as c,L as l,M as u,O as d,R as f,U as p,W as m,b as h,f as g,g as _,h as v,k as y,m as b,o as x,p as ee,t as S,u as C,v as w}from"./vue.esm-bundler-CexDUOsq.js";import{n as T,t as te}from"./rotate-ccw-Bv7sQS_2.js";import{a as E,i as D,o as O,s as k,t as A}from"./moodMoves-Bq7J4813.js";import{$ as j,Ct as M,G as N,H as P,J as ne,K as re,Q as ie,St as F,U as ae,W as oe,X as se,Y as ce,Z as le,_t as ue,at as de,bt as fe,ct as pe,dt as me,et as he,ft as ge,gt as _e,ht as ve,it as ye,lt as be,mt as xe,nt as Se,ot as Ce,pt as we,q as Te,rt as Ee,st as De,tt as Oe,ut as ke,vt as Ae,xt as je,yt as Me}from"./iframe-BZ8vN16-.js";import{i as Ne,n as Pe,r as Fe,t as Ie}from"./PoulpyViewer-B3MyQfGf.js";var Le;function Re(){return(Re=e((()=>{Le=`<div class="wrap">

<header class="hero">
  <div class="copy">
    <span class="eyebrow">Coach Poulpy · brand book</span>
    <p class="hero-title">One octopus, <span class="blue">two</span> <span class="ember">moods</span>.</p>
    <p class="lede">Poulpy is a friendly, reversible plush: blue and smiling by default, with a playful grumpy mood on his ember side. In the app, ember shifts from burnt orange to red as that mood intensifies.</p>
    <p class="muted">Tap Poulpy to flip him.</p>
  </div>
  <div class="flipbox">
    <button class="stage" id="stage" aria-label="Flip Poulpy" aria-pressed="false">
      <div class="card">
        <div class="face front" data-poulpy="happy"></div>
        <div class="face back" data-poulpy="grumpy"></div>
      </div>
    </button>
    <button class="flip-btn" id="flipBtn" type="button">Flip Poulpy</button>
    <div class="flip-state" id="flipState" aria-live="polite">Blue side: you trained, Poulpy is happy.</div>
  </div>
</header>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">1 · Logo</span>
    <h2 id="logo">Poulpy as he is</h2>
    <p class="muted">A faithful front view of the plush: tall soft dome, five visible arms, and the ember lining peeking out underneath, which hints at the other side. The logo and the in-app mascot are the same drawing.</p>
  </div>
  <div class="logo-final">
    <div class="lf light" data-logo="light"></div>
    <div class="lf dark" data-logo="dark"></div>
    <div class="lf lagoon" data-logo="lagoon"></div>
  </div>
  <div class="icons" id="icons"></div>
  <div class="wordmarks">
    <div class="wm l"><span data-poulpy="happy"></span><div class="name"><small>Coach</small><span class="p">poulpy</span></div></div>
    <div class="wm d"><span data-poulpy="happy"></span><div class="name"><small>Coach</small><span class="p">poulpy</span></div></div>
  </div>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">2 · Colour</span>
    <h2 id="color">Blue when it goes well, ember when it doesn't</h2>
    <p class="muted">The grumpy side is a warm ember: burnt orange (#D9622B) for everyday grumpiness, deepening into red when Poulpy gets really cross.</p>
  </div>
  <div class="sec-head" style="margin-top:10px">
    <h3>The mood spectrum</h3>
    <p class="muted">Poulpy's colour follows his emotion, not just his side. The blue side stays close to the plush: a touch richer when proud, lighter and fresher while recovering. Missed sessions heat the ember up toward red. Tap a shade.</p>
  </div>
  <div class="spectrum">
    <div class="spec-art" id="specArt"></div>
    <div class="spec-info">
      <span class="spec-side" id="specSide"></span>
      <span class="spec-name" id="specName"></span>
      <p id="specWhen"></p>
      <span class="spec-meta" id="specHex"></span>
      <div class="spec-bar" id="specBar" role="group" aria-label="Mood shades"></div>
      <div class="spec-labels" id="specLabels"></div>
    </div>
  </div>

  <div class="ramps" id="ramps"></div>
  <div class="roles">
    <div class="role-table" id="rolesLight"><h3>Light theme roles</h3></div>
    <div class="role-table" id="rolesDark" style="background:#0A1520;color:#E8F1F7;border-color:#21394A"><h3>Dark theme roles</h3></div>
  </div>
  <p class="muted">With a red-leaning mascot, error messages move to a crimson (#C8264A) and always come with an icon and text, so a cross Poulpy is never mistaken for an error. The plush ember (#D9622B) is too light for white text, so buttons use the deeper ember 600.</p>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">3 · Poses</span>
    <h2 id="poses">Poulpy in action</h2>
    <p class="muted">A sticker sheet for the big moments: end of session screens, records, rest days, notifications. The apps draw these same poses, generated from this page's drawing code by <code>design/build-poulpy.mjs</code>.</p>
  </div>
  <div class="poses">
    <div class="pose"><div class="art b" data-pose="flex"></div><b>Cocky</b><span>New personal record. Smug wink, one eyebrow up, a golden glow.</span><q>Yeah. I did that.</q></div>
    <div class="pose"><div class="art b" data-pose="lift"></div><b>Straining</b><span>Mid-session and during heavy sets. Eyes squeezed, teeth gritted, a dumbbell nearby.</span><q>Hnnngh… one more!</q></div>
    <div class="pose"><div class="art b" data-pose="champion"></div><b>Champion</b><span>All eight tentacles lit: a full octopus, or a plan block finished.</span><q>Gold for the octopus!</q></div>
    <div class="pose"><div class="art b" data-pose="tall"></div><b>Standing tall</b><span>Stretched up a little on his tentacles. A new goal set, a level reached.</span><q>Look how big I am now.</q></div>
    <div class="pose"><div class="art b" data-pose="exhausted"></div><b>Exhausted</b><span>Right after a hard session. Sweatband, panting, dripping.</span><q>Worth it. Probably.</q></div>
    <div class="pose"><div class="art b" data-pose="side"></div><b>Side glance</b><span>Turned a little to the side. Tired between sets, or quietly waiting. The pose can carry other faces too.</span><q>Just… give me a sec.</q></div>
    <div class="pose"><div class="art b" data-pose="sleeping"></div><b>Sleeping</b><span>Planned rest day. Nightcap on.</span><q>Rest is training too.</q></div>
    <div class="pose"><div class="art b" data-pose="hurt"></div><b>Patched up</b><span>Pain logged or a rehab day. A plaster, a soft look, never grumpy.</span><q>Easy today. I'm right here.</q></div>
    <div class="pose"><div class="art b" data-pose="hide"></div><b>Shy</b><span>Eyes squeezed shut, big blush. Embarrassed, or welcoming you back after a break.</span><q>Oh… you're back!</q></div>
    <div class="pose"><div class="art b" data-pose="scratch"></div><b>Thinking</b><span>Puzzled look and a thought bubble. Picking a plan, an unknown exercise.</span><q>Hmm, which one…</q></div>
    <div class="pose"><div class="art b" data-pose="spin"></div><b>Spinning</b><span>He spins on the spot, shown comic-style with motion lines. The loading indicator, with the front arc running in a loop.</span><q>Wheee…</q></div>
    <div class="pose"><div class="art e" data-pose="crossed"></div><b>Pouting</b><span>A session is overdue. Frowning and looking away.</span><q>Hmph. We had a date.</q></div>
    <div class="pose"><div class="art e" data-pose="fuming"></div><b>Fuming</b><span>Two sessions missed. Steam, shaking, deep red.</span><q>EIGHT tentacles and none of them trained!</q></div>
  </div>
  <p class="muted">Poulpy never lifts his tentacles: every pose keeps the plush's shape and tells the mood with his face, his shade, props and little effects. More ideas for later: stretching, a tiny treadmill, sipping from a water bottle, dancing when the eighth tentacle lights.</p>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">4 · Typography</span>
    <h2 id="type">Round like the plush, readable mid-set</h2>
    <p class="muted">Both faces are free (SIL Open Font License), so they can ship inside the Android and iOS apps and load from Google Fonts on the web.</p>
  </div>
  <div class="type-grid">
    <div class="specimen">
      <div class="spec-row"><span class="k">display 40/44</span><span class="t-display">Leg day, Poulpy approves</span></div>
      <div class="spec-row"><span class="k">title 28/32</span><span class="t-title">Lower body strength</span></div>
      <div class="spec-row"><span class="k">headline 21/25</span><span class="t-headline">Back squat · 4 × 8 @ 60 kg</span></div>
      <div class="spec-row"><span class="k">body 16/24</span><span class="t-body">Rest 90 s between sets. Keep the bar over mid-foot and stop one rep before form breaks.</span></div>
      <div class="spec-row"><span class="k">label 12.5</span><span class="t-label">Next up · Romanian deadlift</span></div>
      <div class="spec-row"><span class="k">timer 64</span><span class="t-timer">01:27</span></div>
    </div>
    <div class="faces">
      <div class="face-card"><span class="big" style="font-family:var(--font-display);font-weight:800">Baloo 2</span><b>Display and titles</b><span class="muted">Heavy, rounded, friendly. Used for headings, Poulpy's speech and big moments only.</span></div>
      <div class="face-card"><span class="big" style="font-family:var(--font-body);font-weight:800">Nunito</span><b>Body, labels and numbers</b><span class="muted">Rounded terminals that match Baloo, with tabular figures so timers and rep counts don't jitter while they tick.</span></div>
    </div>
  </div>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">5 · Design tokens</span>
    <h2 id="tokens">One file, three platforms</h2>
    <p class="muted">A single <code>design/tokens.json</code> is the source of truth, generated into a Compose theme, SwiftUI colour assets and CSS variables. The mood shades are tokens too, so every platform paints Poulpy the same way.</p>
  </div>
  <div class="tabs" role="tablist" aria-label="Token output">
    <button class="tab" role="tab" aria-selected="true" data-tab="json" id="tab-json">tokens.json</button>
    <button class="tab" role="tab" aria-selected="false" data-tab="kt" id="tab-kt">Compose (Kotlin)</button>
    <button class="tab" role="tab" aria-selected="false" data-tab="swift" id="tab-swift">SwiftUI</button>
    <button class="tab" role="tab" aria-selected="false" data-tab="css" id="tab-css">Vue (CSS)</button>
  </div>
<pre class="code" data-panel="json">{
  "color": {
    "poulpy": { "blue": "#3AA6E0", "ember": "#D9622B", "ink": "#141A26" },
    "lagoon": { "50": "#EAF6FD", "100": "#CDEBFA", "300": "#7FCBF0", "400": "#3AA6E0",
                "600": "#1574B0", "800": "#0C4A73", "900": "#082F4A" },
    "ember":  { "50": "#FFF1E8", "100": "#FFDCC7", "300": "#F4A473", "400": "#E8873F",
                "500": "#D9622B", "600": "#B4471C", "800": "#7A2C12" }
  },
  "mood": {
    "proud":   "#2C9CE0", "happy":  "#3AA6E0", "recovering": "#5DB4E6",
    "annoyed": "#E8873F", "grumpy": "#D9622B", "fuming":     "#C0352B"
  },
  "role": {
    "light": { "primary": "{color.lagoon.600}", "onPrimary": "#FFFFFF",
               "grumpy": "{color.ember.600}", "error": "#C8264A",
               "background": "#F4F9FC", "surface": "#FFFFFF" },
    "dark":  { "primary": "#5BBEF0", "onPrimary": "#06202F",
               "grumpy": "#FF9A66", "error": "#FF6B85",
               "background": "#0A1520", "surface": "#112230" }
  },
  "font":   { "display": "Baloo 2", "body": "Nunito" },
  "space":  { "xs": 4, "sm": 8, "md": 12, "lg": 16, "xl": 24, "2xl": 32, "3xl": 48 },
  "radius": { "sm": 10, "md": 16, "lg": 24, "pill": 999 },
  "motion": { "flip": { "duration": 700, "easing": [0.5, -0.35, 0.35, 1.4] } }
}</pre>
<pre class="code" data-panel="kt" hidden>// app-android/.../theme/PoulpyTokens.kt  (generated)
object PoulpyColors {
    val Blue  = Color(0xFF3AA6E0)
    val Ember = Color(0xFFD9622B)
    val Ink   = Color(0xFF141A26)
    val Lagoon600 = Color(0xFF1574B0)
    val Ember600  = Color(0xFFB4471C)
}

enum class Mood(val tone: Color) {
    Proud(Color(0xFF2C9CE0)), Happy(Color(0xFF3AA6E0)), Recovering(Color(0xFF5DB4E6)),
    Annoyed(Color(0xFFE8873F)), Grumpy(Color(0xFFD9622B)), Fuming(Color(0xFFC0352B)),
}

val PoulpyLight = lightColorScheme(
    primary = PoulpyColors.Lagoon600, onPrimary = Color.White,
    tertiary = PoulpyColors.Ember600,
    background = Color(0xFFF4F9FC), surface = Color.White,
    error = Color(0xFFC8264A),
)</pre>
<pre class="code" data-panel="swift" hidden>// app-ios/CoachPoulpy/Theme/PoulpyTokens.swift  (generated)
// Colours live in Assets.xcassets with Any/Dark appearances, generated from tokens.json.
extension Color {
    static let poulpyBlue = Color("PoulpyBlue")    // fixed, same in both appearances
    static let poulpyEmber = Color("PoulpyEmber")
    static let brandPrimary = Color("Primary")     // #1574B0 light, #5BBEF0 dark
    static let brandGrumpy = Color("Grumpy")       // #B4471C light, #FF9A66 dark
}

enum Mood: String, CaseIterable {
    case proud, happy, recovering, annoyed, grumpy, fuming
    var tone: Color { Color("Mood/\\(rawValue)") }
}

extension Font {
    static let poulpyDisplay = Font.custom("Baloo2-ExtraBold", size: 40, relativeTo: .largeTitle)
    static let poulpyBody = Font.custom("Nunito-Medium", size: 16, relativeTo: .body)
}</pre>
<pre class="code" data-panel="css" hidden>/* web/ui/src/tokens.css  (generated) */
:root {
  --cp-poulpy-blue: #3AA6E0;
  --cp-poulpy-ember: #D9622B;
  --cp-mood-proud: #2C9CE0;  --cp-mood-happy: #3AA6E0;  --cp-mood-recovering: #5DB4E6;
  --cp-mood-annoyed: #E8873F; --cp-mood-grumpy: #D9622B; --cp-mood-fuming: #C0352B;
  --cp-primary: #1574B0;
  --cp-grumpy: #B4471C;
  --cp-error: #C8264A;
  --cp-bg: #F4F9FC;
  --cp-font-display: "Baloo 2", "Arial Rounded MT Bold", sans-serif;
  --cp-font-body: "Nunito", system-ui, sans-serif;
}
@media (prefers-color-scheme: dark) {
  :root { --cp-primary: #5BBEF0; --cp-grumpy: #FF9A66; --cp-error: #FF6B85; --cp-bg: #0A1520; }
}</pre>
  <div class="scales">
    <div class="scale"><h3>Spacing</h3><div id="spacing"></div></div>
    <div class="scale"><h3>Radius</h3><p class="muted">Soft like stitched fabric. No sharp corners anywhere.</p><div class="rad-row"><div style="border-radius:10px">10</div><div style="border-radius:16px">16</div><div style="border-radius:24px">24</div><div style="border-radius:999px">pill</div></div></div>
    <div class="scale"><h3>Motion</h3><p class="muted">One signature move: the flip, 700 ms with a small overshoot, like turning the plush inside out. Mood shade changes fade over 300 ms. Everything else stays quick (150 to 250 ms) and plain.</p></div>
  </div>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">6 · In the app</span>
    <h2 id="screens">The Today screen, on both sides</h2>
    <p class="muted">Poulpy sits in the middle of the tentacle ring. Only Poulpy and the header tint change with his mood; the rest of the screen stays calm so the app never feels like it's shouting at you.</p>
  </div>
  <div class="phones">
    <div class="phone-col">
      <div class="phone">
        <div class="ph-top happy">
          <span class="ph-date">Saturday 24 Oct</span>
          <span class="ph-hello">Nice streak, 9 days</span>
          <div class="ring-wrap"><span data-ring="6"></span><div class="mini" data-poulpy="happy"></div></div>
          <div class="bubble">6 of 8 tentacles lit. One more session and I'm doing a little dance.</div>
        </div>
        <div class="ph-body">
          <div class="ph-row"><span>This week</span><span>3 sessions · 2 h 10</span></div>
          <div class="session">
            <span class="t-label" style="color:var(--primary)">Next session</span>
            <span class="t">Lower body strength</span>
            <span class="m">45 min · Back squat, Romanian deadlift, split squat</span>
            <button class="cta" type="button" tabindex="-1">Start session</button>
          </div>
        </div>
      </div>
      <p class="caption">Happy side. On track with the week, streak alive.</p>
    </div>
    <div class="phone-col">
      <div class="phone">
        <div class="ph-top grumpy">
          <span class="ph-date">Saturday 24 Oct</span>
          <span class="ph-hello">Poulpy is grumpy</span>
          <div class="ring-wrap"><span data-ring="3" data-ring-mood="ember"></span><div class="mini" data-poulpy="crossedmini"></div></div>
          <div class="bubble g">Thursday's run is still waiting. 20 minutes and I flip back, promise.</div>
        </div>
        <div class="ph-body">
          <div class="ph-row"><span>This week</span><span>1 session · 35 min</span></div>
          <div class="session">
            <span class="t-label" style="color:var(--ember)">Catch up</span>
            <span class="t">Easy treadmill run</span>
            <span class="m">20 min · Zone 2, 8.5 km/h, 1% incline</span>
            <button class="cta g" type="button" tabindex="-1">Flip him back</button>
          </div>
        </div>
      </div>
      <p class="caption">Grumpy side. A planned session was missed; the fix is offered right away and kept short.</p>
    </div>
  </div>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">7 · Poulpy's behaviour</span>
    <h2 id="behaviour">The side is the status, the shade is the feeling</h2>
    <p class="muted">Which side shows says whether you're keeping your plan. The shade and the face on that side react to what just happened.</p>
  </div>
  <div class="table-wrap">
    <table>
      <thead><tr><th>What happens</th><th>Mood</th><th>Pose</th><th>Rule</th></tr></thead>
      <tbody id="rules"></tbody>
    </table>
  </div>
</section>

<section class="block">
  <div class="sec-head">
    <span class="eyebrow">8 · Gamification</span>
    <h2 id="game">Ideas built from real octopus facts</h2>
    <p class="muted">Core ideas come first; the others can join later.</p>
  </div>
  <div class="ideas">
    <article class="idea"><div class="idea-head"><h3>The flip</h3><span class="prio core">Core</span></div><p>Poulpy's side follows the mood rules above. Coming back is always one session away, and the flip animation plus a haptic tap is the reward for it.</p><p class="fact">Inspired by the plush itself.</p></article>
    <article class="idea"><div class="idea-head"><h3>Eight tentacles</h3><span class="prio core">Core</span></div><p>The ring has eight tentacles. Each finished session lights one, and a lit tentacle never goes dark: there is no weekly reset. The eighth gives the Champion pose and a "full octopus", and the next session starts a new ring.</p><p class="fact">Octopuses have eight tentacles, and Poulpy never grows more.</p></article>
    <article class="idea"><div class="idea-head"><h3>Mood shades</h3><span class="prio core">Core</span></div><p>Poulpy's colour moves along the spectrum with each emotion: a touch richer after a record, lighter while recovering, hotter ember the longer a session waits.</p><p class="fact">Octopuses change colour with their mood.</p></article>
    <article class="idea"><div class="idea-head"><h3>Shells in the den</h3><span class="prio later">Later</span></div><p>Achievements are shells Poulpy collects in his den: first 5 km on the treadmill, 10 sessions, a squat record, a full rehab block finished.</p><p class="fact">Octopuses decorate their dens with shells and stones.</p></article>
    <article class="idea"><div class="idea-head"><h3>Colourways</h3><span class="prio maybe">Maybe</span></div><p>Unlock seasonal skins for Poulpy after milestones (a coral reef edition, a deep-sea night edition). Purely cosmetic.</p><p class="fact">Octopuses can camouflage in a fraction of a second.</p></article>
    <article class="idea"><div class="idea-head"><h3>Flip the real one</h3><span class="prio maybe">Maybe</span></div><p>A home-screen widget and an optional morning nudge that says which side the real plush on your desk should show today.</p><p class="fact">Only works because the real Poulpy exists.</p></article>
  </div>
  <div class="guard">
    <h3>Ground rules for Poulpy</h3>
    <ul>
      <li>Grumpy is comic, never guilt. No "you failed", no streak-loss alarms. Even Fuming is a cartoon tantrum.</li>
      <li>Pain and rehab always win: a hurt user gets the Patched up pose, never the ember side.</li>
      <li>One way back, always short: the catch-up session offered while ember is the easiest one in the plan.</li>
      <li>Notifications speak in Poulpy's voice at most once a day, and can be turned off without losing the mascot.</li>
      <li>All of Poulpy's lines are written in French and English from the start.</li>
    </ul>
  </div>
</section>

<footer>Coach Poulpy brand book. What was decided, and why, is traced on the <a href="__BRAND_DECISIONS__">brand decisions</a> page.</footer>
</div>`})))()}function ze(){(function(){var e=`#141A26`,t=`#3AA6E0`,n=`#D9622B`,r=[{id:`fuming`,name:`Fuming`,tone:`#C0352B`,side:`ember`,face:`fuming`,when:`Two planned sessions missed.`},{id:`grumpy`,name:`Grumpy`,tone:`#D9622B`,side:`ember`,face:`grumpy`,when:`A planned session is still not done 24 h after its day.`},{id:`annoyed`,name:`Annoyed`,tone:`#E8873F`,side:`ember`,face:`annoyed`,when:`Today's session isn't done yet and the evening is coming (the grace period).`},{id:`recover`,name:`Recovering`,tone:`#5DB4E6`,side:`blue`,face:`calm`,when:`Rest days, the hours after a hard session, and pain or rehab days. A lighter, fresher blue, never grey.`},{id:`happy`,name:`Happy`,tone:`#3AA6E0`,side:`blue`,face:`happy`,when:`Session done, plan on track. The plush's own colour and the default.`},{id:`proud`,name:`Proud`,tone:`#2C9CE0`,side:`blue`,face:`cocky`,when:`Personal record, or a full octopus. Just a touch richer than Happy.`}],i={};r.forEach(function(e){i[e.id]=e});function a(t,n,r){return`<path d="`+t+`" fill="none" stroke="`+(r||e)+`" stroke-width="`+(n||4.5)+`" stroke-linecap="round" stroke-linejoin="round"/>`}function o(t,n){return`<path d="`+t+`" fill="`+(n||e)+`"/>`}function s(t,n,r,i,a){return`<ellipse cx="`+t+`" cy="`+n+`" rx="`+r+`" ry="`+i+`" fill="`+e+`"/><circle cx="`+(t+(a||-3))+`" cy="`+(n-4)+`" r="2.8" fill="#fff"/>`}var c=s(80,96,9,10.5)+s(120,96,9,10.5);function l(){return`<ellipse cx="64" cy="112" rx="8" ry="4.5" fill="#F49A6B" opacity=".75"/><ellipse cx="136" cy="112" rx="8" ry="4.5" fill="#F49A6B" opacity=".75"/>`}function u(){return`<ellipse cx="64" cy="114" rx="8" ry="4.5" fill="#8E2414" opacity=".35"/><ellipse cx="136" cy="114" rx="8" ry="4.5" fill="#8E2414" opacity=".35"/>`}function d(e,t,n,r){return n||=1,`<path d="M`+e+` `+(t-9*n)+` C`+(e+6*n)+` `+(t-2*n)+` `+(e+6*n)+` `+(t+5*n)+` `+e+` `+(t+5*n)+` C`+(e-6*n)+` `+(t+5*n)+` `+(e-6*n)+` `+(t-2*n)+` `+e+` `+(t-9*n)+` Z" fill="`+(r||`#A8DDF7`)+`" stroke="#3AA6E0" stroke-width="1.5"/>`}function f(e,t,n,r){return`<path d="M`+e+` `+(t-n)+` L`+(e+n*.3)+` `+(t-n*.3)+` L`+(e+n)+` `+t+` L`+(e+n*.3)+` `+(t+n*.3)+` L`+e+` `+(t+n)+` L`+(e-n*.3)+` `+(t+n*.3)+` L`+(e-n)+` `+t+` L`+(e-n*.3)+` `+(t-n*.3)+` Z" fill="`+(r||`#F0B53A`)+`"/>`}var p={happy:c+a(`M90 112 Q100 123 110 112`)+l(),calm:a(`M71 97 Q80 90 89 97`,4.5)+a(`M111 97 Q120 90 129 97`,4.5)+a(`M92 113 Q100 119 108 113`,4)+l(),cocky:o(`M71 97 a9 9 0 0 0 18 0 Z`)+a(`M68 95 L92 93`,4)+s(120,96,9,10.5)+a(`M109 79 Q120 70 132 77`,4.5)+a(`M88 114 Q102 123 115 108`,4.5)+l(),proud:a(`M70 99 Q80 86 90 99`)+a(`M110 99 Q120 86 130 99`)+o(`M87 109 Q100 132 113 109 Z`)+l(),tired:o(`M71 97 a9 9 0 0 0 18 0 Z`)+o(`M111 97 a9 9 0 0 0 18 0 Z`)+a(`M69 98 L91 95`,4)+a(`M109 95 L131 98`,4)+a(`M73 110 Q80 114 87 110`,2.5,`#4B6274`)+a(`M113 110 Q120 114 127 110`,2.5,`#4B6274`)+`<ellipse cx="100" cy="121" rx="8" ry="6.5" fill="`+e+`"/><ellipse cx="100" cy="125" rx="5" ry="2.6" fill="#F07A7A"/>`,hurt:s(80,97,9,10.5)+s(120,97,9,10.5)+`<circle cx="84" cy="101" r="2" fill="#fff"/><circle cx="124" cy="101" r="2" fill="#fff"/>`+a(`M68 82 Q78 76 88 80`,4)+a(`M132 82 Q122 76 112 80`,4)+a(`M91 117 q3 -3 6 0 t6 0 t6 0`,3.5)+l(),annoyed:o(`M71 97 a9 9 0 0 0 18 0 Z`)+o(`M111 97 a9 9 0 0 0 18 0 Z`)+a(`M69 96 L91 96`,4)+a(`M109 96 L131 96`,4)+a(`M110 82 L130 78`,4)+a(`M91 116 L109 116`,4.5)+u(),grumpy:s(80,98,8.5,9)+s(120,98,8.5,9)+a(`M66 80 L92 89`,5)+a(`M134 80 L108 89`,5)+a(`M89 121 Q100 110 111 121`)+u(),fuming:s(81,100,7,7.5)+s(119,100,7,7.5)+a(`M64 82 L93 93`,5.5)+a(`M136 82 L107 93`,5.5)+`<rect x="86" y="111" width="28" height="13" rx="5" fill="#fff" stroke="`+e+`" stroke-width="3.5"/>`+a(`M86 117.5 L114 117.5`,2.5)+a(`M95 111 L95 124`,2)+a(`M105 111 L105 124`,2)+u(),lift:a(`M70 90 L87 96 L70 102`,4.5)+a(`M130 90 L113 96 L130 102`,4.5)+`<rect x="87" y="110" width="26" height="12" rx="5" fill="#fff" stroke="`+e+`" stroke-width="3.5"/>`+a(`M87 116 L113 116`,2.5)+l(),crossed:s(84,94,8.5,9.5,-5)+s(124,94,8.5,9.5,-5)+a(`M68 79 L92 85`,5)+a(`M134 76 L110 85`,5)+a(`M90 110 Q98 104 106 110`,4.5)+u()};function m(e,t,n){var r=[[26,126,25,17],[62,139,24,18],[100,143,24,18],[138,139,24,18],[174,126,25,17]];return n&&(r=r.filter(function(e,t){return n.indexOf(t)<0})),r.map(function(n){return`<ellipse cx="`+n[0]+`" cy="`+(n[1]+t)+`" rx="`+n[2]+`" ry="`+n[3]+`" fill="`+e+`"/>`}).join(``)}function h(e){return r.some(function(t){return t.tone===e&&t.side===`ember`})?t:n}function g(e,t){return m(h(e),6,t)+m(e,0,t)+`<path d="M37 130 C33 68 64 30 100 30 C136 30 167 68 163 130 Z" fill="`+e+`"/><ellipse cx="76" cy="56" rx="17" ry="9" fill="#fff" opacity=".22" transform="rotate(-20 76 56)"/>`}function _(e,t,n){return`<svg viewBox="`+(t||`0 0 200 180`)+`" role="img" aria-label="`+n+`">`+e+`</svg>`}function v(e,r,a){var o=i[e]||null;return r||=o?o.tone:[`grumpy`,`annoyed`,`fuming`,`crossed`].indexOf(e)>-1?n:t,_(g(r)+p[o&&o.face||e],null,a||`Poulpy, `+e)}var y=`-24 -36 248 226`;function b(t,n){return`<rect x="`+(t-16)+`" y="`+(n-2.5)+`" width="32" height="5" rx="2.5" fill="#56687A"/><rect x="`+(t-22)+`" y="`+(n-11)+`" width="9" height="22" rx="3" fill="`+e+`"/><rect x="`+(t+13)+`" y="`+(n-11)+`" width="9" height="22" rx="3" fill="`+e+`"/>`}var x={flex:function(){var t=i.proud.tone;return _(`<circle cx="100" cy="90" r="96" fill="#F0B53A" opacity=".12"/>`+g(t)+p.cocky+f(6,52,10)+f(196,40,8)+f(176,4,5)+f(24,10,5)+`<rect x="128" y="-12" width="54" height="24" rx="12" fill="`+e+`"/><text x="155" y="5" text-anchor="middle" font-family="Nunito, sans-serif" font-weight="800" font-size="14" fill="#F0B53A">PR!</text>`,y,`Poulpy looking cocky after a personal record`)},lift:function(){var e=i.happy.tone;return _(g(e)+p.lift+d(152,50,.9)+d(46,64,.7)+a(`M12 70 l-12 -4 M10 86 l-14 0`,3,`#56687A`)+a(`M188 70 l12 -4 M190 86 l14 0`,3,`#56687A`)+`<g transform="rotate(-8 196 156)">`+b(196,156)+`</g>`,y,`Poulpy straining with gritted teeth, a dumbbell beside him`)},champion:function(){var e=i.proud.tone;return _([[-8,40,`#F0B53A`,20],[20,0,n,-30],[180,4,`#5BBEF0`,40],[204,50,`#F0B53A`,-15],[150,-14,n,10],[46,-12,`#5BBEF0`,60],[100,-24,`#F0B53A`,30]].map(function(e){return`<rect x="`+e[0]+`" y="`+e[1]+`" width="9" height="5" rx="1.5" fill="`+e[2]+`" transform="rotate(`+e[3]+` `+e[0]+` `+e[1]+`)"/>`}).join(``)+g(e)+p.proud+a(`M74 120 L96 146`,7,n)+a(`M126 120 L104 146`,7,n)+`<circle cx="100" cy="152" r="14" fill="#F0B53A" stroke="#C98A0C" stroke-width="3"/>`+f(100,152,7,`#FFF3C4`),y,`Poulpy wearing a gold medal under confetti`)},tall:function(){var t=i.proud.tone;return _(`<ellipse cx="100" cy="172" rx="88" ry="6" fill="`+e+`" opacity=".1"/><g transform="translate(0 -12)"><g transform="translate(0 -36) scale(1 1.3)">`+m(h(t),6)+m(t,0)+`</g><path d="M37 130 C33 68 64 30 100 30 C136 30 167 68 163 130 Z" fill="`+t+`"/><ellipse cx="76" cy="56" rx="17" ry="9" fill="#fff" opacity=".22" transform="rotate(-20 76 56)"/>`+p.proud+`</g>`+a(`M-4 70 l0 -16 M-10 60 l6 -6 6 6`,3,`#56687A`)+a(`M204 70 l0 -16 M198 60 l6 -6 6 6`,3,`#56687A`),y,`Poulpy standing a little taller on his tentacles`)},exhausted:function(){var t=i.recover.tone;return _(g(t)+`<path d="M45 60 Q100 38 155 60 L158 76 Q100 54 42 76 Z" fill="#FFFFFF" stroke="`+e+`" stroke-opacity=".25" stroke-width="1.5"/>`+a(`M44 68 Q100 46 156 68`,4,n)+a(`M157 66 q16 0 22 12`,6,`#FFFFFF`)+a(`M157 70 q10 10 6 22`,6,`#FFFFFF`)+p.tired+d(30,76,1.1)+d(172,88,1)+d(160,112,.7)+d(12,128,.8)+d(194,140,.9),y,`Poulpy exhausted with a sweatband, dripping with sweat`)},side:function(){var e=i.recover.tone,t=o(`M89 99 a8.5 8.5 0 0 0 17 0 Z`)+o(`M124 98 a6.5 7.5 0 0 0 13 0 Z`)+a(`M87 98 L108 96`,3.5)+a(`M122 96 L139 97`,3.5)+a(`M108 117 Q115 120 122 117`,4)+`<ellipse cx="86" cy="112" rx="7" ry="4" fill="#F49A6B" opacity=".7"/><ellipse cx="144" cy="111" rx="5" ry="3.5" fill="#F49A6B" opacity=".7"/>`;return _(`<g transform="rotate(-6 100 140)">`+g(e)+`<path d="M70 40 Q56 86 62 128" fill="none" stroke="#fff" stroke-opacity=".25" stroke-width="2"/>`+t+`</g><text x="160" y="40" font-family="Baloo 2, sans-serif" font-weight="800" font-size="22" fill="#56687A">…</text>`,y,`Poulpy turned slightly to the side, tired`)},sleeping:function(){var t=i.recover.tone;return _(g(t)+`<path d="M62 52 Q100 30 140 46 C132 26 120 10 108 -4 C96 12 78 30 62 52 Z" fill="#1574B0"/>`+a(`M62 52 Q100 32 140 46`,8,`#FFFFFF`)+`<circle cx="108" cy="-6" r="8" fill="#FFFFFF" stroke="#B6C4D0" stroke-width="1.5"/>`+a(`M71 96 Q80 104 89 96`)+a(`M111 96 Q120 104 129 96`)+`<circle cx="100" cy="117" r="4" fill="`+e+`"/>`+l()+`<text x="160" y="70" font-family="Baloo 2, sans-serif" font-weight="800" font-size="24" fill="#1574B0">z</text><text x="178" y="52" font-family="Baloo 2, sans-serif" font-weight="800" font-size="17" fill="#1574B0">z</text>`,y,`Poulpy asleep wearing a nightcap`)},hurt:function(){var e=i.recover.tone;return _(g(e)+`<g transform="rotate(35 132 56)"><rect x="112" y="50" width="40" height="13" rx="5" fill="#F5D7B5" stroke="#D9B48C" stroke-width="1.5"/></g><g transform="rotate(-35 132 56)"><rect x="112" y="50" width="40" height="13" rx="5" fill="#F5D7B5" stroke="#D9B48C" stroke-width="1.5"/></g>`+p.hurt+d(66,112,.55),y,`Poulpy with a plaster on his head`)},hide:function(){var e=i.happy.tone;return _(g(e)+a(`M70 100 Q80 92 90 100`,4.5)+a(`M110 100 Q120 92 130 100`,4.5)+a(`M93 116 Q100 121 107 116`,4)+`<ellipse cx="66" cy="112" rx="12" ry="6" fill="#F49A6B" opacity=".9"/><ellipse cx="134" cy="112" rx="12" ry="6" fill="#F49A6B" opacity=".9"/>`+a(`M60 110 l4 4 M66 109 l4 4 M128 109 l4 4 M134 110 l4 4`,2,`#C96A3E`)+d(150,62,.7),y,`Poulpy blushing with his eyes squeezed shut`)},scratch:function(){var e=i.happy.tone;return _(g(e)+s(80,97,8,9.5,0)+s(120,95,9.5,11,0)+a(`M66 84 L90 86`,4)+a(`M110 78 Q120 70 132 76`,4)+a(`M90 116 q5 -4 10 0 t10 0`,4)+l()+`<circle cx="160" cy="44" r="4" fill="#B6C4D0"/><circle cx="172" cy="28" r="6" fill="#B6C4D0"/><ellipse cx="196" cy="-4" rx="24" ry="18" fill="#FFFFFF" stroke="#B6C4D0" stroke-width="2"/><text x="196" y="6" text-anchor="middle" font-family="Baloo 2, sans-serif" font-weight="800" font-size="26" fill="#1574B0">?</text>`,y,`Poulpy thinking, with a question mark in a thought bubble`)},crossed:function(){var e=i.grumpy.tone;return _(g(e)+p.crossed+a(`M196 150 q6 -6 12 0`,3,`#7A2C12`)+a(`M200 140 q8 -8 16 0`,3,`#7A2C12`)+`<text x="-16" y="34" font-family="Baloo 2, sans-serif" font-weight="800" font-size="22" fill="#7A2C12">hmph</text>`,y,`Poulpy with two front tentacles crossed, looking away`)},fuming:function(){var e=i.fuming.tone;function t(e,t){return[[0,0,12],[13,-6,10],[-12,-4,9],[3,-15,9]].map(function(n){return`<circle cx="`+(e+n[0])+`" cy="`+(t+n[1])+`" r="`+n[2]+`" fill="#E6EEF4" stroke="#B6C4D0" stroke-width="1.5"/>`}).join(``)}return _(t(52,6)+t(150,-2)+a(`M70 30 L64 18`,4,`#B6C4D0`)+a(`M132 28 L140 14`,4,`#B6C4D0`)+g(e)+a(`M14 80 l-12 -4 M12 96 l-14 0`,3,`#7A2C12`)+a(`M186 80 l12 -4 M188 96 l14 0`,3,`#7A2C12`)+p.fuming+a(`M140 50 q6 6 0 12 M152 50 q-6 6 0 12 M140 50 q6 -6 12 0 M140 62 q6 6 12 0`,3.5,`#FFDCC7`),y,`Poulpy fuming with steam, shaking two side tentacles`)},spin:function(){var e=i.happy.tone,t=a(`M186 132 Q100 100 14 132`,4,`#7FCBF0`),n=`<path class="spin-arc" d="M8 140 Q100 192 194 140" fill="none" stroke="#1574B0" stroke-width="5" stroke-linecap="round"/>`+o(`M202 132 L188 130 L194 146 Z`,`#1574B0`),r=s(88,96,9,10.5,2)+s(128,96,9,10.5,2)+a(`M98 112 Q108 122 118 112`)+l();return _(t+g(e)+r+n+a(`M-14 70 l18 0 M-18 84 l14 0`,3,`#7FCBF0`)+a(`M196 64 l18 0 M200 78 l14 0`,3,`#7FCBF0`)+`<path d="M150 40 q8 -10 18 -4" fill="none" stroke="#7FCBF0" stroke-width="3" stroke-linecap="round"/>`,y,`Poulpy spinning on the spot, drawn with comic motion lines`)}};document.querySelectorAll(`[data-poulpy]`).forEach(function(e){var t=e.getAttribute(`data-poulpy`);e.innerHTML=t===`crossedmini`?v(`crossed`,i.grumpy.tone,`Poulpy grumpy`):v(t)}),document.querySelectorAll(`[data-pose]`).forEach(function(e){e.innerHTML=x[e.getAttribute(`data-pose`)]()}),document.querySelectorAll(`[data-logo]`).forEach(function(e){e.innerHTML=v(`happy`,t,`Coach Poulpy logo`)});var ee=function(e){return`<rect width="200" height="200" fill="`+e+`"/><g transform="translate(14 24) scale(.86)">`+g(t)+p.happy+`</g>`};document.getElementById(`icons`).innerHTML=[[`#F4F9FC`,72,`Foam`],[`#0A1520`,72,`Deep sea`],[`#FFFFFF`,48,`48 px`],[`#FFFFFF`,28,`28 px`]].map(function(e){return`<figure><svg viewBox="0 0 200 200" width="`+e[1]+`" height="`+e[1]+`" role="img" aria-label="App icon, `+e[2]+`">`+ee(e[0])+`</svg><figcaption>`+e[2]+`</figcaption></figure>`}).join(``);var S=document.getElementById(`specBar`),C=document.getElementById(`specLabels`);S.innerHTML=r.map(function(e,t){return`<button type="button" id="mood-`+e.id+`" aria-pressed="false" aria-label="`+e.name+`" style="background:`+e.tone+`" data-i="`+t+`"></button>`}).join(``),C.innerHTML=r.map(function(e){return`<span>`+e.name+`</span>`}).join(``);function w(e){var t=r[e];document.getElementById(`specArt`).innerHTML=v(t.id,t.tone,`Poulpy, `+t.name),document.getElementById(`specArt`).style.backgroundColor=t.side===`blue`?`var(--primary-soft)`:`var(--ember-soft)`,document.getElementById(`specName`).textContent=t.name,document.getElementById(`specName`).style.color=t.tone;var n=document.getElementById(`specSide`);n.textContent=t.side===`blue`?`Blue side`:`Ember side`,n.style.color=t.side===`blue`?`var(--primary)`:`var(--ember)`,document.getElementById(`specWhen`).textContent=t.when,document.getElementById(`specHex`).textContent=`mood.`+t.id+` · `+t.tone,S.querySelectorAll(`button`).forEach(function(t,n){t.setAttribute(`aria-pressed`,n===e?`true`:`false`)})}S.addEventListener(`click`,function(e){var t=e.target.closest(`button`);t&&w(+t.getAttribute(`data-i`))}),w(5);var T=[[`Lagoon`,[[`50`,`#EAF6FD`],[`100`,`#CDEBFA`],[`300`,`#7FCBF0`],[`400`,`#3AA6E0`],[`600`,`#1574B0`],[`800`,`#0C4A73`],[`900`,`#082F4A`]]],[`Ember`,[[`50`,`#FFF1E8`],[`100`,`#FFDCC7`],[`300`,`#F4A473`],[`400`,`#E8873F`],[`500`,`#D9622B`],[`600`,`#B4471C`],[`800`,`#7A2C12`]]],[`Neutral`,[[`0`,`#FFFFFF`],[`50`,`#F4F9FC`],[`100`,`#E6EFF5`],[`300`,`#B6C4D0`],[`500`,`#6B7C8C`],[`700`,`#374555`],[`900`,`#141A26`]]]];document.getElementById(`ramps`).innerHTML=T.map(function(e){function t(e){var t=function(e){var t=[1,3,5].map(function(t){var n=parseInt(e.substr(t,2),16)/255;return n<=.03928?n/12.92:((n+.055)/1.055)**2.4});return .2126*t[0]+.7152*t[1]+.0722*t[2]},n=function(e,n){var r=t(e),i=t(n);return(Math.max(r,i)+.05)/(Math.min(r,i)+.05)};return[`#141A26`,`#F4F9FC`,`#000000`,`#FFFFFF`].filter(function(t){return n(t,e)>=4.5})[0]||(n(`#000000`,e)>n(`#FFFFFF`,e)?`#000000`:`#FFFFFF`)}return`<div class="ramp"><span class="lbl">`+e[0]+`</span>`+e[1].map(function(e,n){return`<div class="sw" style="background:`+e[1]+`;color:`+t(e[1])+(e[1]===`#FFFFFF`?`;border:1px solid #D3E0E9`:``)+`">`+e[0]+`<br>`+e[1]+`</div>`}).join(``)+`</div>`}).join(``);function te(e,t){e.insertAdjacentHTML(`beforeend`,t.map(function(e){return`<div class="role-row"><span class="dot" style="background:`+e[1]+`"></span><span>`+e[0]+`</span><code style="color:inherit;opacity:.7">`+e[1]+`</code></div>`}).join(``))}te(document.getElementById(`rolesLight`),[[`Primary · buttons, links`,`#1574B0`],[`Grumpy accent`,`#B4471C`],[`Background`,`#F4F9FC`],[`Surface`,`#FFFFFF`],[`Text`,`#141A26`],[`Success`,`#2E9E6A`],[`Error`,`#C8264A`]]),te(document.getElementById(`rolesDark`),[[`Primary · buttons, links`,`#5BBEF0`],[`Grumpy accent`,`#FF9A66`],[`Background · deep sea`,`#0A1520`],[`Surface`,`#112230`],[`Text`,`#E8F1F7`],[`Success`,`#4CC38A`],[`Error`,`#FF6B85`]]),document.getElementById(`spacing`).innerHTML=[[`xs`,4],[`sm`,8],[`md`,12],[`lg`,16],[`xl`,24],[`2xl`,32],[`3xl`,48]].map(function(e){return`<div class="sp-row"><span>`+e[0]+` · `+e[1]+`</span><div class="sp-bar" style="width:`+e[1]*2.4+`px;max-width:100%"></div></div>`}).join(``);var E=[[`You finish any session`,`happy`,`Happy`,`Always flips back to blue, with the flip animation. The flip is the reward.`],[`You set a record`,`proud`,`Cocky`,`Lasts until the end of the day, then back to Happy.`],[`The eighth tentacle lights`,`proud`,`Champion`,`Shown on the end-of-session screen and on Home; the next session starts a new ring.`],[`A hard session is finished (effort 8+)`,`recover`,`Exhausted or Side glance`,`For the rest of the day; Poulpy recommends recovery.`],[`Today is a planned rest day`,`recover`,`Sleeping`,`Rest days from the plan never count as missed.`],[`You log pain, or a rehab session is marked too hard`,`recover`,`Patched up`,`Blocks the ember side for 72 h. Poulpy never gets cross at someone who is hurt.`],[`Today's session is not done yet by the evening`,`annoyed`,`Annoyed`,`The grace period: a nudge, not a scolding.`],[`A planned session is still not done 24 h after its day`,`grumpy`,`Pouting`,`Flips to the ember side and offers a short catch-up session.`],[`Two planned sessions missed`,`fuming`,`Fuming`,`Same short catch-up offer. Any session flips him straight back to Happy.`],[`No plan at all (free training)`,`happy`,`Happy or Sleeping`,`With nothing promised, nothing can be missed. Sleeping after 7 days without training.`]];document.getElementById(`rules`).innerHTML=E.map(function(e){var t=i[e[1]];return`<tr><td>`+e[0]+`</td><td><span class="pill"><i style="background:`+t.tone+`"></i>`+t.name+`</span></td><td>`+e[2]+`</td><td>`+e[3]+`</td></tr>`}).join(``);function D(e,t,n,r,i){var a=function(r){var i=(r-90)*Math.PI/180;return[e+n*Math.cos(i),t+n*Math.sin(i)]},o=a(r),s=a(i);return`M`+o[0].toFixed(2)+` `+o[1].toFixed(2)+` A`+n+` `+n+` 0 0 1 `+s[0].toFixed(2)+` `+s[1].toFixed(2)}document.querySelectorAll(`[data-ring]`).forEach(function(e){for(var t=+e.getAttribute(`data-ring`),n=e.getAttribute(`data-ring-mood`)===`ember`?`var(--ember)`:`var(--primary)`,r=``,i=0;i<8;i++)r+=`<path d="`+D(100,100,86,i*45+5,i*45+40)+`" fill="none" stroke="`+(i<t?n:`var(--line)`)+`" stroke-width="15" stroke-linecap="round"/>`;e.outerHTML=`<svg viewBox="0 0 200 200" role="img" aria-label="`+t+` of 8 tentacles lit">`+r+`</svg>`});var O=document.getElementById(`stage`),k=document.getElementById(`flipState`);function A(){var e=O.classList.toggle(`flipped`);O.setAttribute(`aria-pressed`,e?`true`:`false`),k.textContent=e?`Ember side: a planned session slipped, Poulpy is grumpy.`:`Blue side: you trained, Poulpy is happy.`}O.addEventListener(`click`,A),document.getElementById(`flipBtn`).addEventListener(`click`,A),document.querySelectorAll(`.tab`).forEach(function(e){e.addEventListener(`click`,function(){document.querySelectorAll(`.tab`).forEach(function(t){t.setAttribute(`aria-selected`,t===e?`true`:`false`)}),document.querySelectorAll(`[data-panel]`).forEach(function(t){t.hidden=t.getAttribute(`data-panel`)!==e.getAttribute(`data-tab`)})})})})()}var Be,Ve;function He(){return(He=e((()=>{S(),Re(),Be=[`innerHTML`],Ve=h({__name:`BrandBook`,setup(e){let t=Le.replace(`__BRAND_DECISIONS__`,`?path=/docs/brand-decisions--docs`);return d(async()=>{await a(),ze()}),(e,n)=>(y(),v(`div`,{class:`brand-book`,innerHTML:m(t)},null,8,Be))}})})))()}var Ue;function We(){return(We=e((()=>{He(),Ue=Ve,Ve.__docgenInfo=Object.assign({displayName:Ve.name??Ve.__name},{exportName:`default`,displayName:`BrandBook`,description:``,tags:{},sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/brand/BrandBook.vue`]})})))()}var Ge,Ke,qe,Je,Ye,Xe;function Ze(){return(Ze=e((()=>{S(),T(),k(),Pe(),Ge=[`aria-label`],Ke=[`aria-pressed`,`aria-label`,`onClick`],qe={key:0,class:`wide`},Je={class:`sides`,role:`group`,"aria-label":`Direction, seen from above`},Ye=[`aria-pressed`,`aria-label`,`title`,`onClick`],Xe=h({__name:`PoulpyModel`,props:{moods:{type:Boolean},poses:{type:Boolean}},setup(e){let t=p(e.poses?`cocky`:e.moods?`happy`:`blue`),n=p(`anticlockwise`),r=e.poses?[{id:`cocky`,label:`Cocky`},{id:`straining`,label:`Straining`},{id:`champion`,label:`Champion`},{id:`standingTall`,label:`Standing tall`},{id:`exhausted`,label:`Exhausted`},{id:`sideGlance`,label:`Side glance`},{id:`sleeping`,label:`Sleeping`},{id:`patchedUp`,label:`Patched up`},{id:`shy`,label:`Shy`},{id:`thinking`,label:`Thinking`},{id:`pouting`,label:`Pouting`},{id:`fuming`,label:`Fuming`}]:e.moods?[{id:`proud`,label:`Proud`},{id:`happy`,label:`Happy`},{id:`recovering`,label:`Recovering`},{id:`annoyed`,label:`Annoyed`},{id:`grumpy`,label:`Grumpy`},{id:`fuming`,label:`Fuming`}]:[{id:`blue`,label:`Blue`,suffix:`side`},{id:`ember`,label:`Grumpy`,suffix:`side`}],a=[{id:`anticlockwise`,label:`Anticlockwise`,icon:te},{id:`clockwise`,label:`Clockwise`,icon:O}],o;s(t,()=>o?.());async function d(r){let i=!1,a=()=>r.show(e.poses?{pose:t.value}:t.value,i);return o=a,{show(e){i=e,a()},advance:e=>e*28*(n.value===`anticlockwise`?1:-1),dispose(){o=void 0}}}let f=()=>({page:e.poses?`poses`:e.moods?`moods`:`sides`,shown:t.value});return(o,s)=>(y(),ee(Ie,{label:`Poulpy in 3D.`,framing:{tilt:17,distance:9.6},"move-label":`Spin`,"stop-on-grab":``,speeds:{max:5,step:.1},start:d,values:f},{pickers:l(()=>[g(`div`,{class:`sides`,role:`group`,"aria-label":e.poses?`Pose`:e.moods?`Mood`:`Side`},[(y(!0),v(C,null,i(m(r),e=>(y(),v(`button`,{key:e.id,type:`button`,class:`choice`,"aria-pressed":t.value===e.id,"aria-label":e.suffix?`${e.label} ${e.suffix}`:e.label,onClick:n=>t.value=e.id},[w(c(e.label),1),e.suffix?(y(),v(`span`,qe,`\xA0`+c(e.suffix),1)):b(``,!0)],8,Ke))),128))],8,Ge),g(`div`,Je,[(y(),v(C,null,i(a,e=>g(`button`,{key:e.id,type:`button`,class:`choice icon`,"aria-pressed":n.value===e.id,"aria-label":e.label,title:e.label,onClick:t=>n.value=e.id},[(y(),ee(u(e.icon),{size:18,"aria-hidden":`true`}))],8,Ye)),64))])]),_:1}))}})})))()}var Qe;function $e(){return($e=e((()=>{Ze(),Qe=Xe,Xe.__docgenInfo=Object.assign({displayName:Xe.name??Xe.__name},{exportName:`default`,displayName:`PoulpyModel`,description:``,tags:{},props:[{name:`moods`,required:!1,type:{name:`boolean`}},{name:`poses`,required:!1,type:{name:`boolean`}}],sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/brand/PoulpyModel.vue`]})})))()}var et,tt,nt,rt,it,at;function ot(){return(ot=e((()=>{S(),A(),Pe(),et={class:`steps`},tt=[`aria-pressed`],nt=[`aria-label`],rt=[`aria-pressed`,`title`,`onClick`],it={key:1,class:`note`},at=h({__name:`PoulpyMoodMoves`,props:{only:{},plain:{type:Boolean}},setup(e){let t=e,n=[{id:`spot`,label:`On the spot`,note:`A quick turn, feet on the floor, arms trailing.`,make:D(0,!1),seconds:1.5,swap:`half`},{id:`hop`,label:`With a hop`,note:`A small jump with a full turn in the air.`,make:D(.45,!1),seconds:1.7,swap:`half`},{id:`whip`,label:`Wind-up and whip`,note:`He leans back first, then whips round and rocks past the mark.`,make:D(0,!0),seconds:2.1,swap:`half`}],r=[{id:`small`,label:`Small tuck`,note:`He tucks a little and comes back up; the fumes puff twice as fast meanwhile.`,make:E(.12,.08,0,0),seconds:1.8,swap:`start`,boost:1},{id:`deep`,label:`Deep tuck, release`,note:`He squeezes down into his arms, then pops back up with a tiny hop.`,make:E(.22,.2,.08,0),seconds:2,swap:`start`,boost:1.4},{id:`shudder`,label:`Shudder`,note:`A short tuck and a shake from side to side, fumes at full steam.`,make:E(.16,.12,.05,9),seconds:1.9,swap:`start`,boost:2}],a=[{id:`shake`,label:`Shake`,note:`A quick shake of the head and body, side to side, without leaving the floor.`,make:E(.05,.03,0,12),seconds:1.4,swap:`start`},{id:`stomp`,label:`Stomp`,note:`A small stamp: he drops a little and lands with a bounce.`,make:E(.1,.06,.04,0),seconds:1.4,swap:`start`},{id:`huff`,label:`Huff`,note:`He puffs up, then lets it out with a small lean.`,make:E(.14,.1,0,5),seconds:1.6,swap:`start`}],s=[{id:`annoyed`,title:`1. Tapped a few times: he spins and turns annoyed`,text:`From his normal self, a full turn, and his face changes half way round.`,from:`happy`,to:`annoyed`,variants:n.map(e=>({...e}))},{id:`fuming`,title:`2. Tapped again: he spins and goes fuming`,text:`The same turn, to the fuming one, with his fumes starting up behind him.`,from:`annoyed`,to:`fuming`,variants:n.map(e=>({...e}))},{id:`tuck`,title:`3. Tapped while fuming: he tucks and the fumes move`,text:`He stays fuming and goes back to standing, the fumes puffing harder while he tucks.`,from:`fuming`,to:`fuming`,variants:r},{id:`huff`,title:`Tapped while annoyed: he huffs`,text:`He stays annoyed and answers the tap with a short grumpy move.`,from:`annoyed`,to:`annoyed`,variants:a},{id:`calm`,title:`4. After a while: he spins back to annoyed`,text:`The turn the other way, clockwise, and the fumes go.`,from:`fuming`,to:`annoyed`,clockwise:!0,variants:n.map(e=>({...e}))},{id:`normal`,title:`5. Later still: he spins back to normal`,text:`The same turn back, clockwise, to his normal self.`,from:`annoyed`,to:`happy`,clockwise:!0,variants:n.map(e=>({...e}))}],u=t.only?s.filter(e=>t.only.includes(e.id)):s.filter(e=>e.id!==`huff`),d=p({annoyed:`spot`,fuming:`spot`,tuck:`shudder`,huff:`shake`,calm:`spot`,normal:`hop`}),f=p(``),h=o(`viewer`),_=[],x,S=!1,w=`happy`,T=0,te=0,O=e=>{w=e,x?.show(e===`fuming`?{pose:`fuming`,effects:!0}:e,S)};function k(e,t){let{step:n,variant:r}=e;e.move||(e.move=r.make(!!n.clockwise),O(n.from));let i=Math.min(t,.05);e.elapsed+=i;let a=e.move.advance(i);return x?.pose(a),!e.swapped&&(r.swap===`start`?e.elapsed>.05:Math.abs(a.yaw??0)>Math.PI)&&(e.swapped=!0,n.from!==n.to&&O(n.to),x?.pose(a)),te=n.to===`fuming`&&e.swapped?(r.boost??.4)*Math.max(0,Math.min(1,1-(e.elapsed-r.seconds+.5)/.5)):0,x?.fumes(T,te),e.elapsed<r.seconds?!1:(n.from!==n.to&&!e.swapped&&O(n.to),x?.pose(void 0),te=0,!0)}function A(e){T+=e;let t=_[0];if(!t){w===`fuming`&&x?.fumes(T,0);return}let n;t.kind===`perform`?n=k(t,e):(t.seconds-=e,t.fumes&&x?.fumes(T,0),n=t.seconds<=0),n&&(_.shift(),_.length||(f.value=``))}let j=e=>e.variants.find(t=>t.id===d.value[e.id])??e.variants[0];function M(e,t){d.value={...d.value,[e.id]:t.id},f.value=`${e.id}/${t.id}`,_=[{kind:`perform`,step:e,variant:t,elapsed:0,swapped:!1}],h.value?.play()}function N(){let e=u.find(e=>e.id===(w===`fuming`?`tuck`:w===`annoyed`?`huff`:``));e&&!f.value&&M(e,j(e))}function P(){f.value=`all`,O(u[0].from),_=u.flatMap(e=>[{kind:`wait`,seconds:.9,fumes:!1},{kind:`perform`,step:e,variant:j(e),elapsed:0,swapped:!1},...e.id===`tuck`?[{kind:`wait`,seconds:2.5,fumes:!0}]:[]]),h.value?.play()}async function ne(e){return x=e,{show(e){S=e,O(w)},advance:A,dispose(){_=[],x=void 0}}}w=u[0].from;let re=()=>({page:`mood moves: ${u.map(e=>e.id).join(`, `)}`,...t.plain?{}:{picked:Object.fromEntries(u.map(e=>[e.id,d.value[e.id]]))}});return(t,n)=>(y(),ee(Ie,{ref_key:`viewer`,ref:h,label:`Poulpy in 3D, in his moods from normal to annoyed and fuming. Tap him and he reacts to his mood.`,framing:{tilt:8,distance:9.4,centre:1.35},start:ne,values:re,onTap:N},{default:l(()=>[g(`div`,et,[m(u).length>1?(y(),v(`button`,{key:0,type:`button`,class:`play-all`,"aria-pressed":f.value===`all`,onClick:P},c(e.only?`Play them one after the other`:`Play the whole sequence`),9,tt)):b(``,!0),(y(!0),v(C,null,i(m(u),t=>(y(),v(`section`,{key:t.id,class:`step`},[g(`h3`,null,c(e.only?t.title.replace(/^\d\. /,``):t.title),1),g(`p`,null,c(t.text),1),e.plain?b(``,!0):(y(),v(`div`,{key:0,class:`sides`,role:`group`,"aria-label":t.title},[(y(!0),v(C,null,i(t.variants,e=>(y(),v(`button`,{key:e.id,type:`button`,class:`choice`,"aria-pressed":d.value[t.id]===e.id,title:e.note,onClick:n=>M(t,e)},c(e.label),9,rt))),128))],8,nt)),e.plain?b(``,!0):(y(),v(`p`,it,c(t.variants.find(e=>e.id===d.value[t.id])?.note),1))]))),128))])]),_:1},512))}})})))()}var I;function st(){return(st=e((()=>{ot(),Ne(),I=Fe(at,[[`__scopeId`,`data-v-8a653bf8`]]),at.__docgenInfo=Object.assign({displayName:at.name??at.__name},{exportName:`default`,displayName:`PoulpyMoodMoves`,description:``,tags:{},props:[{name:`only`,required:!1,type:{name:`Array`,elements:[{name:`string`}]}},{name:`plain`,required:!1,type:{name:`boolean`}}],sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/brand/PoulpyMoodMoves.vue`]})})))()}var ct,lt,ut,dt,ft,pt,mt,ht,gt,_t,vt,yt,bt,xt,St,Ct;function wt(){return(wt=e((()=>{S(),T(),k(),Pe(),n(),ct={key:0,class:`sides`,role:`group`,"aria-label":`The eye he shuts, his own`},lt=[`aria-pressed`,`onClick`],ut={key:1,class:`sides`,role:`group`,"aria-label":`Direction, seen from above`},dt=[`aria-pressed`,`aria-label`,`title`,`onClick`],ft=[`aria-pressed`],pt={class:`slider`},mt={class:`slider`},ht={class:`slider`},gt={class:`slider`},_t={class:`slider`},vt={class:`slider`},yt={class:`slider`},bt={key:1,class:`slider`},xt=[`max`],St={key:2,class:`slider`},Ct=h({__name:`PoulpyMove`,props:{motion:{}},setup(e){let t=e.motion===`cheer`,n=e.motion===`wink`,a=t||n,o=e.motion===`jump`||e.motion===`jump-turn`||t,d=e.motion!==`jump`&&!a,h=p(1),S=p(`anticlockwise`),w=p(24),T=p(!0),E=p(0),D=p(0),k=p(0),A=p(0),j=p(1),M=p(0),N=p(0),P=p(`right`),ne=[{id:`right`,label:`His right eye`},{id:`left`,label:`His left eye`}],re=[{id:`anticlockwise`,label:`Anticlockwise`,icon:te},{id:`clockwise`,label:`Clockwise`,icon:O}],ie={turn:`turning around in short steps`,jump:`doing little jumps on the spot`,"jump-turn":`doing little jumps that turn him around`,cheer:`the champion, jumping for joy with his medal while confetti flies`,wink:`the cocky one, winking as stars pop up around him`},F,ae;s(P,e=>{F&&`side`in F&&(F.side=e),ae?.()}),s(h,e=>{F&&`softness`in F&&(F.softness=e)});let oe=()=>d?w.value/24*(S.value===`anticlockwise`?1:-1):0;s([w,S],()=>{F&&`stride`in F&&(F.stride=oe())}),s(T,e=>{F&&`wiggle`in F&&(F.wiggle=e)}),s([E,D,k,A,j,M,N],e=>{F&&`height`in F&&([F.height,F.tuck,F.landing,F.spread,F.hang,F.lean,F.pause]=e)});async function se(e){let{createCheer:i,createJump:a,createTurn:s,createWink:c}=await r(async()=>{let{createCheer:e,createJump:t,createTurn:n,createWink:r}=await import(`./motion-DoAzzIG7.js`).then(e=>(e.c(),e.l));return{createCheer:e,createJump:t,createTurn:n,createWink:r}},__vite__mapDeps([0,1,2,3]),import.meta.url),l;if(n)l=(await r(async()=>{let{winkFace:e}=await import(`./poses-BljLuoRA.js`).then(e=>(e.a(),e.l));return{winkFace:e}},__vite__mapDeps([3,1,2]),import.meta.url)).winkFace,F=c();else if(o){let e=(t?i:a)({softness:h.value,stride:oe(),wiggle:T.value});[E.value,D.value,k.value,A.value,M.value,N.value]=[e.height,e.tuck,e.landing,e.spread,e.lean,e.pause],F=e}else F=s({softness:h.value});let u=F,d=!1,f=()=>e.show(t?{pose:`champion`,effects:!1}:n?{pose:`cocky`,effects:!1,faces:e=>l(e,P.value)}:`blue`,d);return ae=f,{show(e){d=e,f()},advance:t=>e.pose(u.advance(t)),dispose(){F=void 0,ae=void 0}}}function ce(){let t=e=>Math.round(e*1e3)/1e3;return{page:`motion: ${e.motion}`,...n?{eye:P.value}:{},...o?{jump:{height:t(E.value),tuck:t(D.value),landing:t(k.value),spread:t(A.value),hang:t(j.value),lean:t(M.value),pause:t(N.value)}}:{},...d?{step:w.value,wiggle:T.value,direction:S.value}:{},...n?{}:{softArms:t(h.value)}}}return(t,r)=>(y(),ee(Ie,{label:`Poulpy in 3D, ${ie[e.motion]}.`,framing:{tilt:26,distance:9.6},"move-label":n?`Wink`:m(o)?`Jump`:`Turn`,speeds:{max:5,step:.1},start:se,values:ce},_({pickers:l(()=>[n?(y(),v(`div`,ct,[(y(),v(C,null,i(ne,e=>g(`button`,{key:e.id,type:`button`,class:`choice`,"aria-pressed":P.value===e.id,onClick:t=>P.value=e.id},c(e.label),9,lt)),64))])):b(``,!0),m(d)?(y(),v(`div`,ut,[(y(),v(C,null,i(re,e=>g(`button`,{key:e.id,type:`button`,class:`choice icon`,"aria-pressed":S.value===e.id,"aria-label":e.label,title:e.label,onClick:t=>S.value=e.id},[(y(),ee(u(e.icon),{size:18,"aria-hidden":`true`}))],8,dt)),64))])):b(``,!0)]),sliders:l(()=>[m(o)?(y(),v(C,{key:0},[g(`label`,pt,[r[10]||=g(`span`,null,`Height`,-1),f(g(`input`,{"onUpdate:modelValue":r[1]||=e=>E.value=e,type:`range`,min:`0`,max:`1`,step:`0.01`},null,512),[[x,E.value,void 0,{number:!0}]]),g(`output`,null,c(E.value.toFixed(2)),1)]),g(`label`,mt,[r[11]||=g(`span`,null,`Tuck`,-1),f(g(`input`,{"onUpdate:modelValue":r[2]||=e=>D.value=e,type:`range`,min:`0`,max:`0.5`,step:`0.01`},null,512),[[x,D.value,void 0,{number:!0}]]),g(`output`,null,c(D.value.toFixed(2)),1)]),g(`label`,ht,[r[12]||=g(`span`,null,`Landing`,-1),f(g(`input`,{"onUpdate:modelValue":r[3]||=e=>k.value=e,type:`range`,min:`0`,max:`0.5`,step:`0.01`},null,512),[[x,k.value,void 0,{number:!0}]]),g(`output`,null,c(k.value.toFixed(2)),1)]),g(`label`,gt,[r[13]||=g(`span`,null,`Spread`,-1),f(g(`input`,{"onUpdate:modelValue":r[4]||=e=>A.value=e,type:`range`,min:`0`,max:`0.5`,step:`0.01`},null,512),[[x,A.value,void 0,{number:!0}]]),g(`output`,null,c(A.value.toFixed(2)),1)]),g(`label`,_t,[r[14]||=g(`span`,null,`Hang`,-1),f(g(`input`,{"onUpdate:modelValue":r[5]||=e=>j.value=e,type:`range`,min:`0`,max:`4`,step:`0.05`},null,512),[[x,j.value,void 0,{number:!0}]]),g(`output`,null,c(j.value.toFixed(2)),1)]),g(`label`,vt,[r[15]||=g(`span`,null,`Lean`,-1),f(g(`input`,{"onUpdate:modelValue":r[6]||=e=>M.value=e,type:`range`,min:`0`,max:`30`,step:`0.5`},null,512),[[x,M.value,void 0,{number:!0}]]),g(`output`,null,c(M.value.toFixed(1))+`°`,1)]),g(`label`,yt,[r[16]||=g(`span`,null,`Pause`,-1),f(g(`input`,{"onUpdate:modelValue":r[7]||=e=>N.value=e,type:`range`,min:`0`,max:`3`,step:`0.05`},null,512),[[x,N.value,void 0,{number:!0}]]),g(`output`,null,c(N.value.toFixed(2))+` s`,1)])],64)):b(``,!0),m(d)?(y(),v(`label`,bt,[r[17]||=g(`span`,null,`Step`,-1),f(g(`input`,{"onUpdate:modelValue":r[8]||=e=>w.value=e,type:`range`,min:`1`,max:m(o)?720:180,step:`1`},null,8,xt),[[x,w.value,void 0,{number:!0}]]),g(`output`,null,c(w.value)+`°`,1)])):b(``,!0),n?b(``,!0):(y(),v(`label`,St,[r[18]||=g(`span`,null,`Soft arms`,-1),f(g(`input`,{"onUpdate:modelValue":r[9]||=e=>h.value=e,type:`range`,min:`0`,max:`4`,step:`0.05`},null,512),[[x,h.value,void 0,{number:!0}]]),g(`output`,null,c(h.value.toFixed(2)),1)]))]),_:2},[m(d)?{name:`toggles`,fn:l(()=>[g(`button`,{type:`button`,class:`toggle`,"aria-pressed":T.value,title:`On, each step differs a little (20° to 30° around 24°). Off, every step is exactly the Step.`,onClick:r[0]||=e=>T.value=!T.value},`Wiggle`,8,ft)]),key:`0`}:void 0]),1032,[`label`,`move-label`]))}})})))()}var Tt;function Et(){return(Et=e((()=>{wt(),Tt=Ct,Ct.__docgenInfo=Object.assign({displayName:Ct.name??Ct.__name},{exportName:`default`,displayName:`PoulpyMove`,description:``,tags:{},props:[{name:`motion`,required:!0,type:{name:`union`,elements:[{name:`"turn"`},{name:`"jump"`},{name:`"jump-turn"`},{name:`"cheer"`},{name:`"wink"`}]}}],sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/brand/PoulpyMove.vue`]})})))()}var Dt,Ot,kt,At;function jt(){return(jt=e((()=>{F(),fe(),Ae(),_e(),xe(),ge(),ke(),pe(),Ce(),ye(),Se(),he(),ie(),se(),ne(),re(),oe(),P(),S(),Dt={class:`pictures`},Ot=[`aria-label`],kt=[`src`,`alt`],At=h({__name:`PoulpyPictures`,setup(e){let t=Object.assign({"../../../../design/poulpy-3d/pictures/mood_annoyed.png":M,"../../../../design/poulpy-3d/pictures/mood_fuming.png":je,"../../../../design/poulpy-3d/pictures/mood_grumpy.png":Me,"../../../../design/poulpy-3d/pictures/mood_happy.png":ue,"../../../../design/poulpy-3d/pictures/mood_proud.png":ve,"../../../../design/poulpy-3d/pictures/mood_recovering.png":we,"../../../../design/poulpy-3d/pictures/pose_champion.png":me,"../../../../design/poulpy-3d/pictures/pose_cocky.png":be,"../../../../design/poulpy-3d/pictures/pose_exhausted.png":De,"../../../../design/poulpy-3d/pictures/pose_fuming.png":de,"../../../../design/poulpy-3d/pictures/pose_patched_up.png":Ee,"../../../../design/poulpy-3d/pictures/pose_pouting.png":Oe,"../../../../design/poulpy-3d/pictures/pose_shy.png":j,"../../../../design/poulpy-3d/pictures/pose_side_glance.png":le,"../../../../design/poulpy-3d/pictures/pose_sleeping.png":ce,"../../../../design/poulpy-3d/pictures/pose_standing_tall.png":Te,"../../../../design/poulpy-3d/pictures/pose_straining.png":N,"../../../../design/poulpy-3d/pictures/pose_thinking.png":ae}),n=e=>e.replace(/^(mood|pose)_/,``).replace(/_/g,` `),r=e=>Object.entries(t).map(([e,t])=>({name:e.slice(e.lastIndexOf(`/`)+1,-4),url:t})).filter(({name:t})=>t.startsWith(`${e}_`)),a=[{title:`Moods`,pictures:r(`mood`)},{title:`Poses`,pictures:r(`pose`)}];return(e,t)=>(y(),v(`div`,Dt,[(y(),v(C,null,i(a,e=>g(`section`,{key:e.title,"aria-label":e.title},[g(`h3`,null,c(e.title),1),g(`ul`,null,[(y(!0),v(C,null,i(e.pictures,e=>(y(),v(`li`,{key:e.name},[g(`img`,{src:e.url,alt:`Poulpy, ${n(e.name)}`,width:`248`,height:`226`,loading:`lazy`},null,8,kt),g(`span`,null,c(n(e.name)),1)]))),128))])],8,Ot)),64))]))}})})))()}var Mt;function Nt(){return(Nt=e((()=>{jt(),Ne(),Mt=Fe(At,[[`__scopeId`,`data-v-764512e2`]]),At.__docgenInfo=Object.assign({displayName:At.name??At.__name},{exportName:`default`,displayName:`PoulpyPictures`,description:``,tags:{},sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/brand/PoulpyPictures.vue`]})})))()}var Pt=t({Annoyed:()=>q,BackToNormal:()=>Q,Book:()=>$,CalmDown:()=>Z,Cheering:()=>W,Fuming:()=>K,Jumping:()=>H,JumpingAround:()=>U,Model:()=>L,Moods:()=>R,Pictures:()=>B,Poses:()=>z,Sequence:()=>J,ToAnnoyed:()=>Y,ToFuming:()=>X,Turning:()=>V,Winking:()=>G,__namedExportsOrder:()=>It,default:()=>Ft}),Ft,L,R,z,B,V,H,U,W,G,K,q,J,Y,X,Z,Q,$,It;function Lt(){return(Lt=e((()=>{We(),$e(),st(),Et(),Nt(),Ft={title:`Brand/Live`,tags:[`!dev`,`!autodocs`],parameters:{layout:`fullscreen`}},L={render:()=>({components:{PoulpyModel:Qe},template:`<PoulpyModel />`})},R={render:()=>({components:{PoulpyModel:Qe},template:`<PoulpyModel moods />`})},z={render:()=>({components:{PoulpyModel:Qe},template:`<PoulpyModel poses />`})},B={render:()=>({components:{PoulpyPictures:Mt},template:`<PoulpyPictures />`})},V={render:()=>({components:{PoulpyMove:Tt},template:`<PoulpyMove motion="turn" />`})},H={render:()=>({components:{PoulpyMove:Tt},template:`<PoulpyMove motion="jump" />`})},U={render:()=>({components:{PoulpyMove:Tt},template:`<PoulpyMove motion="jump-turn" />`})},W={render:()=>({components:{PoulpyMove:Tt},template:`<PoulpyMove motion="cheer" />`})},G={render:()=>({components:{PoulpyMove:Tt},template:`<PoulpyMove motion="wink" />`})},K={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves :only="['tuck']" />`})},q={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves :only="['huff']" />`})},J={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves plain />`})},Y={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves :only="['annoyed']" />`})},X={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves :only="['fuming']" />`})},Z={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves :only="['calm']" />`})},Q={render:()=>({components:{PoulpyMoodMoves:I},template:`<PoulpyMoodMoves :only="['normal']" />`})},$={render:()=>({components:{BrandBook:Ue},template:`<BrandBook />`}),parameters:{a11y:{context:{exclude:[`.wm.l .name .p`]}}}},It=[`Model`,`Moods`,`Poses`,`Pictures`,`Turning`,`Jumping`,`JumpingAround`,`Cheering`,`Winking`,`Fuming`,`Annoyed`,`Sequence`,`ToAnnoyed`,`ToFuming`,`CalmDown`,`BackToNormal`,`Book`],L.parameters={...L.parameters,docs:{...L.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyModel
    },
    template: '<PoulpyModel />'
  })
}`,...L.parameters?.docs?.source},description:{story:`Poulpy in 3D, still, to turn around by hand.`,...L.parameters?.docs?.description}}},R.parameters={...R.parameters,docs:{...R.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyModel
    },
    template: '<PoulpyModel moods />'
  })
}`,...R.parameters?.docs?.source},description:{story:`Poulpy in 3D in each of his six moods.`,...R.parameters?.docs?.description}}},z.parameters={...z.parameters,docs:{...z.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyModel
    },
    template: '<PoulpyModel poses />'
  })
}`,...z.parameters?.docs?.source},description:{story:`Poulpy in 3D in each of his twelve poses.`,...z.parameters?.docs?.description}}},B.parameters={...B.parameters,docs:{...B.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyPictures
    },
    template: '<PoulpyPictures />'
  })
}`,...B.parameters?.docs?.source},description:{story:`The pictures painted from the 3D Poulpy: his six moods and twelve poses.`,...B.parameters?.docs?.description}}},V.parameters={...V.parameters,docs:{...V.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMove
    },
    template: '<PoulpyMove motion="turn" />'
  })
}`,...V.parameters?.docs?.source},description:{story:`Poulpy in 3D, turning in short steps, with the controls to try other feels.`,...V.parameters?.docs?.description}}},H.parameters={...H.parameters,docs:{...H.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMove
    },
    template: '<PoulpyMove motion="jump" />'
  })
}`,...H.parameters?.docs?.source},description:{story:`Poulpy in 3D, doing little jumps on the spot, with the controls to tune the jump.`,...H.parameters?.docs?.description}}},U.parameters={...U.parameters,docs:{...U.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMove
    },
    template: '<PoulpyMove motion="jump-turn" />'
  })
}`,...U.parameters?.docs?.source},description:{story:`The same jumps, each turning him a step.`,...U.parameters?.docs?.description}}},W.parameters={...W.parameters,docs:{...W.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMove
    },
    template: '<PoulpyMove motion="cheer" />'
  })
}`,...W.parameters?.docs?.source},description:{story:`The champion jumping for joy: his medal bounces on its ribbon and confetti flies at each jump.`,...W.parameters?.docs?.description}}},G.parameters={...G.parameters,docs:{...G.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMove
    },
    template: '<PoulpyMove motion="wink" />'
  })
}`,...G.parameters?.docs?.source},description:{story:`The cocky one: eyes wide open, then a wink and a smirk as his stars pop up and twinkle.`,...G.parameters?.docs?.description}}},K.parameters={...K.parameters,docs:{...K.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves :only="['tuck']" />\`
  })
}`,...K.parameters?.docs?.source},description:{story:`Poulpy fuming: how he answers a tap, no transitions.`,...K.parameters?.docs?.description}}},q.parameters={...q.parameters,docs:{...q.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves :only="['huff']" />\`
  })
}`,...q.parameters?.docs?.source},description:{story:`Poulpy annoyed: how he answers a tap, no transitions.`,...q.parameters?.docs?.description}}},J.parameters={...J.parameters,docs:{...J.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves plain />\`
  })
}`,...J.parameters?.docs?.source},description:{story:`The whole sequence from his normal self to fuming and back, played in one go.`,...J.parameters?.docs?.description}}},Y.parameters={...Y.parameters,docs:{...Y.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves :only="['annoyed']" />\`
  })
}`,...Y.parameters?.docs?.source},description:{story:`Normal to annoyed: the spin.`,...Y.parameters?.docs?.description}}},X.parameters={...X.parameters,docs:{...X.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves :only="['fuming']" />\`
  })
}`,...X.parameters?.docs?.source},description:{story:`Annoyed to fuming: the spin.`,...X.parameters?.docs?.description}}},Z.parameters={...Z.parameters,docs:{...Z.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves :only="['calm']" />\`
  })
}`,...Z.parameters?.docs?.source},description:{story:`Fuming back to annoyed: the spin the other way.`,...Z.parameters?.docs?.description}}},Q.parameters={...Q.parameters,docs:{...Q.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      PoulpyMoodMoves
    },
    template: \`<PoulpyMoodMoves :only="['normal']" />\`
  })
}`,...Q.parameters?.docs?.source},description:{story:`Annoyed back to normal: the spin the other way.`,...Q.parameters?.docs?.description}}},$.parameters={...$.parameters,docs:{...$.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      BrandBook
    },
    template: '<BrandBook />'
  }),
  // The light wordmark is the logo in its plush blue; WCAG 1.4.3 exempts logotypes from the contrast minimum.
  parameters: {
    a11y: {
      context: {
        exclude: ['.wm.l .name .p']
      }
    }
  }
}`,...$.parameters?.docs?.source},description:{story:`The brand book, drawn live.`,...$.parameters?.docs?.description}}}})))()}export{V as _,Z as a,H as c,R as d,B as f,X as g,Y as h,Pt as i,U as l,J as m,Q as n,W as o,z as p,$ as r,K as s,q as t,L as u,G as v,Lt as y};
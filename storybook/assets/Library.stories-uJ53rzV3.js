import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-Bv7eIkwo.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
    <MobileSearchField label="Search your workouts" placeholder="Name, type or exercise" />
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">7 workouts</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li><MobileSettingsRow card icon="list" title="Lower body strength" detail="Sets · 5 exercises · 40 min" note="Done today" note-tone="done" start="Start Lower body strength" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Demo intervals" detail="Circuit · 3 parts · 24 min" note="Last done Fri 23 Oct" start="Start Demo intervals" /></li>
    <li><MobileSettingsRow card icon="list" title="Upper body" detail="Sets · 4 exercises · 32 min" note="Planned · Tue 27 Oct · +2" note-tone="planned" start="Start Upper body" /></li>
    <li><MobileSettingsRow card icon="list" title="Full body" detail="Sets · 6 exercises · 42 min" note="Planned · Thu 29 Oct" note-tone="planned" start="Start Full body" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Core and mobility" detail="Circuit · 5 exercises · 18 min" note="Last done Mon 5 Oct" start="Start Core and mobility" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Easy mobility" detail="Circuit · 6 exercises · 15 min" note="Not done yet" start="Start Easy mobility" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Quick sweat" detail="Circuit · 4 rounds · 16 min" note="Not done yet" start="Start Quick sweat" /></li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
  <div class="scrim" aria-hidden="true"></div>
  <div class="row-menu" id="row-menu">
    <li><MobileSettingsRow card icon="list" title="Lower body strength" detail="Sets · 5 exercises · 40 min" note="Done today" note-tone="done" start="Start Lower body strength" /></li>
    <div class="popover-menu" role="menu" data-component="popover-menu">
      <button type="button" role="menuitem"><MobileIcon name="calendar-plus" :size="22" />Plan for a day</button>
      <button type="button" role="menuitem"><MobileIcon name="copy" :size="22" />Copy</button>
      <hr>
      <button type="button" class="danger" role="menuitem"><MobileIcon name="trash" :size="22" />Delete</button>
    </div>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
  </div>

  <div style="flex: 1; min-height: 0; overflow: hidden; padding: var(--cp-space-sm) var(--cp-space-lg) 0;">
    <section class="empty-card" id="library-empty" data-component="empty-card" aria-label="No workouts yet">
      <img src="../../../poulpy/mood_happy.svg" alt="Poulpy, happy">
      <h2>No workouts yet</h2>
      <p>Build one once, then plan it for any day or keep it here for later.</p>
      <MobileButton size="md" icon="plus">Build your first workout</MobileButton>
    </section>
  </div>

  <MobileNavBar active="library" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Exercises" />
    <MobileSearchField label="Search exercises" placeholder="Name, muscle, mobility or floor" />
    <div class="chip-row" role="group" aria-label="Filters">
      <MobileFilterChip icon="dumbbell" aria-haspopup="dialog" aria-label="Equipment" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="target" aria-haspopup="dialog" aria-label="Focus" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="armchair" aria-haspopup="dialog" aria-label="Position" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="person-standing" aria-haspopup="dialog" aria-label="Zones" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="tag" aria-haspopup="dialog" aria-label="Tags" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">34 exercises</span>
    <button type="button" aria-label="Sort: Movement">Movement<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <div id="catalog" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg) var(--cp-space-lg); display: flex; flex-direction: column; gap: 6px;">
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Squat</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="person-standing" name="Sit to stand" detail="Quadriceps, glutes · No equipment" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Bodyweight squat" detail="Quadriceps, glutes · No equipment" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Goblet squat" detail="Quadriceps, glutes · Dumbbell or kettlebell" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Wall sit" detail="Quadriceps, glutes · Hold" /></li>
    </ul>
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Hinge</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="hand-grab" name="Glute bridge" detail="Glutes, hamstrings · Mat" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Kettlebell swing" detail="Glutes, hamstrings · Kettlebell" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Romanian deadlift" detail="Hamstrings, glutes · Dumbbells or barbell" /></li>
    </ul>
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Lunge</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="footprints" name="Reverse lunge" detail="Quadriceps, glutes · Each side" /></li>
      <li><MobileExerciseRow link icon="footprints" name="Step-up" detail="Quadriceps, glutes · Bench · each side" /></li>
      <li><MobileExerciseRow link icon="footprints" name="Walking lunge" detail="Quadriceps, glutes · Each side" /></li>
    </ul>
  </div>

  <MobileNavBar active="library" />
</main>
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Exercises" />
    <MobileSearchField label="Search exercises" placeholder="Name, muscle, mobility or floor" />
    <div class="chip-row" role="group" aria-label="Filters">
      <MobileFilterChip icon="dumbbell" aria-haspopup="dialog" aria-label="Equipment" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="target" aria-haspopup="dialog" aria-label="Focus" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="armchair" aria-haspopup="dialog" aria-label="Position" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="person-standing" aria-haspopup="dialog" aria-label="Zones" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="tag" aria-haspopup="dialog" aria-label="Tags" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">34 exercises</span>
    <button type="button" aria-label="Sort: Equipment">Equipment<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <div id="catalog" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg) var(--cp-space-lg); display: flex; flex-direction: column; gap: 6px;">
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">No equipment</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="person-standing" name="Bodyweight squat" detail="Quadriceps, glutes" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Glute bridge" detail="Glutes, hamstrings" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Sit to stand" detail="Quadriceps, glutes" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Wall sit" detail="Quadriceps, glutes · Hold" /></li>
    </ul>
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Dumbbells</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="person-standing" name="Goblet squat" detail="Quadriceps, glutes" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Romanian deadlift" detail="Hamstrings, glutes" /></li>
    </ul>
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Kettlebell</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="hand-grab" name="Kettlebell swing" detail="Glutes, hamstrings" /></li>
    </ul>
  </div>

  <MobileNavBar active="library" />
</main>
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Exercises" />
    <MobileSearchField label="Search exercises" placeholder="Name, muscle, mobility or floor" />
    <div class="chip-row" role="group" aria-label="Filters">
      <MobileFilterChip icon="dumbbell" aria-haspopup="dialog" aria-label="Equipment" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="target" aria-haspopup="dialog" aria-label="Focus" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="armchair" aria-haspopup="dialog" aria-label="Position" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="person-standing" aria-haspopup="dialog" aria-label="Zones" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="tag" aria-haspopup="dialog" aria-label="Tags" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">34 exercises</span>
    <button type="button" aria-label="Sort: Name">Name<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <div id="catalog" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg) var(--cp-space-lg); display: flex; flex-direction: column; gap: 6px;">
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="person-standing" name="Bodyweight squat" detail="Quadriceps, glutes · No equipment" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Glute bridge" detail="Glutes, hamstrings · Mat" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Goblet squat" detail="Quadriceps, glutes · Dumbbell or kettlebell" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Kettlebell swing" detail="Glutes, hamstrings · Kettlebell" /></li>
      <li><MobileExerciseRow link icon="footprints" name="Reverse lunge" detail="Quadriceps, glutes · Each side" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Romanian deadlift" detail="Hamstrings, glutes · Dumbbells or barbell" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Sit to stand" detail="Quadriceps, glutes · No equipment" /></li>
      <li><MobileExerciseRow link icon="footprints" name="Step-up" detail="Quadriceps, glutes · Bench · each side" /></li>
    </ul>
  </div>

  <MobileNavBar active="library" />
</main>
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Exercises" />
    <MobileSearchField label="Search exercises" placeholder="Name, muscle, mobility or floor" />
    <div class="chip-row" role="group" aria-label="Filters">
      <MobileFilterChip icon="dumbbell" aria-haspopup="dialog" aria-label="Equipment" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="target" aria-haspopup="dialog" aria-label="Focus" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="armchair" aria-haspopup="dialog" aria-label="Position" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="person-standing" aria-haspopup="dialog" aria-label="Zones" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
      <MobileFilterChip icon="tag" aria-haspopup="dialog" aria-label="Tags" style="padding: 0 var(--cp-space-md); min-width: 48px;" />
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">34 exercises</span>
    <button type="button" aria-label="Sort: Movement" aria-haspopup="menu" aria-expanded="true">Movement<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <div id="catalog" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg) var(--cp-space-lg); display: flex; flex-direction: column; gap: 6px;">
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Squat</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="person-standing" name="Sit to stand" detail="Quadriceps, glutes · No equipment" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Bodyweight squat" detail="Quadriceps, glutes · No equipment" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Goblet squat" detail="Quadriceps, glutes · Dumbbell or kettlebell" /></li>
      <li><MobileExerciseRow link icon="person-standing" name="Wall sit" detail="Quadriceps, glutes · Hold" /></li>
    </ul>
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Hinge</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="hand-grab" name="Glute bridge" detail="Glutes, hamstrings · Mat" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Kettlebell swing" detail="Glutes, hamstrings · Kettlebell" /></li>
      <li><MobileExerciseRow link icon="hand-grab" name="Romanian deadlift" detail="Hamstrings, glutes · Dumbbells or barbell" /></li>
    </ul>
    <h2 class="label" style="margin: 10px var(--cp-space-xs) 2px;">Lunge</h2>
    <ul style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <li><MobileExerciseRow link icon="footprints" name="Reverse lunge" detail="Quadriceps, glutes · Each side" /></li>
      <li><MobileExerciseRow link icon="footprints" name="Step-up" detail="Quadriceps, glutes · Bench · each side" /></li>
      <li><MobileExerciseRow link icon="footprints" name="Walking lunge" detail="Quadriceps, glutes · Each side" /></li>
    </ul>
  </div>


  <MobileNavBar active="library" />
  <div class="scrim" aria-hidden="true"></div>
  <div class="popover-menu" role="menu" aria-label="Sort exercises" data-component="popover-menu" style="position: absolute; top: 296px; right: 20px;">
    <button type="button" role="menuitemradio" aria-checked="true"><MobileIcon name="check" :size="22" />Movement</button>
    <button type="button" role="menuitemradio" aria-checked="false"><span style="width: 22px;" aria-hidden="true"></span>Name</button>
    <button type="button" role="menuitemradio" aria-checked="false"><span style="width: 22px;" aria-hidden="true"></span>Equipment</button>
  </div>
</main>
`})))()}var h;function g(){return(g=e((()=>{h=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
    <MobileSearchField label="Search your workouts" placeholder="Name, type or exercise" />
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">7 workouts</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li><MobileSettingsRow card icon="list" title="Lower body strength" detail="Sets · 5 exercises · 40 min" note="Done today" note-tone="done" start="Start Lower body strength" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Demo intervals" detail="Circuit · 3 parts · 24 min" note="Last done Fri 23 Oct" start="Start Demo intervals" /></li>
    <li><MobileSettingsRow card icon="list" title="Upper body" detail="Sets · 4 exercises · 32 min" note="Planned · Tue 27 Oct · +2" note-tone="planned" start="Start Upper body" /></li>
    <li><MobileSettingsRow card icon="list" title="Full body" detail="Sets · 6 exercises · 42 min" note="Planned · Thu 29 Oct" note-tone="planned" start="Start Full body" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Core and mobility" detail="Circuit · 5 exercises · 18 min" note="Last done Mon 5 Oct" start="Start Core and mobility" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Easy mobility" detail="Circuit · 6 exercises · 15 min" note="Not done yet" start="Start Easy mobility" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Quick sweat" detail="Circuit · 4 rounds · 16 min" note="Not done yet" start="Start Quick sweat" /></li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />

  <div class="scrim" aria-hidden="true"></div>
  <MobileSheet id="choose-format" aria-label="How is this workout built?" style="gap: var(--cp-space-md);">
    <span class="sheet-handle" aria-hidden="true"></span>
    <div class="sheet-head" style="padding: 0;">
      <div class="sheet-title" style="flex: 1;"><h2>How is this workout built?</h2><p>You can change it later in the builder.</p></div>
      <button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button>
    </div>
    <button class="format-card" data-component="format-card" type="button" aria-pressed="false">
      <span><span class="round-icon" aria-hidden="true"><MobileIcon name="repeat" :size="20" /></span>Circuit</span>
      <p>Rounds through several exercises, each done once per round. Fits HIIT, bodyweight circuits and runs.</p>
      <span class="example">Squat 15 · Push-up 10 · Run 200 m, 4 rounds</span>
    </button>
    <button class="format-card" data-component="format-card" type="button" aria-pressed="false">
      <span><span class="round-icon" aria-hidden="true"><MobileIcon name="list" :size="20" /></span>Sets</span>
      <p>One exercise at a time: all its sets, then the next. Fits gym and strength training.</p>
      <span class="example">Bench press 3 × 8, then Row 3 × 10</span>
    </button>
  </MobileSheet>
</main>
`})))()}var _;function v(){return(v=e((()=>{_=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
    <MobileSearchField label="Search your workouts" placeholder="Name, type or exercise" value="zumba" />
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">0 matches</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li class="no-results" data-component="no-results" style="list-style: none;">
      <img src="../../../poulpy/mood_happy.svg" alt="Poulpy, happy">
      <h2>Nothing for “zumba”</h2>
      <p>Try another word, or look in the exercises to build one.</p>
      <MobileButton size="md">Clear search and filters</MobileButton>
    </li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
</main>
`})))()}var y;function b(){return(b=e((()=>{y=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
    <MobileSearchField label="Search your workouts" placeholder="Name, type or exercise" value="squat" />
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">3 matches</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li><MobileSettingsRow card icon="list" title="Lower body strength" detail="Sets · 5 exercises · 40 min" note="Done today" note-tone="done" start="Start Lower body strength" /></li>
    <li><MobileSettingsRow card icon="list" title="Full body" detail="Sets · 6 exercises · 42 min" note="Planned · Thu 29 Oct" note-tone="planned" start="Start Full body" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Quick sweat" detail="Circuit · 4 rounds · 16 min" note="Not done yet" start="Start Quick sweat" /></li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
</main>
`})))()}var x;function S(){return(S=e((()=>{x=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
    <MobileSearchField label="Search your workouts" placeholder="Name, type or exercise" />
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">7 workouts</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<MobileIcon name="chevron-down" :size="16" /></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li><MobileSettingsRow card icon="list" title="Lower body strength" detail="Sets · 5 exercises · 40 min" note="Done today" note-tone="done" start="Start Lower body strength" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Demo intervals" detail="Circuit · 3 parts · 24 min" note="Last done Fri 23 Oct" start="Start Demo intervals" /></li>
    <li><MobileSettingsRow card icon="list" title="Upper body" detail="Sets · 4 exercises · 32 min" note="Planned · Tue 27 Oct · +2" note-tone="planned" start="Start Upper body" /></li>
    <li><MobileSettingsRow card icon="list" title="Full body" detail="Sets · 6 exercises · 42 min" note="Planned · Thu 29 Oct" note-tone="planned" start="Start Full body" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Core and mobility" detail="Circuit · 5 exercises · 18 min" note="Last done Mon 5 Oct" start="Start Core and mobility" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Easy mobility" detail="Circuit · 6 exercises · 15 min" note="Not done yet" start="Start Easy mobility" /></li>
    <li><MobileSettingsRow card icon="repeat" title="Quick sweat" detail="Circuit · 4 rounds · 16 min" note="Not done yet" start="Start Quick sweat" /></li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
</main>
`})))()}var C,w,T,E,D,O,k,A,j,M,N,P;function F(){return(F=e((()=>{t(),i(),o(),c(),u(),f(),m(),g(),v(),b(),S(),C={title:`Mobile/Screens/Library`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},w={name:`Workout actions`,tags:[`!autodocs`,`agreed`],...n(r),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},T={name:`No workouts yet`,...n(a),parameters:{mockup:{issue:104}}},E={name:`Exercises`,tags:[`!autodocs`,`agreed`],...n(s),parameters:{mockup:{issue:104}}},D={name:`Exercises by equipment`,tags:[`!autodocs`,`agreed`],...n(l),parameters:{mockup:{issue:104}}},O={name:`Exercises by name`,tags:[`!autodocs`,`agreed`],...n(d),parameters:{mockup:{issue:104}}},k={name:`Exercises sort menu`,tags:[`!autodocs`,`agreed`],...n(p),parameters:{mockup:{issue:104}}},A={name:`New workout, format sheet`,tags:[`!autodocs`,`agreed`],...n(h),parameters:{mockup:{issue:242}}},j={name:`No workout found`,...n(_),parameters:{mockup:{issue:104}}},M={name:`Search “squat”`,tags:[`!autodocs`,`agreed`],...n(y),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},N={name:`Workouts`,tags:[`!autodocs`,`agreed`],...n(x),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},P=[`LibraryActions`,`LibraryEmpty`,`LibraryExercises`,`LibraryExercisesByEquipment`,`LibraryExercisesByName`,`LibraryExercisesSort`,`LibraryNewWorkoutSheet`,`LibraryNoResults`,`LibrarySearch`,`LibraryWorkouts`],w.parameters={...w.parameters,docs:{...w.parameters?.docs,source:{originalSource:`{
  name: "Workout actions",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryActions),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...w.parameters?.docs?.source},description:{story:`Library · workout actions.`,...w.parameters?.docs?.description}}},T.parameters={...T.parameters,docs:{...T.parameters?.docs,source:{originalSource:`{
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...T.parameters?.docs?.source},description:{story:`Library · no workouts yet.`,...T.parameters?.docs?.description}}},E.parameters={...E.parameters,docs:{...E.parameters?.docs,source:{originalSource:`{
  name: "Exercises",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercises),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...E.parameters?.docs?.source},description:{story:`Library · exercises.`,...E.parameters?.docs?.description}}},D.parameters={...D.parameters,docs:{...D.parameters?.docs,source:{originalSource:`{
  name: "Exercises by equipment",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercisesByEquipment),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...D.parameters?.docs?.source},description:{story:`Library · exercises grouped by equipment, then by name.`,...D.parameters?.docs?.description}}},O.parameters={...O.parameters,docs:{...O.parameters?.docs,source:{originalSource:`{
  name: "Exercises by name",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercisesByName),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...O.parameters?.docs?.source},description:{story:`Library · exercises by name, no groups.`,...O.parameters?.docs?.description}}},k.parameters={...k.parameters,docs:{...k.parameters?.docs,source:{originalSource:`{
  name: "Exercises sort menu",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercisesSort),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...k.parameters?.docs?.source},description:{story:`Library · the sort menu: movement, name or equipment.`,...k.parameters?.docs?.description}}},A.parameters={...A.parameters,docs:{...A.parameters?.docs,source:{originalSource:`{
  name: "New workout, format sheet",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryNewWorkoutSheet),
  parameters: {
    mockup: {
      issue: 242
    }
  }
}`,...A.parameters?.docs?.source},description:{story:`Library · "New workout" opens the format sheet over the Library.`,...A.parameters?.docs?.description}}},j.parameters={...j.parameters,docs:{...j.parameters?.docs,source:{originalSource:`{
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...j.parameters?.docs?.source},description:{story:`Library · no workout found.`,...j.parameters?.docs?.description}}},M.parameters={...M.parameters,docs:{...M.parameters?.docs,source:{originalSource:`{
  name: "Search “squat”",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibrarySearch),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...M.parameters?.docs?.source},description:{story:`Library · search “squat”.`,...M.parameters?.docs?.description}}},N.parameters={...N.parameters,docs:{...N.parameters?.docs,source:{originalSource:`{
  name: "Workouts",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryWorkouts),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...N.parameters?.docs?.source},description:{story:`Library · workouts.`,...N.parameters?.docs?.description}}}})))()}F();export{w as LibraryActions,T as LibraryEmpty,E as LibraryExercises,D as LibraryExercisesByEquipment,O as LibraryExercisesByName,k as LibraryExercisesSort,A as LibraryNewWorkoutSheet,j as LibraryNoResults,M as LibrarySearch,N as LibraryWorkouts,P as __namedExportsOrder,C as default};
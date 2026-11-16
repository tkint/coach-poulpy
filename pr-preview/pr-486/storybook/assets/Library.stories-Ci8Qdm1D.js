import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-BztoV7W4.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
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
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></button>
      <button type="button" class="row-icon" aria-label="Start Lower body strength"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Demo intervals</strong><span>Circuit · 3 parts · 24 min</span><span class="row-note">Last done Fri 23 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Demo intervals"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Upper body</strong><span>Sets · 4 exercises · 32 min</span><span class="row-note row-note--planned">Planned · Tue 27 Oct · +2</span></button>
      <button type="button" class="row-icon" aria-label="Start Upper body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Full body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Core and mobility</strong><span>Circuit · 5 exercises · 18 min</span><span class="row-note">Last done Mon 5 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Core and mobility"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Easy mobility</strong><span>Circuit · 6 exercises · 15 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Easy mobility"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Quick sweat"><MobileIcon name="play" :size="20" /></button>
    </li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
  <div class="scrim" aria-hidden="true"></div>
  <div class="row-menu" id="row-menu">
    <li class="list-row" data-component="library-entry">
        <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
        <button type="button" class="row-text"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></button>
        <button type="button" class="row-icon" aria-label="Start Lower body strength"><MobileIcon name="play" :size="20" /></button>
      </li>
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
      <MobileButton icon="plus" :icon-size="22">Build your first workout</MobileButton>
    </section>
  </div>

  <MobileNavBar active="library" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
<div class="screen-header" style="padding-bottom: var(--cp-space-sm); gap: var(--cp-space-md);">
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

  <div class="list-meta"><span aria-live="polite">34 exercises</span></div>

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
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></button>
      <button type="button" class="row-icon" aria-label="Start Lower body strength"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Demo intervals</strong><span>Circuit · 3 parts · 24 min</span><span class="row-note">Last done Fri 23 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Demo intervals"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Upper body</strong><span>Sets · 4 exercises · 32 min</span><span class="row-note row-note--planned">Planned · Tue 27 Oct · +2</span></button>
      <button type="button" class="row-icon" aria-label="Start Upper body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Full body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Core and mobility</strong><span>Circuit · 5 exercises · 18 min</span><span class="row-note">Last done Mon 5 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Core and mobility"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Easy mobility</strong><span>Circuit · 6 exercises · 15 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Easy mobility"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Quick sweat"><MobileIcon name="play" :size="20" /></button>
    </li>
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
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone">
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
      <MobileButton>Clear search and filters</MobileButton>
    </li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
</main>
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone">
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
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></button>
      <button type="button" class="row-icon" aria-label="Start Lower body strength"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Full body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Quick sweat"><MobileIcon name="play" :size="20" /></button>
    </li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
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
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></button>
      <button type="button" class="row-icon" aria-label="Start Lower body strength"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Demo intervals</strong><span>Circuit · 3 parts · 24 min</span><span class="row-note">Last done Fri 23 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Demo intervals"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Upper body</strong><span>Sets · 4 exercises · 32 min</span><span class="row-note row-note--planned">Planned · Tue 27 Oct · +2</span></button>
      <button type="button" class="row-icon" aria-label="Start Upper body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="list" :size="22" /></span>
      <button type="button" class="row-text"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Full body"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Core and mobility</strong><span>Circuit · 5 exercises · 18 min</span><span class="row-note">Last done Mon 5 Oct</span></button>
      <button type="button" class="row-icon" aria-label="Start Core and mobility"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Easy mobility</strong><span>Circuit · 6 exercises · 15 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Easy mobility"><MobileIcon name="play" :size="20" /></button>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="repeat" :size="22" /></span>
      <button type="button" class="row-text"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></button>
      <button type="button" class="row-icon" aria-label="Start Quick sweat"><MobileIcon name="play" :size="20" /></button>
    </li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
</main>
`})))()}var _,v,y,b,x,S,C,w,T;function E(){return(E=e((()=>{t(),i(),o(),c(),u(),f(),m(),g(),_={title:`Mobile/Screens/Library`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},v={name:`Workout actions`,...n(r),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},y={name:`No workouts yet`,...n(a),parameters:{mockup:{issue:104}}},b={name:`Exercises`,...n(s),parameters:{mockup:{issue:104}}},x={name:`New workout, format sheet`,tags:[`!autodocs`,`agreed`],...n(l),parameters:{mockup:{issue:242}}},S={name:`No workout found`,...n(d),parameters:{mockup:{issue:104}}},C={name:`Search “squat”`,...n(p),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},w={name:`Workouts`,...n(h),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},T=[`LibraryActions`,`LibraryEmpty`,`LibraryExercises`,`LibraryNewWorkoutSheet`,`LibraryNoResults`,`LibrarySearch`,`LibraryWorkouts`],v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  name: "Workout actions",
  ...screen(screenLibraryActions),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...v.parameters?.docs?.source},description:{story:`Library · workout actions.`,...v.parameters?.docs?.description}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...y.parameters?.docs?.source},description:{story:`Library · no workouts yet.`,...y.parameters?.docs?.description}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  name: "Exercises",
  ...screen(screenLibraryExercises),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...b.parameters?.docs?.source},description:{story:`Library · exercises.`,...b.parameters?.docs?.description}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  name: "New workout, format sheet",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryNewWorkoutSheet),
  parameters: {
    mockup: {
      issue: 242
    }
  }
}`,...x.parameters?.docs?.source},description:{story:`Library · "New workout" opens the format sheet over the Library.`,...x.parameters?.docs?.description}}},S.parameters={...S.parameters,docs:{...S.parameters?.docs,source:{originalSource:`{
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...S.parameters?.docs?.source},description:{story:`Library · no workout found.`,...S.parameters?.docs?.description}}},C.parameters={...C.parameters,docs:{...C.parameters?.docs,source:{originalSource:`{
  name: "Search “squat”",
  ...screen(screenLibrarySearch),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...C.parameters?.docs?.source},description:{story:`Library · search “squat”.`,...C.parameters?.docs?.description}}},w.parameters={...w.parameters,docs:{...w.parameters?.docs,source:{originalSource:`{
  name: "Workouts",
  ...screen(screenLibraryWorkouts),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...w.parameters?.docs?.source},description:{story:`Library · workouts.`,...w.parameters?.docs?.description}}}})))()}E();export{v as LibraryActions,y as LibraryEmpty,b as LibraryExercises,x as LibraryNewWorkoutSheet,S as LibraryNoResults,C as LibrarySearch,w as LibraryWorkouts,T as __namedExportsOrder,_ as default};
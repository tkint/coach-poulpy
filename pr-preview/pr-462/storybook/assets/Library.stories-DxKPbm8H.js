import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-BTFDCI6G.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
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
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone">
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
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone">
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
`})))()}var h,g,_,v,y,b,x,S;function C(){return(C=e((()=>{t(),i(),o(),c(),u(),f(),m(),h={title:`Mobile/Screens/Library`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},g={name:`Workout actions`,...n(r),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},_={name:`No workouts yet`,...n(a),parameters:{mockup:{issue:104}}},v={name:`Exercises`,...n(s),parameters:{mockup:{issue:104}}},y={name:`No workout found`,...n(l),parameters:{mockup:{issue:104}}},b={name:`Search “squat”`,...n(d),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},x={name:`Workouts`,...n(p),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},S=[`LibraryActions`,`LibraryEmpty`,`LibraryExercises`,`LibraryNoResults`,`LibrarySearch`,`LibraryWorkouts`],g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
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
}`,...g.parameters?.docs?.source},description:{story:`Library · workout actions.`,...g.parameters?.docs?.description}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,..._.parameters?.docs?.source},description:{story:`Library · no workouts yet.`,..._.parameters?.docs?.description}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  name: "Exercises",
  ...screen(screenLibraryExercises),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...v.parameters?.docs?.source},description:{story:`Library · exercises.`,...v.parameters?.docs?.description}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...y.parameters?.docs?.source},description:{story:`Library · no workout found.`,...y.parameters?.docs?.description}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
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
}`,...b.parameters?.docs?.source},description:{story:`Library · search “squat”.`,...b.parameters?.docs?.description}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
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
}`,...x.parameters?.docs?.source},description:{story:`Library · workouts.`,...x.parameters?.docs?.description}}}})))()}C();export{g as LibraryActions,_ as LibraryEmpty,v as LibraryExercises,y as LibraryNoResults,b as LibrarySearch,x as LibraryWorkouts,S as __namedExportsOrder,h as default};
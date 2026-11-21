import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-DhxQsO5r.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
<MobileTopBar title="Library" />
<div class="screen-header" style="padding-top: 0; padding-bottom: 10px; gap: var(--cp-space-md);">
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
<MobileTopBar title="Library" />
<div class="screen-header" style="padding-top: 0; padding-bottom: 10px; gap: var(--cp-space-md);">
    <MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="Workouts" />
  </div>

  <div style="flex: 1; min-height: 0; overflow: hidden; padding: var(--cp-space-sm) var(--cp-space-lg) 0;">
    <MobileEmptyState id="library-empty" title="No workouts yet">
      Build one once, then plan it for any day or keep it here for later.
      <template #action><MobileButton kind="secondary" size="sm" icon="plus">Build your first workout</MobileButton></template>
    </MobileEmptyState>
  </div>

  <MobileNavBar active="library" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
<MobileTopBar title="Library" />
<div class="screen-header" style="padding-top: 0; padding-bottom: 10px; gap: var(--cp-space-md);">
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
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone">
<MobileTopBar title="Library" />
<div class="screen-header" style="padding-top: 0; padding-bottom: 10px; gap: var(--cp-space-md);">
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
    <li style="list-style: none;">
      <MobileEmptyState title="Nothing for “zumba”">
        Try another word, or look in the exercises to build one.
        <template #action><MobileButton kind="secondary" size="sm">Clear search and filters</MobileButton></template>
      </MobileEmptyState>
    </li>
  </ul>

  <button type="button" class="fab" data-component="fab"><MobileIcon name="plus" :size="22" />New workout</button>

  <MobileNavBar active="library" />
</main>
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone">
<MobileTopBar title="Library" />
<div class="screen-header" style="padding-top: 0; padding-bottom: 10px; gap: var(--cp-space-md);">
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
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone">
<MobileTopBar title="Library" />
<div class="screen-header" style="padding-top: 0; padding-bottom: 10px; gap: var(--cp-space-md);">
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
`})))()}var h;function g(){return(g=e((()=>{h=`<main class="phone" style="--frame-height: 760px">
  <MobileTopBar leading="close" />

  <div class="page-body" style="gap: var(--cp-space-lg);">
    <div style="display: flex; flex-direction: column; gap: var(--cp-space-xs);">
      <h1 class="title" style="font-size: 30px; line-height: 34px;">Can’t add this workout</h1>
    </div>

    <section class="card" id="error" data-component="error-card" role="alert" style="display: flex; gap: var(--cp-space-sm); align-items: flex-start; background: var(--cp-surface-variant); color: var(--cp-error);">
      <MobileIcon name="circle-exclamation-point" style="flex: none;" />
      <p style="font-weight: 700;">This workout uses an exercise this version doesn’t have: from-the-future. Update the app to open it.</p>
    </section>

    <p class="muted">Nothing was added to your library.</p>
  </div>

  <div class="action-bar">
    <MobileButton kind="secondary" size="md">Close</MobileButton>
  </div>
</main>
`})))()}var _,v,y,b,x,S,C,w,T;function E(){return(E=e((()=>{t(),i(),o(),c(),u(),f(),m(),g(),_={title:`Mobile/Screens/Library/Workouts`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},v={name:`Workout actions`,tags:[`!autodocs`,`agreed`],...n(r),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},y={name:`No workouts yet`,...n(a),parameters:{mockup:{issue:104}}},b={name:`New workout, format sheet`,tags:[`!autodocs`,`agreed`],...n(s),parameters:{mockup:{issue:242}}},x={name:`No workout found`,...n(l),parameters:{mockup:{issue:104}}},S={name:`Search “squat”`,tags:[`!autodocs`,`agreed`],...n(d),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},C={name:`Workouts`,tags:[`!autodocs`,`agreed`],...n(p),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},w={name:`Refused`,...n(h),parameters:{mockup:{issue:129}}},T=[`LibraryActions`,`LibraryEmpty`,`LibraryNewWorkoutSheet`,`LibraryNoResults`,`LibrarySearch`,`LibraryWorkouts`,`WorkoutImportError`],v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
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
}`,...v.parameters?.docs?.source},description:{story:`Library · workout actions.`,...v.parameters?.docs?.description}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...y.parameters?.docs?.source},description:{story:`Library · no workouts yet.`,...y.parameters?.docs?.description}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  name: "New workout, format sheet",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryNewWorkoutSheet),
  parameters: {
    mockup: {
      issue: 242
    }
  }
}`,...b.parameters?.docs?.source},description:{story:`Library · "New workout" opens the format sheet over the Library.`,...b.parameters?.docs?.description}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...x.parameters?.docs?.source},description:{story:`Library · no workout found.`,...x.parameters?.docs?.description}}},S.parameters={...S.parameters,docs:{...S.parameters?.docs,source:{originalSource:`{
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
}`,...S.parameters?.docs?.source},description:{story:`Library · search “squat”.`,...S.parameters?.docs?.description}}},C.parameters={...C.parameters,docs:{...C.parameters?.docs,source:{originalSource:`{
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
}`,...C.parameters?.docs?.source},description:{story:`Library · workouts.`,...C.parameters?.docs?.description}}},w.parameters={...w.parameters,docs:{...w.parameters?.docs,source:{originalSource:`{
  name: "Refused",
  ...screen(screenWorkoutImportError),
  parameters: {
    mockup: {
      issue: 129
    }
  }
}`,...w.parameters?.docs?.source},description:{story:`Workout import · refused.`,...w.parameters?.docs?.description}}}})))()}E();export{v as LibraryActions,y as LibraryEmpty,b as LibraryNewWorkoutSheet,x as LibraryNoResults,S as LibrarySearch,C as LibraryWorkouts,w as WorkoutImportError,T as __namedExportsOrder,_ as default};
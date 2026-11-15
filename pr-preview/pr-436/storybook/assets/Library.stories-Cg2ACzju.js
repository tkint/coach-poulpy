import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-DfYKRFCZ.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <div class="tabs" role="tablist" aria-label="Library" data-component="tabs">
      <a role="tab" href="#" aria-selected="true">Workouts</a>
      <a role="tab" href="#" aria-selected="false">Exercises</a>
    </div>
    <label class="search-field" data-component="search-field">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="search"><path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/></svg>
      <input type="search" placeholder="Name, type or exercise" aria-label="Search your workouts">
    </label>
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">7 workouts</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="chevron-down"><path d="m6 9 6 6 6-6"/></svg></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></a>
      <a class="row-icon" href="#" aria-label="Start Lower body strength"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Demo intervals</strong><span>Circuit · 3 parts · 24 min</span><span class="row-note">Last done Fri 23 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Demo intervals"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Upper body</strong><span>Sets · 4 exercises · 32 min</span><span class="row-note row-note--planned">Planned · Tue 27 Oct · +2</span></a>
      <a class="row-icon" href="#" aria-label="Start Upper body"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Full body"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Core and mobility</strong><span>Circuit · 5 exercises · 18 min</span><span class="row-note">Last done Mon 5 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Core and mobility"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Easy mobility</strong><span>Circuit · 6 exercises · 15 min</span><span class="row-note">Not done yet</span></a>
      <a class="row-icon" href="#" aria-label="Start Easy mobility"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></a>
      <a class="row-icon" href="#" aria-label="Start Quick sweat"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
  </ul>

  <a class="fab" data-component="fab" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="plus"><path d="M5 12h14"/><path d="M12 5v14"/></svg>New workout</a>

  <MobileNavBar active="library" />
  <div class="scrim" aria-hidden="true"></div>
  <div class="row-menu" id="row-menu">
    <li class="list-row" data-component="library-entry">
        <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
        <a class="row-text" href="#"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></a>
        <a class="row-icon" href="#" aria-label="Start Lower body strength"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
      </li>
    <div class="popover-menu" role="menu" data-component="popover-menu">
      <a role="menuitem" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="calendar-plus"><path d="M16 18h6"/><path d="M16 2v3"/><path d="M19 15v6"/><path d="M21 11.5V5a2 2 0 00-2-2H5a2 2 0 00-2 2v14a2 2 0 002 2h8.3"/><path d="M3 9h18"/><path d="M8 2v3"/></svg>Plan for a day</a>
      <a role="menuitem" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="copy"><rect width="14" height="14" x="8" y="8" rx="2" ry="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/></svg>Copy</a>
      <hr>
      <a class="danger" role="menuitem" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="trash"><path d="M10 11v6"/><path d="M14 11v6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M3 6h18"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>Delete</a>
    </div>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <div class="tabs" role="tablist" aria-label="Library" data-component="tabs">
      <a role="tab" href="#" aria-selected="true">Workouts</a>
      <a role="tab" href="#" aria-selected="false">Exercises</a>
    </div>
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
    <div class="tabs" role="tablist" aria-label="Library" data-component="tabs">
      <a role="tab" href="#" aria-selected="false">Workouts</a>
      <a role="tab" href="#" aria-selected="true">Exercises</a>
    </div>
    <label class="search-field" data-component="search-field">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="search"><path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/></svg>
      <input type="search" placeholder="Name, muscle, mobility or floor" aria-label="Search exercises">
    </label>
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
    <div class="tabs" role="tablist" aria-label="Library" data-component="tabs">
      <a role="tab" href="#" aria-selected="true">Workouts</a>
      <a role="tab" href="#" aria-selected="false">Exercises</a>
    </div>
    <label class="search-field search-field--filled" data-component="search-field">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="search"><path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/></svg>
      <input type="search" value="zumba" placeholder="Name, type or exercise" aria-label="Search your workouts">
      <button class="clear-button" type="button" aria-label="Clear search"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="x"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg></button>
    </label>
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">0 matches</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="chevron-down"><path d="m6 9 6 6 6-6"/></svg></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li class="no-results" data-component="no-results" style="list-style: none;">
      <img src="../../../poulpy/mood_happy.svg" alt="Poulpy, happy">
      <h2>Nothing for “zumba”</h2>
      <p>Try another word, or look in the exercises to build one.</p>
      <MobileButton>Clear search and filters</MobileButton>
    </li>
  </ul>

  <a class="fab" data-component="fab" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="plus"><path d="M5 12h14"/><path d="M12 5v14"/></svg>New workout</a>

  <MobileNavBar active="library" />
</main>
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <div class="tabs" role="tablist" aria-label="Library" data-component="tabs">
      <a role="tab" href="#" aria-selected="true">Workouts</a>
      <a role="tab" href="#" aria-selected="false">Exercises</a>
    </div>
    <label class="search-field search-field--filled" data-component="search-field">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="search"><path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/></svg>
      <input type="search" value="squat" placeholder="Name, type or exercise" aria-label="Search your workouts">
      <button class="clear-button" type="button" aria-label="Clear search"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="x"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg></button>
    </label>
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">3 matches</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="chevron-down"><path d="m6 9 6 6 6-6"/></svg></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></a>
      <a class="row-icon" href="#" aria-label="Start Lower body strength"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Full body"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></a>
      <a class="row-icon" href="#" aria-label="Start Quick sweat"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
  </ul>

  <a class="fab" data-component="fab" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="plus"><path d="M5 12h14"/><path d="M12 5v14"/></svg>New workout</a>

  <MobileNavBar active="library" />
</main>
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone">
<div class="screen-header" style="padding-bottom: 10px; gap: var(--cp-space-md);">
    <h1>Library</h1>
    <div class="tabs" role="tablist" aria-label="Library" data-component="tabs">
      <a role="tab" href="#" aria-selected="true">Workouts</a>
      <a role="tab" href="#" aria-selected="false">Exercises</a>
    </div>
    <label class="search-field" data-component="search-field">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="search"><path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/></svg>
      <input type="search" placeholder="Name, type or exercise" aria-label="Search your workouts">
    </label>
    <div class="chip-row" role="group" aria-label="Format">
      <MobileFilterChip aria-pressed="true">All</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Circuit</MobileFilterChip>
      <MobileFilterChip aria-pressed="false">Sets</MobileFilterChip>
    </div>
  </div>

  <div class="list-meta">
    <span aria-live="polite">7 workouts</span>
    <button type="button" aria-label="Sort: Recently done">Recently done<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="chevron-down"><path d="m6 9 6 6 6-6"/></svg></button>
  </div>

  <ul id="library" style="flex: 1; min-height: 0; overflow: hidden; padding: 0 var(--cp-space-lg); display: flex; flex-direction: column; gap: var(--cp-space-sm);">
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Lower body strength</strong><span>Sets · 5 exercises · 40 min</span><span class="row-note row-note--done">Done today</span></a>
      <a class="row-icon" href="#" aria-label="Start Lower body strength"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Demo intervals</strong><span>Circuit · 3 parts · 24 min</span><span class="row-note">Last done Fri 23 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Demo intervals"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Upper body</strong><span>Sets · 4 exercises · 32 min</span><span class="row-note row-note--planned">Planned · Tue 27 Oct · +2</span></a>
      <a class="row-icon" href="#" aria-label="Start Upper body"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="list"><path d="M3 5h.01"/><path d="M3 12h.01"/><path d="M3 19h.01"/><path d="M8 5h13"/><path d="M8 12h13"/><path d="M8 19h13"/></svg></span>
      <a class="row-text" href="#"><strong>Full body</strong><span>Sets · 6 exercises · 42 min</span><span class="row-note row-note--planned">Planned · Thu 29 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Full body"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Core and mobility</strong><span>Circuit · 5 exercises · 18 min</span><span class="row-note">Last done Mon 5 Oct</span></a>
      <a class="row-icon" href="#" aria-label="Start Core and mobility"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Easy mobility</strong><span>Circuit · 6 exercises · 15 min</span><span class="row-note">Not done yet</span></a>
      <a class="row-icon" href="#" aria-label="Start Easy mobility"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
    <li class="list-row" data-component="library-entry">
      <span class="row-icon" aria-hidden="true"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="repeat"><path d="m17 2 4 4-4 4"/><path d="M3 11v-1a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v1a4 4 0 0 1-4 4H3"/></svg></span>
      <a class="row-text" href="#"><strong>Quick sweat</strong><span>Circuit · 4 rounds · 16 min</span><span class="row-note">Not done yet</span></a>
      <a class="row-icon" href="#" aria-label="Start Quick sweat"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="play"><path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"/></svg></a>
    </li>
  </ul>

  <a class="fab" data-component="fab" href="#"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" data-lucide="plus"><path d="M5 12h14"/><path d="M12 5v14"/></svg>New workout</a>

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
}`,...g.parameters?.docs?.source},description:{story:`Library · workout actions. Was design/mockups/agreed/phone/library-actions.html.`,...g.parameters?.docs?.description}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,..._.parameters?.docs?.source},description:{story:`Library · no workouts yet. Was design/mockups/agreed/phone/library-empty.html.`,..._.parameters?.docs?.description}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  name: "Exercises",
  ...screen(screenLibraryExercises),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...v.parameters?.docs?.source},description:{story:`Library · exercises. Was design/mockups/agreed/phone/library-exercises.html.`,...v.parameters?.docs?.description}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...y.parameters?.docs?.source},description:{story:`Library · no workout found. Was design/mockups/agreed/phone/library-no-results.html.`,...y.parameters?.docs?.description}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
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
}`,...b.parameters?.docs?.source},description:{story:`Library · search “squat”. Was design/mockups/agreed/phone/library-search.html.`,...b.parameters?.docs?.description}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
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
}`,...x.parameters?.docs?.source},description:{story:`Library · workouts. Was design/mockups/agreed/phone/library-workouts.html.`,...x.parameters?.docs?.description}}}})))()}C();export{g as LibraryActions,_ as LibraryEmpty,v as LibraryExercises,y as LibraryNoResults,b as LibrarySearch,x as LibraryWorkouts,S as __namedExportsOrder,h as default};
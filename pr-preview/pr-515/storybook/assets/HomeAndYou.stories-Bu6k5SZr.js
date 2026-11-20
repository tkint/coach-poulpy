import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-xjtIg7L0.js";var r;function i(){return(i=e((()=>{r=`<main class="phone" style="--frame-height: 900px;">
  <MobileTopBar title="Achievements" leading="back" />
  <div class="page-body" style="padding-top: var(--cp-space-sm);">
    <MobilePoulpyCard id="ring" aria-label="Poulpy" title="5 of 8 tentacles lit" meta="1 full octopus so far">
      <template #art><img src="../../../poulpy/mood_happy.svg" alt="Poulpy, happy" width="71" height="64"></template>
      Three more sessions and I turn into a champion.
      <div class="count-dots" style="margin-top: var(--cp-space-sm);" aria-hidden="true"><i class="lit"></i><i class="lit"></i><i class="lit"></i><i class="lit"></i><i class="lit"></i><i></i><i></i><i></i></div>
    </MobilePoulpyCard>
    <span class="label label-row">Earned</span>
    <div class="achievement-grid" id="earned">
      <div><MobileAchievement size="lg" icon="award" name="First record" />First record<small>12 Oct</small></div>
      <div><MobileAchievement size="lg" icon="target" name="Goal met" />Goal met<small>18 Oct</small></div>
      <div><MobileAchievement size="lg" icon="footprints" name="First walk or run" />First walk<small>20 Oct</small></div>
    </div>
    <span class="label label-row">Still hidden</span>
    <div class="achievement-grid" id="locked">
      <div><MobileAchievement size="lg" locked name="" />?</div>
      <div><MobileAchievement size="lg" locked name="" />?</div>
      <div><MobileAchievement size="lg" locked name="" />?</div>
    </div>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <MobileTopBar title="Home"><MobileButton size="sm" icon="circle-dot">Record</MobileButton></MobileTopBar>
  <div class="page-body" style="padding-top: var(--cp-space-sm); gap: var(--cp-space-sm);">
    <MobileMotivationRow :lit="5" label="5 of 8 tentacles lit. Open achievements." picture="../../../poulpy/mood_happy.svg" picture-alt="Poulpy, happy"><MobileAchievement icon="award" name="First record" /><MobileAchievement icon="target" name="Goal met" /><MobileAchievement locked name="" /></MobileMotivationRow>
    <span class="label label-row">Up next</span>
    <article class="card card--highlight" id="up-next" data-component="planned-workout-card" style="display: flex; flex-direction: column; gap: var(--cp-space-sm); padding: var(--cp-space-sm);">
      <div style="display: flex; align-items: center; gap: var(--cp-space-sm);">
        <span class="row-icon" aria-hidden="true"><MobileIcon name="dumbbell" :size="22" /></span>
        <button type="button" class="row-text"><strong>Lower body strength</strong><span>Today · Sets · about 40 min</span></button>
        <MobileButton size="md" icon="play">Start</MobileButton>
      </div>
    </article>
    <button type="button" class="list-row" id="tomorrow" data-component="list-row" style="border-radius: var(--cp-radius-lg); padding: var(--cp-space-sm);">
      <span class="row-icon" aria-hidden="true"><MobileIcon name="gauge" :size="22" /></span>
      <span class="row-text"><strong>Easy run</strong><span>Tomorrow · about 25 min</span></span>
      <span class="chevron"><MobileIcon name="chevron-right" :size="20" /></span>
    </button>
    <span class="label label-row" style="margin-top: var(--cp-space-xs);">Past</span>
    <button type="button" class="feed-card" id="week-recap"><span><strong>Your week</strong><small>4 sessions · 2 h 10 · legs most worked</small></span><MobileIcon name="chevron-right" :size="20" /></button>
    <button type="button" class="session-row" data-component="session-row"><span class="row-icon" aria-hidden="true"><MobileIcon name="dumbbell" :size="22" /></span><span><strong>Upper body</strong><small>Thu 22 Oct · 32:10 · 15 of 15 sets</small></span></button>
    <div class="feed-card" id="record-card"><MobileAchievement icon="award" name="First record" /><span><strong>New record</strong><small>Goblet squat, 24 kg</small></span></div>
    <button type="button" class="session-row" data-component="session-row"><span class="row-icon" aria-hidden="true"><MobileIcon name="gauge" :size="22" /></span><span><strong>Easy run</strong><small>Tue 20 Oct · 24:00 · 3.4 km</small></span></button>
  </div>
  <MobileNavBar active="home" destinations="three" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
  <MobileTopBar title="Home"><MobileButton size="sm" icon="circle-dot">Record</MobileButton></MobileTopBar>
  <div class="page-body" style="padding-top: var(--cp-space-sm); gap: var(--cp-space-sm);">
    <MobileMotivationRow :lit="5" label="5 of 8 tentacles lit. Open achievements." picture="../../../poulpy/mood_happy.svg" picture-alt="Poulpy, happy"><MobileAchievement icon="award" name="First record" /><MobileAchievement icon="target" name="Goal met" /><MobileAchievement locked name="" /></MobileMotivationRow>
    <span class="label label-row">Up next</span>
    <article class="card" id="suggestion" style="display: flex; flex-direction: column; gap: var(--cp-space-sm); padding: var(--cp-space-sm);">
      <div style="display: flex; align-items: center; gap: var(--cp-space-sm);">
        <span class="row-icon" aria-hidden="true"><MobileIcon name="gauge" :size="22" /></span>
        <button type="button" class="row-text"><strong>Easy run</strong><span>Nothing planned · a quick one, about 25 min</span></button>
        <MobileButton size="md" icon="play">Start</MobileButton>
      </div>
    </article>
    <button type="button" class="text-link" style="align-self: flex-start; margin: -4px var(--cp-space-xs) 0;">Pick something else from your library<MobileIcon name="chevron-right" :size="18" /></button>
    <span class="label label-row" style="margin-top: var(--cp-space-xs);">Past</span>
    <button type="button" class="session-row" data-component="session-row"><span class="row-icon" aria-hidden="true"><MobileIcon name="dumbbell" :size="22" /></span><span><strong>Lower body strength</strong><small>Sat 24 Oct · 38:24 · 17 of 18 sets</small></span></button>
    <button type="button" class="session-row" data-component="session-row"><span class="row-icon" aria-hidden="true"><MobileIcon name="gauge" :size="22" /></span><span><strong>Treadmill</strong><small>Fri 23 Oct · 24:00 · 3.4 km</small></span></button>
  </div>
  <MobileNavBar active="home" destinations="three" />
</main>
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone" style="--frame-height: 900px;">
  <MobileTopBar title="Preferences" leading="back" />
  <div class="page-body" style="padding-top: var(--cp-space-sm);">
    <span class="label label-row">App</span>
    <div class="settings-list" id="app">
      <MobileSettingsRow icon="contrast" title="Appearance and units" detail="System theme · English · Metric" />
      <MobileSettingsRow icon="gauge" title="Devices" detail="3 connected · treadmill, foot pod, heart rate band" />
    </div>
    <span class="field-note">The rows are today's Settings, unchanged inside: this screen is Settings under its new name, reached from the cog on You.</span>
  </div>
</main>
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone" style="--frame-height: 1500px;">
  <MobileTopBar title="Profile" leading="back"><MobileButton kind="primary" size="sm">Done</MobileButton></MobileTopBar>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <span class="label label-row">Account</span>
    <div class="settings-list" id="account-list">
      <MobileSettingsRow icon="user-round" title="Guest" detail="Your training is on this phone only." />
      <MobileSettingsRow icon="circle-plus" title="Create an account" detail="Back up your training and use it on other phones." />
      <MobileSettingsRow icon="log-in" title="Sign in" detail="Already have an account?" />
    </div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">About you</span>
    <div class="field" id="name"><label class="label" for="display-name">What should Poulpy call you?</label><input class="text-field" id="display-name" value="Alex"></div>
    <MobileSelectField label="Poulpy face" value="Happy" id="face" />
    <span class="field-note">The face shown on your profile card. It never changes Poulpy's mood in the app.</span>
    <MobileSelectField label="Birthdate" value="14 March 1991 · 34 years" />
    <MobileSelectField label="Pronouns" value="ze / hir" id="pronouns" />
    <span class="field-note">Used for French wording and Poulpy’s lines. A custom entry is repeated as written; French wording that needs a gender stays neutral.</span>
    <MobileSelectField label="Sex assigned at birth" value="Prefer not to say" />
    <span class="field-note">Only used for estimates such as calories and heart rate zones. Skip it and Poulpy uses a neutral average.</span>
    <MobileSelectField label="Body figure" value="Wider shoulders" id="body-figure" />
    <span class="field-note">Only the drawing used for pain and muscles. It is separate from the line above and is never used for estimates.</span>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">Training</span>
    <MobileSelectField label="Goals" value="Run further, Build strength" />
    <MobileSelectField label="Experience" value="Regular" />
    <div class="field" id="resting-hr"><label class="label" for="resting-hr-input">Resting heart rate</label><span class="number-field" data-component="number-field"><input id="resting-hr-input" inputmode="numeric" value="58" aria-describedby="resting-hr-note"><span class="unit">bpm</span></span><span class="field-note" id="resting-hr-note">Between 30 and 120. Used for heart rate zones.</span></div>
    <MobileSettingsRow icon="ruler" title="Measurements" detail="72.6 kg · 175 cm" />
    <MobileSelectField label="Pain or rehab" value="Left knee, left hip · until 4 Nov" id="pain-or-rehab" />
    <span class="field-note">Poulpy stays on the blue side until then. With nothing set, this row says None.</span>
    <MobilePoulpyCard id="patched-up-note" title="Taking it easy"><template #art><img src="../../../poulpy/pose_patched_up.svg" alt="" width="71" height="64"></template>Poulpy is patched up with you until 4 Nov. No catching up, just rest.</MobilePoulpyCard>
  </div>
</main>
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone" style="--frame-height: 1700px;">
  <MobileTopBar title="Profile" leading="back"><MobileButton kind="primary" size="sm">Done</MobileButton></MobileTopBar>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <span class="label label-row">Account</span>
    <div class="settings-list" id="account-list">
      <MobileSettingsRow icon="mail" title="Email" detail="alex@example.com · confirmed" />
      <MobileSettingsRow icon="key-round" title="Password" detail="Change it" />
      <MobileSettingsRow icon="smartphone" title="Signed-in devices" detail="This phone, Ana’s iPhone" />
      <MobileSettingsRow icon="download" title="Export my data" detail="JSON, and your sessions as FIT files" />
      <MobileSettingsRow icon="log-out" title="Sign out" detail="This phone becomes an empty guest." />
      <MobileSettingsRow icon="trash" title="Delete account" detail="Removes your data from every phone." />
    </div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">About you</span>
    <div class="field" id="name"><label class="label" for="display-name">What should Poulpy call you?</label><input class="text-field" id="display-name" value="Alex"></div>
    <MobileSelectField label="Poulpy face" value="Happy" id="face" />
    <span class="field-note">The face shown on your profile card. It never changes Poulpy's mood in the app.</span>
    <MobileSelectField label="Birthdate" value="14 March 1991 · 34 years" />
    <MobileSelectField label="Pronouns" value="ze / hir" id="pronouns" />
    <span class="field-note">Used for French wording and Poulpy’s lines. A custom entry is repeated as written; French wording that needs a gender stays neutral.</span>
    <MobileSelectField label="Sex assigned at birth" value="Prefer not to say" />
    <span class="field-note">Only used for estimates such as calories and heart rate zones. Skip it and Poulpy uses a neutral average.</span>
    <MobileSelectField label="Body figure" value="Wider shoulders" id="body-figure" />
    <span class="field-note">Only the drawing used for pain and muscles. It is separate from the line above and is never used for estimates.</span>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">Training</span>
    <MobileSelectField label="Goals" value="Run further, Build strength" />
    <MobileSelectField label="Experience" value="Regular" />
    <div class="field" id="resting-hr"><label class="label" for="resting-hr-input">Resting heart rate</label><span class="number-field" data-component="number-field"><input id="resting-hr-input" inputmode="numeric" value="58" aria-describedby="resting-hr-note"><span class="unit">bpm</span></span><span class="field-note" id="resting-hr-note">Between 30 and 120. Used for heart rate zones.</span></div>
    <MobileSettingsRow icon="ruler" title="Measurements" detail="72.6 kg · 175 cm" />
    <MobileSelectField label="Pain or rehab" value="Left knee, left hip · until 4 Nov" id="pain-or-rehab" />
    <span class="field-note">Poulpy stays on the blue side until then. With nothing set, this row says None.</span>
    <MobilePoulpyCard id="patched-up-note" title="Taking it easy"><template #art><img src="../../../poulpy/pose_patched_up.svg" alt="" width="71" height="64"></template>Poulpy is patched up with you until 4 Nov. No catching up, just rest.</MobilePoulpyCard>
  </div>
</main>
`})))()}var h;function g(){return(g=e((()=>{h=`<main class="phone">
  <MobileTopBar title="Record" leading="close" />
  <div class="page-body" style="padding-top: var(--cp-space-sm);">
    <span class="label label-row">What are you recording?</span>
    <MobileSettingsRow card icon="dumbbell" title="Free session" detail="Log exercises and sets as you go" />
    <MobileSettingsRow card icon="gauge" title="Walk or run" detail="Follow it live, with your phone or a treadmill" />
    <MobileSettingsRow card icon="library-big" title="A workout from your library" detail="Pick one and follow its steps" />
  </div>
</main>
`})))()}var _;function v(){return(v=e((()=>{_=`<main class="phone" style="--frame-height: 900px;">
  <MobileTopBar title="Edit statistics" leading="close"><MobileButton kind="primary" size="sm">Done</MobileButton></MobileTopBar>
  <div class="page-body" style="padding-top: var(--cp-space-sm); gap: var(--cp-space-sm);">
    <span class="field-note">Choose the blocks you want on You, and drag the handle to reorder them.</span>
    <div class="block-toggle" id="b1"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Sessions and time</strong><span class="switch" role="switch" aria-checked="true" aria-label="Sessions and time"></span></div>
    <div class="block-toggle"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Records and weight</strong><span class="switch" role="switch" aria-checked="true" aria-label="Records and weight"></span></div>
    <div class="block-toggle"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Body map</strong><span class="switch" role="switch" aria-checked="true" aria-label="Body map"></span></div>
    <div class="block-toggle"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Focus split</strong><span class="switch" role="switch" aria-checked="true" aria-label="Focus split"></span></div>
    <div class="block-toggle"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Cardio</strong><span class="switch" role="switch" aria-checked="true" aria-label="Cardio"></span></div>
    <div class="block-toggle"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Volume and load</strong><span class="switch" role="switch" aria-checked="false" aria-label="Volume and load"></span></div>
    <div class="block-toggle"><span class="grip" aria-hidden="true"><MobileIcon name="grip-vertical" :size="22" /></span><strong>Time of day</strong><span class="switch" role="switch" aria-checked="false" aria-label="Time of day"></span></div>
  </div>
</main>
`})))()}var y;function b(){return(b=e((()=>{y=`<main class="phone" style="--frame-height: 900px;">
  <MobileTopBar title="You"><MobileIconButton icon="settings" :icon-size="22" aria-label="Preferences" /></MobileTopBar>
  <div class="page-body" style="padding-top: var(--cp-space-sm);">
    <button type="button" class="profile-card" id="profile" data-component="profile-card"><img src="../../../poulpy/mood_happy.svg" alt="" aria-hidden="true"><span><strong>Alex</strong><small>Build strength · Regular</small><span class="account-badge">Guest</span></span><MobileIcon name="chevron-right" :size="22" /></button>
    <div class="section-head"><span class="label">Statistics</span><MobileButton kind="tonal" size="sm" icon="pencil">Edit</MobileButton></div>
    <div class="period-tabs" role="tablist" aria-label="Period" id="period"><button type="button" role="tab" aria-selected="false">7 days</button><button type="button" role="tab" aria-selected="true">30 days</button><button type="button" role="tab" aria-selected="false">Year</button><button type="button" role="tab" aria-selected="false">All time</button></div>
    <div class="stat-pair">
      <section class="stat-card stat-card--highlight" id="sessions"><span class="label" style="color: inherit;">Sessions</span><span class="big">11</span><span class="line">386 min · 14.8 km</span></section>
      <section class="stat-card" id="weight"><span class="label label--primary">Weight</span><span class="big">72.6 kg</span><span class="line muted">3 days ago</span></section>
    </div>
    <div class="stat-trio" id="records">
      <section class="stat-card"><span class="name">Records</span><span class="value">3</span><span class="note">this period</span></section>
      <section class="stat-card"><span class="name">Longest run</span><span class="value">5.2 km</span><span class="note">8 Oct</span></section>
      <section class="stat-card"><span class="name">Full octopus</span><span class="value">×1</span><span class="note">5 of 8 lit</span></section>
    </div>
    <button type="button" class="list-row" id="body-map" data-component="list-row" style="border-radius: var(--cp-radius-lg); padding: var(--cp-space-sm);"><span class="row-icon" aria-hidden="true"><MobileIcon name="person-standing" :size="22" /></span><span class="row-text"><strong>Body map</strong><span>Legs most worked, shoulders least</span></span><span class="chevron"><MobileIcon name="chevron-right" :size="20" /></span></button>
  </div>
  <MobileNavBar active="you" destinations="three" />
</main>
`})))()}var x;function S(){return(S=e((()=>{x=`<main class="phone" style="--frame-height: 900px;">
  <MobileTopBar title="You"><MobileIconButton icon="settings" :icon-size="22" aria-label="Preferences" /></MobileTopBar>
  <div class="page-body" style="padding-top: var(--cp-space-sm);">
    <button type="button" class="profile-card" id="profile" data-component="profile-card"><img src="../../../poulpy/mood_happy.svg" alt="" aria-hidden="true"><span><strong>Alex</strong><small>Build strength · Regular</small><span class="account-badge">Confirm your email</span></span><MobileIcon name="chevron-right" :size="22" /></button>
    <div class="section-head"><span class="label">Statistics</span><MobileButton kind="tonal" size="sm" icon="pencil">Edit</MobileButton></div>
    <div class="period-tabs" role="tablist" aria-label="Period" id="period"><button type="button" role="tab" aria-selected="false">7 days</button><button type="button" role="tab" aria-selected="true">30 days</button><button type="button" role="tab" aria-selected="false">Year</button><button type="button" role="tab" aria-selected="false">All time</button></div>
    <div class="stat-pair">
      <section class="stat-card stat-card--highlight" id="sessions"><span class="label" style="color: inherit;">Sessions</span><span class="big">11</span><span class="line">386 min · 14.8 km</span></section>
      <section class="stat-card" id="weight"><span class="label label--primary">Weight</span><span class="big">72.6 kg</span><span class="line muted">3 days ago</span></section>
    </div>
    <div class="stat-trio" id="records">
      <section class="stat-card"><span class="name">Records</span><span class="value">3</span><span class="note">this period</span></section>
      <section class="stat-card"><span class="name">Longest run</span><span class="value">5.2 km</span><span class="note">8 Oct</span></section>
      <section class="stat-card"><span class="name">Full octopus</span><span class="value">×1</span><span class="note">5 of 8 lit</span></section>
    </div>
    <button type="button" class="list-row" id="body-map" data-component="list-row" style="border-radius: var(--cp-radius-lg); padding: var(--cp-space-sm);"><span class="row-icon" aria-hidden="true"><MobileIcon name="person-standing" :size="22" /></span><span class="row-text"><strong>Body map</strong><span>Legs most worked, shoulders least</span></span><span class="chevron"><MobileIcon name="chevron-right" :size="20" /></span></button>
  </div>
  <MobileNavBar active="you" destinations="three" />
</main>
`})))()}var C;function w(){return(w=e((()=>{C=`<main class="phone" style="--frame-height: 900px;">
  <MobileTopBar title="You"><MobileIconButton icon="settings" :icon-size="22" aria-label="Preferences" /></MobileTopBar>
  <div class="page-body" style="padding-top: var(--cp-space-sm);">
    <button type="button" class="profile-card" id="profile" data-component="profile-card"><img src="../../../poulpy/mood_happy.svg" alt="" aria-hidden="true"><span><strong>Alex</strong><small>Build strength · Regular</small><span class="account-badge account-badge--signed-in"><MobileIcon name="check" :size="14" />Signed in</span></span><MobileIcon name="chevron-right" :size="22" /></button>
    <div class="section-head"><span class="label">Statistics</span><MobileButton kind="tonal" size="sm" icon="pencil">Edit</MobileButton></div>
    <div class="period-tabs" role="tablist" aria-label="Period" id="period"><button type="button" role="tab" aria-selected="false">7 days</button><button type="button" role="tab" aria-selected="true">30 days</button><button type="button" role="tab" aria-selected="false">Year</button><button type="button" role="tab" aria-selected="false">All time</button></div>
    <div class="stat-pair">
      <section class="stat-card stat-card--highlight" id="sessions"><span class="label" style="color: inherit;">Sessions</span><span class="big">11</span><span class="line">386 min · 14.8 km</span></section>
      <section class="stat-card" id="weight"><span class="label label--primary">Weight</span><span class="big">72.6 kg</span><span class="line muted">3 days ago</span></section>
    </div>
    <div class="stat-trio" id="records">
      <section class="stat-card"><span class="name">Records</span><span class="value">3</span><span class="note">this period</span></section>
      <section class="stat-card"><span class="name">Longest run</span><span class="value">5.2 km</span><span class="note">8 Oct</span></section>
      <section class="stat-card"><span class="name">Full octopus</span><span class="value">×1</span><span class="note">5 of 8 lit</span></section>
    </div>
    <button type="button" class="list-row" id="body-map" data-component="list-row" style="border-radius: var(--cp-radius-lg); padding: var(--cp-space-sm);"><span class="row-icon" aria-hidden="true"><MobileIcon name="person-standing" :size="22" /></span><span class="row-text"><strong>Body map</strong><span>Legs most worked, shoulders least</span></span><span class="chevron"><MobileIcon name="chevron-right" :size="20" /></span></button>
  </div>
  <MobileNavBar active="you" destinations="three" />
</main>
`})))()}var T,E,D,O,k,A,j,M,N,P,F,I,L;function R(){return(R=e((()=>{t(),i(),o(),c(),u(),f(),m(),g(),v(),b(),S(),w(),T={title:`Mobile/Screens/Home and You`,tags:[`!autodocs`,`draft`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},E={name:`Home, the diary`,...n(a)},D={name:`Home, nothing scheduled`,...n(s)},O={name:`Record`,...n(h)},k={name:`Achievements`,...n(r)},A={name:`You, guest`,...n(y)},j={name:`You, confirm your email`,...n(x)},M={name:`You, signed in`,...n(C)},N={name:`Edit statistics`,...n(_)},P={name:`Preferences`,...n(l)},F={name:`Profile, guest`,...n(d)},I={name:`Profile, signed in`,...n(p)},L=[`HomeDiary`,`HomeDiaryNothingScheduled`,`Record`,`Achievements`,`YouGuest`,`YouPending`,`YouSignedIn`,`YouEditStatistics`,`Preferences`,`Profile`,`ProfileSignedIn`],E.parameters={...E.parameters,docs:{...E.parameters?.docs,source:{originalSource:`{
  name: 'Home, the diary',
  ...screen(screenHomeDiary)
}`,...E.parameters?.docs?.source},description:{story:`Home · the diary: a slim motivation row, what is next with Start, then the past with a recap and a record between sessions. Record sits in the top bar.`,...E.parameters?.docs?.description}}},D.parameters={...D.parameters,docs:{...D.parameters?.docs,source:{originalSource:`{
  name: 'Home, nothing scheduled',
  ...screen(screenHomeDiaryNothingScheduled)
}`,...D.parameters?.docs?.source},description:{story:`Home · nothing planned: Up next suggests a short workout, with Start.`,...D.parameters?.docs?.description}}},O.parameters={...O.parameters,docs:{...O.parameters?.docs,source:{originalSource:`{
  name: 'Record',
  ...screen(screenRecord)
}`,...O.parameters?.docs?.source},description:{story:`Record · what to record: a free session, a walk or a run, or a workout from the library.`,...O.parameters?.docs?.description}}},k.parameters={...k.parameters,docs:{...k.parameters?.docs,source:{originalSource:`{
  name: 'Achievements',
  ...screen(screenAchievements)
}`,...k.parameters?.docs?.source},description:{story:`Achievements · opened from the motivation row: Poulpy and the eight tentacles, what is earned, and what is still hidden.`,...k.parameters?.docs?.description}}},A.parameters={...A.parameters,docs:{...A.parameters?.docs,source:{originalSource:`{
  name: 'You, guest',
  ...screen(screenYouGuest)
}`,...A.parameters?.docs?.source},description:{story:`You · a guest: the profile card carries the Guest badge; Statistics follow it, with a period switch (7 days, 30 days, Year, All time) and an Edit button; the cog opens Preferences.`,...A.parameters?.docs?.description}}},j.parameters={...j.parameters,docs:{...j.parameters?.docs,source:{originalSource:`{
  name: 'You, confirm your email',
  ...screen(screenYouPending)
}`,...j.parameters?.docs?.source},description:{story:`You · account created, email not confirmed yet.`,...j.parameters?.docs?.description}}},M.parameters={...M.parameters,docs:{...M.parameters?.docs,source:{originalSource:`{
  name: 'You, signed in',
  ...screen(screenYouSignedIn)
}`,...M.parameters?.docs?.source},description:{story:`You · signed in.`,...M.parameters?.docs?.description}}},N.parameters={...N.parameters,docs:{...N.parameters?.docs,source:{originalSource:`{
  name: 'Edit statistics',
  ...screen(screenYouEditStatistics)
}`,...N.parameters?.docs?.source},description:{story:`Edit statistics · a switch and a handle per block, to choose and order what You shows.`,...N.parameters?.docs?.description}}},P.parameters={...P.parameters,docs:{...P.parameters?.docs,source:{originalSource:`{
  name: 'Preferences',
  ...screen(screenPreferences)
}`,...P.parameters?.docs?.source},description:{story:`Preferences · today's Settings under its new name, reached from the cog on You.`,...P.parameters?.docs?.description}}},F.parameters={...F.parameters,docs:{...F.parameters?.docs,source:{originalSource:`{
  name: 'Profile, guest',
  ...screen(screenProfile)
}`,...F.parameters?.docs?.source},description:{story:`Profile · a guest: Account on top (Create an account, Sign in), then about you with a Poulpy face, training and body.`,...F.parameters?.docs?.description}}},I.parameters={...I.parameters,docs:{...I.parameters?.docs,source:{originalSource:`{
  name: 'Profile, signed in',
  ...screen(screenProfileSignedIn)
}`,...I.parameters?.docs?.source},description:{story:`Profile · signed in: the Account section holds email, password, devices, export, sign out and delete.`,...I.parameters?.docs?.description}}}})))()}R();export{k as Achievements,E as HomeDiary,D as HomeDiaryNothingScheduled,P as Preferences,F as Profile,I as ProfileSignedIn,O as Record,N as YouEditStatistics,A as YouGuest,j as YouPending,M as YouSignedIn,L as __namedExportsOrder,T as default};
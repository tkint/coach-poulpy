import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-5jmgHvdA.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <MobileTopBar title="Live"><button type="button" class="device-pill" id="devices-pill"><span class="dot" aria-hidden="true"></span>3 connected</button></MobileTopBar>
  <div class="live-body">
      <div class="paused-banner" role="status"><MobileIcon name="pause" :size="20" />Paused with the treadmill</div>
      <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
        <div class="grid">
        <div class="live-tile"><span class="label">Speed</span><span class="big">0.0</span><span class="unit">km/h</span><span class="source">from pod</span></div>
        <div class="live-tile"><span class="label">Pace</span><span class="big">–:––</span><span class="unit">/km · paused</span><span class="source">from pod</span></div>
        </div>
        <div class="grid">
          <div class="live-tile live-tile--small"><span class="label">Distance <span>· pod</span></span><span class="value">1.86 <small>km</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">118 <small>bpm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Cadence <span>· pod</span></span><span class="value">112 <small>spm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Incline <span>· treadmill</span></span><span class="value">1.0 <small>%</small></span></div>
        </div>
      </section>
      <section class="finish-card" id="finish-ask" role="alert" aria-label="Finish the session?" data-component="finish-card">
        <div class="row">
          <MobileIcon name="clock" :size="26" />
          <div><h2>Lost the treadmill</h2><p>It disconnected 2 min ago. Poulpy keeps trying. With no answer, the session ends at 12:18, when it was last seen.</p></div>
        </div>
        <div class="actions">
          <MobileButton kind="secondary" size="md">Keep going</MobileButton>
          <MobileButton size="md">Finish</MobileButton>
        </div>
      </section>
      <section class="recorder" id="recorder" aria-label="Recorder" data-component="recorder">
        <div class="status"><span class="clock">12:18</span><span class="state state--paused"><span class="dot" aria-hidden="true"></span>Paused · Walk</span></div>
        <div class="actions">
          <MobileButton kind="secondary" size="md" class="icon-only" aria-label="Resume recording"><MobileIcon name="play" :size="22" /></MobileButton>
          <MobileButton size="md">Finish</MobileButton>
        </div>
      </section>
  </div>
  <MobileNavBar active="home" />
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <MobileTopBar title="Live"><button type="button" class="device-pill" id="devices-pill"><span class="dot" aria-hidden="true"></span>3 connected</button></MobileTopBar>
  <div class="live-body">
      <div class="paused-banner" role="status"><MobileIcon name="pause" :size="20" />Paused with the treadmill</div>
      <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
        <div class="grid">
        <div class="live-tile"><span class="label">Speed</span><span class="big">0.0</span><span class="unit">km/h</span><span class="source">from pod</span></div>
        <div class="live-tile"><span class="label">Pace</span><span class="big">–:––</span><span class="unit">/km · paused</span><span class="source">from pod</span></div>
        </div>
        <div class="grid">
          <div class="live-tile live-tile--small"><span class="label">Distance <span>· pod</span></span><span class="value">1.86 <small>km</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">118 <small>bpm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Cadence <span>· pod</span></span><span class="value">112 <small>spm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Incline <span>· treadmill</span></span><span class="value">1.0 <small>%</small></span></div>
        </div>
      </section>
      <section class="finish-card" id="finish-ask" role="alert" aria-label="Finish the session?" data-component="finish-card">
        <div class="row">
          <MobileIcon name="clock" :size="26" />
          <div><h2>Finished walking?</h2><p>The belt stopped 10 min ago. With no answer, Poulpy finishes the session at 12:18 in 10 min.</p></div>
        </div>
        <div class="actions">
          <MobileButton kind="secondary" size="md">Keep going</MobileButton>
          <MobileButton size="md">Finish</MobileButton>
        </div>
      </section>
      <section class="recorder" id="recorder" aria-label="Recorder" data-component="recorder">
        <div class="status"><span class="clock">12:18</span><span class="state state--paused"><span class="dot" aria-hidden="true"></span>Paused · Walk</span></div>
        <div class="actions">
          <MobileButton kind="secondary" size="md" class="icon-only" aria-label="Resume recording"><MobileIcon name="play" :size="22" /></MobileButton>
          <MobileButton size="md">Finish</MobileButton>
        </div>
      </section>
  </div>
  <MobileNavBar active="home" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone player">
  <div class="session-progress" id="session-progress">
    <button class="icon-button icon-button--filled" type="button" aria-label="Pause"><MobileIcon name="pause" :size="20" /></button>
    <div>
      <span class="label numbers">Step 5 of 6 · 31:05</span>
      <div class="progress-track" role="progressbar" aria-valuemin="0" aria-valuemax="6" aria-valuenow="4" aria-label="Session progress"><div style="width: 66%;"></div></div>
    </div>
    <button type="button" class="icon-button icon-button--filled" aria-label="End session"><MobileIcon name="x" :size="22" /></button>
  </div>

  <div class="player-step" id="current-step">
    <img src="../../../poulpy/pose_straining.svg" alt="Poulpy waiting">
    <span class="part-line" id="part-line" data-component="part-line">Cool-down</span>
    <h1 class="title">Walk</h1>
    <span class="set-count numbers">Not connected to a treadmill</span>
    <span class="target-value">10</span>
    <span class="target-unit">min</span>
  </div>
  <div class="player-actions">
    <MobileButton kind="secondary" size="lg">Skip</MobileButton>
    <MobileButton size="lg">Start</MobileButton>
  </div>
</main>
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone player player--paused">
  <div class="session-progress" id="session-progress">
    <button class="pause-button pause-button--paused" data-component="pause-button" id="pause-button" type="button" aria-label="Resume"><MobileIcon name="play" :size="28" /></button>
    <div>
      <span class="label numbers">Step 3 of 6 · 14:20</span>
      <div class="progress-track" role="progressbar" aria-valuemin="0" aria-valuemax="6" aria-valuenow="3" aria-label="Session progress"><div style="width: 50%;"></div></div>
    </div>
    <button type="button" class="icon-button icon-button--filled" aria-label="End session"><MobileIcon name="x" :size="22" /></button>
  </div>
  <div class="paused-card" id="paused" data-component="paused-card" role="status"><MobileIcon name="pause" /><strong>Paused</strong></div>
  <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
    <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">121 <small>bpm</small></span></div>
  </section>

  <div class="player-step" id="current-step">
    <img src="../../../poulpy/pose_straining.svg" alt="Poulpy straining">
    <span class="part-line" id="part-line" data-component="part-line">Work</span>
    <h1 class="title">Bodyweight squats</h1>
    <span class="set-count numbers">Set 2 of 3</span>
    <div class="set-dots" aria-hidden="true"><span class="done"></span><span class="current"></span><span></span></div>
    <span class="target-value">15</span>
    <span class="target-unit">reps</span>
  </div>
  <MobileNextCard id="next-step" label="Next">Rest 1 min, then set 3</MobileNextCard>
  <div class="player-actions">
    <MobileButton kind="secondary" size="lg" disabled>Skip</MobileButton>
    <MobileButton size="lg" aria-disabled="true">Done</MobileButton>
  </div>
</main>
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone player">
  <div class="session-progress" id="session-progress">
    <button class="pause-button" data-component="pause-button" id="pause-button" type="button" aria-label="Pause"><MobileIcon name="pause" :size="26" /></button>
    <div>
      <span class="label numbers">Step 3 of 6 · 14:20</span>
      <div class="progress-track" role="progressbar" aria-valuemin="0" aria-valuemax="6" aria-valuenow="3" aria-label="Session progress"><div style="width: 50%;"></div></div>
    </div>
    <button type="button" class="icon-button icon-button--filled" aria-label="End session"><MobileIcon name="x" :size="22" /></button>
  </div>
  <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
    <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">121 <small>bpm</small></span></div>
  </section>

  <div class="player-step" id="current-step">
    <img src="../../../poulpy/pose_straining.svg" alt="Poulpy straining">
    <span class="part-line" id="part-line" data-component="part-line">Work</span>
    <h1 class="title">Bodyweight squats</h1>
    <span class="set-count numbers">Set 2 of 3</span>
    <div class="set-dots" aria-hidden="true"><span class="done"></span><span class="current"></span><span></span></div>
    <span class="target-value">15</span>
    <span class="target-unit">reps</span>
  </div>
  <MobileNextCard id="next-step" label="Next">Rest 1 min, then set 3</MobileNextCard>
  <div class="player-actions">
    <MobileButton kind="secondary" size="lg">Skip</MobileButton>
    <MobileButton size="lg">Done</MobileButton>
  </div>
</main>
`})))()}var p;function m(){return(m=e((()=>{p=`<main class="phone player">
  <div class="session-progress" id="session-progress">
    <button class="icon-button icon-button--filled" type="button" aria-label="Pause"><MobileIcon name="pause" :size="20" /></button>
    <div>
      <span class="label numbers">Step 5 of 6 · 31:05</span>
      <div class="progress-track" role="progressbar" aria-valuemin="0" aria-valuemax="6" aria-valuenow="4" aria-label="Session progress"><div style="width: 66%;"></div></div>
    </div>
    <button type="button" class="icon-button icon-button--filled" aria-label="End session"><MobileIcon name="x" :size="22" /></button>
  </div>
  <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
    <div class="grid">
      <div class="live-tile"><span class="label">Speed</span><span class="big">0.0</span><span class="unit">km/h</span><span class="source">from pod</span></div>
      <div class="live-tile"><span class="label">Distance</span><span class="big">2.00</span><span class="unit">km</span><span class="source">from pod</span></div>
    </div>
    <div class="grid">
      <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">104 <small>bpm</small></span></div>
      <div class="live-tile live-tile--small"><span class="label">Incline <span>· treadmill</span></span><span class="value">1.0 <small>%</small></span></div>
    </div>
  </section>

  <div class="player-step" id="current-step">
    <img src="../../../poulpy/pose_straining.svg" alt="Poulpy waiting">
    <span class="part-line" id="part-line" data-component="part-line">Cool-down</span>
    <h1 class="title">Walk</h1>
    <div class="waiting-row" role="status">
      <span><strong>Start your treadmill</strong><small>The step starts when the belt moves</small></span>
    </div>
    <span class="target-value">10</span>
    <span class="target-unit">min</span>
  </div>
  <div class="player-actions">
    <MobileButton kind="secondary" size="lg">Skip</MobileButton>
    <MobileButton size="lg">Start without it</MobileButton>
  </div>
</main>
`})))()}var h;function g(){return(g=e((()=>{h=`<main class="phone">
  <MobileTopBar title="Record"><button type="button" class="device-pill" id="devices-pill"><span class="dot" aria-hidden="true"></span>3 connected</button></MobileTopBar>
  <div class="live-body">
      <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
        <div class="grid">
        <div class="live-tile"><span class="label">Speed</span><span class="big">5.2</span><span class="unit">km/h</span><span class="source">from pod</span></div>
        <div class="live-tile"><span class="label">Pace</span><span class="big">11:32</span><span class="unit">/km</span><span class="source">from pod</span></div>
        </div>
        <div class="grid">
          <div class="live-tile live-tile--small"><span class="label">Distance <span>· pod</span></span><span class="value">1.86 <small>km</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">118 <small>bpm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Cadence <span>· pod</span></span><span class="value">112 <small>spm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Incline <span>· treadmill</span></span><span class="value">1.0 <small>%</small></span></div>
        </div>
      </section>
      <section class="recorder" id="recorder" aria-label="Recorder" data-component="recorder">
<div style="display: flex; flex-direction: column; gap: 10px;">
        <span class="label">What are you doing?</span>
        <div class="pick-chips" role="radiogroup" aria-label="What are you doing?">
          <button type="button" role="radio" aria-checked="true">Walk</button>
          <button type="button" role="radio" aria-checked="false">Brisk walk</button>
          <button type="button" role="radio" aria-checked="false">Easy run</button>
          <button type="button">More<MobileIcon name="chevron-right" :size="16" /></button>
        </div>
        <p class="hint">Your last three, then every walk, run and workout under More.</p>
        <label class="follow-row">
          <MobileIcon name="activity" :size="22" />
          <span><strong>Follow the treadmill</strong><small>Starts, pauses and finishes with the belt</small></span>
          <input type="checkbox" role="switch" checked>
        </label>
        <MobileButton size="md" icon="play">Wait for the treadmill</MobileButton>
        </div>
      </section>
  </div>
  <MobileNavBar active="home" />
</main>
`})))()}var _;function v(){return(v=e((()=>{_=`<main class="phone">
  <MobileTopBar title="Live"><button type="button" class="device-pill" id="devices-pill"><span class="dot" aria-hidden="true"></span>3 connected</button></MobileTopBar>
  <div class="live-body">
      <section class="live-numbers" id="live-numbers" aria-label="Live numbers" data-component="live-numbers">
        <div class="grid">
        <div class="live-tile"><span class="label">Speed</span><span class="big">0.0</span><span class="unit">km/h</span><span class="source">from pod</span></div>
        <div class="live-tile"><span class="label">Pace</span><span class="big">–:––</span><span class="unit">/km</span><span class="source">from pod</span></div>
        </div>
        <div class="grid">
          <div class="live-tile live-tile--small"><span class="label">Distance <span>· pod</span></span><span class="value">1.86 <small>km</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Heart rate <span>· band</span></span><span class="value">118 <small>bpm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Cadence <span>· pod</span></span><span class="value">112 <small>spm</small></span></div>
          <div class="live-tile live-tile--small"><span class="label">Incline <span>· treadmill</span></span><span class="value">1.0 <small>%</small></span></div>
        </div>
      </section>
      <section class="recorder" id="recorder" aria-label="Recorder" data-component="recorder">
        <div class="waiting-row" role="status">
          <MobileIcon name="circle-dashed" :size="44" />
          <span><strong>Press start on your treadmill</strong><small>Walk · recording starts with the belt</small></span>
        </div>
        <div class="actions">
          <MobileButton kind="secondary" size="md">Cancel</MobileButton>
          <MobileButton size="md">Start now</MobileButton>
        </div>
      </section>
  </div>
  <MobileNavBar active="home" />
</main>
`})))()}var y,b,x,S,C,w,T,E,D,O;function k(){return(k=e((()=>{t(),i(),o(),c(),u(),f(),m(),g(),v(),y={title:`Mobile/Screens/Home/Live`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},b={name:`Treadmill lost, finish?`,...n(r),parameters:{mockup:{issue:104}}},x={name:`Belt still, finish?`,...n(a),parameters:{mockup:{issue:104}}},S={name:`Walk step without a treadmill`,...n(s),parameters:{mockup:{issue:82}}},C={name:`Mixed workout, paused on a strength step`,...n(l),parameters:{a11y:{test:`todo`},mockup:{issue:220}}},w={name:`Mixed workout, strength step with treadmill numbers`,...n(d),parameters:{mockup:{issue:220}}},T={name:`Walk step waits for the belt`,...n(p),parameters:{mockup:{issue:82}}},E={name:`Ready`,...n(h),parameters:{mockup:{issue:104}}},D={name:`Waiting for the treadmill`,...n(_),parameters:{mockup:{issue:104}}},O=[`LiveDisconnected`,`LiveFinish`,`LiveMixedPlayerNoTreadmill`,`LiveMixedPlayerPaused`,`LiveMixedPlayerStrength`,`LiveMixedPlayerWaitsForBelt`,`LiveReady`,`LiveWaiting`],b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  name: "Treadmill lost, finish?",
  ...screen(screenLiveDisconnected),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...b.parameters?.docs?.source},description:{story:`Live · treadmill lost, finish?.`,...b.parameters?.docs?.description}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  name: "Belt still, finish?",
  ...screen(screenLiveFinish),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...x.parameters?.docs?.source},description:{story:`Live · belt still, finish?.`,...x.parameters?.docs?.description}}},S.parameters={...S.parameters,docs:{...S.parameters?.docs,source:{originalSource:`{
  name: "Walk step without a treadmill",
  ...screen(screenLiveMixedPlayerNoTreadmill),
  parameters: {
    mockup: {
      issue: 82
    }
  }
}`,...S.parameters?.docs?.source},description:{story:`Player · walk step without a treadmill.`,...S.parameters?.docs?.description}}},C.parameters={...C.parameters,docs:{...C.parameters?.docs,source:{originalSource:`{
  name: "Mixed workout, paused on a strength step",
  ...screen(screenLiveMixedPlayerPaused),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 220
    }
  }
}`,...C.parameters?.docs?.source},description:{story:`Player · mixed workout, paused on a strength step.`,...C.parameters?.docs?.description}}},w.parameters={...w.parameters,docs:{...w.parameters?.docs,source:{originalSource:`{
  name: "Mixed workout, strength step with treadmill numbers",
  ...screen(screenLiveMixedPlayerStrength),
  parameters: {
    mockup: {
      issue: 220
    }
  }
}`,...w.parameters?.docs?.source},description:{story:`Player · mixed workout, strength step with treadmill numbers.`,...w.parameters?.docs?.description}}},T.parameters={...T.parameters,docs:{...T.parameters?.docs,source:{originalSource:`{
  name: "Walk step waits for the belt",
  ...screen(screenLiveMixedPlayerWaitsForBelt),
  parameters: {
    mockup: {
      issue: 82
    }
  }
}`,...T.parameters?.docs?.source},description:{story:`Player · walk step waits for the belt.`,...T.parameters?.docs?.description}}},E.parameters={...E.parameters,docs:{...E.parameters?.docs,source:{originalSource:`{
  name: "Ready",
  ...screen(screenLiveReady),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...E.parameters?.docs?.source},description:{story:`Live · ready.`,...E.parameters?.docs?.description}}},D.parameters={...D.parameters,docs:{...D.parameters?.docs,source:{originalSource:`{
  name: "Waiting for the treadmill",
  ...screen(screenLiveWaiting),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...D.parameters?.docs?.source},description:{story:`Live · waiting for the treadmill.`,...D.parameters?.docs?.description}}}})))()}k();export{b as LiveDisconnected,x as LiveFinish,S as LiveMixedPlayerNoTreadmill,C as LiveMixedPlayerPaused,w as LiveMixedPlayerStrength,T as LiveMixedPlayerWaitsForBelt,E as LiveReady,D as LiveWaiting,O as __namedExportsOrder,y as default};
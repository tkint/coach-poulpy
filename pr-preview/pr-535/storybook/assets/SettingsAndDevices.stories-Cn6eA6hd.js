import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-x_0cSj1E.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <MobileTopBar title="Data sources" leading="back" back-label="Back to devices" />
  <p class="app-bar-sub">Which device gives each number</p>
  <div class="settings-body" style="padding-top: var(--cp-space-md);">
    <p class="lead">When two devices measure the same thing, pick the one to trust. Automatic prefers what is on your body: pod over treadmill, band over treadmill grips.</p>
    <div class="setting-card" role="group" aria-label="Speed, pace and distance" data-component="setting-card"><strong>Speed, pace and distance</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Automatic</button><button type="button" aria-pressed="false">Foot pod</button><button type="button" aria-pressed="false">Home treadmill</button></div></div>
    <div class="setting-card" role="group" aria-label="Heart rate" data-component="setting-card"><strong>Heart rate</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Automatic</button><button type="button" aria-pressed="false">Heart rate band</button><button type="button" aria-pressed="false">Home treadmill</button></div></div>
    <div class="setting-card" role="group" aria-label="Cadence" data-component="setting-card"><strong>Cadence</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Foot pod</button></div></div>
    <div class="setting-card" role="group" aria-label="Incline" data-component="setting-card"><strong>Incline</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Home treadmill</button></div></div>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <MobileTopBar title="Add a device" leading="back" back-label="Back to devices" />
  <div class="settings-body" style="padding-top: var(--cp-space-md); gap: var(--cp-space-md);">
    <section class="search-card" id="search">
      <MobilePoulpyLoader :size="112" label="Poulpy is searching for devices" />
      <h2 role="status">Searching nearby devices…</h2>
      <p>Treadmills, foot pods and heart rate bands. Wake your device up and stay close to it.</p>
      <MobileButton kind="secondary" size="md">Stop searching</MobileButton>
    </section>
    <span class="label label-row">Found nearby</span>
    <article class="device-row" data-component="device-row"><span class="device-icon device-icon--sm" aria-hidden="true"><MobileIcon name="gauge" :size="22" /></span><span class="device-text" style="gap: var(--cp-space-xs);"><strong>Home treadmill</strong><span class="kind-tag">Treadmill</span></span><MobileButton size="sm">Add</MobileButton></article>
    <article class="device-row" data-component="device-row"><span class="device-icon device-icon--sm device-icon--off" aria-hidden="true"><MobileIcon name="bluetooth" :size="22" /></span><span class="device-text" style="gap: var(--cp-space-xs);"><strong>Heart rate band</strong><span class="kind-tag">Heart rate</span></span></article>
    <span class="lead">Devices you already added are not listed here.</span>
  </div>
  <MobileNavBar active="you" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
  <MobileTopBar title="Home treadmill" leading="back" back-label="Back to devices" />
  <p class="app-bar-sub state">Connected · Treadmill</p>
  <div class="settings-body" style="padding-top: var(--cp-space-md); gap: var(--cp-space-md);">
    <section class="summary-card" data-component="summary-card" id="live-now" style="padding: 18px;">
      <span class="device-icon" style="width: 56px; height: 56px; background: var(--cp-surface);" aria-hidden="true"><MobileIcon name="gauge" /><span class="presence"></span></span>
      <div><span class="headline" style="font: var(--cp-type-headline); font-size: 19px; line-height: 23px;">Live now</span><span class="line">9.4 km/h · 1.0 % incline</span></div>
    </section>
    <span class="label label-row">What it measures</span>
    <ul class="measure-list" id="measures"><li><span>Speed and pace</span><span class="use-tag">Pod used instead</span></li><li><span>Distance</span><span class="use-tag">Pod used instead</span></li><li><span>Incline</span><span class="use-tag use-tag--used">Used</span></li><li><span>Energy</span><span class="use-tag use-tag--used">Used</span></li></ul>
    <button type="button" class="section-link">Change which device is used</button>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">Device</span>
    <label class="switch-row"><span><strong>Connect automatically</strong><small>When you open Devices and it is nearby</small></span><input type="checkbox" role="switch" checked></label>
    <div style="display: flex; gap: var(--cp-space-sm);">
      <MobileButton kind="secondary" size="md" class="text-button" style="border: 2px solid var(--cp-outline);">Disconnect</MobileButton>
      <button type="button" class="text-button text-button--danger">Forget this device</button>
    </div>
  </div>
</main>
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone">
  <MobileTopBar title="Settings"><MobileIconButton icon="plus" :icon-size="22" filled aria-label="Add a device" /></MobileTopBar>
  <div class="view-tabs settings-tabs" role="tablist" aria-label="Settings sections" id="settings-tabs">
    <button type="button" role="tab" aria-selected="false">General</button>
    <button type="button" role="tab" aria-selected="true">Devices</button>
  </div>
  <div class="settings-body" style="padding-bottom: var(--cp-space-xl);">
    <MobileEmptyState id="no-devices" title="No devices yet">
      Add your treadmill, a foot pod or a heart rate band. Poulpy shows their numbers together while you run, and records the session. Switch the device on first.
      <template #action><MobileButton kind="secondary" size="sm" icon="plus">Add my first device</MobileButton></template>
    </MobileEmptyState>
  </div>
  <MobileNavBar active="you" />
</main>
`})))()}var d;function f(){return(f=e((()=>{d=`<main class="phone">

  <MobileTopBar title="Devices" leading="back" back-label="Back to settings"><MobileIconButton icon="plus" :icon-size="22" filled aria-label="Add a device" /></MobileTopBar>
  <div class="settings-body" style="padding-top: var(--cp-space-lg);">
    <span class="label label-row">Your devices</span>
    <button type="button" class="device-row" data-component="device-row"><span class="device-icon" aria-hidden="true"><MobileIcon name="gauge" /><span class="presence"></span></span><span class="device-text"><strong>Home treadmill</strong><span class="state">Connected</span><small>Speed, distance, incline, energy</small></span><span class="chevron-right"><MobileIcon name="chevron-right" :size="22" /></span></button>
    <button type="button" class="device-row" data-component="device-row"><span class="device-icon" aria-hidden="true"><MobileIcon name="footprints" /><span class="presence"></span></span><span class="device-text"><strong>Foot pod</strong><span class="state">Connected · battery 80%</span><small>Speed, distance, cadence</small></span><span class="chevron-right"><MobileIcon name="chevron-right" :size="22" /></span></button>
    <button type="button" class="device-row" data-component="device-row"><span class="device-icon" aria-hidden="true"><MobileIcon name="heart-pulse" /><span class="presence"></span></span><span class="device-text"><strong>Heart rate band</strong><span class="state">Connected · battery 60%</span><small>Heart rate</small></span><span class="chevron-right"><MobileIcon name="chevron-right" :size="22" /></span></button>
    <button type="button" class="device-row" data-component="device-row"><MobileIcon name="sliders-horizontal" :size="22" /><span class="device-text"><strong style="font-size: 15px; line-height: 20px;">Data sources</strong><small>Which device gives each number · Automatic</small></span><span class="chevron-right"><MobileIcon name="chevron-right" :size="22" /></span></button>
  </div>
</main>
`})))()}var p,m,h,g,_,v,y;function b(){return(b=e((()=>{t(),i(),o(),c(),u(),f(),p={title:`Mobile/Screens/You/Devices and data sources`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},m={name:`Data sources`,...n(r),parameters:{mockup:{issue:104}}},h={name:`Add a device`,...n(a),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},g={name:`Device page`,...n(s),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},_={name:`No devices yet`,...n(l),parameters:{mockup:{issue:104}}},v={name:`Devices`,...n(d),parameters:{a11y:{test:`todo`},mockup:{issue:void 0}}},y=[`DataSources`,`DeviceAdd`,`DeviceDetail`,`DevicesEmpty`,`DevicesList`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  name: "Data sources",
  ...screen(screenDataSources),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...m.parameters?.docs?.source},description:{story:`Settings · data sources.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  name: "Add a device",
  ...screen(screenDeviceAdd),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...h.parameters?.docs?.source},description:{story:`Settings · add a device.`,...h.parameters?.docs?.description}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  name: "Device page",
  ...screen(screenDeviceDetail),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 104
    }
  }
}`,...g.parameters?.docs?.source},description:{story:`Settings · device page.`,...g.parameters?.docs?.description}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  name: "No devices yet",
  ...screen(screenDevicesEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,..._.parameters?.docs?.source},description:{story:`Settings · no devices yet.`,..._.parameters?.docs?.description}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  name: "Devices",
  ...screen(screenDevicesList),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: undefined
    }
  }
}`,...v.parameters?.docs?.source},description:{story:`Settings · devices.`,...v.parameters?.docs?.description}}}})))()}b();export{m as DataSources,h as DeviceAdd,g as DeviceDetail,_ as DevicesEmpty,v as DevicesList,y as __namedExportsOrder,p as default};
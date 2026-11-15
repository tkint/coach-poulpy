import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-BRAJNxa-.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to devices"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Data sources</h1><small>Which device gives each number</small></div></header>
  <div class="settings-body" style="padding-top: var(--cp-space-md);">
    <p class="lead">When two devices measure the same thing, pick the one to trust. Automatic prefers what is on your body: pod over treadmill, band over treadmill grips.</p>
    <div class="setting-card" role="group" aria-label="Speed, pace and distance" data-component="setting-card"><strong>Speed, pace and distance</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Automatic</button><button type="button" aria-pressed="false">Foot pod</button><button type="button" aria-pressed="false">Home treadmill</button></div></div>
    <div class="setting-card" role="group" aria-label="Heart rate" data-component="setting-card"><strong>Heart rate</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Automatic</button><button type="button" aria-pressed="false">Heart rate band</button><button type="button" aria-pressed="false">Home treadmill</button></div></div>
    <div class="setting-card" role="group" aria-label="Cadence" data-component="setting-card"><strong>Cadence</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Foot pod</button></div></div>
    <div class="setting-card" role="group" aria-label="Incline" data-component="setting-card"><strong>Incline</strong><div class="pill-group pill-group--wrap"><button type="button" aria-pressed="true">Home treadmill</button></div></div>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to devices"><MobileIcon name="chevron-left" :size="22" /></button><h1>Add a device</h1></header>
  <div class="settings-body" style="padding-top: var(--cp-space-md); gap: var(--cp-space-md);">
    <section class="search-card" id="search">
      <img class="spin" src="../../../poulpy/loader_small.svg" alt="Poulpy spinning while he searches" width="112" height="112">
      <h2 role="status">Searching nearby devices…</h2>
      <p>Treadmills, foot pods and heart rate bands. Wake your device up and stay close to it.</p>
      <button type="button" class="outline-button">Stop searching</button>
    </section>
    <span class="label label-row">Found nearby</span>
    <article class="device-row" data-component="device-row"><span class="device-icon device-icon--sm" aria-hidden="true"><MobileIcon name="gauge" :size="22" /></span><span class="device-text" style="gap: var(--cp-space-xs);"><strong>Home treadmill</strong><span class="kind-tag">Treadmill</span></span><button type="button" class="small-button">Add</button></article>
    <article class="device-row" data-component="device-row"><span class="device-icon device-icon--sm device-icon--off" aria-hidden="true"><MobileIcon name="bluetooth" :size="22" /></span><span class="device-text" style="gap: var(--cp-space-xs);"><strong>Heart rate band</strong><span class="kind-tag">Heart rate</span></span></article>
    <span class="lead">Devices you already added are not listed here.</span>
  </div>
  <MobileNavBar active="settings" />
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to devices"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Home treadmill</h1><span class="state">Connected · Treadmill</span></div></header>
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
      <button type="button" class="secondary-button text-button" style="border: 2px solid var(--cp-outline);">Disconnect</button>
      <button type="button" class="text-button text-button--danger">Forget this device</button>
    </div>
  </div>
</main>
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone">
  <div class="settings-head"><h1>Settings</h1><button type="button" class="header-add-button" aria-label="Add a device"><MobileIcon name="plus" /></button></div>
  <div class="view-tabs settings-tabs" role="tablist" aria-label="Settings sections" id="settings-tabs">
    <button type="button" role="tab" aria-selected="false">General</button>
    <button type="button" role="tab" aria-selected="true">Devices</button>
  </div>
  <div class="settings-body" style="justify-content: center; padding-bottom: var(--cp-space-xl);">
    <section class="empty-card" id="no-devices">
      <img src="../../../poulpy/pose_standing_tall.svg" alt="Poulpy, ready to go">
      <h2>No devices yet</h2>
      <p>Add your treadmill, a foot pod or a heart rate band. Poulpy shows their numbers together while you run, and records the session.</p>
      <MobileButton icon="plus">Add my first device</MobileButton>
      <span class="hint-text" style="font-size: 14px; line-height: 19px;">Switch the device on first.</span>
    </section>
  </div>
  <MobileNavBar active="settings" />
</main>
`})))()}var d,f,p,m,h,g;function _(){return(_=e((()=>{t(),i(),o(),c(),u(),d={title:`Mobile/Screens/Settings and devices`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},f={name:`Data sources`,...n(r),parameters:{mockup:{issue:104}}},p={name:`Add a device`,...n(a),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},m={name:`Device page`,...n(s),parameters:{a11y:{test:`todo`},mockup:{issue:104}}},h={name:`No devices yet`,...n(l),parameters:{mockup:{issue:104}}},g=[`DataSources`,`DeviceAdd`,`DeviceDetail`,`DevicesEmpty`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  name: "Data sources",
  ...screen(screenDataSources),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...f.parameters?.docs?.source},description:{story:`Settings · data sources.`,...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
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
}`,...p.parameters?.docs?.source},description:{story:`Settings · add a device.`,...p.parameters?.docs?.description}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
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
}`,...m.parameters?.docs?.source},description:{story:`Settings · device page.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  name: "No devices yet",
  ...screen(screenDevicesEmpty),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...h.parameters?.docs?.source},description:{story:`Settings · no devices yet.`,...h.parameters?.docs?.description}}}})))()}_();export{f as DataSources,p as DeviceAdd,m as DeviceDetail,h as DevicesEmpty,g as __namedExportsOrder,d as default};
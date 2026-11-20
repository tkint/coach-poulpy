import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-BjL8g0KZ.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <MobileTopBar title="Settings" leading="back" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <span class="label label-row">Appearance</span>
    <div class="field" id="theme"><MobileSelectField label="Theme" value="System" /><span class="field-note">System follows your phone’s light or dark mode.</span></div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">Region</span>
    <div class="field" id="language"><MobileSelectField label="Language" value="System" /></div>
    <div class="field" id="units"><MobileSelectField label="Units" value="Metric" /><span class="field-note">Metric: km, km/h, kg, cm. Imperial: mi, mph, lb, ft and in. Weight and height you logged are converted, never changed.</span></div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">Devices</span>
    <div class="settings-list" id="app">
      <MobileSettingsRow icon="gauge" title="Devices" detail="3 connected · treadmill, foot pod, heart rate band" />
    </div>
    <span class="field-note">Reached from the cog on You.</span>
  </div>
</main>
`})))()}var a,o,s;function c(){return(c=e((()=>{t(),i(),a={title:`Mobile/Screens/You/Settings`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},o={name:`Settings`,...n(r)},s=[`Settings`],o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  name: 'Settings',
  ...screen(screenSettings)
}`,...o.parameters?.docs?.source},description:{story:`Settings · Appearance, Region (language and units) and Devices, all on the page, reached from the cog on You.`,...o.parameters?.docs?.description}}}})))()}c();export{o as Settings,s as __namedExportsOrder,a as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-DhxQsO5r.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <MobileTopBar title="Settings" leading="back" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <span class="label label-row">Appearance</span>
    <div class="field" id="theme"><MobileSelectField label="Theme" value="System" /><span class="field-note">System follows your phone’s light or dark mode.</span></div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">Region</span>
    <div class="field" id="language"><MobileSelectField label="Language" value="System" /></div>
    <div class="field" id="units"><MobileSelectField label="Units" value="Metric" /><span class="field-note">Metric: km, km/h, kg, cm. Imperial: mi, mph, lb, ft and in. Weight and height you logged are converted, never changed.</span></div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">App</span>
    <div class="settings-list" id="app">
      <MobileSettingsRow icon="gauge" title="Devices" detail="3 connected · treadmill, foot pod, heart rate band" />
      <MobileSettingsRow id="app-icon" picture="../../../poulpy/mood_happy.svg" title="App icon" detail="Happy · default" />
    </div>
    <span class="label label-row" style="margin-top: var(--cp-space-sm);">About</span>
    <div class="settings-list" id="about">
      <MobileSettingsRow icon="info" title="About Coach Poulpy" detail="Version 0.1 · data stays on this phone" />
    </div>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <MobileTopBar title="Settings" />
  <div class="scrim"></div>
  <section class="sheet" id="app-icon-sheet" data-component="option-sheet" role="dialog" aria-modal="true" aria-labelledby="app-icon-title" aria-describedby="app-icon-description app-icon-ios-note">
    <span class="sheet-handle" aria-hidden="true"></span>
    <div class="sheet-title">
      <h2 id="app-icon-title">App icon</h2>
      <p id="app-icon-description">Pick Poulpy’s face for your home screen. This doesn’t change Poulpy’s mood in the app.</p>
    </div>
    <div class="option-list" id="app-icon-choices" role="radiogroup" aria-label="App icon">
      <MobileOptionRow control="radio" name="app-icon" value="happy" checked picture="../../../poulpy/mood_happy.svg">Happy · default</MobileOptionRow>
      <MobileOptionRow control="radio" name="app-icon" value="proud" picture="../../../poulpy/mood_proud.svg">Proud</MobileOptionRow>
      <MobileOptionRow control="radio" name="app-icon" value="annoyed" picture="../../../poulpy/mood_annoyed.svg">Annoyed</MobileOptionRow>
      <MobileOptionRow control="radio" name="app-icon" value="grumpy" picture="../../../poulpy/mood_grumpy.svg">Grumpy</MobileOptionRow>
      <MobileOptionRow control="radio" name="app-icon" value="champion" picture="../../../poulpy/pose_champion.svg">Champion</MobileOptionRow>
      <MobileOptionRow control="radio" name="app-icon" value="cocky" picture="../../../poulpy/pose_cocky.svg">Cocky</MobileOptionRow>
    </div>
    <p class="field-note">Tap an icon to apply it and close this sheet.</p>
    <p class="field-note" id="app-icon-ios-note">On iOS, your phone shows a system alert after the icon changes.</p>
    <div class="sheet-actions"><MobileButton kind="secondary" size="xl">Cancel</MobileButton></div>
  </section>
</main>
`})))()}var s,c,l,u;function d(){return(d=e((()=>{t(),i(),o(),s={title:`Mobile/Screens/You/Settings`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},c={name:`Settings`,...n(r)},l={name:`Choose an app icon`,...n(a),parameters:{mockup:{issue:177}}},u=[`Settings`,`SettingsAppIconSheet`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  name: 'Settings',
  ...screen(screenSettings)
}`,...c.parameters?.docs?.source},description:{story:`Settings · Appearance, Region (language and units) and Devices, all on the page, reached from the cog on You.`,...c.parameters?.docs?.description}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  name: "Choose an app icon",
  ...screen(screenSettingsAppIconSheet),
  parameters: {
    mockup: {
      issue: 177
    }
  }
}`,...l.parameters?.docs?.source},description:{story:`Settings · choose an app icon.`,...l.parameters?.docs?.description}}}})))()}d();export{c as Settings,l as SettingsAppIconSheet,u as __namedExportsOrder,s as default};
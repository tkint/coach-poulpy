import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-CDlA4PcU.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <MobileTopBar title="Password" leading="back" back-label="Back to account" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="field" id="current-password-field"><label class="label" for="current-password">Current password</label><span class="password-field"><input class="text-field" id="current-password" type="password" value="correct horse battery" autocomplete="current-password"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <div class="field" id="new-password-field"><label class="label" for="new-password">New password</label><span class="password-field"><input class="text-field" id="new-password" type="password" value="purple octopus stapler" autocomplete="new-password" aria-describedby="new-password-hint"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span><span class="field-note" id="new-password-hint">8 characters or more.</span></div>
    <section class="info-card" id="other-phones" data-component="info-card"><MobileIcon name="smartphone" :size="22" /><p>Your other phones are signed out and ask for the new password. This phone stays signed in.</p></section>
    <p class="alt-line">Forgot it? <button type="button" class="link">Get a reset link by email</button></p>
  </div>
  <div class="action-bar">
    <MobileButton size="md" style="flex: 1;">Change password</MobileButton>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <MobileTopBar title="Password" leading="back" back-label="Back to account" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <section class="error-card" id="password-error" data-component="error-card" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><p>That isn’t your current password. Nothing changed.</p></section>
    <div class="field" id="current-password-field"><label class="label" for="current-password">Current password</label><span class="password-field"><input class="text-field" id="current-password" type="password" value="wrong guess" autocomplete="current-password" aria-invalid="true"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <div class="field" id="new-password-field"><label class="label" for="new-password">New password</label><span class="password-field"><input class="text-field" id="new-password" type="password" value="purple octopus stapler" autocomplete="new-password" aria-describedby="new-password-hint"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span><span class="field-note" id="new-password-hint">8 characters or more.</span></div>
    <section class="info-card" id="other-phones" data-component="info-card"><MobileIcon name="smartphone" :size="22" /><p>Your other phones are signed out and ask for the new password. This phone stays signed in.</p></section>
    <p class="alt-line">Forgot it? <button type="button" class="link">Get a reset link by email</button></p>
  </div>
  <div class="action-bar">
    <MobileButton size="md" style="flex: 1;">Change password</MobileButton>
  </div>
</main>
`})))()}var s;function c(){return(c=e((()=>{s=`<main class="phone">
  <MobileTopBar title="Signed-in devices" leading="back" back-label="Back to account" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <span class="field-note">Every phone and browser signed in to alex@example.com. Signing one out keeps its copy of your training on it, as a guest.</span>
    <article class="device-row" id="this-phone" data-component="device-row"><span class="device-icon device-icon--sm" aria-hidden="true"><MobileIcon name="smartphone" :size="22" /></span><span class="device-text" style="gap: var(--cp-space-xs);"><strong>Pixel 8</strong><span class="kind-tag">This phone</span></span></article>
    <article class="device-row" id="other-phone" data-component="device-row"><span class="device-icon device-icon--sm device-icon--off" aria-hidden="true"><MobileIcon name="smartphone" :size="22" /></span><span class="device-text"><strong>Ana’s iPhone</strong><small>Last used 3 days ago</small></span><MobileButton kind="secondary" size="sm">Sign out</MobileButton></article>
    <article class="device-row" id="browser" data-component="device-row"><span class="device-icon device-icon--sm device-icon--off" aria-hidden="true"><MobileIcon name="monitor" :size="22" /></span><span class="device-text"><strong>Firefox on Windows</strong><small>Last used 2 weeks ago</small></span><MobileButton kind="secondary" size="sm">Sign out</MobileButton></article>
    <div class="leave-actions">
      <button type="button" class="text-button text-button--danger" id="sign-out-others" style="flex: none;"><MobileIcon name="log-out" :size="20" />&nbsp;Sign out every other device</button>
    </div>
  </div>
</main>
`})))()}var l;function u(){return(u=e((()=>{l=`<main class="phone">
  <MobileTopBar title="Export my data" leading="back" back-label="Back to account" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <section class="card" id="contents" data-component="info-card"><h2 class="headline" style="font-size: 17px; line-height: 22px;">What’s in it</h2><p class="muted" style="font-size: 14px; line-height: 20px; margin-top: 4px;">One .zip file: your workouts, plans, sessions, profile and body log as JSON, and each session as a FIT file you can open in Garmin Connect or Strava.</p></section>
    <span class="field-note">It is built from your account, so it holds what every phone sent. Changes still waiting on this phone are sent first.</span>
  </div>
  <div class="action-bar">
    <MobileButton size="md" icon="download" style="flex: 1;">Export my data</MobileButton>
  </div>
</main>
`})))()}var d,f,p,m,h,g;function _(){return(_=e((()=>{t(),i(),o(),c(),u(),d={title:`Mobile/Screens/You/Accounts/Password, devices and export`,tags:[`!autodocs`,`draft`],parameters:{layout:`fullscreen`,mockup:{issue:553},docs:{canvas:{sourceState:`none`}}}},f={name:`Change password`,...n(r)},p={name:`Change password, wrong current password`,...n(a)},m={name:`Signed-in devices`,...n(s)},h={name:`Export my data`,...n(l)},g=[`AccountPassword`,`AccountPasswordError`,`AccountDevices`,`AccountExport`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  name: 'Change password',
  ...screen(screenAccountPassword)
}`,...f.parameters?.docs?.source},description:{story:`Password · the current one and a new one; the other phones are signed out, this one stays signed in.`,...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  name: 'Change password, wrong current password',
  ...screen(screenAccountPasswordError)
}`,...p.parameters?.docs?.source},description:{story:`Password · the current password is wrong, nothing changed.`,...p.parameters?.docs?.description}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  name: 'Signed-in devices',
  ...screen(screenAccountDevices)
}`,...m.parameters?.docs?.source},description:{story:`Signed-in devices · this phone first, then every other phone and browser with when it was last used and Sign out.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  name: 'Export my data',
  ...screen(screenAccountExport)
}`,...h.parameters?.docs?.source},description:{story:`Export my data · one .zip from the account: JSON, and each session as a FIT file.`,...h.parameters?.docs?.description}}}})))()}_();export{m as AccountDevices,h as AccountExport,f as AccountPassword,p as AccountPasswordError,g as __namedExportsOrder,d as default};
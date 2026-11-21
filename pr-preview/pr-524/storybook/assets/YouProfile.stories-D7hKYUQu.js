import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-BZjud16T.js";var r;function i(){return(i=e((()=>{r=`<main class="phone" style="--frame-height: 1460px;">
  <MobileTopBar title="Profile" leading="back" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div id="account-list">
      <MobileSettingsRow card icon="user-round" title="Account" detail="Guest · create an account or sign in" />
    </div>
    <span class="label label-row">About you</span>
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
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone" style="--frame-height: 1460px;">
  <MobileTopBar title="Profile" leading="back" />
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div id="account-list">
      <MobileSettingsRow card icon="user-round" title="Account" detail="alex@example.com · signed in" />
    </div>
    <span class="label label-row">About you</span>
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
`})))()}var s,c,l,u;function d(){return(d=e((()=>{t(),i(),o(),s={title:`Mobile/Screens/You/Profile`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},c={name:`Profile, guest`,...n(r)},l={name:`Profile, signed in`,...n(a)},u=[`Profile`,`ProfileSignedIn`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  name: 'Profile, guest',
  ...screen(screenProfile)
}`,...c.parameters?.docs?.source},description:{story:`Profile · a guest: one Account row on top (it opens Create an account and Sign in), then about you with a Poulpy face, training and body.`,...c.parameters?.docs?.description}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  name: 'Profile, signed in',
  ...screen(screenProfileSignedIn)
}`,...l.parameters?.docs?.source},description:{story:`Profile · signed in: one Account row at the top, showing the email; it opens the existing account screen (password, devices, export, sign out, delete).`,...l.parameters?.docs?.description}}}})))()}d();export{c as Profile,l as ProfileSignedIn,u as __namedExportsOrder,s as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-DPTeSHtq.js";var r;function i(){return(i=e((()=>{r=`<main class="phone" style="--frame-height: 760px">
  <header class="top-bar" style="justify-content: flex-start;">
    <button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button>
  </header>

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
`})))()}var a,o,s;function c(){return(c=e((()=>{t(),i(),a={title:`Mobile/Screens/Workout import`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},o={name:`Refused`,...n(r),parameters:{mockup:{issue:129}}},s=[`WorkoutImportError`],o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  name: "Refused",
  ...screen(screenWorkoutImportError),
  parameters: {
    mockup: {
      issue: 129
    }
  }
}`,...o.parameters?.docs?.source},description:{story:`Workout import · refused.`,...o.parameters?.docs?.description}}}})))()}c();export{o as WorkoutImportError,s as __namedExportsOrder,a as default};
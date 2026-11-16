import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-DY5uz4q6.js";var r;function i(){return(i=e((()=>{r=`<main class="phone" style="--frame-height: 760px">
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
    <MobileButton kind="secondary">Close</MobileButton>
  </div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone" style="--frame-height: 760px">
  <header class="top-bar" style="justify-content: flex-start;">
    <button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button>
  </header>

  <div class="page-body" style="gap: var(--cp-space-lg);">
    <div style="display: flex; flex-direction: column; gap: var(--cp-space-xs);">
      <span class="label label--primary">Someone sent you a workout</span>
      <h1 class="title" style="font-size: 30px; line-height: 34px;">Add this workout?</h1>
    </div>

    <section class="card" id="preview" data-component="card" aria-label="The workout in the file" style="display: flex; flex-direction: column; gap: var(--cp-space-sm);">
      <h2 class="headline">Morning circuit</h2>
      <p class="workout-details">
        <MobileFormatChip icon="repeat">Circuit</MobileFormatChip>
        <span>3 parts · 12 steps</span>
      </p>
    </section>

    <p class="muted">It is added as a copy in your library. Nothing you already have changes.</p>
  </div>

  <div class="action-bar" style="flex-direction: column; gap: var(--cp-space-sm);">
    <MobileButton large>Add to my library</MobileButton>
    <button type="button" class="text-link" style="text-align: center;">Cancel</button>
  </div>
</main>
`})))()}var s,c,l,u;function d(){return(d=e((()=>{t(),i(),o(),s={title:`Mobile/Screens/Workout import`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},c={name:`Refused`,...n(r),parameters:{mockup:{issue:129}}},l={name:`Preview`,...n(a),parameters:{mockup:{issue:129}}},u=[`WorkoutImportError`,`WorkoutImport`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  name: "Refused",
  ...screen(screenWorkoutImportError),
  parameters: {
    mockup: {
      issue: 129
    }
  }
}`,...c.parameters?.docs?.source},description:{story:`Workout import · refused.`,...c.parameters?.docs?.description}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  name: "Preview",
  ...screen(screenWorkoutImport),
  parameters: {
    mockup: {
      issue: 129
    }
  }
}`,...l.parameters?.docs?.source},description:{story:`Workout import · preview.`,...l.parameters?.docs?.description}}}})))()}d();export{l as WorkoutImport,c as WorkoutImportError,u as __namedExportsOrder,s as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,r as n}from"./screen-CD9Yt_Yp.js";var r;function i(){return(i=e((()=>{r=`<main class="phone">
  <iframe class="base-screen" src="iframe.html?id=mobile-screens-home-sessions-session-recap--session-recap-sets-full&amp;viewMode=story&amp;embedded" title="Session recap, behind" aria-hidden="true" tabindex="-1" inert></iframe>
  <div class="scrim" aria-hidden="true"></div>
  <div class="dialog" id="delete-dialog"><div role="dialog" aria-modal="true" aria-labelledby="delete-title" data-component="dialog">
    <img src="../../../poulpy/pose_thinking.svg" alt="">
    <h2 id="delete-title">Delete this session?</h2>
    <p>It leaves your history and stats. This can’t be undone.</p>
    <div class="actions"><MobileButton kind="secondary" size="md">Cancel</MobileButton><button type="button" class="danger-button"><MobileIcon name="trash" :size="20" />Delete</button></div>
  </div></div>
</main>
`})))()}var a;function o(){return(o=e((()=>{a=`<main class="phone">
  <iframe class="base-screen" src="iframe.html?id=mobile-screens-home-sessions-session-recap--session-recap-sets-full&amp;viewMode=story&amp;embedded" title="Session recap, behind" aria-hidden="true" tabindex="-1" inert></iframe>
  <div class="scrim"></div>
  <MobileSheet id="share-sheet" aria-label="Share this session" style="gap: var(--cp-space-sm);">
    <span class="sheet-handle" aria-hidden="true"></span>
    <div class="sheet-head" style="padding: 0;">
      <div class="sheet-title" style="flex: 1;"><h2>Share this session</h2><p>Pick a format.</p></div>
      <button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button>
    </div>
    <MobileSettingsRow card icon="download" title=".FIT file" detail="Save it, then import it on the Garmin Connect or Strava website" />
    <MobileSettingsRow card icon="image" title="Image" detail="A card with your numbers to send or post" />
  </MobileSheet>
</main>
`})))()}var s,c,l,u;function d(){return(d=e((()=>{t(),i(),o(),s={title:`Mobile/Screens/Home/Sessions/Share and delete`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},c={name:`Delete a session`,...n(r),parameters:{mockup:{issue:104}}},l={name:`Share a session`,...n(a),parameters:{mockup:{issue:104}}},u=[`SessionDelete`,`SessionShare`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  name: "Delete a session",
  ...screen(screenSessionDelete),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...c.parameters?.docs?.source},description:{story:`Delete a session, from its recap.`,...c.parameters?.docs?.description}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  name: "Share a session",
  ...screen(screenSessionShare),
  parameters: {
    mockup: {
      issue: 104
    }
  }
}`,...l.parameters?.docs?.source},description:{story:`Share a session, from its recap.`,...l.parameters?.docs?.description}}}})))()}d();export{c as SessionDelete,l as SessionShare,u as __namedExportsOrder,s as default};
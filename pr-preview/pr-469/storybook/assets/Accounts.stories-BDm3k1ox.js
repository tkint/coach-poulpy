import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as ee,r as t}from"./screen-DsyD2Ie0.js";var n;function r(){return(r=e((()=>{n=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="page-body" style="gap: var(--cp-space-lg); padding-top: var(--cp-space-lg);">
    <img src="../../../poulpy/pose_standing_tall.svg" alt="" width="132" height="120" style="align-self: center;">
    <p class="poulpy-line" id="poulpy-line">Take your time. I’ll wait by the mailbox.</p>
    <div class="intro" style="text-align: center;"><h1 class="title" style="font-size: 30px; line-height: 34px;">Check your email</h1>
    <p class="muted">If <strong>alex@example.com</strong> can get an account, a link is on its way. Open it on this phone to finish. It works for 24 hours.</p></div>
    <MobileInfoCard id="guest-note">Until then, your training stays on this phone and the app works as usual.</MobileInfoCard>
    <p class="done-line" id="resent" role="status"><MobileIcon name="check" :size="18" /><span>Link sent again.</span></p>
    <div class="help-lines" id="help-lines"><p>Didn’t get the email? <button type="button" class="link">Send it again</button></p><p>Wrong address? <button type="button" class="link">Try again with another email</button></p></div>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">OK</MobileButton>
  </div>
</main>
`})))()}var i;function a(){return(a=e((()=>{i=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="page-body" style="gap: var(--cp-space-lg); padding-top: var(--cp-space-lg);">
    <img src="../../../poulpy/pose_standing_tall.svg" alt="" width="132" height="120" style="align-self: center;">
    <p class="poulpy-line" id="poulpy-line">Take your time. I’ll wait by the mailbox.</p>
    <div class="intro" style="text-align: center;"><h1 class="title" style="font-size: 30px; line-height: 34px;">Check your email</h1>
    <p class="muted">If <strong>alex@example.com</strong> can get an account, a link is on its way. Open it on this phone to finish. It works for 24 hours.</p></div>
    <MobileInfoCard id="guest-note">Until then, your training stays on this phone and the app works as usual.</MobileInfoCard>
    
    <div class="help-lines" id="help-lines"><p>Didn’t get the email? <button type="button" class="link">Send it again</button></p><p>Wrong address? <button type="button" class="link">Try again with another email</button></p></div>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">OK</MobileButton>
  </div>
</main>
`})))()}var o;function te(){return(te=e((()=>{o=`<main class="phone">
  <div class="screen-header">
    <span class="label">Saturday 24 October</span>
    <h1>Home</h1>
  </div>
  <div class="page-body" style="padding-top: var(--cp-space-sm);"></div>
  <MobileNavBar active="home" />
  <div class="scrim" aria-hidden="true"></div>
  <MobileSheet id="email-confirmed-sheet" aria-label="Email confirmed">
    <span class="sheet-handle"></span>
    <img src="../../../poulpy/pose_champion.svg" alt="" width="140" height="127" style="align-self: center;">
    <p class="poulpy-line" id="poulpy-line">Lifting your training into the cloud. Eight tentacles, no waiting.</p>
    <div class="sheet-title"><h2>Email confirmed</h2></div>
    <section class="card progress-card" id="progress" data-component="progress-card" role="status">
      <strong>Sending your training…</strong>
      <div class="progress-track" aria-hidden="true"><span style="width: 62%;"></span></div>
      <small class="numbers">18 of 29 sent</small>
    </section>
    <span class="field-note">It carries on when you’re back online.</span>
    <div class="sheet-actions"><MobileButton>Continue</MobileButton></div>
  </MobileSheet>
</main>
`})))()}var ne;function re(){return(re=e((()=>{ne=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to settings"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Account</h1></div></header>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="card account-card" id="account-email" data-component="account-card"><span class="label">Email</span><strong>alex@example.com</strong></div>
    <div class="sync-state" id="sync-state" role="status"><span class="dot" aria-hidden="true"></span><span><strong>Up to date</strong><small>Last synced 2 minutes ago</small></span></div>
    <section class="card" id="follows" data-component="info-card"><h2 class="headline" style="font-size: 17px; line-height: 22px;">What follows your account</h2><p class="muted" style="font-size: 14px; line-height: 20px; margin-top: 4px;">Workouts, plans and sessions, your profile and body log, units and language. Theme, app icon and paired devices stay on each phone.</p></section>
    <div class="settings-list" id="data">
      <MobileSettingsRow icon="download" title="Export my data" detail="JSON, and your sessions as FIT files" />
    </div>
    <div class="leave-actions">
      <MobileButton kind="secondary" icon="log-out" id="sign-out">Sign out</MobileButton>
      <button type="button" class="text-button text-button--danger" id="delete" style="flex: none;"><MobileIcon name="trash" :size="20" />&nbsp;Delete account</button>
    </div>
  </div>
  <div class="scrim" aria-hidden="true"></div>
  <MobileSheet id="delete-sheet" aria-label="Delete your account?">
    <span class="sheet-handle"></span>
    <div class="sheet-title"><h2>Delete your account?</h2><p>This deletes everything stored in your account, right away and for good. Every phone signed in goes back to a guest and keeps its own copy. Backups age out within 30 days.</p></div>
    <section class="error-card" id="delete-error" data-component="error-card" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><p>That password isn’t right. Nothing was deleted.</p></section>
    <div class="field" id="delete-password-field"><label class="label" for="delete-password">Enter your password to confirm</label><span class="password-field"><input class="text-field" id="delete-password" type="password" value="poulpy123" autocomplete="current-password"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <div class="sheet-actions"><button type="button" class="secondary-button">Cancel</button><button type="button" class="danger-button" style="flex: 1.6;"><MobileIcon name="trash" :size="20" />Delete account</button></div>
  </MobileSheet>
</main>
`})))()}var ie;function ae(){return(ae=e((()=>{ie=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to settings"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Account</h1></div></header>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="card account-card" id="account-email" data-component="account-card"><span class="label">Email</span><strong>alex@example.com</strong></div>
    <div class="sync-state" id="sync-state" role="status"><span class="dot" aria-hidden="true"></span><span><strong>Up to date</strong><small>Last synced 2 minutes ago</small></span></div>
    <section class="card" id="follows" data-component="info-card"><h2 class="headline" style="font-size: 17px; line-height: 22px;">What follows your account</h2><p class="muted" style="font-size: 14px; line-height: 20px; margin-top: 4px;">Workouts, plans and sessions, your profile and body log, units and language. Theme, app icon and paired devices stay on each phone.</p></section>
    <div class="settings-list" id="data">
      <MobileSettingsRow icon="download" title="Export my data" detail="JSON, and your sessions as FIT files" />
    </div>
    <div class="leave-actions">
      <MobileButton kind="secondary" icon="log-out" id="sign-out">Sign out</MobileButton>
      <button type="button" class="text-button text-button--danger" id="delete" style="flex: none;"><MobileIcon name="trash" :size="20" />&nbsp;Delete account</button>
    </div>
  </div>
  <div class="scrim" aria-hidden="true"></div>
  <MobileSheet id="delete-sheet" aria-label="Delete your account?">
    <span class="sheet-handle"></span>
    <div class="sheet-title"><h2>Delete your account?</h2><p>This deletes everything stored in your account, right away and for good. Every phone signed in goes back to a guest and keeps its own copy. Backups age out within 30 days.</p></div>
    
    <div class="field" id="delete-password-field"><label class="label" for="delete-password">Enter your password to confirm</label><span class="password-field"><input class="text-field" id="delete-password" type="password" value="" autocomplete="current-password"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <div class="sheet-actions"><button type="button" class="secondary-button">Cancel</button><button type="button" class="danger-button" style="flex: 1.6;"><MobileIcon name="trash" :size="20" />Delete account</button></div>
  </MobileSheet>
</main>
`})))()}var oe;function se(){return(se=e((()=>{oe=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to settings"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Account</h1></div></header>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="card account-card" id="account-email" data-component="account-card"><span class="label">Email</span><strong>alex@example.com</strong></div>
    <section class="info-card info-card--stack" id="confirm-card" data-component="info-card">
      <div class="info-title"><MobileIcon name="mail" :size="22" /><strong>Confirm your email</strong></div>
      <p>We sent a link to alex@example.com. Until you open it, nothing leaves this phone.</p>
      <div class="help-lines" id="help-lines"><p>Didn’t get the email? <button type="button" class="link">Send it again</button></p><p>Wrong address? <button type="button" class="link">Try again with another email</button></p></div>
    </section>
    <div class="sync-state" id="sync-state"><span class="dot dot--idle" aria-hidden="true"></span><span><strong>Not syncing yet</strong><small>This phone stays a guest until you confirm your email.</small></span></div>
    
  </div>
</main>
`})))()}var ce;function le(){return(le=e((()=>{ce=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to settings"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Account</h1></div></header>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="card account-card" id="account-email" data-component="account-card"><span class="label">Email</span><strong>alex@example.com</strong></div>
    <div class="sync-state" id="sync-state" role="status"><span class="dot" aria-hidden="true"></span><span><strong>Up to date</strong><small>Last synced 2 minutes ago</small></span></div>
    <section class="card" id="follows" data-component="info-card"><h2 class="headline" style="font-size: 17px; line-height: 22px;">What follows your account</h2><p class="muted" style="font-size: 14px; line-height: 20px; margin-top: 4px;">Workouts, plans and sessions, your profile and body log, units and language. Theme, app icon and paired devices stay on each phone.</p></section>
    <div class="settings-list" id="data">
      <MobileSettingsRow icon="download" title="Export my data" detail="JSON, and your sessions as FIT files" />
    </div>
    <div class="leave-actions">
      <MobileButton kind="secondary" icon="log-out" id="sign-out">Sign out</MobileButton>
      <button type="button" class="text-button text-button--danger" id="delete" style="flex: none;"><MobileIcon name="trash" :size="20" />&nbsp;Delete account</button>
    </div>
  </div>
</main>
`})))()}var ue;function de(){return(de=e((()=>{ue=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <img src="../../../poulpy/pose_standing_tall.svg" alt="" width="120" height="109" style="align-self: center;">
    <div class="intro" style="text-align: center;"><h1 class="title" style="font-size: 30px; line-height: 34px;">Check your email</h1><p class="muted">If <strong>alex@example.com</strong> has an account, a link to choose a new password is on its way.</p></div>
    <MobileInfoCard id="all-phones">Choosing a new password signs every phone out.</MobileInfoCard>
    <div class="help-lines" id="help-lines"><p>Didn’t get the email? <button type="button" class="link">Send it again</button></p></div>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">OK</MobileButton>
  </div>
</main>
`})))()}var fe;function pe(){return(pe=e((()=>{fe=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Reset your password</h1><p class="muted">Enter your email and we’ll send a link to choose a new password.</p></div>
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com" autocomplete="email"></div>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Send the link</MobileButton>
  </div>
</main>
`})))()}var me;function he(){return(he=e((()=>{me=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="page-body" style="gap: var(--cp-space-lg); padding-top: var(--cp-space-lg);">
    <img src="../../../poulpy/pose_thinking.svg" alt="" width="132" height="120" style="align-self: center;">
    <div class="intro" style="text-align: center;"><h1 class="title" style="font-size: 30px; line-height: 34px;">This link doesn’t work anymore</h1>
    <p class="muted">It may have been used already, or it expired after 24 hours. Sign up again with the same address and we’ll send a new one.</p></div>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Back to sign-up</MobileButton>
  </div>
</main>
`})))()}var ge;function _e(){return(_e=e((()=>{ge=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Choose a new password</h1><p class="muted">Every phone signed in to this account will be signed out.</p></div>
    <div class="field" id="new-password-field"><label class="label" for="new-password">New password</label><span class="password-field"><input class="text-field" id="new-password" type="password" value="a new horse battery" autocomplete="new-password" aria-describedby="new-password-hint"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span><span class="field-note" id="new-password-hint">8 characters or more.</span></div>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Save password</MobileButton>
  </div>
</main>
`})))()}var ve;function ye(){return(ye=e((()=>{ve=`<main class="phone" style="--frame-height: 900px;">
  <div class="settings-head"><h1>Settings</h1></div>
  <div class="settings-body" style="padding-top: var(--cp-space-md);">
    <button type="button" class="profile-card" id="profile" data-component="profile-card"><img src="../../../poulpy/mood_happy.svg" alt="" aria-hidden="true"><span><strong>Alex</strong><small>34 years · 175 cm · 72.6 kg</small><small>Edit your profile</small></span><MobileIcon name="chevron-right" :size="22" /></button>
    <span class="label label-row">Account</span>
    <div class="settings-list" id="account-list">
      <MobileSettingsRow id="account" icon="user-round" title="Sign in or create an account" detail="Back up your training and use it on other phones." />
    </div>
    <span class="label label-row">Body</span>
    <div class="settings-list" id="body">
      <MobileSettingsRow icon="move-vertical" title="Height" detail="175 cm · logged in January" />
      <MobileSettingsRow icon="weight" title="Weight" detail="72.6 kg · logged 3 days ago" />
    </div>
    <span class="label label-row">App</span>
    <div class="settings-list" id="app">
      <MobileSettingsRow icon="contrast" title="Appearance and units" detail="System theme · English · Metric" />
      <MobileSettingsRow icon="gauge" title="Devices" detail="3 connected · treadmill, foot pod, heart rate band" />
    </div>
  </div>
  <MobileNavBar active="settings" />
</main>
`})))()}var be;function s(){return(s=e((()=>{be=`<main class="phone" style="--frame-height: 900px;">
  <div class="settings-head"><h1>Settings</h1></div>
  <div class="settings-body" style="padding-top: var(--cp-space-md);">
    <button type="button" class="profile-card" id="profile" data-component="profile-card"><img src="../../../poulpy/mood_happy.svg" alt="" aria-hidden="true"><span><strong>Alex</strong><small>34 years · 175 cm · 72.6 kg</small><small>Edit your profile</small></span><MobileIcon name="chevron-right" :size="22" /></button>
    <span class="label label-row">Account</span>
    <div class="settings-list" id="account-list">
      <MobileSettingsRow id="account" icon="mail" title="Confirm your email" detail="Link sent to alex@example.com" />
    </div>
    <span class="label label-row">Body</span>
    <div class="settings-list" id="body">
      <MobileSettingsRow icon="move-vertical" title="Height" detail="175 cm · logged in January" />
      <MobileSettingsRow icon="weight" title="Weight" detail="72.6 kg · logged 3 days ago" />
    </div>
    <span class="label label-row">App</span>
    <div class="settings-list" id="app">
      <MobileSettingsRow icon="contrast" title="Appearance and units" detail="System theme · English · Metric" />
      <MobileSettingsRow icon="gauge" title="Devices" detail="3 connected · treadmill, foot pod, heart rate band" />
    </div>
  </div>
  <MobileNavBar active="settings" />
</main>
`})))()}var c;function l(){return(l=e((()=>{c=`<main class="phone" style="--frame-height: 900px;">
  <div class="settings-head"><h1>Settings</h1></div>
  <div class="settings-body" style="padding-top: var(--cp-space-md);">
    <button type="button" class="profile-card" id="profile" data-component="profile-card"><img src="../../../poulpy/mood_happy.svg" alt="" aria-hidden="true"><span><strong>Alex</strong><small>34 years · 175 cm · 72.6 kg</small><small>Edit your profile</small></span><MobileIcon name="chevron-right" :size="22" /></button>
    <span class="label label-row">Account</span>
    <div class="settings-list" id="account-list">
      <MobileSettingsRow id="account" icon="user-round" title="alex@example.com" detail="Up to date · Last synced 2 min ago" />
    </div>
    <span class="label label-row">Body</span>
    <div class="settings-list" id="body">
      <MobileSettingsRow icon="move-vertical" title="Height" detail="175 cm · logged in January" />
      <MobileSettingsRow icon="weight" title="Weight" detail="72.6 kg · logged 3 days ago" />
    </div>
    <span class="label label-row">App</span>
    <div class="settings-list" id="app">
      <MobileSettingsRow icon="contrast" title="Appearance and units" detail="System theme · English · Metric" />
      <MobileSettingsRow icon="gauge" title="Devices" detail="3 connected · treadmill, foot pod, heart rate band" />
    </div>
  </div>
  <MobileNavBar active="settings" />
</main>
`})))()}var u;function d(){return(d=e((()=>{u=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Sign in</h1><p class="muted">Use the account you made on another phone. What is on this phone joins your account; nothing is replaced.</p></div>
    
    <section class="error-card" id="sign-in-error" data-component="error-card" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><p>Wrong email or password. Check both and try again.</p></section>
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com" autocomplete="email"></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field" id="password" type="password" value="poulpy123" autocomplete="current-password"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <button type="button" class="link forgot-link" id="forgot">Forgot your password?</button>
    <p class="alt-line" id="alt-line">No account yet? <button type="button" class="link">Create one</button></p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Sign in</MobileButton>
  </div>
</main>
`})))()}var f;function p(){return(p=e((()=>{f=`<main class="phone">
  <div class="page-body" style="gap: var(--cp-space-lg); padding-top: 96px;">
    <img src="../../../poulpy/pose_standing_tall.svg" alt="" width="140" height="127" style="align-self: center;">
    
    <div class="intro" style="text-align: center;"><h1 class="title" style="font-size: 30px; line-height: 34px;">Welcome back</h1>
    <p class="muted">Your workouts and sessions on this phone are joining your account. Nothing is replaced, and both edits are kept.</p></div>
    <section class="card progress-card" id="progress" data-component="progress-card" role="status">
      <strong>Sending your training…</strong>
      <div class="progress-track" aria-hidden="true"><span style="width: 62%;"></span></div>
      <small class="numbers">18 of 29 sent</small>
    </section>
    <p class="field-note" style="text-align: center;">It carries on when you’re back online.</p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Continue</MobileButton>
  </div>
</main>
`})))()}var m;function h(){return(h=e((()=>{m=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Sign in</h1><p class="muted">Use the account you made on another phone. What is on this phone joins your account; nothing is replaced.</p></div>
    <MobileInfoCard icon="check" :icon-size="18" id="reset-done" role="status">Password changed. Sign in with the new one.</MobileInfoCard>
    
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com" autocomplete="email"></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field" id="password" type="password" value="" autocomplete="current-password"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <button type="button" class="link forgot-link" id="forgot">Forgot your password?</button>
    <p class="alt-line" id="alt-line">No account yet? <button type="button" class="link">Create one</button></p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Sign in</MobileButton>
  </div>
</main>
`})))()}var g;function _(){return(_=e((()=>{g=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Sign in</h1><p class="muted">Use the account you made on another phone. What is on this phone joins your account; nothing is replaced.</p></div>
    
    
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com" autocomplete="email"></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field" id="password" type="password" value="" autocomplete="current-password"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
    <button type="button" class="link forgot-link" id="forgot">Forgot your password?</button>
    <p class="alt-line" id="alt-line">No account yet? <button type="button" class="link">Create one</button></p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Sign in</MobileButton>
  </div>
</main>
`})))()}var v;function y(){return(y=e((()=>{v=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to settings"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Account</h1></div></header>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="card account-card" id="account-email" data-component="account-card"><span class="label">Email</span><strong>alex@example.com</strong></div>
    <div class="sync-state" id="sync-state" role="status"><span class="dot" aria-hidden="true"></span><span><strong>Up to date</strong><small>Last synced 2 minutes ago</small></span></div>
    <section class="card" id="follows" data-component="info-card"><h2 class="headline" style="font-size: 17px; line-height: 22px;">What follows your account</h2><p class="muted" style="font-size: 14px; line-height: 20px; margin-top: 4px;">Workouts, plans and sessions, your profile and body log, units and language. Theme, app icon and paired devices stay on each phone.</p></section>
    <div class="settings-list" id="data">
      <MobileSettingsRow icon="download" title="Export my data" detail="JSON, and your sessions as FIT files" />
    </div>
    <div class="leave-actions">
      <MobileButton kind="secondary" icon="log-out" id="sign-out">Sign out</MobileButton>
      <button type="button" class="text-button text-button--danger" id="delete" style="flex: none;"><MobileIcon name="trash" :size="20" />&nbsp;Delete account</button>
    </div>
  </div>
  <div class="scrim" aria-hidden="true"></div>
  <div class="dialog" id="dialog"><div role="alertdialog" aria-modal="true" aria-labelledby="dialog-title" aria-describedby="dialog-body" data-component="dialog">
    <span class="dialog-icon" aria-hidden="true"><MobileIcon name="cloud" :size="22" /></span>
    <h2 id="dialog-title">Some changes aren’t sent yet</h2>
    <p id="dialog-body">They are waiting for a connection. If you sign out now, they never reach your account and are gone from this phone.</p>
    <div class="actions actions--stack"><button type="button" class="primary-button">Wait for the upload</button><button type="button" class="text-button text-button--danger" style="flex: none; height: 48px;">Sign out anyway</button></div>
  </div></div>
</main>
`})))()}var b;function x(){return(x=e((()=>{b=`<main class="phone">
  <header class="page-bar"><button type="button" class="icon-button" aria-label="Back to settings"><MobileIcon name="chevron-left" :size="22" /></button><div><h1>Account</h1></div></header>
  <div class="form-body" style="padding-top: var(--cp-space-lg);">
    <div class="card account-card" id="account-email" data-component="account-card"><span class="label">Email</span><strong>alex@example.com</strong></div>
    <div class="sync-state" id="sync-state" role="status"><span class="dot" aria-hidden="true"></span><span><strong>Up to date</strong><small>Last synced 2 minutes ago</small></span></div>
    <section class="card" id="follows" data-component="info-card"><h2 class="headline" style="font-size: 17px; line-height: 22px;">What follows your account</h2><p class="muted" style="font-size: 14px; line-height: 20px; margin-top: 4px;">Workouts, plans and sessions, your profile and body log, units and language. Theme, app icon and paired devices stay on each phone.</p></section>
    <div class="settings-list" id="data">
      <MobileSettingsRow icon="download" title="Export my data" detail="JSON, and your sessions as FIT files" />
    </div>
    <div class="leave-actions">
      <MobileButton kind="secondary" icon="log-out" id="sign-out">Sign out</MobileButton>
      <button type="button" class="text-button text-button--danger" id="delete" style="flex: none;"><MobileIcon name="trash" :size="20" />&nbsp;Delete account</button>
    </div>
  </div>
  <div class="scrim" aria-hidden="true"></div>
  <div class="dialog" id="dialog"><div role="alertdialog" aria-modal="true" aria-labelledby="dialog-title" aria-describedby="dialog-body" data-component="dialog">
    <img src="../../../poulpy/pose_thinking.svg" alt="">
    <h2 id="dialog-title">Sign out?</h2>
    <p id="dialog-body">Everything is sent already. This phone goes back to an empty guest; your workouts and sessions stay in your account.</p>
    <div class="actions"><button type="button" class="secondary-button">Cancel</button><button type="button" class="primary-button"><MobileIcon name="log-out" :size="20" />Sign out</button></div>
  </div></div>
</main>
`})))()}var S;function C(){return(C=e((()=>{S=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Create your account</h1></div>
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com"></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field" id="password" type="password" value="correct horse battery"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span></div>
  </div>
  <div class="scrim" aria-hidden="true"></div>
  <MobileSheet id="consent-sheet" aria-label="What is stored">
    <span class="sheet-handle"></span>
    <div class="sheet-title"><h2>What is stored</h2><p>Once your email is confirmed, your phone sends this to Coach Poulpy’s server so you can find it on other phones:</p></div>
    <ul class="tick-list"><li><MobileIcon name="check" :size="18" /><span>Workouts, plans and sessions</span></li><li><MobileIcon name="check" :size="18" /><span>Your profile and body measurements, including pain or rehab</span></li><li><MobileIcon name="check" :size="18" /><span>Units and language</span></li></ul>
    <span class="field-note">Theme, app icon and paired devices stay on each phone.</span>
    <span class="field-note">Why: to back up and sync. Nothing is shared with anyone, and you can delete the account and everything in it whenever you like.</span>
    <span class="field-note"><strong>Until you confirm your email, nothing leaves this phone.</strong></span>
    <div class="sheet-actions"><button type="button" class="primary-button">Got it</button></div>
  </MobileSheet>
</main>
`})))()}var w;function T(){return(T=e((()=>{w=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Create your account</h1><p class="muted">Back up your training and pick it up on any phone. The app keeps working without a connection.</p></div>
    
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field text-field--error" id="email" type="email" value="alex@example" autocomplete="email" aria-invalid="true" aria-describedby="email-error"><span class="field-error" id="email-error" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><span>That address doesn’t look right. It should look like name@example.com.</span></span></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field text-field--error" id="password" type="password" value="poulpy" autocomplete="new-password" aria-invalid="true" aria-describedby="password-error"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span><span class="field-error" id="password-error" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><span>Use at least 8 characters.</span></span></div>
    <div class="field" id="consent-field"><label class="check-row" data-component="check-row"><input type="checkbox" id="consent" aria-invalid="true" aria-describedby="consent-error"><span>I agree that Coach Poulpy keeps my health data (sessions, measurements, pain) to sync it between my phones. <button type="button" class="link">What is stored and why</button></span></label><span class="field-error" id="consent-error" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><span>Tick the box to create an account. Without it, nothing leaves this phone.</span></span></div>
    <p class="alt-line" id="alt-line">Already have an account? <button type="button" class="link">Sign in</button></p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Create account</MobileButton>
  </div>
</main>
`})))()}var E;function D(){return(D=e((()=>{E=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Create your account</h1><p class="muted">Back up your training and pick it up on any phone. The app keeps working without a connection.</p></div>
    <section class="error-card" id="offline-error" data-component="error-card" role="alert"><MobileIcon name="circle-exclamation-point" :size="20" /><p>No connection. Try again when you’re online; the app keeps working as a guest.</p></section>
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com" autocomplete="email"></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field" id="password" type="password" value="correct horse battery" autocomplete="new-password" aria-describedby="password-hint"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span><span class="field-note" id="password-hint">8 characters or more.</span></div>
    <div class="field" id="consent-field"><label class="check-row" data-component="check-row"><input type="checkbox" id="consent" checked><span>I agree that Coach Poulpy keeps my health data (sessions, measurements, pain) to sync it between my phones. <button type="button" class="link">What is stored and why</button></span></label></div>
    <p class="alt-line" id="alt-line">Already have an account? <button type="button" class="link">Sign in</button></p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Create account</MobileButton>
  </div>
</main>
`})))()}var O;function k(){return(k=e((()=>{O=`<main class="phone">
  <header class="top-bar" style="justify-content: flex-start;"><button type="button" class="icon-button" aria-label="Close"><MobileIcon name="x" :size="22" /></button></header>
  <div class="form-body" style="padding-top: var(--cp-space-sm);">
    <div class="intro"><h1 class="title" style="font-size: 30px; line-height: 34px;">Create your account</h1><p class="muted">Back up your training and pick it up on any phone. The app keeps working without a connection.</p></div>
    
    <div class="field" id="email-field"><label class="label" for="email">Email</label><input class="text-field" id="email" type="email" value="alex@example.com" autocomplete="email"></div>
    <div class="field" id="password-field"><label class="label" for="password">Password</label><span class="password-field"><input class="text-field" id="password" type="password" value="correct horse battery" autocomplete="new-password" aria-describedby="password-hint"><button type="button" class="icon-button" aria-label="Show password" aria-pressed="false"><MobileIcon name="eye" :size="22" /></button></span><span class="field-note" id="password-hint">8 characters or more.</span></div>
    <div class="field" id="consent-field"><label class="check-row" data-component="check-row"><input type="checkbox" id="consent" checked><span>I agree that Coach Poulpy keeps my health data (sessions, measurements, pain) to sync it between my phones. <button type="button" class="link">What is stored and why</button></span></label></div>
    <p class="alt-line" id="alt-line">Already have an account? <button type="button" class="link">Sign in</button></p>
  </div>
  <div class="action-bar">
    <MobileButton style="flex: 1;">Create account</MobileButton>
  </div>
</main>
`})))()}var xe,A,j,M,N,P,F,I,L,R,z,B,V,H,U,W,G,K,q,J,Y,X,Z,Q,$,Se;function Ce(){return(Ce=e((()=>{ee(),r(),a(),te(),re(),ae(),se(),le(),de(),pe(),he(),_e(),ye(),s(),l(),d(),p(),h(),_(),y(),x(),C(),T(),D(),k(),xe={title:`Mobile/Screens/Accounts`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{},docs:{canvas:{sourceState:`none`}}}},A={name:`Check your email, link sent again`,...t(n),parameters:{a11y:{test:`todo`},mockup:{issue:272}}},j={name:`Check your email`,...t(i),parameters:{mockup:{issue:272}}},M={name:`Email confirmed, first upload (sheet)`,...t(o),parameters:{mockup:{issue:272}}},N={name:`Delete account, wrong password`,...t(ne),parameters:{mockup:{issue:272}}},P={name:`Delete account`,...t(ie),parameters:{mockup:{issue:272}}},F={name:`Account, email not confirmed`,...t(oe),parameters:{a11y:{test:`todo`},mockup:{issue:272}}},I={name:`Account, signed in`,...t(ce),parameters:{mockup:{issue:272}}},L={name:`Forgot password, link sent`,...t(ue),parameters:{mockup:{issue:272}}},R={name:`Forgot password`,...t(fe),parameters:{mockup:{issue:272}}},z={name:`Link expired`,...t(me),parameters:{mockup:{issue:272}}},B={name:`Choose a new password`,...t(ge),parameters:{mockup:{issue:272}}},V={name:`Settings, guest`,...t(ve),parameters:{mockup:{issue:272}}},H={name:`Settings, email not confirmed`,...t(be),parameters:{mockup:{issue:272}}},U={name:`Settings, signed in`,...t(c),parameters:{mockup:{issue:272}}},W={name:`Sign in, wrong password`,...t(u),parameters:{mockup:{issue:272}}},G={name:`Sign in, guest data joins`,...t(f),parameters:{mockup:{issue:272}}},K={name:`Sign in, password changed`,...t(m),parameters:{mockup:{issue:272}}},q={name:`Sign in`,...t(g),parameters:{mockup:{issue:272}}},J={name:`Sign out, changes not sent`,...t(v),parameters:{mockup:{issue:272}}},Y={name:`Sign out`,...t(b),parameters:{mockup:{issue:272}}},X={name:`What is stored`,...t(S),parameters:{mockup:{issue:272}}},Z={name:`Sign up, errors`,...t(w),parameters:{mockup:{issue:272}}},Q={name:`Sign up, offline`,...t(E),parameters:{mockup:{issue:272}}},$={name:`Sign up`,...t(O),parameters:{mockup:{issue:272}}},Se=[`AccountCheckEmailResent`,`AccountCheckEmail`,`AccountConfirmed`,`AccountDeleteError`,`AccountDelete`,`AccountDetailsPending`,`AccountDetails`,`AccountForgotPasswordSent`,`AccountForgotPassword`,`AccountLinkExpired`,`AccountResetPassword`,`AccountSettingsGuest`,`AccountSettingsPending`,`AccountSettingsSynced`,`AccountSignInError`,`AccountSignInMerge`,`AccountSignInResetDone`,`AccountSignIn`,`AccountSignOutUnsent`,`AccountSignOut`,`AccountSignUpConsentSheet`,`AccountSignUpError`,`AccountSignUpOffline`,`AccountSignUp`],A.parameters={...A.parameters,docs:{...A.parameters?.docs,source:{originalSource:`{
  name: "Check your email, link sent again",
  ...screen(screenAccountCheckEmailResent),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 272
    }
  }
}`,...A.parameters?.docs?.source},description:{story:`Account · check your email, link sent again.`,...A.parameters?.docs?.description}}},j.parameters={...j.parameters,docs:{...j.parameters?.docs,source:{originalSource:`{
  name: "Check your email",
  ...screen(screenAccountCheckEmail),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...j.parameters?.docs?.source},description:{story:`Account · check your email.`,...j.parameters?.docs?.description}}},M.parameters={...M.parameters,docs:{...M.parameters?.docs,source:{originalSource:`{
  name: "Email confirmed, first upload (sheet)",
  ...screen(screenAccountConfirmed),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...M.parameters?.docs?.source},description:{story:`Account · email confirmed, first upload (sheet).`,...M.parameters?.docs?.description}}},N.parameters={...N.parameters,docs:{...N.parameters?.docs,source:{originalSource:`{
  name: "Delete account, wrong password",
  ...screen(screenAccountDeleteError),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...N.parameters?.docs?.source},description:{story:`Account · delete account, wrong password.`,...N.parameters?.docs?.description}}},P.parameters={...P.parameters,docs:{...P.parameters?.docs,source:{originalSource:`{
  name: "Delete account",
  ...screen(screenAccountDelete),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...P.parameters?.docs?.source},description:{story:`Account · delete account.`,...P.parameters?.docs?.description}}},F.parameters={...F.parameters,docs:{...F.parameters?.docs,source:{originalSource:`{
  name: "Account, email not confirmed",
  ...screen(screenAccountDetailsPending),
  parameters: {
    a11y: {
      test: 'todo'
    },
    mockup: {
      issue: 272
    }
  }
}`,...F.parameters?.docs?.source},description:{story:`Account · account, email not confirmed.`,...F.parameters?.docs?.description}}},I.parameters={...I.parameters,docs:{...I.parameters?.docs,source:{originalSource:`{
  name: "Account, signed in",
  ...screen(screenAccountDetails),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...I.parameters?.docs?.source},description:{story:`Account · account, signed in.`,...I.parameters?.docs?.description}}},L.parameters={...L.parameters,docs:{...L.parameters?.docs,source:{originalSource:`{
  name: "Forgot password, link sent",
  ...screen(screenAccountForgotPasswordSent),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...L.parameters?.docs?.source},description:{story:`Account · forgot password, link sent.`,...L.parameters?.docs?.description}}},R.parameters={...R.parameters,docs:{...R.parameters?.docs,source:{originalSource:`{
  name: "Forgot password",
  ...screen(screenAccountForgotPassword),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...R.parameters?.docs?.source},description:{story:`Account · forgot password.`,...R.parameters?.docs?.description}}},z.parameters={...z.parameters,docs:{...z.parameters?.docs,source:{originalSource:`{
  name: "Link expired",
  ...screen(screenAccountLinkExpired),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...z.parameters?.docs?.source},description:{story:`Account · link expired.`,...z.parameters?.docs?.description}}},B.parameters={...B.parameters,docs:{...B.parameters?.docs,source:{originalSource:`{
  name: "Choose a new password",
  ...screen(screenAccountResetPassword),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...B.parameters?.docs?.source},description:{story:`Account · choose a new password.`,...B.parameters?.docs?.description}}},V.parameters={...V.parameters,docs:{...V.parameters?.docs,source:{originalSource:`{
  name: "Settings, guest",
  ...screen(screenAccountSettingsGuest),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...V.parameters?.docs?.source},description:{story:`Account · settings, guest.`,...V.parameters?.docs?.description}}},H.parameters={...H.parameters,docs:{...H.parameters?.docs,source:{originalSource:`{
  name: "Settings, email not confirmed",
  ...screen(screenAccountSettingsPending),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...H.parameters?.docs?.source},description:{story:`Account · settings, email not confirmed.`,...H.parameters?.docs?.description}}},U.parameters={...U.parameters,docs:{...U.parameters?.docs,source:{originalSource:`{
  name: "Settings, signed in",
  ...screen(screenAccountSettingsSynced),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...U.parameters?.docs?.source},description:{story:`Account · settings, signed in.`,...U.parameters?.docs?.description}}},W.parameters={...W.parameters,docs:{...W.parameters?.docs,source:{originalSource:`{
  name: "Sign in, wrong password",
  ...screen(screenAccountSignInError),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...W.parameters?.docs?.source},description:{story:`Account · sign in, wrong password.`,...W.parameters?.docs?.description}}},G.parameters={...G.parameters,docs:{...G.parameters?.docs,source:{originalSource:`{
  name: "Sign in, guest data joins",
  ...screen(screenAccountSignInMerge),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...G.parameters?.docs?.source},description:{story:`Account · sign in, guest data joins.`,...G.parameters?.docs?.description}}},K.parameters={...K.parameters,docs:{...K.parameters?.docs,source:{originalSource:`{
  name: "Sign in, password changed",
  ...screen(screenAccountSignInResetDone),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...K.parameters?.docs?.source},description:{story:`Account · sign in, password changed.`,...K.parameters?.docs?.description}}},q.parameters={...q.parameters,docs:{...q.parameters?.docs,source:{originalSource:`{
  name: "Sign in",
  ...screen(screenAccountSignIn),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...q.parameters?.docs?.source},description:{story:`Account · sign in.`,...q.parameters?.docs?.description}}},J.parameters={...J.parameters,docs:{...J.parameters?.docs,source:{originalSource:`{
  name: "Sign out, changes not sent",
  ...screen(screenAccountSignOutUnsent),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...J.parameters?.docs?.source},description:{story:`Account · sign out, changes not sent.`,...J.parameters?.docs?.description}}},Y.parameters={...Y.parameters,docs:{...Y.parameters?.docs,source:{originalSource:`{
  name: "Sign out",
  ...screen(screenAccountSignOut),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...Y.parameters?.docs?.source},description:{story:`Account · sign out.`,...Y.parameters?.docs?.description}}},X.parameters={...X.parameters,docs:{...X.parameters?.docs,source:{originalSource:`{
  name: "What is stored",
  ...screen(screenAccountSignUpConsentSheet),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...X.parameters?.docs?.source},description:{story:`Account · what is stored.`,...X.parameters?.docs?.description}}},Z.parameters={...Z.parameters,docs:{...Z.parameters?.docs,source:{originalSource:`{
  name: "Sign up, errors",
  ...screen(screenAccountSignUpError),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...Z.parameters?.docs?.source},description:{story:`Account · sign up, errors.`,...Z.parameters?.docs?.description}}},Q.parameters={...Q.parameters,docs:{...Q.parameters?.docs,source:{originalSource:`{
  name: "Sign up, offline",
  ...screen(screenAccountSignUpOffline),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...Q.parameters?.docs?.source},description:{story:`Account · sign up, offline.`,...Q.parameters?.docs?.description}}},$.parameters={...$.parameters,docs:{...$.parameters?.docs,source:{originalSource:`{
  name: "Sign up",
  ...screen(screenAccountSignUp),
  parameters: {
    mockup: {
      issue: 272
    }
  }
}`,...$.parameters?.docs?.source},description:{story:`Account · sign up.`,...$.parameters?.docs?.description}}}})))()}Ce();export{j as AccountCheckEmail,A as AccountCheckEmailResent,M as AccountConfirmed,P as AccountDelete,N as AccountDeleteError,I as AccountDetails,F as AccountDetailsPending,R as AccountForgotPassword,L as AccountForgotPasswordSent,z as AccountLinkExpired,B as AccountResetPassword,V as AccountSettingsGuest,H as AccountSettingsPending,U as AccountSettingsSynced,q as AccountSignIn,W as AccountSignInError,G as AccountSignInMerge,K as AccountSignInResetDone,Y as AccountSignOut,J as AccountSignOutUnsent,$ as AccountSignUp,X as AccountSignUpConsentSheet,Z as AccountSignUpError,Q as AccountSignUpOffline,Se as __namedExportsOrder,xe as default};
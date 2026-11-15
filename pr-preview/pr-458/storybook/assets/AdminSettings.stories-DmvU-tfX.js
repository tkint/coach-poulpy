import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{n as r,t as i}from"./send-D_vxyxCg.js";import{Cn as a,an as o,i as s,ln as c,p as l,s as u,xn as d}from"./iframe-Dw_7nB4c.js";import{n as f,t as p}from"./AdminShell-DWfazfe-.js";var m,h,g,_,v,y,b;function x(){return(x=e((()=>{n(),r(),s(),f(),m={title:`Web/Screens/Admin/Settings`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:336},a11y:{test:`todo`}}},h=(e,n)=>({render:()=>({components:{AdminShell:p,CpButton:a,CpCard:d,CpDivider:c,CpErrorNote:o,CpSuccessNote:l,CpTextField:u,Send:i},setup:()=>({from:t(e),outcome:n}),template:`<AdminShell title="Settings" active="settings">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Settings</h1>
          <p class="text-base-content/70">Changes take effect on the next email. Every change is written to the audit log.</p>
        </div>
        <CpCard title="Email sender">
          <form class="flex flex-col gap-4" @submit.prevent>
            <CpTextField v-model="from" label="From address" type="email" autocomplete="off"
              :hint="outcome === 'invalid' ? undefined : 'The address emails come from, such as Coach Poulpy <hello@example.com>.'"
              :error="outcome === 'invalid' ? 'This is not an email address. Use the form name@example.com.' : undefined" />
            <div class="flex flex-wrap justify-end gap-2"><CpButton type="submit" size="md">Save</CpButton></div>
          </form>
          <CpDivider class="my-0" />
          <div class="flex flex-col gap-3">
            <div class="flex flex-col gap-1">
              <h3 class="cp-type-label">Test email</h3>
              <p class="text-sm text-base-content/70">Sends a message to ana@example.com with the saved settings.</p>
            </div>
            <div><CpButton variant="outline" size="md" class="gap-2"><Send class="size-5 shrink-0" aria-hidden="true" />Send test email</CpButton></div>
            <CpSuccessNote v-if="outcome === 'sent'">Test email sent to ana@example.com.</CpSuccessNote>
            <CpErrorNote v-if="outcome === 'failed'">The test email was not sent. The mail server said: 535 Authentication failed.</CpErrorNote>
          </div>
          <p class="text-sm text-base-content/70">The mail server (host, port, user and password) is set on the server and is not editable here.</p>
        </CpCard>
      </main></AdminShell>`})}),g=h(`hello@coachpoulpy.example`),_=h(`hello@`,`invalid`),v=h(`hello@coachpoulpy.example`,`sent`),y=h(`hello@coachpoulpy.example`,`failed`),b=[`Mail`,`MailInvalid`,`MailTestSent`,`MailTestFailed`],g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`settings('hello@coachpoulpy.example')`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`settings('hello@', 'invalid')`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`settings('hello@coachpoulpy.example', 'sent')`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`settings('hello@coachpoulpy.example', 'failed')`,...y.parameters?.docs?.source}}}})))()}x();export{g as Mail,_ as MailInvalid,y as MailTestFailed,v as MailTestSent,b as __namedExportsOrder,m as default};
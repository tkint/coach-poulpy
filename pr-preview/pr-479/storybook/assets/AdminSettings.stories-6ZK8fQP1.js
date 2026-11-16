import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{n as r,t as i}from"./send-D_vxyxCg.js";import{On as a,Pt as o,Qt as s,Rt as c,Xt as l,i as u,p as d,s as f}from"./iframe-QlTKe7mH.js";import{n as p,t as m}from"./AdminShell-jtmUdiqy.js";var h,g,_,v,y,b,x;function S(){return(S=e((()=>{n(),r(),u(),p(),h={title:`Web/Screens/Admin/Settings`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:336},a11y:{test:`todo`}}},g=(e,n)=>({render:()=>({components:{AdminShell:m,CpButton:s,CpCard:l,CpDivider:c,CpErrorNote:o,CpSuccessNote:d,CpTextField:f,CpIcon:a},setup:()=>({from:t(e),outcome:n,Send:i}),template:`<AdminShell title="Settings" active="settings">
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
            <div><CpButton variant="outline" size="md" class="gap-2"><CpIcon :icon="Send" />Send test email</CpButton></div>
            <CpSuccessNote v-if="outcome === 'sent'">Test email sent to ana@example.com.</CpSuccessNote>
            <CpErrorNote v-if="outcome === 'failed'">The test email was not sent. The mail server said: 535 Authentication failed.</CpErrorNote>
          </div>
          <p class="text-sm text-base-content/70">The mail server (host, port, user and password) is set on the server and is not editable here.</p>
        </CpCard>
      </main></AdminShell>`})}),_=g(`hello@coachpoulpy.example`),v=g(`hello@`,`invalid`),y=g(`hello@coachpoulpy.example`,`sent`),b=g(`hello@coachpoulpy.example`,`failed`),x=[`Mail`,`MailInvalid`,`MailTestSent`,`MailTestFailed`],_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`settings('hello@coachpoulpy.example')`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`settings('hello@', 'invalid')`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`settings('hello@coachpoulpy.example', 'sent')`,...y.parameters?.docs?.source}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`settings('hello@coachpoulpy.example', 'failed')`,...b.parameters?.docs?.source}}}})))()}S();export{_ as Mail,v as MailInvalid,b as MailTestFailed,y as MailTestSent,x as __namedExportsOrder,h as default};
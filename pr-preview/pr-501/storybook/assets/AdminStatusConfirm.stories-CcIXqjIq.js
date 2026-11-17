import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./circle-check-Cgg1bgKk.js";import{On as r,Qt as i,T as a,Xt as o,i as s,l as c,p as l}from"./iframe-DkcAZo2b.js";import{n as u,t as d}from"./AdminShell-5SK3wStM.js";var f,p,m,h,g,_,v;function y(){return(y=e((()=>{t(),s(),u(),f={title:`Web/Screens/Admin/Status, confirm by hand`,tags:[`!autodocs`,`draft`],parameters:{layout:`fullscreen`,mockup:{issue:382},a11y:{test:`todo`}}},p=[{email:`léa@example.com`,signedUp:`9 Nov, 08:41`,expires:`10 Nov, 08:41`},{email:`tom@example.com`,signedUp:`8 Nov, 19:12`,expires:`9 Nov, 19:12`}],m=e=>({render:()=>({components:{AdminShell:d,CpButton:i,CpCard:o,CpDialog:a,CpIcon:r,CpSuccessNote:l,CpTable:c},setup:()=>({state:e,CircleCheck:n,shown:e===`done`?p.slice(1):p}),template:`<AdminShell title="Status" active="status">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Status</h1>
          <p class="text-base-content/70">What the server can do right now. Only admins see this.</p>
        </div>
        <CpCard title="Pending registrations" description="Accounts that have not confirmed their email yet. No email is sent from here. Confirm one only when you trust its address, for example when the email never arrived.">
          <CpSuccessNote v-if="state === 'done'">léa@example.com is confirmed and can sign in.</CpSuccessNote>
          <CpTable>
            <thead><tr><th>Email</th><th>Signed up</th><th>Link expires</th><th><span class="sr-only">Action</span></th></tr></thead>
            <tbody>
              <tr v-for="p in shown" :key="p.email">
                <td class="break-all">{{ p.email }}</td>
                <td class="whitespace-nowrap">{{ p.signedUp }}</td>
                <td class="whitespace-nowrap">{{ p.expires }}</td>
                <td class="text-end"><CpButton variant="outline" size="sm" class="gap-2"><CpIcon :icon="CircleCheck" size="sm" />Confirm<span class="sr-only"> {{ p.email }}</span></CpButton></td>
              </tr>
            </tbody>
          </CpTable>
        </CpCard>
        <CpDialog v-if="state === 'dialog'" :open="true" title="Confirm léa@example.com?" description="You vouch for an address nobody proved. The account can sign in at once, and its confirmation links stop working. This is written to the audit log.">
          <template #actions>
            <CpButton variant="outline">Cancel</CpButton>
            <CpButton class="gap-2"><CpIcon :icon="CircleCheck" size="sm" />Confirm email</CpButton>
          </template>
        </CpDialog>
      </main></AdminShell>`})}),h=m(`list`),g=m(`dialog`),_=m(`done`),v=[`List`,`Dialog`,`Confirmed`],h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`screen('list')`,...h.parameters?.docs?.source},description:{story:`The pending list, each row with a Confirm action.`,...h.parameters?.docs?.description}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`screen('dialog')`,...g.parameters?.docs?.source},description:{story:`The confirmation dialog for one row.`,...g.parameters?.docs?.description}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`screen('done')`,..._.parameters?.docs?.source},description:{story:`After a confirmation: the row is gone and a note says who can sign in now.`,..._.parameters?.docs?.description}}}})))()}y();export{_ as Confirmed,g as Dialog,h as List,v as __namedExportsOrder,f as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-Bgdnb2bH.js";import{H as r,P as i,i as a,ot as o,t as s}from"./src-Cc0VKo53.js";import{n as c,t as l}from"./SitePage-DlP9I2id.js";var u,d,f,p,m;function h(){return(h=e((()=>{n(),s(),c(),u={title:`Web/Screens/App/Sign in`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355}}},d=(e,n)=>({render:()=>({components:{SitePage:l,CpTextField:a,CpPasswordField:i,CpButton:o,CpErrorNote:r},setup:()=>({email:t(`ana@example.com`),password:t(e),failed:n}),template:`<SitePage :drawing="failed ? 'mood_happy' : 'pose_standing_tall'">
      <div class="flex flex-col gap-2">
        <h1 class="cp-type-display">Sign in</h1>
        <p>Your account, on the web.</p>
      </div>
      <form class="flex flex-col gap-6" novalidate @submit.prevent>
        <CpTextField v-model="email" label="Email" type="email" autocomplete="username" :invalid="failed" />
        <CpPasswordField v-model="password" label="Password" show-label="Show password" hide-label="Hide password" :invalid="failed" autocomplete="current-password" />
        <CpErrorNote v-if="failed">Wrong email or password.</CpErrorNote>
        <CpButton type="submit" class="w-full">Sign in</CpButton>
      </form></SitePage>`})}),f=d(`correct horse`,!1),p=d(`wrong horse`,!0),m=[`Default`,`WrongPassword`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`signIn('correct horse', false)`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`signIn('wrong horse', true)`,...p.parameters?.docs?.source}}}})))()}h();export{f as Default,p as WrongPassword,m as __namedExportsOrder,u as default};
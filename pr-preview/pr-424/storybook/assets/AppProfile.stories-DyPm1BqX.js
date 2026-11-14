import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{C as t,J as n,N as r,S as i,W as a,a as o,g as s,h as c,j as l,x as u,z as d}from"./iframe-C08GtdKu.js";import{I as f,J as p,N as m,X as h,bt as g,ft as _,it as v,ot as y,t as b,vt as x,yt as S}from"./src-bcTs06N1.js";import{n as C,t as w}from"./createLucideIcon-CLRs3udq.js";import{a as T,i as E,n as D,o as O,r as k,t as A}from"./shield-check-Dvrdzlty.js";var j,M;function N(){return(N=e((()=>{C(),j={name:`list`,size:24,node:[[`path`,{d:`M3 5h.01`,key:`18ugdj`}],[`path`,{d:`M3 12h.01`,key:`nlz23k`}],[`path`,{d:`M3 19h.01`,key:`noohij`}],[`path`,{d:`M8 5h13`,key:`1pao27`}],[`path`,{d:`M8 12h13`,key:`1za7za`}],[`path`,{d:`M8 19h13`,key:`m83p4d`}]]},M=w(j)})))()}var P,F;function I(){return(I=e((()=>{C(),P={name:`user-round`,size:24,node:[[`circle`,{cx:`12`,cy:`8`,r:`5`,key:`1hypcn`}],[`path`,{d:`M20 21a8 8 0 0 0-16 0`,key:`rfgkzh`}]],aliases:[`user-2`]},F=w(P)})))()}var L,R,z,B,V,H;function U(){return(U=e((()=>{o(),N(),O(),E(),D(),I(),b(),L={class:`flex h-16 w-full items-center gap-2 px-4`},R=[`src`],z={class:`w-full grow px-2`},B={class:`flex w-full flex-col gap-2 p-2`},V={class:`flex items-center gap-3 px-2`},H=t({__name:`AppShell`,props:{title:{},active:{}},setup(e){return(t,o)=>(l(),s(a(h),{"toggle-label":`Toggle the navigation`,"close-label":`Close the navigation`,class:`min-h-screen bg-base-200 text-base-content`},{"toggle-icon":d(()=>[i(a(k),{class:`size-5 shrink-0`,"aria-hidden":`true`})]),title:d(()=>[u(n(e.title),1)]),sidebar:d(()=>[c(`div`,L,[c(`img`,{src:a(m).mood_happy,alt:``,width:`32`,height:`32`,class:`shrink-0`},null,8,R),o[0]||=c(`span`,{class:`cp-type-headline cp-is-drawer-close:hidden`},[u(`Coach `),c(`span`,{class:`text-primary`},`poulpy`)],-1)]),c(`div`,z,[i(a(f),null,{default:d(()=>[i(a(p),{label:`Workouts`,active:e.active===`workouts`},{icon:d(()=>[i(a(M),{class:`size-5 shrink-0`,"aria-hidden":`true`})]),_:1},8,[`active`]),i(a(p),{label:`Profile`,active:e.active===`profile`},{icon:d(()=>[i(a(F),{class:`size-5 shrink-0`,"aria-hidden":`true`})]),_:1},8,[`active`])]),_:1})]),c(`div`,B,[i(a(f),null,{default:d(()=>[i(a(p),{label:`Admin`,href:`#`},{icon:d(()=>[i(a(A),{class:`size-5 shrink-0`,"aria-hidden":`true`})]),_:1})]),_:1}),c(`div`,V,[i(a(g),{initials:`A`,tone:`primary`,size:`md`}),o[1]||=c(`span`,{class:`truncate text-sm font-bold cp-is-drawer-close:hidden`},`ana@example.com`,-1)]),i(a(f),null,{default:d(()=>[i(a(p),{label:`Sign out`},{icon:d(()=>[i(a(T),{class:`size-5 shrink-0`,"aria-hidden":`true`})]),_:1})]),_:1})])]),default:d(()=>[r(t.$slots,`default`)]),_:3}))}})})))()}var W;function G(){return(G=e((()=>{U(),W=H,H.__docgenInfo=Object.assign({displayName:H.name??H.__name},{exportName:`default`,displayName:`AppShell`,description:``,tags:{},props:[{name:`title`,required:!0,type:{name:`string`}},{name:`active`,required:!0,type:{name:`union`,elements:[{name:`"workouts"`},{name:`"profile"`}]}}],slots:[{name:`default`}],sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/web/AppShell.vue`]})})))()}var K,q,J;function Y(){return(Y=e((()=>{S(),b(),G(),K={title:`Web/Screens/App/Profile`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355},a11y:{test:`todo`}}},q={render:()=>({components:{AppShell:W,CpAvatar:g,CpBadge:_,CpButton:y,CpCard:v,Check:x},template:`<AppShell title="Profile" active="profile">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Profile</h1>
          <p class="text-base-content/70">Your account on Coach Poulpy.</p>
        </div>
        <CpCard>
          <div class="flex items-center gap-4">
            <CpAvatar initials="A" tone="primary" size="lg" />
            <div class="flex min-w-0 flex-col gap-1">
              <h2 class="cp-card-title truncate">ana@example.com</h2>
              <CpBadge class="gap-1"><Check class="size-3 shrink-0" aria-hidden="true" />Email confirmed</CpBadge>
            </div>
          </div>
        </CpCard>
        <CpCard title="Password">
          <p class="text-base-content/70">We email you a link to choose a new password. Every device you are signed in on is signed out when you use it.</p>
          <div class="cp-card-actions"><CpButton variant="outline">Email me a reset link</CpButton></div>
        </CpCard>
        <CpCard title="Your data">
          <p class="text-base-content/70">Your workouts, sessions and body log live on your phone and follow your account. To change them, open the app.</p>
        </CpCard>
      </main></AppShell>`})},J=[`Default`],q.parameters={...q.parameters,docs:{...q.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      AppShell,
      CpAvatar,
      CpBadge,
      CpButton,
      CpCard,
      Check
    },
    template: \`<AppShell title="Profile" active="profile">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Profile</h1>
          <p class="text-base-content/70">Your account on Coach Poulpy.</p>
        </div>
        <CpCard>
          <div class="flex items-center gap-4">
            <CpAvatar initials="A" tone="primary" size="lg" />
            <div class="flex min-w-0 flex-col gap-1">
              <h2 class="cp-card-title truncate">ana@example.com</h2>
              <CpBadge class="gap-1"><Check class="size-3 shrink-0" aria-hidden="true" />Email confirmed</CpBadge>
            </div>
          </div>
        </CpCard>
        <CpCard title="Password">
          <p class="text-base-content/70">We email you a link to choose a new password. Every device you are signed in on is signed out when you use it.</p>
          <div class="cp-card-actions"><CpButton variant="outline">Email me a reset link</CpButton></div>
        </CpCard>
        <CpCard title="Your data">
          <p class="text-base-content/70">Your workouts, sessions and body log live on your phone and follow your account. To change them, open the app.</p>
        </CpCard>
      </main></AppShell>\`
  })
}`,...q.parameters?.docs?.source}}}})))()}Y();export{q as Default,J as __namedExportsOrder,K as default};
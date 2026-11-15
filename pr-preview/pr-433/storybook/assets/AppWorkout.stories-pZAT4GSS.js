import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./createLucideIcon-C5FEPL1F.js";import{i as r,n as i,r as a,t as o}from"./AppShell-C5czNUKW.js";import{Cn as s,Tn as c,_n as l,h as u,hn as d,n as f,p,y as m,yn as h}from"./iframe-CDicdodC.js";import{i as g,r as _}from"./bodyMap-BLo918g0.js";var v,y;function b(){return(b=e((()=>{t(),v={name:`moon`,size:24,node:[[`path`,{d:`M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401`,key:`kfwtm`}]]},y=n(v)})))()}var x,S;function C(){return(C=e((()=>{t(),x={name:`sun`,size:24,node:[[`circle`,{cx:`12`,cy:`12`,r:`4`,key:`4exip2`}],[`path`,{d:`M12 2v2`,key:`tus03m`}],[`path`,{d:`M12 20v2`,key:`1lh1kg`}],[`path`,{d:`m4.93 4.93 1.41 1.41`,key:`149t6j`}],[`path`,{d:`m17.66 17.66 1.41 1.41`,key:`ptbguv`}],[`path`,{d:`M2 12h2`,key:`1t8f8n`}],[`path`,{d:`M20 12h2`,key:`1q8mjw`}],[`path`,{d:`m6.34 17.66-1.41 1.41`,key:`1m8zz5`}],[`path`,{d:`m19.07 4.93-1.41 1.41`,key:`1shlcs`}]]},S=n(x)})))()}var w,T;function E(){return(E=e((()=>{t(),w={name:`zap`,size:24,node:[[`path`,{d:`M15.914 4a1.5 1.5 0 00-2.474-1.561l-9 9A1.5 1.5 0 005.5 14h4.002a.5.5 0 01.471.666L8.086 20a1.5 1.5 0 002.475 1.56l9-9A1.5 1.5 0 0018.5 10h-3.997a.5.5 0 01-.472-.667z`,key:`1v7up4`}]]},T=n(w)})))()}var D,O,k,A,j,M,N,P,F;function I(){return(I=e((()=>{r(),b(),C(),E(),f(),g(),i(),D={title:`Web/Screens/App/Workout`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355},a11y:{test:`todo`}}},O=[{icon:S,name:`Warm-up`,summary:`2 steps · about 6 min`,rows:[[`1`,`Walk`,`5 min`],[`2`,`Arm circle`,`30 s`]]},{icon:T,name:`Work`,summary:`4 exercises · about 25 min`,rows:[[`3`,`Bench press`,`3 × 8 · 40 kg · 2 min between sets`],[`4`,`Bent-over row`,`3 × 10 · 30 kg · 1 min 30 s between sets`],[`5`,`Biceps curl`,`12 reps · 10 kg · repeat 3 times`],[`6`,`Triceps dip`,`12 reps · repeat 3 times`]]},{icon:y,name:`Cool down`,summary:`1 step · 1 min`,rows:[[`7`,`Chest stretch`,`30 s hold · each side`]]}],k=_({deltoid:3,triceps:3,pectoral:2,back:2,scapula:2,"lower-back":2,biceps:2,abs:1,obliques:1}),A={front:`Front`,back:`Back`},j=`Muscles worked: shoulders and triceps most, then chest, back and biceps, a little core`,M=e=>({components:{AppShell:o,CpBadge:c,CpBodyMap:h,CpBreadcrumbs:s,CpButton:l,CpCard:d,CpSheet:m,CpStat:u,CpStats:p,List:a},setup:()=>({parts:O,heat:k,captions:A,description:j,sheet:e}),template:`<AppShell title="Workouts" active="workouts">
    <main class="flex flex-col gap-6 p-4 md:p-8">
      <CpBreadcrumbs label="You are here"><li><a href="#">Workouts</a></li><li>Upper body</li></CpBreadcrumbs>
      <div class="flex flex-col gap-2">
        <h1 class="cp-type-display">Upper body</h1>
        <p class="flex flex-wrap items-center gap-2 text-base-content/70">
          <CpBadge tone="primary" class="gap-1"><List class="size-4 shrink-0" aria-hidden="true" />Sets</CpBadge>
          <span>7 exercises · about 32 min · barbell, dumbbells</span>
        </p>
      </div>
      <a href="#" class="cp-btn cp-btn-outline lg:hidden" aria-haspopup="dialog">Stats and muscles worked</a>
      <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <div class="flex flex-col gap-6">
          <CpCard v-for="part in parts" :key="part.name">
            <div class="flex items-center gap-3">
              <component :is="part.icon" class="size-6 shrink-0 text-primary" aria-hidden="true" />
              <div><h2 class="cp-card-title">{{ part.name }}</h2><p class="text-base-content/70">{{ part.summary }}</p></div>
            </div>
            <div class="overflow-x-auto" data-component="table">
              <table class="cp-table"><tbody>
                <tr v-for="[n, name, line] in part.rows" :key="n"><td class="w-10 text-base-content/70">{{ n }}</td><td><strong>{{ name }}</strong></td><td class="text-base-content/70">{{ line }}</td></tr>
              </tbody></table>
            </div>
          </CpCard>
        </div>
        <div class="hidden flex-col gap-6 lg:sticky lg:top-8 lg:flex" role="region" aria-label="Workout stats and muscles worked">
          <CpStats vertical class="w-full">
            <CpStat title="Done" value="3 times" />
            <CpStat title="Last time" value="Wed 21" />
            <CpStat title="Usually" value="31 min" />
          </CpStats>
          <CpCard title="Muscles worked"><CpBodyMap :heat="heat" :captions="captions" legend="top" :description="description" less="Less" more="More" /></CpCard>
        </div>
      </div>
      <CpCard title="Change it on your phone"><p class="text-base-content/70">Workouts are read-only here. Open the app to edit, plan or start this one.</p></CpCard>
      <CpSheet :open="sheet" title="Stats and muscles worked" close-label="Close">
        <div class="flex flex-col gap-4 py-2">
          <CpStats vertical class="w-full"><CpStat title="Done" value="3 times" /><CpStat title="Last time" value="Wed 21" /><CpStat title="Usually" value="31 min" /></CpStats>
          <CpBodyMap :heat="heat" :captions="captions" legend="top" :description="description" less="Less" more="More" />
        </div>
      </CpSheet>
    </main></AppShell>`}),N={render:()=>M(!1)},P={render:()=>M(!0),globals:{viewport:{value:`mobile2`,isRotated:!1}}},F=[`Default`,`OverviewSheet`],N.parameters={...N.parameters,docs:{...N.parameters?.docs,source:{originalSource:`{
  render: () => page(false)
}`,...N.parameters?.docs?.source},description:{story:`The parts of the workout with their steps, read-only, and its stats and muscles in a column on the right.`,...N.parameters?.docs?.description}}},P.parameters={...P.parameters,docs:{...P.parameters?.docs,source:{originalSource:`{
  render: () => page(true),
  globals: {
    viewport: {
      value: 'mobile2',
      isRotated: false
    }
  }
}`,...P.parameters?.docs?.source},description:{story:`On a phone the stats and muscles open in a sheet.`,...P.parameters?.docs?.description}}}})))()}I();export{N as Default,P as OverviewSheet,F as __namedExportsOrder,D as default};
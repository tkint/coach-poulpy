import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{i as t,r as n}from"./user-round-CcOMp868.js";import{a as r,i,n as a,o,r as s,t as c}from"./zap-BaiiS9fC.js";import{En as l,Jt as u,Xt as d,_ as f,en as p,h as m,in as h,n as g,on as _,x as v}from"./iframe-balQP6bY.js";import{n as y,r as b}from"./bodyMap-CucdvdS5.js";import{n as x,t as S}from"./AppShell-CALgi8Zq.js";var C,w,T,E,D,O,k,A;function j(){return(j=e((()=>{t(),o(),i(),a(),g(),b(),x(),C={title:`Web/Screens/App/Workout`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355},a11y:{test:`todo`}}},w=[{icon:s,name:`Warm-up`,summary:`2 steps · about 6 min`,rows:[[`1`,`Walk`,`5 min`],[`2`,`Arm circle`,`30 s`]]},{icon:c,name:`Work`,summary:`4 exercises · about 25 min`,rows:[[`3`,`Bench press`,`3 × 8 · 40 kg · 2 min between sets`],[`4`,`Bent-over row`,`3 × 10 · 30 kg · 1 min 30 s between sets`],[`5`,`Biceps curl`,`12 reps · 10 kg · repeat 3 times`],[`6`,`Triceps dip`,`12 reps · repeat 3 times`]]},{icon:r,name:`Cool down`,summary:`1 step · 1 min`,rows:[[`7`,`Chest stretch`,`30 s hold · each side`]]}],T=y({deltoid:3,triceps:3,pectoral:2,back:2,scapula:2,"lower-back":2,biceps:2,abs:1,obliques:1}),E=`Muscles worked: shoulders and triceps most, then chest, back and biceps, a little core`,D=e=>({components:{AppShell:S,CpBadge:_,CpBodyMap:p,CpBreadcrumbs:h,CpButton:d,CpCard:u,CpSheet:v,CpStat:f,CpStats:m,CpIcon:l},setup:()=>({parts:w,heat:T,description:E,sheet:e,List:n}),template:`<AppShell title="Workouts" active="workouts">
    <main class="flex flex-col gap-6 p-4 md:p-8">
      <CpBreadcrumbs label="You are here"><li><a href="#">Workouts</a></li><li>Upper body</li></CpBreadcrumbs>
      <div class="flex flex-col gap-2">
        <h1 class="cp-type-display">Upper body</h1>
        <p class="flex flex-wrap items-center gap-2 text-base-content/70">
          <CpBadge tone="primary" class="gap-1"><CpIcon :icon="List" size="sm" />Sets</CpBadge>
          <span>7 exercises · about 32 min · barbell, dumbbells</span>
        </p>
      </div>
      <a href="#" class="cp-btn cp-btn-outline lg:hidden" aria-haspopup="dialog">Stats and muscles worked</a>
      <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <div class="flex flex-col gap-6">
          <CpCard v-for="part in parts" :key="part.name">
            <div class="flex items-center gap-3">
              <CpIcon :icon="part.icon" size="lg" class="text-primary" />
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
          <CpCard title="Muscles worked"><CpBodyMap :heat="heat" legend="top" :description="description" /></CpCard>
        </div>
      </div>
      <CpCard title="Change it on your phone"><p class="text-base-content/70">Workouts are read-only here. Open the app to edit, plan or start this one.</p></CpCard>
      <CpSheet :open="sheet" title="Stats and muscles worked">
        <div class="flex flex-col gap-4 py-2">
          <CpStats vertical class="w-full"><CpStat title="Done" value="3 times" /><CpStat title="Last time" value="Wed 21" /><CpStat title="Usually" value="31 min" /></CpStats>
          <CpBodyMap :heat="heat" legend="top" :description="description" />
        </div>
      </CpSheet>
    </main></AppShell>`}),O={render:()=>D(!1)},k={render:()=>D(!0),globals:{viewport:{value:`mobile2`,isRotated:!1}}},A=[`Default`,`OverviewSheet`],O.parameters={...O.parameters,docs:{...O.parameters?.docs,source:{originalSource:`{
  render: () => page(false)
}`,...O.parameters?.docs?.source},description:{story:`The parts of the workout with their steps, read-only, and its stats and muscles in a column on the right.`,...O.parameters?.docs?.description}}},k.parameters={...k.parameters,docs:{...k.parameters?.docs,source:{originalSource:`{
  render: () => page(true),
  globals: {
    viewport: {
      value: 'mobile2',
      isRotated: false
    }
  }
}`,...k.parameters?.docs?.source},description:{story:`On a phone the stats and muscles open in a sheet.`,...k.parameters?.docs?.description}}}})))()}j();export{O as Default,k as OverviewSheet,A as __namedExportsOrder,C as default};
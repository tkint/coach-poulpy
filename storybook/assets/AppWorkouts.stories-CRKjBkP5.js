import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{i as r,r as i}from"./user-round-D7n6NY4b.js";import{n as a,t as o}from"./repeat-Dxk85M_7.js";import{N as s,On as c,Wt as l,Xt as u,cn as d,i as f,l as p}from"./iframe-CMuzri_h.js";import{n as m,t as h}from"./AppShell-DarazgSR.js";var g,_,v,y;function b(){return(b=e((()=>{n(),r(),a(),f(),m(),g={title:`Web/Screens/App/Workouts`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355},a11y:{test:`todo`}}},_=[[`Lower body strength`,`Sets`,`5 exercises`,`40 min`,`Today`],[`Demo intervals`,`Circuit`,`3 exercises`,`24 min`,`Fri 23 Oct`],[`Upper body`,`Sets`,`7 exercises`,`32 min`,`Wed 21 Oct`],[`Full body`,`Sets`,`6 exercises`,`42 min`,`Sat 17 Oct`],[`Core and mobility`,`Circuit`,`5 exercises`,`18 min`,`Mon 5 Oct`],[`Easy mobility`,`Circuit`,`6 exercises`,`15 min`,`Not done yet`],[`Quick sweat`,`Circuit`,`4 exercises`,`16 min`,`Not done yet`]],v={render:()=>({components:{AppShell:h,CpBadge:d,CpCard:u,CpChip:l,CpSearchField:s,CpTable:p,CpIcon:c},setup:()=>({rows:_,search:t(``),format:t(``),List:i,Repeat:o}),template:`<AppShell title="Workouts" active="workouts">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Workouts</h1>
          <p class="text-base-content/70">The workouts on your phone, as they were last synced.</p>
        </div>
        <form class="flex flex-col gap-3 md:flex-row md:items-center" role="search" @submit.prevent>
          <CpSearchField size="lg" v-model="search" class="w-full md:max-w-sm" label="Search your workouts" placeholder="Name or exercise" />
          <div class="flex gap-2" role="group" aria-label="Format">
            <CpChip v-for="one in ['All', 'Circuit', 'Sets']" :key="one" :model-value="(format || 'All') === one" @update:model-value="format = one === 'All' ? '' : one">{{ one }}</CpChip>
          </div>
        </form>
        <CpCard>
          <CpTable>
            <thead><tr><th>Name</th><th>Format</th><th>Content</th><th>Length</th><th>Last done</th></tr></thead>
            <tbody>
              <tr v-for="[name, kind, content, length, last] in rows" :key="name" class="cp-hover">
                <td><a href="#" class="cp-link cp-link-hover font-bold">{{ name }}</a></td>
                <td><CpBadge tone="primary" class="gap-1 whitespace-nowrap"><CpIcon :icon="kind === 'Circuit' ? Repeat : List" size="sm" />{{ kind }}</CpBadge></td>
                <td class="whitespace-nowrap">{{ content }}</td>
                <td class="whitespace-nowrap">{{ length }}</td>
                <td class="whitespace-nowrap text-base-content/70">{{ last }}</td>
              </tr>
            </tbody>
          </CpTable>
          <p class="text-base-content/70" aria-live="polite">7 workouts</p>
        </CpCard>
      </main></AppShell>`})},y=[`Default`],v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      AppShell,
      CpBadge,
      CpCard,
      CpChip,
      CpSearchField,
      CpTable,
      CpIcon
    },
    setup: () => ({
      rows,
      search: ref(''),
      format: ref(''),
      List,
      Repeat
    }),
    template: \`<AppShell title="Workouts" active="workouts">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Workouts</h1>
          <p class="text-base-content/70">The workouts on your phone, as they were last synced.</p>
        </div>
        <form class="flex flex-col gap-3 md:flex-row md:items-center" role="search" @submit.prevent>
          <CpSearchField size="lg" v-model="search" class="w-full md:max-w-sm" label="Search your workouts" placeholder="Name or exercise" />
          <div class="flex gap-2" role="group" aria-label="Format">
            <CpChip v-for="one in ['All', 'Circuit', 'Sets']" :key="one" :model-value="(format || 'All') === one" @update:model-value="format = one === 'All' ? '' : one">{{ one }}</CpChip>
          </div>
        </form>
        <CpCard>
          <CpTable>
            <thead><tr><th>Name</th><th>Format</th><th>Content</th><th>Length</th><th>Last done</th></tr></thead>
            <tbody>
              <tr v-for="[name, kind, content, length, last] in rows" :key="name" class="cp-hover">
                <td><a href="#" class="cp-link cp-link-hover font-bold">{{ name }}</a></td>
                <td><CpBadge tone="primary" class="gap-1 whitespace-nowrap"><CpIcon :icon="kind === 'Circuit' ? Repeat : List" size="sm" />{{ kind }}</CpBadge></td>
                <td class="whitespace-nowrap">{{ content }}</td>
                <td class="whitespace-nowrap">{{ length }}</td>
                <td class="whitespace-nowrap text-base-content/70">{{ last }}</td>
              </tr>
            </tbody>
          </CpTable>
          <p class="text-base-content/70" aria-live="polite">7 workouts</p>
        </CpCard>
      </main></AppShell>\`
  })
}`,...v.parameters?.docs?.source},description:{story:`A read-only, searchable table of the workouts the phone synced.`,...v.parameters?.docs?.description}}}})))()}b();export{v as Default,y as __namedExportsOrder,g as default};
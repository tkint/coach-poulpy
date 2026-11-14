import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-CaUtbVDg.js";import{E as r,Q as i,ft as a,it as o,o as s,t as c}from"./src-BHi6WSw8.js";import{n as l,t as u}from"./createLucideIcon-B1Tx585i.js";import{i as d,n as f,r as p,t as m}from"./AppShell-DMuvnREM.js";var h,g;function _(){return(_=e((()=>{l(),h={name:`repeat`,size:24,node:[[`path`,{d:`m17 2 4 4-4 4`,key:`nntrym`}],[`path`,{d:`M3 11v-1a4 4 0 0 1 4-4h14`,key:`84bu3i`}],[`path`,{d:`m7 22-4-4 4-4`,key:`1wqhfi`}],[`path`,{d:`M21 13v1a4 4 0 0 1-4 4H3`,key:`1rx37r`}]]},g=u(h)})))()}var v,y,b,x;function S(){return(S=e((()=>{n(),d(),_(),c(),f(),v={title:`Web/Screens/App/Workouts`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355},a11y:{test:`todo`}}},y=[[`Lower body strength`,`Sets`,`5 exercises`,`40 min`,`Today`],[`Demo intervals`,`Circuit`,`3 exercises`,`24 min`,`Fri 23 Oct`],[`Upper body`,`Sets`,`7 exercises`,`32 min`,`Wed 21 Oct`],[`Full body`,`Sets`,`6 exercises`,`42 min`,`Sat 17 Oct`],[`Core and mobility`,`Circuit`,`5 exercises`,`18 min`,`Mon 5 Oct`],[`Easy mobility`,`Circuit`,`6 exercises`,`15 min`,`Not done yet`],[`Quick sweat`,`Circuit`,`4 exercises`,`16 min`,`Not done yet`]],b={render:()=>({components:{AppShell:m,CpBadge:a,CpCard:o,CpChip:i,CpSearchField:r,CpTable:s,List:p,Repeat:g},setup:()=>({rows:y,search:t(``),format:t(``)}),template:`<AppShell title="Workouts" active="workouts">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Workouts</h1>
          <p class="text-base-content/70">The workouts on your phone, as they were last synced.</p>
        </div>
        <form class="flex flex-col gap-3 md:flex-row md:items-center" role="search" @submit.prevent>
          <CpSearchField v-model="search" class="w-full md:max-w-sm" label="Search your workouts" clear-label="Clear the search" placeholder="Name or exercise" />
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
                <td><CpBadge tone="primary" class="gap-1 whitespace-nowrap"><component :is="kind === 'Circuit' ? Repeat : List" class="size-4 shrink-0" aria-hidden="true" />{{ kind }}</CpBadge></td>
                <td class="whitespace-nowrap">{{ content }}</td>
                <td class="whitespace-nowrap">{{ length }}</td>
                <td class="whitespace-nowrap text-base-content/70">{{ last }}</td>
              </tr>
            </tbody>
          </CpTable>
          <p class="text-base-content/70" aria-live="polite">7 workouts</p>
        </CpCard>
      </main></AppShell>`})},x=[`Default`],b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      AppShell,
      CpBadge,
      CpCard,
      CpChip,
      CpSearchField,
      CpTable,
      List,
      Repeat
    },
    setup: () => ({
      rows,
      search: ref(''),
      format: ref('')
    }),
    template: \`<AppShell title="Workouts" active="workouts">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Workouts</h1>
          <p class="text-base-content/70">The workouts on your phone, as they were last synced.</p>
        </div>
        <form class="flex flex-col gap-3 md:flex-row md:items-center" role="search" @submit.prevent>
          <CpSearchField v-model="search" class="w-full md:max-w-sm" label="Search your workouts" clear-label="Clear the search" placeholder="Name or exercise" />
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
                <td><CpBadge tone="primary" class="gap-1 whitespace-nowrap"><component :is="kind === 'Circuit' ? Repeat : List" class="size-4 shrink-0" aria-hidden="true" />{{ kind }}</CpBadge></td>
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
}`,...b.parameters?.docs?.source},description:{story:`A read-only, searchable table of the workouts the phone synced.`,...b.parameters?.docs?.description}}}})))()}S();export{b as Default,x as __namedExportsOrder,v as default};
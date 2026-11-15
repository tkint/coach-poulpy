import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{i as r,r as i}from"./user-round-D7n6NY4b.js";import{n as a,t as o}from"./repeat-Dxk85M_7.js";import{D as s,Tn as c,hn as l,ln as u,n as d,s as f}from"./iframe-DCS6dTw1.js";import{n as p,t as m}from"./AppShell-DneOxgQV.js";var h,g,_,v;function y(){return(y=e((()=>{n(),r(),a(),d(),p(),h={title:`Web/Screens/App/Workouts`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:355},a11y:{test:`todo`}}},g=[[`Lower body strength`,`Sets`,`5 exercises`,`40 min`,`Today`],[`Demo intervals`,`Circuit`,`3 exercises`,`24 min`,`Fri 23 Oct`],[`Upper body`,`Sets`,`7 exercises`,`32 min`,`Wed 21 Oct`],[`Full body`,`Sets`,`6 exercises`,`42 min`,`Sat 17 Oct`],[`Core and mobility`,`Circuit`,`5 exercises`,`18 min`,`Mon 5 Oct`],[`Easy mobility`,`Circuit`,`6 exercises`,`15 min`,`Not done yet`],[`Quick sweat`,`Circuit`,`4 exercises`,`16 min`,`Not done yet`]],_={render:()=>({components:{AppShell:m,CpBadge:c,CpCard:l,CpChip:u,CpSearchField:s,CpTable:f,List:i,Repeat:o},setup:()=>({rows:g,search:t(``),format:t(``)}),template:`<AppShell title="Workouts" active="workouts">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Workouts</h1>
          <p class="text-base-content/70">The workouts on your phone, as they were last synced.</p>
        </div>
        <form class="flex flex-col gap-3 md:flex-row md:items-center" role="search" @submit.prevent>
          <CpSearchField v-model="search" class="w-full md:max-w-sm" label="Search your workouts" placeholder="Name or exercise" />
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
      </main></AppShell>`})},v=[`Default`],_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
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
          <CpSearchField v-model="search" class="w-full md:max-w-sm" label="Search your workouts" placeholder="Name or exercise" />
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
}`,..._.parameters?.docs?.source},description:{story:`A read-only, searchable table of the workouts the phone synced.`,..._.parameters?.docs?.description}}}})))()}y();export{_ as Default,v as __namedExportsOrder,h as default};
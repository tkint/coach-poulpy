import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{Jt as t,On as n,_ as r,h as i,n as a,on as o,s}from"./iframe-CFDVK3oo.js";import{n as c,t as l}from"./AdminShell-VRdt-0ae.js";var u,d,f,p;function m(){return(m=e((()=>{a(),c(),u={title:`Web/Screens/Admin/Audit log`,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:336},a11y:{test:`todo`}}},d=[{when:`8 Nov, 14:20`,who:`Ana`,what:`Published catalog`,tone:`primary`,target:`version 12`},{when:`8 Nov, 14:02`,who:`Ana`,what:`Disabled account`,tone:`outline`,target:`0190d4d2-…-0042`},{when:`8 Nov, 13:40`,who:`The server`,initial:`S`,what:`Made admin`,tone:`neutral`,target:`0190d4d2-…-0001`}],f={render:()=>({components:{AdminShell:l,CpAvatar:n,CpBadge:o,CpCard:t,CpStat:r,CpStats:i,CpTable:s},setup:()=>({entries:d}),template:`<AdminShell title="Audit log" active="audit">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Audit log</h1>
          <p class="text-base-content/70">Everything an admin changed, newest first. Only admins see this.</p>
        </div>
        <CpStats>
          <CpStat title="Changes this week" value="12" description="Since Monday" accent />
          <CpStat title="Admins" value="1" description="Ana" />
          <CpStat title="Last change" value="14:20" description="Today, by Ana" />
        </CpStats>
        <CpCard title="Latest changes">
          <CpTable>
            <thead><tr><th>When</th><th>Who</th><th>What</th><th>Target</th></tr></thead>
            <tbody>
              <tr v-for="e in entries" :key="e.when">
                <td class="whitespace-nowrap">{{ e.when }}</td>
                <td><div class="flex items-center gap-3"><CpAvatar :initials="e.initial ?? e.who.charAt(0)" /><span class="whitespace-nowrap">{{ e.who }}</span></div></td>
                <td><CpBadge :tone="e.tone">{{ e.what }}</CpBadge></td>
                <td class="font-mono text-sm">{{ e.target }}</td>
              </tr>
            </tbody>
          </CpTable>
        </CpCard>
      </main></AdminShell>`})},p=[`Default`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      AdminShell,
      CpAvatar,
      CpBadge,
      CpCard,
      CpStat,
      CpStats,
      CpTable
    },
    setup: () => ({
      entries
    }),
    template: \`<AdminShell title="Audit log" active="audit">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Audit log</h1>
          <p class="text-base-content/70">Everything an admin changed, newest first. Only admins see this.</p>
        </div>
        <CpStats>
          <CpStat title="Changes this week" value="12" description="Since Monday" accent />
          <CpStat title="Admins" value="1" description="Ana" />
          <CpStat title="Last change" value="14:20" description="Today, by Ana" />
        </CpStats>
        <CpCard title="Latest changes">
          <CpTable>
            <thead><tr><th>When</th><th>Who</th><th>What</th><th>Target</th></tr></thead>
            <tbody>
              <tr v-for="e in entries" :key="e.when">
                <td class="whitespace-nowrap">{{ e.when }}</td>
                <td><div class="flex items-center gap-3"><CpAvatar :initials="e.initial ?? e.who.charAt(0)" /><span class="whitespace-nowrap">{{ e.who }}</span></div></td>
                <td><CpBadge :tone="e.tone">{{ e.what }}</CpBadge></td>
                <td class="font-mono text-sm">{{ e.target }}</td>
              </tr>
            </tbody>
          </CpTable>
        </CpCard>
      </main></AdminShell>\`
  })
}`,...f.parameters?.docs?.source}}}})))()}m();export{f as Default,p as __namedExportsOrder,u as default};
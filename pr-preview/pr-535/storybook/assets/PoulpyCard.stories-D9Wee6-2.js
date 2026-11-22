import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{t,x as n}from"./vue.esm-bundler-CexDUOsq.js";import{n as r,t as i}from"./screen-B5J6rFvO.js";var a,o,s,c,l,u;function d(){return(d=e((()=>{t(),r(),a=e=>n(i,{markup:`<div style="width: 390px; padding: 16px;">${e}</div>`}),o={title:`Mobile/Patterns/Poulpy card`,parameters:{docs:{canvas:{sourceState:`none`},description:{component:"`MobilePoulpyCard`: Poulpy speaking from a primary container card. His art goes in the `art` slot (a `MobileTentacleRing` or a drawing), then a `title`, a line (the content) and an optional `meta` line. `compact` is the smaller card that closes a session."}}}},s={render:()=>a(`<MobilePoulpyCard title="Ready when you are" meta="6 of 8 lit · 1 full octopus">
  <template #art><MobileTentacleRing :lit="6" label="6 of 8 lit · 1 full octopus" picture="../../../poulpy/mood_happy.svg" picture-alt="Poulpy, happy" /></template>
  Today’s session lights tentacle 7.
</MobilePoulpyCard>`)},c={render:()=>a(`<MobilePoulpyCard title="Tentacle 7 lit!" compact>
  <template #art><MobileTentacleRing :lit="7" fresh :size="64" label="7 of 8 tentacles lit" /></template>
  One more and Poulpy is a champion.
</MobilePoulpyCard>`)},l={render:()=>a(`<MobilePoulpyCard title="Taking it easy">
  <template #art><img src="../../../poulpy/pose_patched_up.svg" alt="" width="71" height="64"></template>
  Poulpy is patched up with you until 4 Nov. No catching up, just rest.
</MobilePoulpyCard>`)},u=[`Home`,`SessionDone`,`WithDrawing`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  render: () => frame(\`<MobilePoulpyCard title="Ready when you are" meta="6 of 8 lit · 1 full octopus">
  <template #art><MobileTentacleRing :lit="6" label="6 of 8 lit · 1 full octopus" picture="../../../poulpy/mood_happy.svg" picture-alt="Poulpy, happy" /></template>
  Today’s session lights tentacle 7.
</MobilePoulpyCard>\`)
}`,...s.parameters?.docs?.source}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  render: () => frame(\`<MobilePoulpyCard title="Tentacle 7 lit!" compact>
  <template #art><MobileTentacleRing :lit="7" fresh :size="64" label="7 of 8 tentacles lit" /></template>
  One more and Poulpy is a champion.
</MobilePoulpyCard>\`)
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  render: () => frame(\`<MobilePoulpyCard title="Taking it easy">
  <template #art><img src="../../../poulpy/pose_patched_up.svg" alt="" width="71" height="64"></template>
  Poulpy is patched up with you until 4 Nov. No catching up, just rest.
</MobilePoulpyCard>\`)
}`,...l.parameters?.docs?.source}}}})))()}d();export{s as Home,c as SessionDone,l as WithDrawing,u as __namedExportsOrder,o as default};
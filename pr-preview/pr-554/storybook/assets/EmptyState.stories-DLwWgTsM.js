import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{t,x as n}from"./vue.esm-bundler-CexDUOsq.js";import{n as r,t as i}from"./screen-DP-o4lbY.js";var a,o,s,c,l,u;function d(){return(d=e((()=>{t(),r(),a=e=>n(i,{markup:`<div style="width: 390px; padding: 16px;">${e}</div>`}),o={title:`Mobile/Patterns/Empty state`,tags:[`draft`],parameters:{docs:{canvas:{sourceState:`none`},description:{component:"`MobileEmptyState`: what a list or screen shows when nothing is there yet or nothing matches. It is deliberately quiet: a dashed outline and no fill, a small Poulpy thinking beside a `title` and a line, then at most one small secondary button (the `action` slot). Poulpy is never guilt, so the line says what to do next, not what is missing."}}}},s={render:()=>a(`<MobileEmptyState title="No workouts yet">
  Build one once, then plan it for any day or keep it here for later.
  <template #action><MobileButton kind="secondary" size="sm" icon="plus">Build your first workout</MobileButton></template>
</MobileEmptyState>`)},c={render:()=>a(`<MobileEmptyState title="Nothing for “zumba”">
  Try another word, or look in the exercises to build one.
  <template #action><MobileButton kind="secondary" size="sm">Clear search and filters</MobileButton></template>
</MobileEmptyState>`)},l={render:()=>a(`<MobileEmptyState title="No sessions yet">
  Finish a workout or a treadmill session and it will show up here.
</MobileEmptyState>`)},u=[`WithAction`,`NoMatch`,`TextOnly`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  render: () => frame(\`<MobileEmptyState title="No workouts yet">
  Build one once, then plan it for any day or keep it here for later.
  <template #action><MobileButton kind="secondary" size="sm" icon="plus">Build your first workout</MobileButton></template>
</MobileEmptyState>\`)
}`,...s.parameters?.docs?.source}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  render: () => frame(\`<MobileEmptyState title="Nothing for “zumba”">
  Try another word, or look in the exercises to build one.
  <template #action><MobileButton kind="secondary" size="sm">Clear search and filters</MobileButton></template>
</MobileEmptyState>\`)
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  render: () => frame(\`<MobileEmptyState title="No sessions yet">
  Finish a workout or a treadmill session and it will show up here.
</MobileEmptyState>\`)
}`,...l.parameters?.docs?.source}}}})))()}d();export{c as NoMatch,l as TextOnly,s as WithAction,u as __namedExportsOrder,o as default};
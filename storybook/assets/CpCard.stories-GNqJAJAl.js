import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{Xt as t,cn as n,i as r}from"./iframe-_rvzeZSp.js";var i;function a(){return(a=e((()=>{i="`CpCard`: a raised panel, with a title when it has one. A `badge` slot puts the state of what the card is about beside the title (mail: configured), and a `description` says under it what the card holds. `CpPoulpyCard` is for empty states.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpCard>` | `section.cp-card.bg-base-100.shadow-sm` around `div.cp-card-body.gap-4.p-4.md:p-6` and, with a `title`, `h2.cp-card-title` |\n| `<CpCard title description>` with `#badge` | the title in `div.flex.flex-col.gap-1`: `div.flex.flex-wrap.items-center.gap-3` holding the `h2.cp-card-title` and the badge, then `p.text-sm.text-base-content/70` |\n"})))()}var o,s,c,l,u;function d(){return(d=e((()=>{r(),a(),o={title:`Web/Components/Display/Card`,parameters:{docs:{description:{component:i}}},component:t,render:e=>({components:{CpCard:t,CpBadge:n},setup:()=>({args:e}),template:`<CpCard v-bind="args"><template v-if="args.description" #badge><CpBadge tone="success">Configured</CpBadge></template><p>What the card holds.</p></CpCard>`})},s={},c={args:{title:`Mail`}},l={parameters:{a11y:{test:`todo`}},args:{title:`Mail`,description:`Where sign-in emails come from.`}},u=[`Untitled`,`Titled`,`WithDescriptionAndBadge`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{}`,...s.parameters?.docs?.source}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    title: 'Mail'
  }
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    title: 'Mail',
    description: 'Where sign-in emails come from.'
  }
}`,...l.parameters?.docs?.source}}}})))()}d();export{c as Titled,s as Untitled,l as WithDescriptionAndBadge,u as __namedExportsOrder,o as default};
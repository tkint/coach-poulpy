import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{cn as t,i as n}from"./iframe-BYe5Z0Tl.js";var r;function i(){return(i=e((()=>{r='`CpBadge`: what kind of change, what state. Primary, neutral, outline, success (a state that is fine), or error (crimson, with its alert icon drawn in, so it never relies on colour alone); never ember or `warning`.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpBadge>` | `cp-badge cp-badge-primary cp-badge-soft` |\n| `<CpBadge tone="neutral">` | `cp-badge cp-badge-neutral cp-badge-soft` |\n| `<CpBadge tone="outline">` | `cp-badge cp-badge-outline` |\n| `<CpBadge tone="success">` | `cp-badge cp-badge-success cp-badge-soft` |\n| `<CpBadge tone="error">` | `cp-badge cp-badge-error cp-badge-soft gap-1` with the alert icon (`svg.size-3`) before the words |\n| `<CpBadge small>` | adds `cp-badge-sm`, for the badge under a field that says where its translation stands; an error draws no icon |\n'})))()}var a,o,s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Web/Components/Display/Badge`,parameters:{docs:{description:{component:r}}},component:t,render:e=>({components:{CpBadge:t},setup:()=>({args:e}),template:`<CpBadge v-bind="args">Configured</CpBadge>`})},l={args:{tone:`primary`}},u={args:{tone:`neutral`}},d={args:{tone:`outline`}},f={parameters:{a11y:{test:`todo`}},args:{tone:`success`}},p={args:{tone:`error`}},m={args:{tone:`neutral`,small:!0}},h=[`Primary`,`Neutral`,`Outline`,`Success`,`Error`,`Small`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    tone: 'primary'
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    tone: 'neutral'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    tone: 'outline'
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    tone: 'success'
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    tone: 'error'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  args: {
    tone: 'neutral',
    small: true
  }
}`,...m.parameters?.docs?.source},description:{story:`Under a field, saying where its translation stands.`,...m.parameters?.docs?.description}}}})))()}g();export{p as Error,u as Neutral,d as Outline,l as Primary,m as Small,f as Success,h as __namedExportsOrder,c as default};
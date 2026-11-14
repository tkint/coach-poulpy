import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-BE2rJGN_.js";import{n as r,t as i}from"./src-CpRmoIye.js";var a;function o(){return(o=e((()=>{a="`CpTextarea`: a multiline field that grows to fit its content. It resizes after typing, when its model changes, and when its width changes and text wraps onto different lines. Its appearance comes from the shared DaisyUI theme and is included in both generated UI stylesheets.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpTextarea v-model label rows hint>` | `div.cp-fieldset` around `label.cp-type-label`, `textarea.cp-textarea.w-full`, and an optional `p.cp-label.whitespace-normal` helper |\n"})))()}var s,c,l,u,d,f;function p(){return(p=e((()=>{n(),i(),o(),s={title:`Fields/Textarea`,parameters:{docs:{description:{component:a}}},component:r,args:{label:`Notes`},render:e=>({components:{CpTextarea:r},setup:()=>({args:e,value:t(``)}),template:`<CpTextarea v-bind="args" v-model="value" />`})},c={args:{placeholder:`How did it feel?`}},l={parameters:{a11y:{test:`todo`}},args:{hint:`Only you see this.`}},u={parameters:{a11y:{test:`todo`}},args:{invalid:!0,hint:`Write at least a few words.`}},d={args:{disabled:!0}},f=[`Empty`,`WithAHint`,`Invalid`,`Disabled`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    placeholder: 'How did it feel?'
  }
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'Only you see this.'
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    invalid: true,
    hint: 'Write at least a few words.'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    disabled: true
  }
}`,...d.parameters?.docs?.source}}}})))()}p();export{d as Disabled,c as Empty,u as Invalid,l as WithAHint,f as __namedExportsOrder,s as default};
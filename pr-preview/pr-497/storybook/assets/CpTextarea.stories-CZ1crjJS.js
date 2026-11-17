import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{a as r,cn as i,i as a}from"./iframe-DGxr61L9.js";var o;function s(){return(s=e((()=>{o="`CpTextarea`: a multiline field that grows to fit its content. It resizes after typing, when its model changes, and when its width changes and text wraps onto different lines. Its appearance comes from the shared DaisyUI theme and is included in both generated UI stylesheets.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpTextarea v-model label rows hint>` | `div.cp-fieldset` around `label.cp-type-label`, `textarea.cp-textarea.w-full`, and an optional `p.cp-label.whitespace-normal` helper; a `#status` slot under the helper takes a badge |\n"})))()}var c,l,u,d,f,p,m;function h(){return(h=e((()=>{n(),a(),s(),c={title:`Web/Components/Fields/Textarea`,parameters:{docs:{description:{component:o}}},component:r,args:{label:`Notes`},render:e=>({components:{CpTextarea:r},setup:()=>({args:e,value:t(``)}),template:`<CpTextarea v-bind="args" v-model="value" />`})},l={args:{placeholder:`How did it feel?`}},u={parameters:{a11y:{test:`todo`}},args:{hint:`Only you see this.`}},d={parameters:{a11y:{test:`todo`}},args:{invalid:!0,hint:`Write at least a few words.`}},f={args:{disabled:!0}},p={render:e=>({components:{CpTextarea:r,CpBadge:i},setup:()=>({args:e,value:t(``)}),template:`<CpTextarea v-bind="args" v-model="value"><template #status><CpBadge tone="neutral" small>To translate</CpBadge></template></CpTextarea>`})},m=[`Empty`,`WithAHint`,`Invalid`,`Disabled`,`WithAStatus`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    placeholder: 'How did it feel?'
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'Only you see this.'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    invalid: true,
    hint: 'Write at least a few words.'
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    disabled: true
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpTextarea,
      CpBadge
    },
    setup: () => ({
      args,
      value: ref('')
    }),
    template: '<CpTextarea v-bind="args" v-model="value"><template #status><CpBadge tone="neutral" small>To translate</CpBadge></template></CpTextarea>'
  })
}`,...p.parameters?.docs?.source},description:{story:`A badge under the helper, saying where a translated text stands.`,...p.parameters?.docs?.description}}}})))()}h();export{f as Disabled,l as Empty,d as Invalid,u as WithAHint,p as WithAStatus,m as __namedExportsOrder,c as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-BE2rJGN_.js";import{i as r,t as i}from"./src-CpRmoIye.js";var a;function o(){return(o=e((()=>{a='`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn\'t one, passes `error` instead, which takes the hint\'s place in crimson.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |\n| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input\'s `aria-describedby` |\n| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.whitespace-normal.text-error` with `role="alert"` in the hint\'s place |\n'})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Fields/TextField`,parameters:{docs:{description:{component:a}}},component:r,args:{label:`Email`,type:`email`},render:e=>({components:{CpTextField:r},setup:()=>({args:e,value:t(``)}),template:`<CpTextField v-bind="args" v-model="value" />`})},d={},f={parameters:{a11y:{test:`todo`}},args:{hint:`We only use it to sign you in.`}},p={args:{invalid:!0,error:`This email address is not valid.`}},m={play:async({canvasElement:e})=>{let t=l(e).getByLabelText(`Email`);await c.type(t,`alex@example.com`),s(t).toHaveValue(`alex@example.com`)}},h=[`Empty`,`WithAHint`,`WithAnError`,`Typing`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'We only use it to sign you in.'
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    invalid: true,
    error: 'This email address is not valid.'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const input = within(canvasElement).getByLabelText('Email');
    await userEvent.type(input, 'alex@example.com');
    expect(input).toHaveValue('alex@example.com');
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{d as Empty,m as Typing,f as WithAHint,p as WithAnError,h as __namedExportsOrder,u as default};
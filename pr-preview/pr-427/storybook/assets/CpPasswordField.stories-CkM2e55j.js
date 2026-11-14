import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-DmC-1T4B.js";import{P as r,t as i}from"./src-B7lwVhBH.js";var a;function o(){return(o=e((()=>{a="`CpPasswordField`, with a button that shows the password. In error, it turns crimson and a `CpErrorNote` under the form says why.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPasswordField>` | `div.cp-fieldset` around a `label.cp-type-label`, `label.cp-input` (`cp-input-error` in error) holding the `input` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm`, and `p.cp-label.whitespace-normal` for the hint |\n"})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/PasswordField`,parameters:{docs:{description:{component:a}}},component:r,args:{label:`Password`,showLabel:`Show the password`,hideLabel:`Hide the password`,autocomplete:`new-password`},render:e=>({components:{CpPasswordField:r},setup:()=>({args:e,value:t(``)}),template:`<CpPasswordField v-bind="args" v-model="value" />`})},d={play:async({canvasElement:e})=>{s(l(e).getByLabelText(`Password`,{selector:`input`})).toHaveAttribute(`type`,`password`)}},f={play:async({canvasElement:e})=>{await c.click(l(e).getByRole(`button`,{name:`Show the password`})),s(l(e).getByLabelText(`Password`,{selector:`input`})).toHaveAttribute(`type`,`text`)}},p={parameters:{a11y:{test:`todo`}},args:{hint:`At least 8 characters.`}},m={parameters:{a11y:{test:`todo`}},args:{invalid:!0,hint:`This password is too short.`}},h=[`Hidden`,`Shown`,`WithAHint`,`Invalid`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Password', {
      selector: 'input'
    })).toHaveAttribute('type', 'password');
  }
}`,...d.parameters?.docs?.source},description:{story:`The password is hidden on load.`,...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Show the password'
    }));
    expect(within(canvasElement).getByLabelText('Password', {
      selector: 'input'
    })).toHaveAttribute('type', 'text');
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'At least 8 characters.'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    invalid: true,
    hint: 'This password is too short.'
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{d as Hidden,m as Invalid,f as Shown,p as WithAHint,h as __namedExportsOrder,u as default};
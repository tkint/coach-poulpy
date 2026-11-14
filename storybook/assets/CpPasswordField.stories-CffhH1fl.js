import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-BSXg-Hva.js";import{A as r,t as i}from"./src-DRMNSWcR.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/PasswordField`,component:r,args:{label:`Password`,showLabel:`Show the password`,hideLabel:`Hide the password`,autocomplete:`new-password`},render:e=>({components:{CpPasswordField:r},setup:()=>({args:e,value:t(``)}),template:`<CpPasswordField v-bind="args" v-model="value" />`})},l={play:async({canvasElement:e})=>{a(s(e).getByLabelText(`Password`,{selector:`input`})).toHaveAttribute(`type`,`password`)}},u={play:async({canvasElement:e})=>{await o.click(s(e).getByRole(`button`,{name:`Show the password`})),a(s(e).getByLabelText(`Password`,{selector:`input`})).toHaveAttribute(`type`,`text`)}},d={parameters:{a11y:{test:`todo`}},args:{hint:`At least 8 characters.`}},f={parameters:{a11y:{test:`todo`}},args:{invalid:!0,hint:`This password is too short.`}},p=[`Hidden`,`Shown`,`WithAHint`,`Invalid`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Password', {
      selector: 'input'
    })).toHaveAttribute('type', 'password');
  }
}`,...l.parameters?.docs?.source},description:{story:`The password is hidden on load.`,...l.parameters?.docs?.description}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
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
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'At least 8 characters.'
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    invalid: true,
    hint: 'This password is too short.'
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{l as Hidden,f as Invalid,u as Shown,d as WithAHint,p as __namedExportsOrder,c as default};
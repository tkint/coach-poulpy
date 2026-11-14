import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-BSXg-Hva.js";import{i as r,t as i}from"./src-DRMNSWcR.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/TextField`,component:r,args:{label:`Email`,type:`email`},render:e=>({components:{CpTextField:r},setup:()=>({args:e,value:t(``)}),template:`<CpTextField v-bind="args" v-model="value" />`})},l={},u={parameters:{a11y:{test:`todo`}},args:{hint:`We only use it to sign you in.`}},d={args:{invalid:!0,error:`This email address is not valid.`}},f={play:async({canvasElement:e})=>{let t=s(e).getByLabelText(`Email`);await o.type(t,`alex@example.com`),a(t).toHaveValue(`alex@example.com`)}},p=[`Empty`,`WithAHint`,`WithAnError`,`Typing`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'We only use it to sign you in.'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    invalid: true,
    error: 'This email address is not valid.'
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const input = within(canvasElement).getByLabelText('Email');
    await userEvent.type(input, 'alex@example.com');
    expect(input).toHaveValue('alex@example.com');
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{l as Empty,f as Typing,u as WithAHint,d as WithAnError,p as __namedExportsOrder,c as default};
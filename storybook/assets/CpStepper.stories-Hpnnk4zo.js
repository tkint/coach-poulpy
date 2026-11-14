import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{B as t,a as n}from"./iframe-sFxG7Zxs.js";import{t as r,u as i}from"./src-Bujm5Dv6.js";var a,o,s,c,l,u,d,f;function p(){return(p=e((()=>{n(),r(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/Stepper`,component:i,args:{label:`Rest`,valueText:`90 s`,lessLabel:`Less rest`,moreLabel:`More rest`,step:15,min:30,max:120},render:e=>({components:{CpStepper:i},setup:()=>({args:e,value:t(90)}),template:`<CpStepper v-bind="args" v-model="value" />`})},l={},u={render:e=>({components:{CpStepper:i},setup:()=>({args:e,value:t(30)}),template:`<CpStepper v-bind="args" v-model="value" />`}),play:async({canvasElement:e})=>{a(s(e).getByRole(`button`,{name:`Less rest`})).toBeDisabled()}},d={render:e=>({components:{CpStepper:i},setup:()=>({args:e,value:t(120)}),template:`<CpStepper v-bind="args" v-model="value" />`}),play:async({canvasElement:e})=>{a(s(e).getByRole(`button`,{name:`More rest`})).toBeDisabled()}},f=[`Default`,`AtItsMinimum`,`AtItsMaximum`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpStepper
    },
    setup: () => ({
      args,
      value: ref(30)
    }),
    template: '<CpStepper v-bind="args" v-model="value" />'
  }),
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Less rest'
    })).toBeDisabled();
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpStepper
    },
    setup: () => ({
      args,
      value: ref(120)
    }),
    template: '<CpStepper v-bind="args" v-model="value" />'
  }),
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'More rest'
    })).toBeDisabled();
  }
}`,...d.parameters?.docs?.source}}}})))()}p();export{d as AtItsMaximum,u as AtItsMinimum,l as Default,f as __namedExportsOrder,c as default};
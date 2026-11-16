import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{h as r,i}from"./iframe-DYosJWzf.js";var a;function o(){return(o=e((()=>{a="`CpStepper`.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpStepper>` | `.cp-stepper`, `.cp-stepper-row`, `cp-btn cp-btn-circle cp-btn-outline`, `output.cp-stepper-value` |\n"})))()}var s,c,l,u,d,f,p,m;function h(){return(h=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/Stepper`,parameters:{docs:{description:{component:a}}},component:r,args:{label:`Rest`,valueText:`90 s`,lessLabel:`Less rest`,moreLabel:`More rest`,step:15,min:30,max:120},render:e=>({components:{CpStepper:r},setup:()=>({args:e,value:t(90)}),template:`<CpStepper v-bind="args" v-model="value" />`})},d={},f={render:e=>({components:{CpStepper:r},setup:()=>({args:e,value:t(30)}),template:`<CpStepper v-bind="args" v-model="value" />`}),play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`Less rest`})).toBeDisabled()}},p={render:e=>({components:{CpStepper:r},setup:()=>({args:e,value:t(120)}),template:`<CpStepper v-bind="args" v-model="value" />`}),play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`More rest`})).toBeDisabled()}},m=[`Default`,`AtItsMinimum`,`AtItsMaximum`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
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
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
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
}`,...p.parameters?.docs?.source}}}})))()}h();export{p as AtItsMaximum,f as AtItsMinimum,d as Default,m as __namedExportsOrder,u as default};
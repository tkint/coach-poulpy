import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-B7of4zb0.js";import{t as r,w as i}from"./src-CtrKF2-p.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),r(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/SearchField`,component:i,args:{label:`Search exercises`,clearLabel:`Clear the search`,placeholder:`Search`},render:e=>({components:{CpSearchField:i},setup:()=>({args:e,query:t(e.modelValue??``)}),template:`<CpSearchField v-bind="args" v-model="query" />`})},l={},u={args:{modelValue:`squat`}},d={args:{compact:!0}},f={args:{modelValue:`squat`},play:async({canvasElement:e})=>{await o.click(s(e).getByRole(`button`,{name:`Clear the search`})),a(s(e).getByRole(`searchbox`)).toHaveValue(``)}},p=[`Empty`,`Filled`,`Compact`,`ClearingEmptiesIt`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: 'squat'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: 'squat'
  },
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Clear the search'
    }));
    expect(within(canvasElement).getByRole('searchbox')).toHaveValue('');
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{f as ClearingEmptiesIt,d as Compact,l as Empty,u as Filled,p as __namedExportsOrder,c as default};
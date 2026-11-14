import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{R as t,c as n,t as r}from"./src-Bujm5Dv6.js";var i,a,o,s,c,l;function u(){return(u=e((()=>{r(),{expect:i,within:a}=__STORYBOOK_MODULE_TEST__,o={title:`Display/Notes`,render:e=>({components:{CpErrorNote:t,CpSuccessNote:n},setup:()=>({args:e}),template:`<CpErrorNote v-if="args.kind === 'error'">The link has expired.</CpErrorNote><CpSuccessNote v-else>Your password is changed.</CpSuccessNote>`})},s={args:{kind:`error`},play:async({canvasElement:e})=>{i(a(e).getByRole(`alert`)).toHaveTextContent(`The link has expired.`)}},c={args:{kind:`success`}},l=[`Error`,`Success`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'error'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('alert')).toHaveTextContent('The link has expired.');
  }
}`,...s.parameters?.docs?.source},description:{story:`Crimson with an icon and words, never ember.`,...s.parameters?.docs?.description}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'success'
  }
}`,...c.parameters?.docs?.source}}}})))()}u();export{s as Error,c as Success,l as __namedExportsOrder,o as default};
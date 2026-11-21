import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{Mt as t,d as n,n as r}from"./iframe-balQP6bY.js";var i;function a(){return(a=e((()=>{i='### Error\n\n`CpErrorNote`: crimson with an icon, never ember.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpErrorNote>` | `cp-alert cp-alert-error cp-error-note` with an icon, `role="alert"` |\n\n### Success\n\n`CpSuccessNote`: something went through, such as a test email sent. Green with a check and words; a status, not an alert.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSuccessNote>` | `cp-alert cp-alert-success` with a check icon, `role="status"` |\n'})))()}var o,s,c,l,u,d;function f(){return(f=e((()=>{r(),a(),{expect:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Web/Components/Display/Notes`,parameters:{docs:{description:{component:i}}},render:e=>({components:{CpErrorNote:t,CpSuccessNote:n},setup:()=>({args:e}),template:`<CpErrorNote v-if="args.kind === 'error'">The link has expired.</CpErrorNote><CpSuccessNote v-else>Your password is changed.</CpSuccessNote>`})},l={args:{kind:`error`},play:async({canvasElement:e})=>{o(s(e).getByRole(`alert`)).toHaveTextContent(`The link has expired.`)}},u={args:{kind:`success`}},d=[`Error`,`Success`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'error'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('alert')).toHaveTextContent('The link has expired.');
  }
}`,...l.parameters?.docs?.source},description:{story:`Crimson with an icon and words, never ember.`,...l.parameters?.docs?.description}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'success'
  }
}`,...u.parameters?.docs?.source}}}})))()}f();export{l as Error,u as Success,d as __namedExportsOrder,c as default};
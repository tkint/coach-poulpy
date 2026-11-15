import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{_n as t,k as n,n as r}from"./iframe-D0zhMBEY.js";var i,a,o,s,c;function l(){return(l=e((()=>{r(),{expect:i,within:a}=__STORYBOOK_MODULE_TEST__,o={title:`Web/Patterns/Empty state`,component:n},s={render:()=>({components:{CpPoulpyCard:n,CpButton:t},template:`<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>`}),play:async({canvasElement:e})=>{i(a(e).getByText(`No workout yet`)).toBeInTheDocument()}},c=[`EmptyStateCard`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      CpPoulpyCard,
      CpButton
    },
    template: '<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>'
  }),
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByText('No workout yet')).toBeInTheDocument();
  }
}`,...s.parameters?.docs?.source},description:{story:`No results or an empty list: Poulpy says what is missing and offers the next step.`,...s.parameters?.docs?.description}}}})))()}l();export{s as EmptyStateCard,c as __namedExportsOrder,o as default};
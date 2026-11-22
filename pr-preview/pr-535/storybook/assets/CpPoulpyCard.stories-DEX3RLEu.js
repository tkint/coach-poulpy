import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{F as t,Xt as n,n as r}from"./iframe-D3G3DqtX.js";var i,a,o,s,c,l,u;function d(){return(d=e((()=>{r(),{expect:i,within:a}=__STORYBOOK_MODULE_TEST__,o={title:`Web/Patterns/Poulpy card`,component:t,render:e=>({components:{CpPoulpyCard:t,CpButton:n},setup:()=>({args:e}),template:`<CpPoulpyCard :title="args.title">{{ args.body }}<template v-if="args.action" #actions><CpButton size="xl">{{ args.action }}</CpButton></template></CpPoulpyCard>`})},s={args:{title:`No workout yet`,body:`Start with a session of your own.`,action:`Create a workout`},play:async({canvasElement:e})=>{i(a(e).getByRole(`button`,{name:`Create a workout`})).toBeInTheDocument()}},c={args:{title:`No account matches`,body:`Change or clear the filters to see more accounts.`}},l={args:{title:`This workout is gone`,body:`It may have been deleted.`,action:`Back to workouts`}},u=[`EmptyList`,`NoResults`,`Missing`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  args: {
    title: 'No workout yet',
    body: 'Start with a session of your own.',
    action: 'Create a workout'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Create a workout'
    })).toBeInTheDocument();
  }
}`,...s.parameters?.docs?.source},description:{story:`An empty list: say what is missing and offer the next step.`,...s.parameters?.docs?.description}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    title: 'No account matches',
    body: 'Change or clear the filters to see more accounts.'
  }
}`,...c.parameters?.docs?.source},description:{story:`No results for the filters in use: the way out is to change them, so there is no button.`,...c.parameters?.docs?.description}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    title: 'This workout is gone',
    body: 'It may have been deleted.',
    action: 'Back to workouts'
  }
}`,...l.parameters?.docs?.source},description:{story:`Something that is gone: the way back is the one button.`,...l.parameters?.docs?.description}}}})))()}d();export{s as EmptyList,l as Missing,c as NoResults,u as __namedExportsOrder,o as default};
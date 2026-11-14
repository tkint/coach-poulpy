import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{E as t,O as n,et as r,t as i}from"./src-CpRmoIye.js";var a;function o(){return(o=e((()=>{a="`CpPoulpyCard`: no results, an empty list.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPoulpy>` | `img.cp-poulpy` from `design/poulpy/` |\n| `<CpPoulpyCard>` | `cp-card bg-secondary text-secondary-content cp-poulpy-card` |\n"})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{i(),o(),{expect:s,within:c}=__STORYBOOK_MODULE_TEST__,l={title:`Poulpy/Poulpy`,component:n,parameters:{docs:{description:{component:a}}}},u={args:{drawing:`mood_happy`}},d={args:{drawing:`mood_recovering`}},f={args:{drawing:`mood_grumpy`}},p={args:{drawing:`pose_thinking`,label:`Poulpy is thinking`},play:async({canvasElement:e})=>{s(c(e).getByAltText(`Poulpy is thinking`)).toBeInTheDocument()}},m={render:()=>({components:{CpPoulpyCard:t,CpButton:r},template:`<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>`})},h=[`Happy`,`Recovering`,`Grumpy`,`Described`,`EmptyStateCard`],u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_happy'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_recovering'
  }
}`,...d.parameters?.docs?.source},description:{story:`The blue side of pain and rehab: Poulpy is never guilt.`,...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_grumpy'
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'pose_thinking',
    label: 'Poulpy is thinking'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByAltText('Poulpy is thinking')).toBeInTheDocument();
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      CpPoulpyCard,
      CpButton
    },
    template: '<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>'
  })
}`,...m.parameters?.docs?.source}}}})))()}g();export{p as Described,m as EmptyStateCard,f as Grumpy,u as Happy,d as Recovering,h as __namedExportsOrder,l as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{E as t,O as n,et as r,t as i}from"./src-CtrKF2-p.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{i(),{expect:a,within:o}=__STORYBOOK_MODULE_TEST__,s={title:`Poulpy/Poulpy`,component:n},c={args:{drawing:`mood_happy`}},l={args:{drawing:`mood_recovering`}},u={args:{drawing:`mood_grumpy`}},d={args:{drawing:`pose_thinking`,label:`Poulpy is thinking`},play:async({canvasElement:e})=>{a(o(e).getByAltText(`Poulpy is thinking`)).toBeInTheDocument()}},f={render:()=>({components:{CpPoulpyCard:t,CpButton:r},template:`<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>`})},p=[`Happy`,`Recovering`,`Grumpy`,`Described`,`EmptyStateCard`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_happy'
  }
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_recovering'
  }
}`,...l.parameters?.docs?.source},description:{story:`The blue side of pain and rehab: Poulpy is never guilt.`,...l.parameters?.docs?.description}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_grumpy'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'pose_thinking',
    label: 'Poulpy is thinking'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByAltText('Poulpy is thinking')).toBeInTheDocument();
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  render: () => ({
    components: {
      CpPoulpyCard,
      CpButton
    },
    template: '<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>'
  })
}`,...f.parameters?.docs?.source}}}})))()}m();export{d as Described,f as EmptyStateCard,u as Grumpy,c as Happy,l as Recovering,p as __namedExportsOrder,s as default};
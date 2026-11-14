import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{g as t,t as n,ut as r}from"./src-DRMNSWcR.js";var i,a,o,s;function c(){return(c=e((()=>{n(),{expect:i}=__STORYBOOK_MODULE_TEST__,a={title:`Overlays/SidePanel`,component:t,args:{title:`Alex Martin`,closeLabel:`Close`},render:e=>({components:{CpSidePanel:t,CpAvatar:r},setup:()=>({args:e}),template:`<CpSidePanel v-bind="args">
      <template #avatar><CpAvatar initials="AM" size="lg" /></template>
      <template #subtitle><span class="text-sm">alex@example.com</span></template>
      <p>Joined in March.</p>
    </CpSidePanel>`})},o={play:async({canvasElement:e})=>{i(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},s=[`Open`],o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...o.parameters?.docs?.source},description:{story:`A side panel is opened by whoever shows it, so it is open here.`,...o.parameters?.docs?.description}}}})))()}c();export{o as Open,s as __namedExportsOrder,a as default};
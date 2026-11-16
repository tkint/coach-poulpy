import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{An as t,i as n,x as r}from"./iframe-RTCwLKrr.js";var i;function a(){return(a=e((()=>{i="`CpSidePanel`: the details of the row picked in a list, floating over the page from the right (the whole width on a phone) with a dim backdrop, so the list never moves. A click outside, the close button and Escape close it. Render it with `v-if` while a row is picked.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSidePanel>` | `dialog.cp-modal.cp-modal-end` around a `div.cp-modal-box` (`w-full sm:w-96`) with an `h2.cp-type-headline` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm` close button, and a `form.cp-modal-backdrop` |\n"})))()}var o,s,c,l;function u(){return(u=e((()=>{n(),a(),{expect:o}=__STORYBOOK_MODULE_TEST__,s={title:`Web/Components/Overlays/SidePanel`,parameters:{docs:{description:{component:i}}},component:r,args:{title:`Alex Martin`,closeLabel:`Close`},render:e=>({components:{CpSidePanel:r,CpAvatar:t},setup:()=>({args:e}),template:`<CpSidePanel v-bind="args">
      <template #avatar><CpAvatar initials="AM" size="lg" /></template>
      <template #subtitle><span class="text-sm">alex@example.com</span></template>
      <p>Joined in March.</p>
    </CpSidePanel>`})},c={play:async({canvasElement:e})=>{o(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},l=[`Open`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...c.parameters?.docs?.source},description:{story:`A side panel is opened by whoever shows it, so it is open here.`,...c.parameters?.docs?.description}}}})))()}u();export{c as Open,l as __namedExportsOrder,s as default};
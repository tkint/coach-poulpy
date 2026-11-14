import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-BSXg-Hva.js";import{et as r,t as i,v as a}from"./src-DRMNSWcR.js";var o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:o,userEvent:s,within:c}=__STORYBOOK_MODULE_TEST__,l={title:`Overlays/Sheet`,component:a,args:{title:`Delete this workout?`,closeLabel:`Close`},render:(e,{argTypes:n})=>({components:{CpSheet:a,CpButton:r},setup:()=>({args:e,open:t(!!e.open)}),template:`<div class="min-h-80">
      <CpButton variant="outline" @click="open = true">Open the sheet</CpButton>
      <CpSheet v-bind="args" v-model:open="open">
        <p>It cannot be undone.</p>
        <template #actions><CpButton variant="danger" @click="open = false">Delete</CpButton></template>
      </CpSheet>
    </div>`})},u={play:async({canvasElement:e})=>{o(e.querySelector(`dialog`)).not.toHaveAttribute(`open`)}},d={args:{open:!0},play:async({canvasElement:e})=>{o(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},f={play:async({canvasElement:e})=>{await s.click(c(e).getByRole(`button`,{name:`Open the sheet`})),o(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},p=[`Closed`,`Open`,`OpensFromItsButton`],u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).not.toHaveAttribute('open');
  }
}`,...u.parameters?.docs?.source},description:{story:`A sheet never shows itself on load.`,...u.parameters?.docs?.description}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    open: true
  },
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Open the sheet'
    }));
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{u as Closed,d as Open,f as OpensFromItsButton,p as __namedExportsOrder,l as default};
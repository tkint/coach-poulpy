import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{C as r,Qt as i,i as a}from"./iframe-Br18AScW.js";var o;function s(){return(s=e((()=>{o="`CpSheet`: a bottom sheet on a phone, a dialog on a wider screen.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSheet>` | `dialog.cp-modal.cp-modal-bottom.sm:cp-modal-middle.cp-sheet` and `.cp-modal-box` |\n"})))()}var c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),a(),s(),{expect:c,userEvent:l,within:u}=__STORYBOOK_MODULE_TEST__,d={title:`Web/Components/Overlays/Sheet`,parameters:{docs:{description:{component:o}}},component:r,args:{title:`Delete this workout?`,closeLabel:`Close`},render:(e,{argTypes:n})=>({components:{CpSheet:r,CpButton:i},setup:()=>({args:e,open:t(!!e.open)}),template:`<div class="min-h-80">
      <CpButton size="xl" variant="outline" @click="open = true">Open the sheet</CpButton>
      <CpSheet v-bind="args" v-model:open="open">
        <p>It cannot be undone.</p>
        <template #actions><CpButton size="xl" variant="danger" @click="open = false">Delete</CpButton></template>
      </CpSheet>
    </div>`})},f={play:async({canvasElement:e})=>{c(e.querySelector(`dialog`)).not.toHaveAttribute(`open`)}},p={args:{open:!0},play:async({canvasElement:e})=>{c(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},m={play:async({canvasElement:e})=>{await l.click(u(e).getByRole(`button`,{name:`Open the sheet`})),c(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},h=[`Closed`,`Open`,`OpensFromItsButton`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).not.toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source},description:{story:`A sheet never shows itself on load.`,...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    open: true
  },
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Open the sheet'
    }));
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{f as Closed,p as Open,m as OpensFromItsButton,h as __namedExportsOrder,d as default};
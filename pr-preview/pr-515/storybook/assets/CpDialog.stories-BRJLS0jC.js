import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{C as r,Xt as i,n as a}from"./iframe-BKOHQaMK.js";var o;function s(){return(s=e((()=>{o="`CpDialog`: a centred dialog for a review the person confirms, with a title, a line of help, a close button, the content and a row of actions. `wide` gives a long table room; `rising` makes it rise from the bottom on a phone-width page.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDialog>` | `dialog.cp-modal` (`.cp-modal-bottom.sm:cp-modal-middle` when `rising`) and `.cp-modal-box`, `.cp-modal-backdrop` |\n"})))()}var c,l,u,d,f,p,m,h,g,_;function v(){return(v=e((()=>{n(),a(),s(),{expect:c,userEvent:l,within:u}=__STORYBOOK_MODULE_TEST__,d={title:`Web/Components/Overlays/Dialog`,parameters:{docs:{description:{component:o}}},component:r,args:{title:`Publish catalog changes`,description:`Review the changes before publishing. Phones download the updated catalog the next time they open the app, guests included.`,closeLabel:`Close publication review`},render:e=>({components:{CpDialog:r,CpButton:i},setup:()=>({args:e,open:t(!!e.open)}),template:`<div class="min-h-80">
      <CpButton size="xl" variant="outline" @click="open = true">Open the dialog</CpButton>
      <CpDialog v-bind="args" v-model:open="open">
        <p>Goblet squat · French name changed.</p>
        <template #actions>
          <CpButton variant="ghost" size="md" @click="open = false">Cancel</CpButton>
          <CpButton size="md" @click="open = false">Publish changes</CpButton>
        </template>
      </CpDialog>
    </div>`})},f={args:{wide:!0,open:!0}},p={args:{rising:!0,open:!0}},m={play:async({canvasElement:e})=>{c(e.querySelector(`dialog`)).not.toHaveAttribute(`open`)}},h={args:{open:!0},play:async({canvasElement:e})=>{c(e.querySelector(`dialog`)).toHaveAttribute(`open`)}},g={play:async({canvasElement:e})=>{await l.click(u(e).getByRole(`button`,{name:`Open the dialog`})),c(e.querySelector(`dialog`)).toHaveAttribute(`open`),await l.click(u(e.querySelector(`dialog`)).getByRole(`button`,{name:`Cancel`})),c(e.querySelector(`dialog`)).not.toHaveAttribute(`open`)}},_=[`Wide`,`Rising`,`Closed`,`Open`,`OpensFromItsButtonAndClosesWithCancel`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    wide: true,
    open: true
  }
}`,...f.parameters?.docs?.source},description:{story:`The wide one holds a table; the rising one is the retirement review, a sheet on a phone.`,...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    rising: true,
    open: true
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).not.toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source},description:{story:`A dialog never shows itself on load.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    open: true
  },
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Open the dialog'
    }));
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open');
    await userEvent.click(within(canvasElement.querySelector('dialog')!).getByRole('button', {
      name: 'Cancel'
    }));
    expect(canvasElement.querySelector('dialog')).not.toHaveAttribute('open');
  }
}`,...g.parameters?.docs?.source}}}})))()}v();export{m as Closed,h as Open,g as OpensFromItsButtonAndClosesWithCancel,p as Rising,f as Wide,_ as __namedExportsOrder,d as default};
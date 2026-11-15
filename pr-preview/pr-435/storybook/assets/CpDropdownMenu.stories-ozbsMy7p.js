import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./ellipsis-vertical-CddiAeYG.js";import{n as r,tn as i}from"./iframe-D9ZbQK3_.js";var a;function o(){return(o=e((()=>{a="`CpDropdownMenu`: a button that opens a short menu of actions only while it is used. Choosing an entry, Escape or a click elsewhere closes it.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDropdownMenu>` | `details` with a `summary.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` and a `ul.cp-menu` panel |\n\nAll the dropdowns (select, multi-select, menu) share the closing rule of `web/ui/src/dismissable.ts`, and their panels float over the page (`absolute`), so opening or closing one never moves anything.\n"})))()}var s,c,l,u,d,f,p,m;function h(){return(h=e((()=>{t(),r(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Navigation/DropdownMenu`,parameters:{popover:!0,docs:{description:{component:a}}},component:i,args:{label:`More actions`},render:e=>({components:{CpDropdownMenu:i,EllipsisVertical:n},setup:()=>({args:e}),template:`<CpDropdownMenu v-bind="args">
      <template #trigger><EllipsisVertical class="size-5" aria-hidden="true" /></template>
      <li><a>Rename</a></li><li><a>Duplicate</a></li>
    </CpDropdownMenu>`})},d={play:async({canvasElement:e})=>{s(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},f={play:async({canvasElement:e})=>{await c.click(l(e).getByLabelText(`More actions`)),s(e.querySelector(`details`)).toHaveAttribute(`open`)}},p={play:async({canvasElement:e})=>{await c.click(l(e).getByLabelText(`More actions`)),await c.click(l(e).getByText(`Rename`)),s(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},m=[`Closed`,`Open`,`ChoosingClosesTheMenu`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...d.parameters?.docs?.source},description:{story:`The menu is shut on load.`,...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'));
    expect(canvasElement.querySelector('details')).toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'));
    await userEvent.click(within(canvasElement).getByText('Rename'));
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...p.parameters?.docs?.source}}}})))()}h();export{p as ChoosingClosesTheMenu,d as Closed,f as Open,m as __namedExportsOrder,u as default};
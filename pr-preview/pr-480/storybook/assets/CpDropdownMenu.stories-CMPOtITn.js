import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./ellipsis-vertical-CqgkGio0.js";import{It as r,On as i,i as a}from"./iframe-RTCwLKrr.js";var o;function s(){return(s=e((()=>{o="`CpDropdownMenu`: a button that opens a short menu of actions only while it is used. Choosing an entry, Escape or a click elsewhere closes it.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDropdownMenu>` | `details` with a `summary.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` and a `ul.cp-menu` panel |\n\nAll the dropdowns (select, multi-select, menu) share the closing rule of `web/ui/src/dismissable.ts`, and their panels float over the page, so opening or closing one never moves anything. The select panels are `absolute`; the menu's panel is `fixed` under its button, so a table's own overflow never clips it or adds a scrollbar to a small table, and it closes when the page scrolls or resizes.\n"})))()}var c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{t(),a(),s(),{expect:c,userEvent:l,within:u}=__STORYBOOK_MODULE_TEST__,d={title:`Web/Components/Navigation/DropdownMenu`,parameters:{popover:!0,docs:{description:{component:o}}},component:r,args:{label:`More actions`},render:e=>({components:{CpDropdownMenu:r,CpIcon:i},setup:()=>({args:e,EllipsisVertical:n}),template:`<CpDropdownMenu v-bind="args">
      <template #trigger><CpIcon :icon="EllipsisVertical" /></template>
      <li><a>Rename</a></li><li><a>Duplicate</a></li>
    </CpDropdownMenu>`})},f={play:async({canvasElement:e})=>{c(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},p={play:async({canvasElement:e})=>{await l.click(u(e).getByLabelText(`More actions`)),c(e.querySelector(`details`)).toHaveAttribute(`open`)}},m={play:async({canvasElement:e})=>{await l.click(u(e).getByLabelText(`More actions`)),await l.click(u(e).getByText(`Rename`)),c(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},h=[`Closed`,`Open`,`ChoosingClosesTheMenu`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source},description:{story:`The menu is shut on load.`,...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'));
    expect(canvasElement.querySelector('details')).toHaveAttribute('open');
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'));
    await userEvent.click(within(canvasElement).getByText('Rename'));
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{m as ChoosingClosesTheMenu,f as Closed,p as Open,h as __namedExportsOrder,d as default};
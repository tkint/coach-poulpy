import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./src-DBNPVg7J.js";import{n as r,t as i}from"./createLucideIcon-D0_Vg3K_.js";var a,o;function s(){return(s=e((()=>{r(),a={name:`ellipsis-vertical`,size:24,node:[[`circle`,{cx:`12`,cy:`12`,r:`1`,key:`41hilf`}],[`circle`,{cx:`12`,cy:`5`,r:`1`,key:`gxeob9`}],[`circle`,{cx:`12`,cy:`19`,r:`1`,key:`lyex9k`}]],aliases:[`more-vertical`]},o=i(a)})))()}var c;function l(){return(l=e((()=>{c="`CpDropdownMenu`: a button that opens a short menu of actions only while it is used. Choosing an entry, Escape or a click elsewhere closes it.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDropdownMenu>` | `details` with a `summary.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` and a `ul.cp-menu` panel |\n\nAll the dropdowns (select, multi-select, menu) share the closing rule of `web/ui/src/dismissable.ts`, and their panels float over the page (`absolute`), so opening or closing one never moves anything.\n"})))()}var u,d,f,p,m,h,g,_;function v(){return(v=e((()=>{s(),n(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Navigation/DropdownMenu`,parameters:{popover:!0,docs:{description:{component:c}}},component:t,args:{label:`More actions`},render:e=>({components:{CpDropdownMenu:t,EllipsisVertical:o},setup:()=>({args:e}),template:`<CpDropdownMenu v-bind="args">
      <template #trigger><EllipsisVertical class="size-5" aria-hidden="true" /></template>
      <li><a>Rename</a></li><li><a>Duplicate</a></li>
    </CpDropdownMenu>`})},m={play:async({canvasElement:e})=>{u(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},h={play:async({canvasElement:e})=>{await d.click(f(e).getByLabelText(`More actions`)),u(e.querySelector(`details`)).toHaveAttribute(`open`)}},g={play:async({canvasElement:e})=>{await d.click(f(e).getByLabelText(`More actions`)),await d.click(f(e).getByText(`Rename`)),u(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},_=[`Closed`,`Open`,`ChoosingClosesTheMenu`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source},description:{story:`The menu is shut on load.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'));
    expect(canvasElement.querySelector('details')).toHaveAttribute('open');
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'));
    await userEvent.click(within(canvasElement).getByText('Rename'));
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...g.parameters?.docs?.source}}}})))()}v();export{g as ChoosingClosesTheMenu,m as Closed,h as Open,_ as __namedExportsOrder,p as default};
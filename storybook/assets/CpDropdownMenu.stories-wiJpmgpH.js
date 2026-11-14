import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{B as t,ct as n,lt as r,t as i}from"./src-Bujm5Dv6.js";var a,o;function s(){return(s=e((()=>{r(),a={name:`ellipsis-vertical`,size:24,node:[[`circle`,{cx:`12`,cy:`12`,r:`1`,key:`41hilf`}],[`circle`,{cx:`12`,cy:`5`,r:`1`,key:`gxeob9`}],[`circle`,{cx:`12`,cy:`19`,r:`1`,key:`lyex9k`}]],aliases:[`more-vertical`]},o=n(a)})))()}var c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{s(),i(),{expect:c,userEvent:l,within:u}=__STORYBOOK_MODULE_TEST__,d={title:`Navigation/DropdownMenu`,component:t,args:{label:`More actions`},render:e=>({components:{CpDropdownMenu:t,EllipsisVertical:o},setup:()=>({args:e}),template:`<CpDropdownMenu v-bind="args">
      <template #trigger><EllipsisVertical class="size-5" aria-hidden="true" /></template>
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
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-Bgdnb2bH.js";import{b as r,t as i}from"./src-Cc0VKo53.js";var a;function o(){return(o=e((()=>{a='`CpPagination`: previous, the page numbers and next, joined in one row; a long list keeps the first and last pages and those around the current one, with a gap between. Put the count of what is shown beside it.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPagination>` | `nav` around `div.cp-join` of `cp-join-item cp-btn cp-btn-sm` buttons, `cp-btn-active` and `aria-current="page"` on the current page |\n'})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Navigation/Pagination`,parameters:{docs:{description:{component:a}}},component:r,args:{pages:12,previousLabel:`Previous page`,nextLabel:`Next page`,navLabel:`Pages`},render:e=>({components:{CpPagination:r},setup:()=>({args:e,page:t(e.page??1)}),template:`<CpPagination v-bind="args" v-model:page="page" />`})},d={play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`Previous page`})).toBeDisabled()}},f={args:{page:6}},p={args:{page:12},play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`Next page`})).toBeDisabled()}},m={args:{pages:3}},h=[`FirstPage`,`MiddlePage`,`LastPage`,`OnlyAFewPages`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Previous page'
    })).toBeDisabled();
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    page: 6
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    page: 12
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Next page'
    })).toBeDisabled();
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  args: {
    pages: 3
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{d as FirstPage,p as LastPage,f as MiddlePage,m as OnlyAFewPages,h as __namedExportsOrder,u as default};
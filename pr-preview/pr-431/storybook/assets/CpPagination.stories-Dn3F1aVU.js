import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-C5FEPL1F.js";import{et as r,n as i,x as a}from"./iframe-CLqi8p6L.js";var o;function s(){return(s=e((()=>{o='`CpPagination`: previous, the page numbers and next, joined in one row; a long list keeps the first and last pages and those around the current one, with a gap between. Put the count of what is shown beside it.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPagination>` | `nav` around `div.cp-join` of `cp-join-item cp-btn cp-btn-sm` buttons, `cp-btn-active` and `aria-current="page"` on the current page |\n'})))()}var c,l,u,d,f,p,m,h,g,_,v;function y(){return(y=e((()=>{n(),i(),s(),{expect:c,userEvent:l,within:u}=__STORYBOOK_MODULE_TEST__,d={title:`Web/Components/Navigation/Pagination`,parameters:{docs:{description:{component:o}}},component:a,args:{pages:12,previousLabel:`Previous page`,nextLabel:`Next page`,navLabel:`Pages`},render:e=>({components:{CpPagination:a},setup:()=>({args:e,page:t(e.page??1)}),template:`<CpPagination v-bind="args" v-model:page="page" />`})},f={play:async({canvasElement:e})=>{c(u(e).getByRole(`button`,{name:`Previous page`})).toBeDisabled()}},p={args:{page:6}},m={args:{page:12},play:async({canvasElement:e})=>{c(u(e).getByRole(`button`,{name:`Next page`})).toBeDisabled()}},h={args:{pages:3}},g={args:{previousLabel:void 0,nextLabel:void 0,navLabel:void 0},play:async({canvasElement:e})=>{c(u(e).getByRole(`button`,{name:`Previous page`})).toBeDisabled()}},_={args:{previousLabel:void 0,nextLabel:void 0,navLabel:void 0},loaders:[()=>void r(`fr`)],play:async({canvasElement:e})=>{c(u(e).getByRole(`button`,{name:`Page précédente`})).toBeDisabled(),r(`en`)}},v=[`FirstPage`,`MiddlePage`,`LastPage`,`OnlyAFewPages`,`SaysItsOwnWords`,`SaysItsOwnWordsInFrench`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Previous page'
    })).toBeDisabled();
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    page: 6
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
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
}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    pages: 3
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  args: {
    previousLabel: undefined,
    nextLabel: undefined,
    navLabel: undefined
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Previous page'
    })).toBeDisabled();
  }
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  args: {
    previousLabel: undefined,
    nextLabel: undefined,
    navLabel: undefined
  },
  loaders: [() => void setUiLocale('fr')],
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Page précédente'
    })).toBeDisabled();
    setUiLocale('en');
  }
}`,..._.parameters?.docs?.source}}}})))()}y();export{f as FirstPage,m as LastPage,p as MiddlePage,h as OnlyAFewPages,g as SaysItsOwnWords,_ as SaysItsOwnWordsInFrench,v as __namedExportsOrder,d as default};
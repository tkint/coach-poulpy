import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-B7of4zb0.js";import{b as r,t as i}from"./src-CtrKF2-p.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Navigation/Pagination`,component:r,args:{pages:12,previousLabel:`Previous page`,nextLabel:`Next page`,navLabel:`Pages`},render:e=>({components:{CpPagination:r},setup:()=>({args:e,page:t(e.page??1)}),template:`<CpPagination v-bind="args" v-model:page="page" />`})},l={play:async({canvasElement:e})=>{a(s(e).getByRole(`button`,{name:`Previous page`})).toBeDisabled()}},u={args:{page:6}},d={args:{page:12},play:async({canvasElement:e})=>{a(s(e).getByRole(`button`,{name:`Next page`})).toBeDisabled()}},f={args:{pages:3}},p=[`FirstPage`,`MiddlePage`,`LastPage`,`OnlyAFewPages`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Previous page'
    })).toBeDisabled();
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    page: 6
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
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
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    pages: 3
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{l as FirstPage,d as LastPage,u as MiddlePage,f as OnlyAFewPages,p as __namedExportsOrder,c as default};
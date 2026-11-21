import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{Qt as r,T as i,n as a}from"./iframe-Cv7XDtNd.js";import{i as o,r as s}from"./sizes-CyfVehCB.js";var c;function l(){return(l=e((()=>{c='`CpPagination`: previous, the page numbers and next, joined in one row; a long list keeps the first and last pages and those around the current one, with a gap between. Put the count of what is shown beside it.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPagination>` | `nav` around `div.cp-join` of `cp-join-item cp-btn cp-btn-sm` buttons, `cp-btn-active` and `aria-current="page"` on the current page |\n| `<CpPagination size="sm">` | `cp-btn-sm` on every button; `size` is the websites\' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n'})))()}var u,d,f,p,m,h,g,_,v,y,b,x;function S(){return(S=e((()=>{n(),a(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Navigation/Pagination`,parameters:{docs:{description:{component:c}}},component:i,argTypes:{size:o},args:{pages:12,previousLabel:`Previous page`,nextLabel:`Next page`,navLabel:`Pages`},render:e=>({components:{CpPagination:i},setup:()=>({args:e,page:t(e.page??1)}),template:`<CpPagination v-bind="args" v-model:page="page" />`})},m={play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`Previous page`})).toBeDisabled()}},h={args:{page:6}},g={args:{page:12},play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`Next page`})).toBeDisabled()}},_={args:{pages:3}},v={args:{previousLabel:void 0,nextLabel:void 0,navLabel:void 0},play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`Previous page`})).toBeDisabled()}},y={args:{previousLabel:void 0,nextLabel:void 0,navLabel:void 0},globals:{locale:`fr`},play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`Page précédente`})).toBeDisabled()}},b={render:e=>({components:{CpPagination:i},setup:()=>({args:e,cpSizes:r,value:t(1)}),template:'<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpPagination v-bind="args" :size="s" :nav-label="`Pages ${s}`" v-model:page="value" /></div></div>'})},x=[`FirstPage`,`MiddlePage`,`LastPage`,`OnlyAFewPages`,`SaysItsOwnWords`,`SaysItsOwnWordsInFrench`,`Sizes`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Previous page'
    })).toBeDisabled();
  }
}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    page: 6
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
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
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  args: {
    pages: 3
  }
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
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
}`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  args: {
    previousLabel: undefined,
    nextLabel: undefined,
    navLabel: undefined
  },
  globals: {
    locale: 'fr'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Page précédente'
    })).toBeDisabled();
  }
}`,...y.parameters?.docs?.source}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpPagination
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref(1)
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpPagination v-bind="args" :size="s" :nav-label="\`Pages \${s}\`" v-model:page="value" /></div></div>'
  })
}`,...b.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...b.parameters?.docs?.description}}}})))()}S();export{m as FirstPage,g as LastPage,h as MiddlePage,_ as OnlyAFewPages,v as SaysItsOwnWords,y as SaysItsOwnWordsInFrench,b as Sizes,x as __namedExportsOrder,p as default};
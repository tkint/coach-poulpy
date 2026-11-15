import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{N as r,Zt as i,i as a}from"./iframe-z5tRAIcj.js";import{i as o,r as s}from"./sizes-BGGQTY6O.js";var c;function l(){return(l=e((()=>{c='`CpSearchField`: a pill with a search icon. With `compact` it is one control of a filter bar: 14rem wide instead of filling its row (pass `size="sm"` for it to be as high as a small button). The input is `type="search"` with `autocomplete="off"`, so a password manager does not take a search for an email address as a sign-in.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSearchField>` | `label.cp-input.cp-search-field` around the icon and an `input type="search"`; `cp-search-field-compact` with `compact` |\n| `<CpSearchField size="lg">` | `cp-input-lg` on the field, as a list\'s own search; `size` is the websites\' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n'})))()}var u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),a(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Fields/SearchField`,parameters:{docs:{description:{component:c}}},component:r,argTypes:{size:o},args:{label:`Search exercises`,clearLabel:`Clear the search`,placeholder:`Search`},render:e=>({components:{CpSearchField:r},setup:()=>({args:e,query:t(e.modelValue??``)}),template:`<CpSearchField v-bind="args" v-model="query" />`})},m={},h={args:{modelValue:`squat`}},g={args:{compact:!0}},_={args:{modelValue:`squat`},play:async({canvasElement:e})=>{await d.click(f(e).getByRole(`button`,{name:`Clear the search`})),u(f(e).getByRole(`searchbox`)).toHaveValue(``)}},v={render:e=>({components:{CpSearchField:r},setup:()=>({args:e,cpSizes:i,value:t(``)}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSearchField v-bind="args" :size="s" v-model="value" /></div></div>`})},y=[`Empty`,`Filled`,`Compact`,`ClearingEmptiesIt`,`Sizes`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: 'squat'
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: 'squat'
  },
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Clear the search'
    }));
    expect(within(canvasElement).getByRole('searchbox')).toHaveValue('');
  }
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpSearchField
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref('')
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSearchField v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...v.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...v.parameters?.docs?.description}}}})))()}b();export{_ as ClearingEmptiesIt,g as Compact,m as Empty,h as Filled,v as Sizes,y as __namedExportsOrder,p as default};
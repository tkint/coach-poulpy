import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-C08GtdKu.js";import{E as r,t as i}from"./src-bcTs06N1.js";var a;function o(){return(o=e((()=>{a='`CpSearchField`: a pill with a search icon. With `compact` it is one control of a filter bar: as high as a small button and 14rem wide instead of filling its row. The input is `type="search"` with `autocomplete="off"`, so a password manager does not take a search for an email address as a sign-in.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSearchField>` | `label.cp-input.cp-search-field` around the icon and an `input type="search"`; `cp-search-field-compact` with `compact` |\n'})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/SearchField`,parameters:{docs:{description:{component:a}}},component:r,args:{label:`Search exercises`,clearLabel:`Clear the search`,placeholder:`Search`},render:e=>({components:{CpSearchField:r},setup:()=>({args:e,query:t(e.modelValue??``)}),template:`<CpSearchField v-bind="args" v-model="query" />`})},d={},f={args:{modelValue:`squat`}},p={args:{compact:!0}},m={args:{modelValue:`squat`},play:async({canvasElement:e})=>{await c.click(l(e).getByRole(`button`,{name:`Clear the search`})),s(l(e).getByRole(`searchbox`)).toHaveValue(``)}},h=[`Empty`,`Filled`,`Compact`,`ClearingEmptiesIt`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: 'squat'
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
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
}`,...m.parameters?.docs?.source}}}})))()}g();export{m as ClearingEmptiesIt,p as Compact,d as Empty,f as Filled,h as __namedExportsOrder,u as default};
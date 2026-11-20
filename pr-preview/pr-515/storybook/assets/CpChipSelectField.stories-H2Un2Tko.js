import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{Qt as r,Wt as i,n as a}from"./iframe-DLtWRhFS.js";import{i as o,r as s}from"./sizes-o9v6mpza.js";var c;function l(){return(l=e((()=>{c="`CpChipSelectField`: any number of values in a form. The picked ones sit in the field as removable badges; typing narrows the list its panel shows while it is used, and a picked value is checked there. Clicking a value again, its badge's cross or Backspace in an empty field takes it out; Enter picks the first match (so it never sends the form); Escape or a click elsewhere closes the panel. `aria-label` names a field whose label is written beside it, such as a heading above.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpChipSelectField>` | `div.cp-fieldset` around a `label.cp-type-label`, a `label.cp-input` holding `span.cp-badge.cp-badge-neutral.cp-badge-soft` badges (each with a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-xs` cross), the `input[role=combobox]` and a chevron, a `cp-card` `ul[role=listbox]` of option `button`s (as the select field's) and an optional `p.cp-label` hint |\n| `<CpChipSelectField size=\"lg\">` | `cp-input-lg`: the field's least height, which grows as badges wrap; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n"})))()}var u,d,f,p,m,h,g,_,v,y,b,x,S,C,w;function T(){return(T=e((()=>{n(),a(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Fields/ChipSelectField`,component:i,argTypes:{size:o},parameters:{popover:!0,docs:{description:{component:c}}},args:{label:`Equipment`,placeholder:`Add equipment`,options:[{value:`standing`,text:`Standing`},{value:`bench`,text:`Bench`},{value:`bar`,text:`Barbell`}],noneLabel:`No equipment matches`,removeLabel:e=>`Remove ${e}`},render:(e,{name:n})=>({components:{CpChipSelectField:i},setup:()=>({args:e,picked:t(n===`Empty`||n===`With A Hint`?[]:[`standing`])}),template:`<div class="pb-48"><CpChipSelectField v-bind="args" v-model="picked" /></div>`})},m={},h={play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`Remove Standing`})).toBeInTheDocument()}},g={parameters:{a11y:{test:`todo`}},args:{hint:`Only what the exercise needs.`}},_={play:async({canvasElement:e})=>{u(e.querySelector(`[role=listbox]`)).not.toBeVisible()}},v={play:async({canvasElement:e})=>{await d.click(f(e).getByRole(`combobox`)),u(e.querySelector(`[role=listbox]`)).toBeVisible()}},y={parameters:{a11y:{test:`todo`}},play:async({canvasElement:e})=>{await d.type(f(e).getByRole(`combobox`),`zzz`),u(f(e).getByText(`No equipment matches`)).toBeVisible()}},b={play:async({canvasElement:e})=>{await d.click(f(e).getByRole(`button`,{name:`Remove Standing`})),u(f(e).queryByRole(`button`,{name:`Remove Standing`})).toBeNull()}},x={play:async({canvasElement:e})=>{await d.click(e.querySelector(`.cp-badge`)),u(f(e).getByRole(`button`,{name:`Remove Standing`})).toBeInTheDocument()}},S={play:async({canvasElement:e})=>{await d.click(f(e).getByRole(`combobox`)),await d.keyboard(`{Escape}`),u(e.querySelector(`[role=listbox]`)).not.toBeVisible()}},C={render:e=>({components:{CpChipSelectField:i},setup:()=>({args:e,cpSizes:r,value:t([])}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpChipSelectField v-bind="args" :size="s" v-model="value" /></div></div>`})},w=[`Empty`,`WithPickedValues`,`WithAHint`,`ClosedOnLoad`,`Open`,`NothingMatches`,`RemovingAValue`,`ClickingABadgeKeepsIt`,`EscapeClosesTheList`,`Sizes`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Remove Standing'
    })).toBeInTheDocument();
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'Only what the exercise needs.'
  }
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('[role=listbox]')).not.toBeVisible();
  }
}`,..._.parameters?.docs?.source},description:{story:`The list is shut until the field is used.`,..._.parameters?.docs?.description}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('combobox'));
    expect(canvasElement.querySelector('[role=listbox]')).toBeVisible();
  }
}`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  play: async ({
    canvasElement
  }) => {
    await userEvent.type(within(canvasElement).getByRole('combobox'), 'zzz');
    expect(within(canvasElement).getByText('No equipment matches')).toBeVisible();
  }
}`,...y.parameters?.docs?.source}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Remove Standing'
    }));
    expect(within(canvasElement).queryByRole('button', {
      name: 'Remove Standing'
    })).toBeNull();
  }
}`,...b.parameters?.docs?.source}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('.cp-badge') as HTMLElement);
    expect(within(canvasElement).getByRole('button', {
      name: 'Remove Standing'
    })).toBeInTheDocument();
  }
}`,...x.parameters?.docs?.source},description:{story:`Only a badge's cross takes a value out; a click on the badge itself leaves it.`,...x.parameters?.docs?.description}}},S.parameters={...S.parameters,docs:{...S.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('combobox'));
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('[role=listbox]')).not.toBeVisible();
  }
}`,...S.parameters?.docs?.source}}},C.parameters={...C.parameters,docs:{...C.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpChipSelectField
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref([])
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpChipSelectField v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...C.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...C.parameters?.docs?.description}}}})))()}T();export{x as ClickingABadgeKeepsIt,_ as ClosedOnLoad,m as Empty,S as EscapeClosesTheList,y as NothingMatches,v as Open,b as RemovingAValue,C as Sizes,g as WithAHint,h as WithPickedValues,w as __namedExportsOrder,p as default};
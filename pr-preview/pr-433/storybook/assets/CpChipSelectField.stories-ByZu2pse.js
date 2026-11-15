import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-C5FEPL1F.js";import{dn as r,n as i}from"./iframe-D0zhMBEY.js";var a;function o(){return(o=e((()=>{a="`CpChipSelectField`: any number of values in a form. The picked ones sit in the field as removable badges; typing narrows the list its panel shows while it is used, and a picked value is checked there. Clicking a value again, its badge's cross or Backspace in an empty field takes it out; Enter picks the first match (so it never sends the form); Escape or a click elsewhere closes the panel. `aria-label` names a field whose label is written beside it, such as a heading above.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpChipSelectField>` | `div.cp-fieldset` around a `label.cp-type-label`, a `label.cp-input` holding `span.cp-badge.cp-badge-neutral.cp-badge-soft` badges (each with a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-xs` cross), the `input[role=combobox]` and a chevron, a `cp-card` `ul[role=listbox]` of option `button`s (as the select field's) and an optional `p.cp-label` hint |\n"})))()}var s,c,l,u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/ChipSelectField`,component:r,parameters:{popover:!0,docs:{description:{component:a}}},args:{label:`Equipment`,placeholder:`Add equipment`,options:[{value:`standing`,text:`Standing`},{value:`bench`,text:`Bench`},{value:`bar`,text:`Barbell`}],noneLabel:`No equipment matches`,removeLabel:e=>`Remove ${e}`},render:(e,{name:n})=>({components:{CpChipSelectField:r},setup:()=>({args:e,picked:t(n===`Empty`||n===`With A Hint`?[]:[`standing`])}),template:`<div class="pb-48"><CpChipSelectField v-bind="args" v-model="picked" /></div>`})},d={},f={play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`Remove Standing`})).toBeInTheDocument()}},p={parameters:{a11y:{test:`todo`}},args:{hint:`Only what the exercise needs.`}},m={play:async({canvasElement:e})=>{s(e.querySelector(`[role=listbox]`)).not.toBeVisible()}},h={play:async({canvasElement:e})=>{await c.click(l(e).getByRole(`combobox`)),s(e.querySelector(`[role=listbox]`)).toBeVisible()}},g={parameters:{a11y:{test:`todo`}},play:async({canvasElement:e})=>{await c.type(l(e).getByRole(`combobox`),`zzz`),s(l(e).getByText(`No equipment matches`)).toBeVisible()}},_={play:async({canvasElement:e})=>{await c.click(l(e).getByRole(`button`,{name:`Remove Standing`})),s(l(e).queryByRole(`button`,{name:`Remove Standing`})).toBeNull()}},v={play:async({canvasElement:e})=>{await c.click(l(e).getByRole(`combobox`)),await c.keyboard(`{Escape}`),s(e.querySelector(`[role=listbox]`)).not.toBeVisible()}},y=[`Empty`,`WithPickedValues`,`WithAHint`,`ClosedOnLoad`,`Open`,`NothingMatches`,`RemovingAValue`,`EscapeClosesTheList`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Remove Standing'
    })).toBeInTheDocument();
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'Only what the exercise needs.'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('[role=listbox]')).not.toBeVisible();
  }
}`,...m.parameters?.docs?.source},description:{story:`The list is shut until the field is used.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('combobox'));
    expect(canvasElement.querySelector('[role=listbox]')).toBeVisible();
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
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
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
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
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('combobox'));
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('[role=listbox]')).not.toBeVisible();
  }
}`,...v.parameters?.docs?.source}}}})))()}b();export{m as ClosedOnLoad,d as Empty,v as EscapeClosesTheList,g as NothingMatches,h as Open,_ as RemovingAValue,p as WithAHint,f as WithPickedValues,y as __namedExportsOrder,u as default};
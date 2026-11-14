import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-BCxua76K.js";import{S as r,t as i}from"./src-B8c5jiAB.js";var a;function o(){return(o=e((()=>{a="`CpSelectField`: one value from a list. A button shows the current value and opens its panel only while it is used; the options look like the multi-select field's, without the checkboxes, and the chosen one is bold with a check. Choosing an option, Escape or a click elsewhere closes the panel. With `compact` it is one control of a filter bar: its label is hidden (still read aloud, and its first option usually says it too) and it is only as wide as its longest option.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSelectField>` | `div.cp-fieldset`, a `span.cp-type-label` and a `details` with a `summary.cp-btn.cp-btn-outline` and a `cp-card` `ul[role=listbox]` of option `button`s (`flex w-full items-center gap-3 rounded-field px-2 py-1 hover:bg-base-200`); `cp-btn-sm w-auto` and a `sr-only` label with `compact` |\n"})))()}var s,c,l,u,d,f,p,m,h,g;function _(){return(_=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/SelectField`,component:r,args:{label:`Format`,options:[{value:`circuit`,text:`Circuit`},{value:`strength`,text:`Strength`},{value:`mobility`,text:`Mobility`}]},parameters:{popover:!0,docs:{description:{component:a}},a11y:{test:`todo`}},render:e=>({components:{CpSelectField:r},setup:()=>({args:e,value:t(`circuit`)}),template:`<CpSelectField v-bind="args" v-model="value" />`})},d={play:async({canvasElement:e})=>{s(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},f={play:async({canvasElement:e})=>{await c.click(e.querySelector(`summary`)),s(e.querySelector(`details`)).toHaveAttribute(`open`)}},p={play:async({canvasElement:e})=>{await c.click(e.querySelector(`summary`)),await c.click(l(e).getByRole(`option`,{name:`Mobility`})),s(e.querySelector(`details`)).not.toHaveAttribute(`open`),s(e.querySelector(`summary`)).toHaveTextContent(`Mobility`)}},m={play:async({canvasElement:e})=>{await c.click(e.querySelector(`summary`)),await c.keyboard(`{Escape}`),s(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},h={args:{compact:!0}},g=[`Closed`,`Open`,`ChoosingClosesThePanel`,`EscapeClosesThePanel`,`Compact`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...d.parameters?.docs?.source},description:{story:`The panel is shut until the button is used.`,...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    expect(canvasElement.querySelector('details')).toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.click(within(canvasElement).getByRole('option', {
      name: 'Mobility'
    }));
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
    expect(canvasElement.querySelector('summary')).toHaveTextContent('Mobility');
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...h.parameters?.docs?.source}}}})))()}_();export{p as ChoosingClosesThePanel,d as Closed,h as Compact,m as EscapeClosesThePanel,f as Open,g as __namedExportsOrder,u as default};
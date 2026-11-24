import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{D as r,Qt as i,n as a}from"./iframe-BbXyDo7t.js";import{i as o,r as s}from"./sizes-D73DCVJ0.js";var c;function l(){return(l=e((()=>{c="`CpSelectField`: one value from a list. A button shows the current value and opens its panel only while it is used; the options look like the multi-select field's, without the checkboxes, and the chosen one is bold with a check. Choosing an option, Escape or a click elsewhere closes the panel. With `compact` it is one control of a filter bar: its label is hidden (still read aloud, and its first option usually says it too) and it is only as wide as its longest option.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSelectField>` | `div.cp-fieldset`, a `span.cp-type-label` and a `details` with a `summary.cp-btn.cp-btn-outline` and a `cp-card` `ul[role=listbox]` of option `button`s (`flex w-full items-center gap-3 rounded-field px-2 py-1 hover:bg-base-200`); `cp-btn-sm w-auto` and a `sr-only` label with `compact` |\n| `<CpSelectField size=\"sm\">` | `cp-btn-sm` on the `summary`, as in a filter bar; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n"})))()}var u,d,f,p,m,h,g,_,v,y,b;function x(){return(x=e((()=>{n(),a(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Fields/SelectField`,component:r,argTypes:{size:o},args:{label:`Format`,options:[{value:`circuit`,text:`Circuit`},{value:`strength`,text:`Strength`},{value:`mobility`,text:`Mobility`}]},parameters:{popover:!0,docs:{description:{component:c}},a11y:{test:`todo`}},render:e=>({components:{CpSelectField:r},setup:()=>({args:e,value:t(`circuit`)}),template:`<CpSelectField v-bind="args" v-model="value" />`})},m={play:async({canvasElement:e})=>{u(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},h={play:async({canvasElement:e})=>{await d.click(e.querySelector(`summary`)),u(e.querySelector(`details`)).toHaveAttribute(`open`)}},g={play:async({canvasElement:e})=>{await d.click(e.querySelector(`summary`)),await d.click(f(e).getByRole(`option`,{name:`Mobility`})),u(e.querySelector(`details`)).not.toHaveAttribute(`open`),u(e.querySelector(`summary`)).toHaveTextContent(`Mobility`)}},_={play:async({canvasElement:e})=>{await d.click(e.querySelector(`summary`)),await d.keyboard(`{Escape}`),u(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},v={args:{compact:!0}},y={render:e=>({components:{CpSelectField:r},setup:()=>({args:e,cpSizes:i,value:t(`circuit`)}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSelectField v-bind="args" :size="s" v-model="value" /></div></div>`})},b=[`Closed`,`Open`,`ChoosingClosesThePanel`,`EscapeClosesThePanel`,`Compact`,`Sizes`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source},description:{story:`The panel is shut until the button is used.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    expect(canvasElement.querySelector('details')).toHaveAttribute('open');
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
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
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpSelectField
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref('circuit')
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSelectField v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...y.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...y.parameters?.docs?.description}}}})))()}x();export{g as ChoosingClosesThePanel,m as Closed,v as Compact,_ as EscapeClosesThePanel,h as Open,y as Sizes,b as __namedExportsOrder,p as default};
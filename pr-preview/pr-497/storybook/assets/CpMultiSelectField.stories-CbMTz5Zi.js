import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{Mt as r,en as i,i as a}from"./iframe-DGxr61L9.js";import{i as o,r as s}from"./sizes-21k_CWz1.js";var c;function l(){return(l=e((()=>{c='`CpMultiSelectField`: any number of values from a long list. It is a button that opens its panel only while it is used: a search box narrows the list and each match is a checkbox. The button says how many are picked; Escape or a click elsewhere closes the panel.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpMultiSelectField>` | `details` with a `summary.cp-btn.cp-btn-sm.cp-btn-outline` and a `cp-card` panel of `input.cp-input.cp-input-sm` and a list of `cp-checkbox cp-checkbox-sm` |\n| `<CpMultiSelectField size="sm">` | `cp-btn-sm` on the `summary` and `cp-input-sm` on its search box; `size` is the websites\' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n'})))()}var u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),a(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Fields/MultiSelectField`,parameters:{popover:!0,docs:{description:{component:c}}},component:r,argTypes:{size:o},args:{label:`Muscles`,placeholder:`Any muscle`,options:[{value:`quadriceps`,text:`Quadriceps`},{value:`hamstrings`,text:`Hamstrings`},{value:`glutes`,text:`Glutes`},{value:`calves`,text:`Calves`}],noneLabel:`No muscle matches`},render:e=>({components:{CpMultiSelectField:r},setup:()=>({args:e,picked:t([`quadriceps`])}),template:`<CpMultiSelectField v-bind="args" v-model="picked" />`})},m={play:async({canvasElement:e})=>{u(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},h={play:async({canvasElement:e})=>{await d.click(e.querySelector(`summary`)),u(e.querySelector(`details`)).toHaveAttribute(`open`)}},g={play:async({canvasElement:e})=>{await d.click(e.querySelector(`summary`)),await d.type(f(e).getByRole(`searchbox`),`zzz`),u(f(e).getByText(`No muscle matches`)).toBeVisible()}},_={play:async({canvasElement:e})=>{await d.click(e.querySelector(`summary`)),await d.keyboard(`{Escape}`),u(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},v={render:e=>({components:{CpMultiSelectField:r},setup:()=>({args:e,cpSizes:i,value:t([])}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpMultiSelectField v-bind="args" :size="s" v-model="value" /></div></div>`})},y=[`Closed`,`Open`,`NothingMatches`,`EscapeClosesThePanel`,`Sizes`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
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
    await userEvent.type(within(canvasElement).getByRole('searchbox'), 'zzz');
    expect(within(canvasElement).getByText('No muscle matches')).toBeVisible();
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
  render: args => ({
    components: {
      CpMultiSelectField
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref([])
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpMultiSelectField v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...v.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...v.parameters?.docs?.description}}}})))()}b();export{m as Closed,_ as EscapeClosesThePanel,g as NothingMatches,h as Open,v as Sizes,y as __namedExportsOrder,p as default};
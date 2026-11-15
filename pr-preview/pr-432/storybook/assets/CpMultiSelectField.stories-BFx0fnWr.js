import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-C5FEPL1F.js";import{V as r,n as i}from"./iframe-B-_ctaQ9.js";var a;function o(){return(o=e((()=>{a="`CpMultiSelectField`: any number of values from a long list. It is a button that opens its panel only while it is used: a search box narrows the list and each match is a checkbox. The button says how many are picked; Escape or a click elsewhere closes the panel.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpMultiSelectField>` | `details` with a `summary.cp-btn.cp-btn-sm.cp-btn-outline` and a `cp-card` panel of `input.cp-input.cp-input-sm` and a list of `cp-checkbox cp-checkbox-sm` |\n"})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/MultiSelectField`,parameters:{popover:!0,docs:{description:{component:a}}},component:r,args:{label:`Muscles`,placeholder:`Any muscle`,options:[{value:`quadriceps`,text:`Quadriceps`},{value:`hamstrings`,text:`Hamstrings`},{value:`glutes`,text:`Glutes`},{value:`calves`,text:`Calves`}],noneLabel:`No muscle matches`},render:e=>({components:{CpMultiSelectField:r},setup:()=>({args:e,picked:t([`quadriceps`])}),template:`<CpMultiSelectField v-bind="args" v-model="picked" />`})},d={play:async({canvasElement:e})=>{s(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},f={play:async({canvasElement:e})=>{await c.click(e.querySelector(`summary`)),s(e.querySelector(`details`)).toHaveAttribute(`open`)}},p={play:async({canvasElement:e})=>{await c.click(e.querySelector(`summary`)),await c.type(l(e).getByRole(`searchbox`),`zzz`),s(l(e).getByText(`No muscle matches`)).toBeVisible()}},m={play:async({canvasElement:e})=>{await c.click(e.querySelector(`summary`)),await c.keyboard(`{Escape}`),s(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},h=[`Closed`,`Open`,`NothingMatches`,`EscapeClosesThePanel`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
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
    await userEvent.type(within(canvasElement).getByRole('searchbox'), 'zzz');
    expect(within(canvasElement).getByText('No muscle matches')).toBeVisible();
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{d as Closed,m as EscapeClosesThePanel,p as NothingMatches,f as Open,h as __namedExportsOrder,u as default};
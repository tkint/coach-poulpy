import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{B as t,a as n}from"./iframe-sFxG7Zxs.js";import{I as r,t as i}from"./src-Bujm5Dv6.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/MultiSelectField`,component:r,args:{label:`Muscles`,placeholder:`Any muscle`,options:[{value:`quadriceps`,text:`Quadriceps`},{value:`hamstrings`,text:`Hamstrings`},{value:`glutes`,text:`Glutes`},{value:`calves`,text:`Calves`}],noneLabel:`No muscle matches`},render:e=>({components:{CpMultiSelectField:r},setup:()=>({args:e,picked:t([`quadriceps`])}),template:`<CpMultiSelectField v-bind="args" v-model="picked" />`})},l={play:async({canvasElement:e})=>{a(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},u={play:async({canvasElement:e})=>{await o.click(e.querySelector(`summary`)),a(e.querySelector(`details`)).toHaveAttribute(`open`)}},d={play:async({canvasElement:e})=>{await o.click(e.querySelector(`summary`)),await o.type(s(e).getByRole(`searchbox`),`zzz`),a(s(e).getByText(`No muscle matches`)).toBeVisible()}},f={play:async({canvasElement:e})=>{await o.click(e.querySelector(`summary`)),await o.keyboard(`{Escape}`),a(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},p=[`Closed`,`Open`,`NothingMatches`,`EscapeClosesThePanel`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...l.parameters?.docs?.source},description:{story:`The panel is shut until the button is used.`,...l.parameters?.docs?.description}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    expect(canvasElement.querySelector('details')).toHaveAttribute('open');
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.type(within(canvasElement).getByRole('searchbox'), 'zzz');
    expect(within(canvasElement).getByText('No muscle matches')).toBeVisible();
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{l as Closed,f as EscapeClosesThePanel,d as NothingMatches,u as Open,p as __namedExportsOrder,c as default};
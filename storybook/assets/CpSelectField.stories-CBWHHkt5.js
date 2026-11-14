import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{B as t,a as n}from"./iframe-sFxG7Zxs.js";import{S as r,t as i}from"./src-Bujm5Dv6.js";var a,o,s,c,l,u,d,f,p,m;function h(){return(h=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/SelectField`,component:r,args:{label:`Format`,options:[{value:`circuit`,text:`Circuit`},{value:`strength`,text:`Strength`},{value:`mobility`,text:`Mobility`}]},parameters:{a11y:{test:`todo`}},render:e=>({components:{CpSelectField:r},setup:()=>({args:e,value:t(`circuit`)}),template:`<CpSelectField v-bind="args" v-model="value" />`})},l={play:async({canvasElement:e})=>{a(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},u={play:async({canvasElement:e})=>{await o.click(e.querySelector(`summary`)),a(e.querySelector(`details`)).toHaveAttribute(`open`)}},d={play:async({canvasElement:e})=>{await o.click(e.querySelector(`summary`)),await o.click(s(e).getByRole(`option`,{name:`Mobility`})),a(e.querySelector(`details`)).not.toHaveAttribute(`open`),a(e.querySelector(`summary`)).toHaveTextContent(`Mobility`)}},f={play:async({canvasElement:e})=>{await o.click(e.querySelector(`summary`)),await o.keyboard(`{Escape}`),a(e.querySelector(`details`)).not.toHaveAttribute(`open`)}},p={args:{compact:!0}},m=[`Closed`,`Open`,`ChoosingClosesThePanel`,`EscapeClosesThePanel`,`Compact`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
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
    await userEvent.click(within(canvasElement).getByRole('option', {
      name: 'Mobility'
    }));
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
    expect(canvasElement.querySelector('summary')).toHaveTextContent('Mobility');
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(canvasElement.querySelector('summary')!);
    await userEvent.keyboard('{Escape}');
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open');
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...p.parameters?.docs?.source}}}})))()}h();export{d as ChoosingClosesThePanel,l as Closed,p as Compact,f as EscapeClosesThePanel,u as Open,m as __namedExportsOrder,c as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{L as t,a as n}from"./iframe-BSXg-Hva.js";import{J as r,t as i}from"./src-DRMNSWcR.js";var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Fields/Chip`,component:r,render:e=>({components:{CpChip:r},setup:()=>({args:e,on:t(!!e.modelValue)}),template:`<CpChip v-bind="args" v-model="on">Legs</CpChip>`})},l={play:async({canvasElement:e})=>{a(s(e).getByRole(`button`)).toHaveAttribute(`aria-pressed`,`false`)}},u={args:{modelValue:!0}},d={args:{compact:!0}},f={play:async({canvasElement:e})=>{let t=s(e).getByRole(`button`);await o.click(t),a(t).toHaveAttribute(`aria-pressed`,`true`)}},p=[`Off`,`On`,`Compact`,`Toggles`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button')).toHaveAttribute('aria-pressed', 'false');
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: true
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const chip = within(canvasElement).getByRole('button');
    await userEvent.click(chip);
    expect(chip).toHaveAttribute('aria-pressed', 'true');
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{d as Compact,l as Off,u as On,f as Toggles,p as __namedExportsOrder,c as default};
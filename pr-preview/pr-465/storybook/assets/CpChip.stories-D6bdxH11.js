import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{Vt as r,i}from"./iframe-Cutyr3Yn.js";var a;function o(){return(o=e((()=>{a="`CpChip`, a toggle with `aria-pressed`. With `compact` it is as high as the small controls of a filter bar.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpChip v-model>` | `cp-btn cp-chip cp-btn-primary` when on, `cp-btn cp-chip cp-btn-outline` when off, with `aria-pressed`; `cp-chip-compact` with `compact` |\n"})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Fields/Chip`,parameters:{docs:{description:{component:a}}},component:r,render:e=>({components:{CpChip:r},setup:()=>({args:e,on:t(!!e.modelValue)}),template:`<CpChip v-bind="args" v-model="on">Legs</CpChip>`})},d={play:async({canvasElement:e})=>{s(l(e).getByRole(`button`)).toHaveAttribute(`aria-pressed`,`false`)}},f={args:{modelValue:!0}},p={args:{compact:!0}},m={play:async({canvasElement:e})=>{let t=l(e).getByRole(`button`);await c.click(t),s(t).toHaveAttribute(`aria-pressed`,`true`)}},h=[`Off`,`On`,`Compact`,`Toggles`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button')).toHaveAttribute('aria-pressed', 'false');
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    modelValue: true
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    compact: true
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const chip = within(canvasElement).getByRole('button');
    await userEvent.click(chip);
    expect(chip).toHaveAttribute('aria-pressed', 'true');
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{p as Compact,d as Off,f as On,m as Toggles,h as __namedExportsOrder,u as default};
import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-C5FEPL1F.js";import{T as r,n as i}from"./iframe-CfcALw2D.js";var a;function o(){return(o=e((()=>{a="`CpSegmentedControl`: a few values side by side, one of them on, such as the language an exercise's text is written in. An option can be flagged with a crimson alert icon, read aloud as its `flagLabel`, such as a language some text still lacks.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSegmentedControl>` | `div.cp-join[role=group]` of `button.cp-join-item.cp-btn` (`cp-btn-primary` for the picked one, `cp-btn-outline` for the others, `aria-current`), with the alert icon (`svg.size-4.text-error`) after a flagged option's text |\n"})))()}var s,c,l,u,d,f,p,m,h;function g(){return(g=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u=[{value:`en`,text:`English`},{value:`fr`,text:`Français`}],d={title:`Web/Components/Fields/SegmentedControl`,component:r,parameters:{docs:{description:{component:a}}},args:{label:`Language`,options:u},render:e=>({components:{CpSegmentedControl:r},setup:()=>({args:e,value:t(`en`)}),template:`<CpSegmentedControl v-bind="args" v-model="value" />`})},f={play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`English`})).toHaveAttribute(`aria-current`,`true`)}},p={args:{options:[u[0],{...u[1],flagged:!0,flagLabel:`Text missing`}]},play:async({canvasElement:e})=>{s(l(e).getByRole(`button`,{name:`Français Text missing`})).toBeInTheDocument()}},m={play:async({canvasElement:e})=>{let t=l(e).getByRole(`button`,{name:`Français`});await c.click(t),s(t).toHaveAttribute(`aria-current`,`true`)}},h=[`Default`,`WithAFlaggedOption`,`Switching`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'English'
    })).toHaveAttribute('aria-current', 'true');
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    options: [options[0], {
      ...options[1],
      flagged: true,
      flagLabel: 'Text missing'
    }]
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'Français Text missing'
    })).toBeInTheDocument();
  }
}`,...p.parameters?.docs?.source},description:{story:`A language some text still lacks carries a crimson icon, read aloud as its flag label.`,...p.parameters?.docs?.description}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const french = within(canvasElement).getByRole('button', {
      name: 'Français'
    });
    await userEvent.click(french);
    expect(french).toHaveAttribute('aria-current', 'true');
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{f as Default,m as Switching,p as WithAFlaggedOption,h as __namedExportsOrder,d as default};
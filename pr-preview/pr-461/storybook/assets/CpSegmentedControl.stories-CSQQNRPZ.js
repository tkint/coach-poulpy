import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{Tn as r,i,j as a}from"./iframe-C_9sKtzQ.js";import{i as o,r as s}from"./sizes-aF3Gw5c_.js";var c;function l(){return(l=e((()=>{c="`CpSegmentedControl`: a few values side by side, one of them on, such as the language an exercise's text is written in. An option can be flagged with a crimson alert icon, read aloud as its `flagLabel`, such as a language some text still lacks.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpSegmentedControl>` | `div.cp-join[role=group]` of `button.cp-join-item.cp-btn` (`cp-btn-primary` for the picked one, `cp-btn-outline` for the others, `aria-current`), with the alert icon (`svg.size-4.text-error`) after a flagged option's text |\n| `<CpSegmentedControl size=\"sm\">` | `cp-btn-sm` on every button; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n"})))()}var u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),i(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p=[{value:`en`,text:`English`},{value:`fr`,text:`Français`}],m={title:`Web/Components/Fields/SegmentedControl`,component:a,argTypes:{size:o},parameters:{docs:{description:{component:c}}},args:{label:`Language`,options:p},render:e=>({components:{CpSegmentedControl:a},setup:()=>({args:e,value:t(`en`)}),template:`<CpSegmentedControl v-bind="args" v-model="value" />`})},h={play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`English`})).toHaveAttribute(`aria-current`,`true`)}},g={args:{options:[p[0],{...p[1],flagged:!0,flagLabel:`Text missing`}]},play:async({canvasElement:e})=>{u(f(e).getByRole(`button`,{name:`Français Text missing`})).toBeInTheDocument()}},_={play:async({canvasElement:e})=>{let t=f(e).getByRole(`button`,{name:`Français`});await d.click(t),u(t).toHaveAttribute(`aria-current`,`true`)}},v={render:e=>({components:{CpSegmentedControl:a},setup:()=>({args:e,cpSizes:r,value:t(``)}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSegmentedControl v-bind="args" :size="s" v-model="value" /></div></div>`})},y=[`Default`,`WithAFlaggedOption`,`Switching`,`Sizes`],h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('button', {
      name: 'English'
    })).toHaveAttribute('aria-current', 'true');
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
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
}`,...g.parameters?.docs?.source},description:{story:`A language some text still lacks carries a crimson icon, read aloud as its flag label.`,...g.parameters?.docs?.description}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const french = within(canvasElement).getByRole('button', {
      name: 'Français'
    });
    await userEvent.click(french);
    expect(french).toHaveAttribute('aria-current', 'true');
  }
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpSegmentedControl
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref('')
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSegmentedControl v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...v.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...v.parameters?.docs?.description}}}})))()}b();export{h as Default,v as Sizes,_ as Switching,g as WithAFlaggedOption,y as __namedExportsOrder,m as default};
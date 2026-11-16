import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./dumbbell-DuqGz8h_.js";import{On as r,en as i,i as a}from"./iframe-BRhb8NdU.js";import{i as o,n as s,r as c}from"./sizes-Df36x-jN.js";var l;function u(){return(u=e((()=>{l='`CpIcon` draws a Lucide icon at one of the scale\'s sizes: xs 12, sm 16, md 20, lg 24, xl 32, xxl 40. Pass the Lucide component itself (`<CpIcon :icon="Search" size="sm" />`); without a `size` it is md. It is always decorative (`aria-hidden`): a control that has only an icon carries its own label. The sizes are the `--cp-icon-*` tokens, generated from `design/tokens.json`.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpIcon :icon="Search">` | the Lucide `svg` with `shrink-0 cp-icon-md` |\n| `<CpIcon :icon="Search" size="xl">` | the same with `cp-icon-xl` (`cp-icon-xs`, `-sm`, `-md`, `-lg`, `-xl`, `-xxl`) |\n'})))()}var d,f,p,m,h;function g(){return(g=e((()=>{t(),a(),c(),u(),{expect:d}=__STORYBOOK_MODULE_TEST__,f={title:`Web/Components/Actions/Icon`,parameters:{docs:{description:{component:l}}},component:r,argTypes:{size:o},args:{icon:n}},p={play:async({canvasElement:e})=>{d(Math.round(e.querySelector(`svg`).getBoundingClientRect().width)).toBe(s.md)}},m={render:e=>({components:{CpIcon:r},setup:()=>({args:e,cpSizes:i}),template:`<div class="flex items-end gap-4"><div v-for="s in cpSizes" :key="s" class="flex flex-col items-center gap-1"><CpIcon v-bind="args" :size="s" /><span class="cp-type-label">{{ s }}</span></div></div>`}),play:async({canvasElement:e})=>{let t=[...e.querySelectorAll(`svg`)].map(e=>Math.round(e.getBoundingClientRect().width));d(t).toEqual(Object.values(s))}},h=[`Default`,`Sizes`],p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(Math.round(canvasElement.querySelector('svg')!.getBoundingClientRect().width)).toBe(iconPx.md);
  }
}`,...p.parameters?.docs?.source},description:{story:"Without a `size` an icon is md, 20 wide.",...p.parameters?.docs?.description}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpIcon
    },
    setup: () => ({
      args,
      cpSizes
    }),
    template: '<div class="flex items-end gap-4"><div v-for="s in cpSizes" :key="s" class="flex flex-col items-center gap-1"><CpIcon v-bind="args" :size="s" /><span class="cp-type-label">{{ s }}</span></div></div>'
  }),
  play: async ({
    canvasElement
  }) => {
    const widths = [...canvasElement.querySelectorAll('svg')].map(svg => Math.round(svg.getBoundingClientRect().width));
    expect(widths).toEqual(Object.values(iconPx));
  }
}`,...m.parameters?.docs?.source},description:{story:`Every step of the size scale, xs to xxl.`,...m.parameters?.docs?.description}}}})))()}g();export{p as Default,m as Sizes,h as __namedExportsOrder,f as default};
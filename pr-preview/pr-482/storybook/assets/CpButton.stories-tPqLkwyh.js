import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{Qt as t,en as n,i as r}from"./iframe-Dkekoaoh.js";import{i,r as a,t as o}from"./sizes-Drm4cudS.js";var s;function c(){return(c=e((()=>{s='Primary, tonal, outline, ghost (a quieter cancel beside a save), grumpy and danger (crimson, for a deletion that can\'t be undone): `CpButton`. `size` is the websites\' scale (xs, sm, md, lg, xl, xxl), md when none is given: a screen\'s main action is `size="xl"` (56 high), a card\'s actions are md (40).\n\n| Vue | Classes |\n| --- | --- |\n| `<CpButton>` | `cp-btn cp-btn-md cp-btn-primary` |\n| `<CpButton variant="tonal">` | `cp-btn cp-btn-md cp-btn-secondary` |\n| `<CpButton variant="outline">` | `cp-btn cp-btn-md cp-btn-outline` |\n| `<CpButton variant="ghost">` | `cp-btn cp-btn-md cp-btn-ghost` |\n| `<CpButton variant="grumpy">` | `cp-btn cp-btn-md cp-btn-accent` |\n| `<CpButton variant="danger">` | `cp-btn cp-btn-md cp-btn-error` |\n| `<CpButton size="xl">` | `cp-btn-xl` in place of `cp-btn-md` (`cp-btn-xs`, `-sm`, `-lg` and `-xxl` the same way) |\n\n`loading` is for a button that waits for an answer, such as Save or Publish: it is disabled, keeps its width, and shows a spinner over its label (`cp-loading cp-loading-spinner`).\n'})))()}var l,u,d,f,p,m,h,g,_,v,y,b,x;function S(){return(S=e((()=>{r(),a(),c(),{expect:l,within:u}=__STORYBOOK_MODULE_TEST__,d={title:`Web/Components/Actions/Button`,parameters:{docs:{description:{component:s}}},component:t,argTypes:{size:i},render:e=>({components:{CpButton:t},setup:()=>({args:e}),template:`<CpButton v-bind="args">Start the session</CpButton>`})},f={args:{variant:`primary`,size:`xl`}},p={args:{variant:`tonal`,size:`xl`}},m={args:{variant:`outline`,size:`xl`}},h={args:{variant:`ghost`,size:`xl`}},g={args:{variant:`grumpy`,size:`xl`}},_={args:{variant:`danger`,size:`xl`}},v={args:{variant:`primary`,size:`md`,loading:!0},play:async({canvasElement:e})=>{let t=u(e).getByRole(`button`);l(t).toBeDisabled(),l(t).toHaveAttribute(`aria-busy`,`true`)}},y={render:e=>({components:{CpButton:t},setup:()=>({args:e,cpSizes:n}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpButton v-bind="args" :size="s">Start</CpButton></div></div>`}),play:async({canvasElement:e})=>{let t=u(e).getAllByRole(`button`).map(e=>Math.round(e.getBoundingClientRect().height));l(t).toEqual(Object.values(o))}},b={play:async({canvasElement:e})=>{l(Math.round(u(e).getByRole(`button`).getBoundingClientRect().height)).toBe(o.md)}},x=[`Primary`,`Tonal`,`Outline`,`Ghost`,`Grumpy`,`Danger`,`Loading`,`Sizes`,`DefaultsToMedium`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'primary',
    size: 'xl'
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'tonal',
    size: 'xl'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'outline',
    size: 'xl'
  }
}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'ghost',
    size: 'xl'
  }
}`,...h.parameters?.docs?.source},description:{story:`A quieter cancel beside a save.`,...h.parameters?.docs?.description}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'grumpy',
    size: 'xl'
  }
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'danger',
    size: 'xl'
  }
}`,..._.parameters?.docs?.source},description:{story:`Crimson, for a deletion that cannot be undone.`,..._.parameters?.docs?.description}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'primary',
    size: 'md',
    loading: true
  },
  play: async ({
    canvasElement
  }) => {
    const button = within(canvasElement).getByRole('button');
    expect(button).toBeDisabled();
    expect(button).toHaveAttribute('aria-busy', 'true');
  }
}`,...v.parameters?.docs?.source},description:{story:`Waiting for an answer: the label stays in place under a spinner, and clicks are ignored.`,...v.parameters?.docs?.description}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpButton
    },
    setup: () => ({
      args,
      cpSizes
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpButton v-bind="args" :size="s">Start</CpButton></div></div>'
  }),
  play: async ({
    canvasElement
  }) => {
    const heights = within(canvasElement).getAllByRole('button').map(button => Math.round(button.getBoundingClientRect().height));
    expect(heights).toEqual(Object.values(controlPx));
  }
}`,...y.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a button without a `size` draws.",...y.parameters?.docs?.description}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(Math.round(within(canvasElement).getByRole('button').getBoundingClientRect().height)).toBe(controlPx.md);
  }
}`,...b.parameters?.docs?.source},description:{story:"Without a `size` a button is md.",...b.parameters?.docs?.description}}}})))()}S();export{_ as Danger,b as DefaultsToMedium,h as Ghost,g as Grumpy,v as Loading,m as Outline,f as Primary,y as Sizes,p as Tonal,x as __namedExportsOrder,d as default};
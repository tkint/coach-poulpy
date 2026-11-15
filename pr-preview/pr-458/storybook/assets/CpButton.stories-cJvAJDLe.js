import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{Cn as t,i as n}from"./iframe-Bl15jW7x.js";var r;function i(){return(i=e((()=>{r='Primary, tonal, outline, ghost (a quieter cancel beside a save), grumpy and danger (crimson, for a deletion that can\'t be undone): `CpButton`. The large size is the default; `size="md"` drops `cp-btn-xl` for the actions of a card.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpButton>` | `cp-btn cp-btn-xl cp-btn-primary` |\n| `<CpButton variant="tonal">` | `cp-btn cp-btn-xl cp-btn-secondary` |\n| `<CpButton variant="outline">` | `cp-btn cp-btn-xl cp-btn-outline` |\n| `<CpButton variant="ghost">` | `cp-btn cp-btn-xl cp-btn-ghost` |\n| `<CpButton variant="grumpy">` | `cp-btn cp-btn-xl cp-btn-accent` |\n| `<CpButton variant="danger">` | `cp-btn cp-btn-xl cp-btn-error` |\n| `<CpButton size="md">` | the same without `cp-btn-xl` |\n\n`loading` is for a button that waits for an answer, such as Save or Publish: it is disabled, keeps its width, and shows a spinner over its label (`cp-loading cp-loading-spinner`).\n'})))()}var a,o,s,c,l,u,d,f,p,m,h,g,_;function v(){return(v=e((()=>{n(),i(),{expect:a,userEvent:o,within:s}=__STORYBOOK_MODULE_TEST__,c={title:`Web/Components/Actions/Button`,parameters:{docs:{description:{component:r}}},component:t,render:e=>({components:{CpButton:t},setup:()=>({args:e}),template:`<CpButton v-bind="args">Start the session</CpButton>`})},l={args:{variant:`primary`,size:`xl`}},u={args:{variant:`tonal`,size:`xl`}},d={args:{variant:`outline`,size:`xl`}},f={args:{variant:`ghost`,size:`xl`}},p={args:{variant:`grumpy`,size:`xl`}},m={args:{variant:`danger`,size:`xl`}},h={args:{variant:`primary`,size:`md`}},g={args:{variant:`primary`,size:`md`,loading:!0},play:async({canvasElement:e})=>{let t=s(e).getByRole(`button`);a(t).toBeDisabled(),a(t).toHaveAttribute(`aria-busy`,`true`)}},_=[`Primary`,`Tonal`,`Outline`,`Ghost`,`Grumpy`,`Danger`,`Medium`,`Loading`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'primary',
    size: 'xl'
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'tonal',
    size: 'xl'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'outline',
    size: 'xl'
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'ghost',
    size: 'xl'
  }
}`,...f.parameters?.docs?.source},description:{story:`A quieter cancel beside a save.`,...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'grumpy',
    size: 'xl'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'danger',
    size: 'xl'
  }
}`,...m.parameters?.docs?.source},description:{story:`Crimson, for a deletion that cannot be undone.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    variant: 'primary',
    size: 'md'
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
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
}`,...g.parameters?.docs?.source},description:{story:`Waiting for an answer: the label stays in place under a spinner, and clicks are ignored.`,...g.parameters?.docs?.description}}}})))()}v();export{m as Danger,f as Ghost,p as Grumpy,g as Loading,h as Medium,d as Outline,l as Primary,u as Tonal,_ as __namedExportsOrder,c as default};
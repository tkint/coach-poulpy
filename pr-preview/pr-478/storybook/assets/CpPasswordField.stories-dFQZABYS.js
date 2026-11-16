import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{Et as r,en as i,i as a}from"./iframe-DuK-y347.js";import{i as o,r as s}from"./sizes-DpQNlzcN.js";var c;function l(){return(l=e((()=>{c='`CpPasswordField`, with a button that shows the password. In error, it turns crimson and a `CpErrorNote` under the form says why.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPasswordField>` | `div.cp-fieldset` around a `label.cp-type-label`, `label.cp-input` (`cp-input-error` in error) holding the `input` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm`, and `p.cp-label.whitespace-normal` for the hint |\n| `<CpPasswordField size="lg">` | `cp-input-lg` on the field, and the eye button and its icon follow it; `size` is the websites\' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n'})))()}var u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),a(),s(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Fields/PasswordField`,parameters:{docs:{description:{component:c}}},component:r,argTypes:{size:o},args:{label:`Password`,showLabel:`Show the password`,hideLabel:`Hide the password`,autocomplete:`new-password`},render:e=>({components:{CpPasswordField:r},setup:()=>({args:e,value:t(``)}),template:`<CpPasswordField v-bind="args" v-model="value" />`})},m={play:async({canvasElement:e})=>{u(f(e).getByLabelText(`Password`,{selector:`input`})).toHaveAttribute(`type`,`password`)}},h={play:async({canvasElement:e})=>{await d.click(f(e).getByRole(`button`,{name:`Show the password`})),u(f(e).getByLabelText(`Password`,{selector:`input`})).toHaveAttribute(`type`,`text`)}},g={parameters:{a11y:{test:`todo`}},args:{hint:`At least 8 characters.`}},_={parameters:{a11y:{test:`todo`}},args:{invalid:!0,hint:`This password is too short.`}},v={render:e=>({components:{CpPasswordField:r},setup:()=>({args:e,cpSizes:i,value:t(``)}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpPasswordField v-bind="args" :size="s" v-model="value" /></div></div>`})},y=[`Hidden`,`Shown`,`WithAHint`,`Invalid`,`Sizes`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Password', {
      selector: 'input'
    })).toHaveAttribute('type', 'password');
  }
}`,...m.parameters?.docs?.source},description:{story:`The password is hidden on load.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    await userEvent.click(within(canvasElement).getByRole('button', {
      name: 'Show the password'
    }));
    expect(within(canvasElement).getByLabelText('Password', {
      selector: 'input'
    })).toHaveAttribute('type', 'text');
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'At least 8 characters.'
  }
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    invalid: true,
    hint: 'This password is too short.'
  }
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpPasswordField
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref('')
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpPasswordField v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...v.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...v.parameters?.docs?.description}}}})))()}b();export{m as Hidden,_ as Invalid,h as Shown,v as Sizes,g as WithAHint,y as __namedExportsOrder,p as default};
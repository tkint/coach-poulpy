import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-DILzM0VL.js";import{ft as r,i,t as a}from"./src-D_jBWipc.js";import{n as o,t as s}from"./lock-DoGfCqVP.js";var c;function l(){return(l=e((()=>{c='`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn\'t one, passes `error` instead, which takes the hint\'s place in crimson.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |\n| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input\'s `aria-describedby` |\n| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.flex.items-center.gap-1.whitespace-normal.text-error` with `role="alert"` and the alert icon (`svg.size-4`), in the hint\'s place |\n| `<CpTextField placeholder>` | a placeholder, such as "Needed before publishing" in a translation still to write |\n| `<CpTextField>` with `#icon` | an icon before the text inside `label.cp-input`, such as a lock on a slug that can\'t change |\n| `<CpTextField disabled>` | a field that can\'t be changed, such as a slug locked once its exercise is published |\n| `<CpTextField>` with `#status` | a slot under the hint for a badge, such as a field\'s translation status |\n'})))()}var u,d,f,p,m,h,g,_,v,y,b,x,S;function C(){return(C=e((()=>{n(),o(),a(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Fields/TextField`,parameters:{docs:{description:{component:c}}},component:i,args:{label:`Email`,type:`email`},render:e=>({components:{CpTextField:i},setup:()=>({args:e,value:t(``)}),template:`<CpTextField v-bind="args" v-model="value" />`})},m={},h={parameters:{a11y:{test:`todo`}},args:{hint:`We only use it to sign you in.`}},g={args:{invalid:!0,error:`This email address is not valid.`}},_={play:async({canvasElement:e})=>{let t=f(e).getByLabelText(`Email`);await d.type(t,`alex@example.com`),u(t).toHaveValue(`alex@example.com`)}},v={args:{type:`text`,label:`Name`,placeholder:`Needed before publishing`}},y={args:{type:`text`,label:`Slug`,disabled:!0}},b={args:{type:`text`,label:`Slug`,disabled:!0},render:e=>({components:{CpTextField:i,Lock:s},setup:()=>({args:e,value:t(`squat`)}),template:`<CpTextField v-bind="args" v-model="value"><template #icon><Lock class="size-4" aria-hidden="true" /></template></CpTextField>`})},x={parameters:{a11y:{test:`todo`}},args:{type:`text`,label:`Name`},render:e=>({components:{CpTextField:i,CpBadge:r},setup:()=>({args:e,value:t(`Squat`)}),template:`<CpTextField v-bind="args" v-model="value"><template #status><CpBadge tone="success" small>Translated</CpBadge></template></CpTextField>`})},S=[`Empty`,`WithAHint`,`WithAnError`,`Typing`,`WithAPlaceholder`,`Disabled`,`WithAnIcon`,`WithAStatus`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'We only use it to sign you in.'
  }
}`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  args: {
    invalid: true,
    error: 'This email address is not valid.'
  }
}`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const input = within(canvasElement).getByLabelText('Email');
    await userEvent.type(input, 'alex@example.com');
    expect(input).toHaveValue('alex@example.com');
  }
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Name',
    placeholder: 'Needed before publishing'
  }
}`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Slug',
    disabled: true
  }
}`,...y.parameters?.docs?.source}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Slug',
    disabled: true
  },
  render: args => ({
    components: {
      CpTextField,
      Lock
    },
    setup: () => ({
      args,
      value: ref('squat')
    }),
    template: '<CpTextField v-bind="args" v-model="value"><template #icon><Lock class="size-4" aria-hidden="true" /></template></CpTextField>'
  })
}`,...b.parameters?.docs?.source},description:{story:`An icon before the text, such as a lock on a slug that cannot change.`,...b.parameters?.docs?.description}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    type: 'text',
    label: 'Name'
  },
  render: args => ({
    components: {
      CpTextField,
      CpBadge
    },
    setup: () => ({
      args,
      value: ref('Squat')
    }),
    template: '<CpTextField v-bind="args" v-model="value"><template #status><CpBadge tone="success" small>Translated</CpBadge></template></CpTextField>'
  })
}`,...x.parameters?.docs?.source},description:{story:`A badge under the hint, saying where a translated text stands.`,...x.parameters?.docs?.description}}}})))()}C();export{y as Disabled,m as Empty,_ as Typing,h as WithAHint,v as WithAPlaceholder,x as WithAStatus,g as WithAnError,b as WithAnIcon,S as __namedExportsOrder,p as default};
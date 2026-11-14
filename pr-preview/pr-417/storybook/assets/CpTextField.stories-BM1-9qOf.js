import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{B as t,a as n}from"./iframe-GDyB0IGv.js";import{i as r,t as i,ut as a}from"./src-TIjE_3Dh.js";import{a as o,o as s}from"./circle-exclamation-point-U-HZxrt1.js";var c,l;function u(){return(u=e((()=>{s(),c={name:`lock`,size:24,node:[[`rect`,{width:`18`,height:`11`,x:`3`,y:`11`,rx:`2`,ry:`2`,key:`1w4ew1`}],[`path`,{d:`M7 11V7a5 5 0 0 1 10 0v4`,key:`fwvmzm`}]]},l=o(c)})))()}var d;function f(){return(f=e((()=>{d='`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn\'t one, passes `error` instead, which takes the hint\'s place in crimson.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |\n| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input\'s `aria-describedby` |\n| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.flex.items-center.gap-1.whitespace-normal.text-error` with `role="alert"` and the alert icon (`svg.size-4`), in the hint\'s place |\n| `<CpTextField placeholder>` | a placeholder, such as "Needed before publishing" in a translation still to write |\n| `<CpTextField>` with `#icon` | an icon before the text inside `label.cp-input`, such as a lock on a slug that can\'t change |\n| `<CpTextField disabled>` | a field that can\'t be changed, such as a slug locked once its exercise is published |\n| `<CpTextField>` with `#status` | a slot under the hint for a badge, such as a field\'s translation status |\n'})))()}var p,m,h,g,_,v,y,b,x,S,C,w,T;function E(){return(E=e((()=>{n(),u(),i(),f(),{expect:p,userEvent:m,within:h}=__STORYBOOK_MODULE_TEST__,g={title:`Fields/TextField`,parameters:{docs:{description:{component:d}}},component:r,args:{label:`Email`,type:`email`},render:e=>({components:{CpTextField:r},setup:()=>({args:e,value:t(``)}),template:`<CpTextField v-bind="args" v-model="value" />`})},_={},v={parameters:{a11y:{test:`todo`}},args:{hint:`We only use it to sign you in.`}},y={args:{invalid:!0,error:`This email address is not valid.`}},b={play:async({canvasElement:e})=>{let t=h(e).getByLabelText(`Email`);await m.type(t,`alex@example.com`),p(t).toHaveValue(`alex@example.com`)}},x={args:{type:`text`,label:`Name`,placeholder:`Needed before publishing`}},S={args:{type:`text`,label:`Slug`,disabled:!0}},C={args:{type:`text`,label:`Slug`,disabled:!0},render:e=>({components:{CpTextField:r,Lock:l},setup:()=>({args:e,value:t(`squat`)}),template:`<CpTextField v-bind="args" v-model="value"><template #icon><Lock class="size-4" aria-hidden="true" /></template></CpTextField>`})},w={parameters:{a11y:{test:`todo`}},args:{type:`text`,label:`Name`},render:e=>({components:{CpTextField:r,CpBadge:a},setup:()=>({args:e,value:t(`Squat`)}),template:`<CpTextField v-bind="args" v-model="value"><template #status><CpBadge tone="success" small>Translated</CpBadge></template></CpTextField>`})},T=[`Empty`,`WithAHint`,`WithAnError`,`Typing`,`WithAPlaceholder`,`Disabled`,`WithAnIcon`,`WithAStatus`],_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'We only use it to sign you in.'
  }
}`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  args: {
    invalid: true,
    error: 'This email address is not valid.'
  }
}`,...y.parameters?.docs?.source}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const input = within(canvasElement).getByLabelText('Email');
    await userEvent.type(input, 'alex@example.com');
    expect(input).toHaveValue('alex@example.com');
  }
}`,...b.parameters?.docs?.source}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Name',
    placeholder: 'Needed before publishing'
  }
}`,...x.parameters?.docs?.source}}},S.parameters={...S.parameters,docs:{...S.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Slug',
    disabled: true
  }
}`,...S.parameters?.docs?.source}}},C.parameters={...C.parameters,docs:{...C.parameters?.docs,source:{originalSource:`{
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
}`,...C.parameters?.docs?.source},description:{story:`An icon before the text, such as a lock on a slug that cannot change.`,...C.parameters?.docs?.description}}},w.parameters={...w.parameters,docs:{...w.parameters?.docs,source:{originalSource:`{
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
}`,...w.parameters?.docs?.source},description:{story:`A badge under the hint, saying where a translated text stands.`,...w.parameters?.docs?.description}}}})))()}E();export{S as Disabled,_ as Empty,b as Typing,v as WithAHint,x as WithAPlaceholder,w as WithAStatus,y as WithAnError,C as WithAnIcon,T as __namedExportsOrder,g as default};
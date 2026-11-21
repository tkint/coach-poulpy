import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{n as r,t as i}from"./lock-ZnpjnxGO.js";import{En as a,Qt as o,a as s,n as c,on as l}from"./iframe-balQP6bY.js";import{i as u,r as d}from"./sizes-DMc4ScR5.js";var f;function p(){return(p=e((()=>{f='`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn\'t one, passes `error` instead, which takes the hint\'s place in crimson.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |\n| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input\'s `aria-describedby` |\n| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.flex.items-center.gap-1.whitespace-normal.text-error` with `role="alert"` and the alert icon (`svg.size-4`), in the hint\'s place |\n| `<CpTextField placeholder>` | a placeholder, such as "Needed before publishing" in a translation still to write |\n| `<CpTextField>` with `#icon` | an icon before the text inside `label.cp-input`, such as a lock on a slug that can\'t change |\n| `<CpTextField disabled>` | a field that can\'t be changed, such as a slug locked once its exercise is published |\n| `<CpTextField>` with `#status` | a slot under the hint for a badge, such as a field\'s translation status |\n| `<CpTextField size="lg">` | `cp-input-lg` in place of `cp-input-md`; `size` is the websites\' scale (xs, sm, md, lg, xl, xxl), md when none is given |\n'})))()}var m,h,g,_,v,y,b,x,S,C,w,T,E,D;function O(){return(O=e((()=>{n(),r(),c(),d(),p(),{expect:m,userEvent:h,within:g}=__STORYBOOK_MODULE_TEST__,_={title:`Web/Components/Fields/TextField`,parameters:{docs:{description:{component:f}}},component:s,argTypes:{size:u},args:{label:`Email`,type:`email`},render:e=>({components:{CpTextField:s},setup:()=>({args:e,value:t(``)}),template:`<CpTextField v-bind="args" v-model="value" />`})},v={},y={parameters:{a11y:{test:`todo`}},args:{hint:`We only use it to sign you in.`}},b={args:{invalid:!0,error:`This email address is not valid.`}},x={play:async({canvasElement:e})=>{let t=g(e).getByLabelText(`Email`);await h.type(t,`alex@example.com`),m(t).toHaveValue(`alex@example.com`)}},S={args:{type:`text`,label:`Name`,placeholder:`Needed before publishing`}},C={args:{type:`text`,label:`Slug`,disabled:!0}},w={args:{type:`text`,label:`Slug`,disabled:!0},render:e=>({components:{CpTextField:s,CpIcon:a},setup:()=>({args:e,value:t(`squat`),Lock:i}),template:`<CpTextField v-bind="args" v-model="value"><template #icon><CpIcon :icon="Lock" size="sm" /></template></CpTextField>`})},T={parameters:{a11y:{test:`todo`}},args:{type:`text`,label:`Name`},render:e=>({components:{CpTextField:s,CpBadge:l},setup:()=>({args:e,value:t(`Squat`)}),template:`<CpTextField v-bind="args" v-model="value"><template #status><CpBadge tone="success" small>Translated</CpBadge></template></CpTextField>`})},E={render:e=>({components:{CpTextField:s},setup:()=>({args:e,cpSizes:o,value:t(``)}),template:`<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpTextField v-bind="args" :size="s" v-model="value" /></div></div>`})},D=[`Empty`,`WithAHint`,`WithAnError`,`Typing`,`WithAPlaceholder`,`Disabled`,`WithAnIcon`,`WithAStatus`,`Sizes`],v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{}`,...v.parameters?.docs?.source}}},y.parameters={...y.parameters,docs:{...y.parameters?.docs,source:{originalSource:`{
  parameters: {
    a11y: {
      test: 'todo'
    }
  },
  args: {
    hint: 'We only use it to sign you in.'
  }
}`,...y.parameters?.docs?.source}}},b.parameters={...b.parameters,docs:{...b.parameters?.docs,source:{originalSource:`{
  args: {
    invalid: true,
    error: 'This email address is not valid.'
  }
}`,...b.parameters?.docs?.source}}},x.parameters={...x.parameters,docs:{...x.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    const input = within(canvasElement).getByLabelText('Email');
    await userEvent.type(input, 'alex@example.com');
    expect(input).toHaveValue('alex@example.com');
  }
}`,...x.parameters?.docs?.source}}},S.parameters={...S.parameters,docs:{...S.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Name',
    placeholder: 'Needed before publishing'
  }
}`,...S.parameters?.docs?.source}}},C.parameters={...C.parameters,docs:{...C.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Slug',
    disabled: true
  }
}`,...C.parameters?.docs?.source}}},w.parameters={...w.parameters,docs:{...w.parameters?.docs,source:{originalSource:`{
  args: {
    type: 'text',
    label: 'Slug',
    disabled: true
  },
  render: args => ({
    components: {
      CpTextField,
      CpIcon
    },
    setup: () => ({
      args,
      value: ref('squat'),
      Lock
    }),
    template: '<CpTextField v-bind="args" v-model="value"><template #icon><CpIcon :icon="Lock" size="sm" /></template></CpTextField>'
  })
}`,...w.parameters?.docs?.source},description:{story:`An icon before the text, such as a lock on a slug that cannot change.`,...w.parameters?.docs?.description}}},T.parameters={...T.parameters,docs:{...T.parameters?.docs,source:{originalSource:`{
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
}`,...T.parameters?.docs?.source},description:{story:`A badge under the hint, saying where a translated text stands.`,...T.parameters?.docs?.description}}},E.parameters={...E.parameters,docs:{...E.parameters?.docs,source:{originalSource:`{
  render: args => ({
    components: {
      CpTextField
    },
    setup: () => ({
      args,
      cpSizes,
      value: ref('')
    }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpTextField v-bind="args" :size="s" v-model="value" /></div></div>'
  })
}`,...E.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl; md is what a component without a `size` draws.",...E.parameters?.docs?.description}}}})))()}O();export{C as Disabled,v as Empty,E as Sizes,x as Typing,y as WithAHint,S as WithAPlaceholder,T as WithAStatus,b as WithAnError,w as WithAnIcon,D as __namedExportsOrder,_ as default};
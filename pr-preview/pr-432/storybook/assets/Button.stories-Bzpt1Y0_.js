import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./story-DGKBMel4.js";var r,i,a,o,s,c,l,u,d,f;function p(){return(p=e((()=>{n(),r={title:`Mobile/Components/Button`,render:t(`MobileButton`),args:{label:`Start now`,kind:`primary`,large:!1,icon:``,tag:`a`},argTypes:{kind:{control:`inline-radio`,options:[`primary`,`secondary`,`tonal`,`small`]},tag:{control:`inline-radio`,options:[`a`,`button`]},icon:{control:`text`,description:"A Lucide icon name, such as `play`."},iconSize:{control:{type:`number`,min:16,max:24}}},parameters:{docs:{canvas:{sourceState:`none`},description:{component:'`MobileButton`. One primary button per screen; secondary and tonal for the others; the small one sits in a top bar. A link by default, `tag="button"` for an action. `style`, `id` and `disabled` go through to the element.'}}}},i={},a={args:{kind:`secondary`,label:`Edit`}},o={args:{kind:`tonal`,label:`Skip the rest`}},s={args:{kind:`small`,label:`Save`}},c={args:{icon:`play`}},l={args:{large:!0,icon:`check`,label:`Done`}},u={args:{kind:`secondary`,large:!0,tag:`button`,label:`Skip`,disabled:!0}},d={render:()=>({template:`<div class="cp-mobile"><div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div></div>`})},f=[`Primary`,`Secondary`,`Tonal`,`Small`,`WithIcon`,`Large`,`Disabled`,`Shapes`],i.parameters={...i.parameters,docs:{...i.parameters?.docs,source:{originalSource:`{}`,...i.parameters?.docs?.source}}},a.parameters={...a.parameters,docs:{...a.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'secondary',
    label: 'Edit'
  }
}`,...a.parameters?.docs?.source}}},o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'tonal',
    label: 'Skip the rest'
  }
}`,...o.parameters?.docs?.source}}},s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'small',
    label: 'Save'
  }
}`,...s.parameters?.docs?.source}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    icon: 'play'
  }
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    large: true,
    icon: 'check',
    label: 'Done'
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'secondary',
    large: true,
    tag: 'button',
    label: 'Skip',
    disabled: true
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  render: () => ({
    template: \`<div class="cp-mobile"><div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div></div>\`
  })
}`,...d.parameters?.docs?.source},description:{story:`The four shapes of the design-system sheet, as mobile.css draws them.`,...d.parameters?.docs?.description}}}})))()}p();export{u as Disabled,l as Large,i as Primary,a as Secondary,d as Shapes,s as Small,o as Tonal,c as WithIcon,f as __namedExportsOrder,r as default};
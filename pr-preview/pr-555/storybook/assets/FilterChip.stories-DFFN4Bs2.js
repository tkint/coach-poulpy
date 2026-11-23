import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./story-CY-VDLuC.js";var r,i,a,o,s,c,l;function u(){return(u=e((()=>{n(),r={title:`Mobile/Components/Filter chip`,render:t(`MobileFilterChip`),args:{label:`Sets`,ariaPressed:`false`,have:!1,small:!1},argTypes:{ariaPressed:{control:`inline-radio`,options:[`false`,`true`]},icon:{control:`text`,description:`A Lucide icon name, for a chip that opens a picker.`},count:{control:{type:`number`,min:0}}},parameters:{docs:{canvas:{sourceState:`none`},description:{component:"`MobileFilterChip`: a word that filters a list (`aria-pressed`), or an icon that opens a picker (`aria-haspopup`, `aria-label`). `have` and `count` when values are ticked; `small` in a row of presets."}}}},i={},a={args:{label:`All`,ariaPressed:`true`}},o={args:{label:``,ariaPressed:void 0,icon:`dumbbell`,ariaHaspopup:`dialog`,ariaLabel:`Equipment`,style:`padding: 0 var(--cp-space-md); min-width: 48px;`}},s={args:{label:``,ariaPressed:void 0,icon:`dumbbell`,have:!0,count:2,ariaHaspopup:`dialog`,ariaLabel:`Equipment, 2 ticked`,style:`padding: 0 var(--cp-space-md); min-width: 48px;`}},c={args:{label:`5 km`,small:!0}},l=[`Word`,`Pressed`,`Picker`,`Ticked`,`Small`],i.parameters={...i.parameters,docs:{...i.parameters?.docs,source:{originalSource:`{}`,...i.parameters?.docs?.source}}},a.parameters={...a.parameters,docs:{...a.parameters?.docs,source:{originalSource:`{
  args: {
    label: 'All',
    ariaPressed: 'true'
  }
}`,...a.parameters?.docs?.source}}},o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  args: {
    label: '',
    ariaPressed: undefined,
    icon: 'dumbbell',
    ariaHaspopup: 'dialog',
    ariaLabel: 'Equipment',
    style: 'padding: 0 var(--cp-space-md); min-width: 48px;'
  }
}`,...o.parameters?.docs?.source}}},s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  args: {
    label: '',
    ariaPressed: undefined,
    icon: 'dumbbell',
    have: true,
    count: 2,
    ariaHaspopup: 'dialog',
    ariaLabel: 'Equipment, 2 ticked',
    style: 'padding: 0 var(--cp-space-md); min-width: 48px;'
  }
}`,...s.parameters?.docs?.source}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    label: '5 km',
    small: true
  }
}`,...c.parameters?.docs?.source}}}})))()}u();export{o as Picker,a as Pressed,c as Small,s as Ticked,i as Word,l as __namedExportsOrder,r as default};
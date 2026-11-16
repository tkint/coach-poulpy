import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{C as t,r as n}from"./createLucideIcon-DV8V5Z3X.js";import{n as r,t as i}from"./screen-CvSdBhk-.js";import{n as a,t as o}from"./story-C7AjXrcP.js";var s,c,l,u,d,f,p,m,h,g;function _(){return(_=e((()=>{n(),r(),o(),s=[`xs`,`sm`,`md`,`lg`,`xl`,`xxl`],c={title:`Mobile/Components/Button`,tags:[`draft`],render:a(`MobileButton`),args:{label:`Start now`,kind:`primary`,size:`md`,icon:``},argTypes:{kind:{control:`inline-radio`,options:[`primary`,`secondary`,`tonal`,`grumpy`]},size:{control:`select`,options:[...s],description:`One of the websites' size scale: xs, sm, md, lg, xl, xxl.`,table:{defaultValue:{summary:`md`}}},icon:{control:`text`,description:"A Lucide icon name, such as `play`."},iconSize:{control:{type:`number`,min:12,max:24}}},parameters:{docs:{canvas:{sourceState:`none`},description:{component:'`MobileButton` (draft). A rounded square (10 radius, like the New workout button and the settings rows), never a pill, on the websites\' size scale: xs 24, sm 32, md 40, lg 48, xl 56, xxl 64. Every example is md; the Sizes story shows the rest. Below 44 the touch area still reaches 44. One primary button per screen; secondary (outlined) and tonal for the others; grumpy only for the catch-up action. Always a plain `type="button"`. `style`, `id` and `disabled` go through to the element.'}}}},l={},u={args:{kind:`secondary`,label:`Edit`}},d={args:{kind:`tonal`,label:`Skip the rest`}},f={args:{kind:`grumpy`,label:`Catch up`}},p={args:{icon:`play`}},m={args:{kind:`secondary`,label:`Skip`,disabled:!0}},h={render:()=>{let e=[`primary`,`tonal`,`secondary`].map(e=>`<div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">${s.map(t=>`<MobileButton kind="${e}" size="${t}">${t}</MobileButton>`).join(``)}</div>`).join(``);return t(i,{markup:`<div style="width: 390px; padding: 16px; display: flex; flex-direction: column; gap: 16px;">${e}</div>`})}},g=[`Primary`,`Secondary`,`Tonal`,`Grumpy`,`WithIcon`,`Disabled`,`Sizes`],l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'secondary',
    label: 'Edit'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'tonal',
    label: 'Skip the rest'
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'grumpy',
    label: 'Catch up'
  }
}`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    icon: 'play'
  }
}`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  args: {
    kind: 'secondary',
    label: 'Skip',
    disabled: true
  }
}`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  render: () => {
    const rows = (['primary', 'tonal', 'secondary'] as const).map(kind => \`<div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">\${sizes.map(s => \`<MobileButton kind="\${kind}" size="\${s}">\${s}</MobileButton>\`).join('')}</div>\`).join('');
    return h(Screen, {
      markup: \`<div style="width: 390px; padding: 16px; display: flex; flex-direction: column; gap: 16px;">\${rows}</div>\`
    });
  }
}`,...h.parameters?.docs?.source},description:{story:"Every step of the size scale, xs to xxl, in each kind; md is what a button without a `size` draws.",...h.parameters?.docs?.description}}}})))()}_();export{m as Disabled,f as Grumpy,l as Primary,u as Secondary,h as Sizes,d as Tonal,p as WithIcon,g as __namedExportsOrder,c as default};
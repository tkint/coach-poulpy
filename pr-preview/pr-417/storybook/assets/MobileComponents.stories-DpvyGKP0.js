import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{i as t,n,r,t as i}from"./circle-exclamation-point-7u7JZjDz.js";var a,o,s,c,l,u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),t(),a={title:`Mobile/Components`,parameters:{docs:{description:{component:"The Android and iOS components, drawn with the values of `design/tokens.json`. The shape and notes of each one are in the Components and Base fields tables of the design system. Their classes live in `stories/mobile/mobile.css`, under `.cp-mobile`, and use only the `--cp-*` tokens."}}}},o=(e,t={})=>({render:()=>({components:t,template:`<div class="cp-mobile">${e}</div>`})}),s=o(`<div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div>`),c=o(`<div class="row">
  <button type="button" class="chip on" aria-pressed="true">Legs</button>
  <button type="button" class="chip" aria-pressed="false">Core</button>
  <button type="button" class="chip" aria-pressed="false">Dumbbell</button></div>`),l=o(`<div class="card"><span class="badge" aria-hidden="true">SQ</span>
  <span class="card-text"><strong>Goblet squat</strong><span>4 × 8 at 16 kg</span></span></div>`),u=o(`<div class="stepper"><span class="eyebrow">Rest</span><div class="stepper-row">
  <button type="button" aria-label="Less rest">−</button><span class="value">90 s</span>
  <button type="button" aria-label="More rest">+</button></div></div>`),d=o(`<div class="alert" role="note"><CircleExclamationPoint :size="20" aria-hidden="true" />
  The treadmill stopped answering. Recording resumes when it's back.</div>`,{CircleExclamationPoint:i}),f=o(`<div class="sample"><label class="label" for="name">Name</label>
  <div class="box"><input id="name" value="Alex" /></div>
  <span class="note">Shown to you only, on this phone.</span></div>`),p=o(`<div class="sample">
  <button type="button" class="box select"><span><span class="label">Experience</span><strong>Getting going</strong></span><ChevronRight :size="20" aria-hidden="true" /></button>
  <button type="button" class="box select"><span><span class="label">Sex</span><strong class="muted">Not set</strong></span><ChevronRight :size="20" aria-hidden="true" /></button></div>`,{ChevronRight:r}),m=o(`<div class="sheet"><strong class="sheet-title">Pronouns</strong>
  <div class="option"><span class="radio on" aria-hidden="true"></span>They / them</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>She / her</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>Custom</div>
  <div class="box"><input value="" placeholder="Custom pronouns" aria-label="Custom pronouns" /></div>
  <button type="button" class="btn primary sm">Save</button></div>`),h=o(`<div class="sample">
  <div class="box"><input inputmode="decimal" value="64" aria-label="Resting heart rate" /><span class="unit">bpm</span></div>
  <div class="entry"><button type="button" aria-label="Less">−</button>
    <div class="box"><input inputmode="numeric" value="8" aria-label="Reps" /><span class="unit">reps</span></div>
    <button type="button" aria-label="More">+</button></div></div>`),g=o(`<div class="sample"><strong>Getting going</strong>
  <div class="slider" aria-hidden="true"><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar off"></span><span class="dot off"></span><span class="bar off"></span><span class="dot off"></span></div>
  <div class="ends"><span>Just starting</span><span>Very experienced</span></div></div>`),_={render:()=>({setup:()=>({grid:[0,25,50,75,100],points:`8,62 60,56 112,58 164,44 216,40 268,30`}),template:`<div class="cp-mobile"><div class="sample">
      <svg viewBox="0 0 290 100" class="chart" role="img" aria-label="Weight from 72 kg to 70 kg over three months">
        <text x="0" y="8" class="tick">kg</text>
        <g v-for="line in grid" :key="line">
          <line :x1="28" :x2="290" :y1="12 + line * 0.64" :y2="12 + line * 0.64" class="grid" />
          <text x="0" :y="16 + line * 0.64" class="tick">{{ 74 - line * 0.06 }}</text>
        </g>
        <polyline :points="points" class="line" transform="translate(28 0)" />
      </svg>
      <div class="ends"><span>1 Aug</span><span>1 Nov</span></div></div></div>`})},v=o(`<div class="empty"><strong>No weight yet</strong>
  <span>Log it once to see your chart. It stays on this phone, and only you see it.</span>
  <button type="button" class="btn primary sm">Log your weight</button></div>`),y=[`Button`,`Chip`,`Card`,`Stepper`,`Alert`,`TextField`,`SelectField`,`OptionSheet`,`NumberField`,`StepSlider`,`LineChart`,`EmptyChartCard`],s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`sample(\`<div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div>\`)`,...s.parameters?.docs?.source},description:{story:`The four button shapes. One primary button per screen; the grumpy button is only the catch-up action.`,...s.parameters?.docs?.description}}},c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`sample(\`<div class="row">
  <button type="button" class="chip on" aria-pressed="true">Legs</button>
  <button type="button" class="chip" aria-pressed="false">Core</button>
  <button type="button" class="chip" aria-pressed="false">Dumbbell</button></div>\`)`,...c.parameters?.docs?.source},description:{story:"Filters, with `aria-pressed`. On is `primary`, off is a 1.5 px `outline`.",...c.parameters?.docs?.description}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:'sample(`<div class="card"><span class="badge" aria-hidden="true">SQ</span>\n  <span class="card-text"><strong>Goblet squat</strong><span>4 × 8 at 16 kg</span></span></div>`)',...l.parameters?.docs?.source},description:{story:`A row with a pattern badge: the exercise and workout lists are made of these.`,...l.parameters?.docs?.description}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`sample(\`<div class="stepper"><span class="eyebrow">Rest</span><div class="stepper-row">
  <button type="button" aria-label="Less rest">−</button><span class="value">90 s</span>
  <button type="button" aria-label="More rest">+</button></div></div>\`)`,...u.parameters?.docs?.source},description:{story:`Sets, reps, load and rest.`,...u.parameters?.docs?.description}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`sample(\`<div class="alert" role="note"><CircleExclamationPoint :size="20" aria-hidden="true" />
  The treadmill stopped answering. Recording resumes when it's back.</div>\`, {
  CircleExclamationPoint
})`,...d.parameters?.docs?.source},description:{story:"The error banner: `error` tint, an icon and text, never ember.",...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`sample(\`<div class="sample"><label class="label" for="name">Name</label>
  <div class="box"><input id="name" value="Alex" /></div>
  <span class="note">Shown to you only, on this phone.</span></div>\`)`,...f.parameters?.docs?.source}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`sample(\`<div class="sample">
  <button type="button" class="box select"><span><span class="label">Experience</span><strong>Getting going</strong></span><ChevronRight :size="20" aria-hidden="true" /></button>
  <button type="button" class="box select"><span><span class="label">Sex</span><strong class="muted">Not set</strong></span><ChevronRight :size="20" aria-hidden="true" /></button></div>\`, {
  ChevronRight
})`,...p.parameters?.docs?.source}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`sample(\`<div class="sheet"><strong class="sheet-title">Pronouns</strong>
  <div class="option"><span class="radio on" aria-hidden="true"></span>They / them</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>She / her</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>Custom</div>
  <div class="box"><input value="" placeholder="Custom pronouns" aria-label="Custom pronouns" /></div>
  <button type="button" class="btn primary sm">Save</button></div>\`)`,...m.parameters?.docs?.source}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`sample(\`<div class="sample">
  <div class="box"><input inputmode="decimal" value="64" aria-label="Resting heart rate" /><span class="unit">bpm</span></div>
  <div class="entry"><button type="button" aria-label="Less">−</button>
    <div class="box"><input inputmode="numeric" value="8" aria-label="Reps" /><span class="unit">reps</span></div>
    <button type="button" aria-label="More">+</button></div></div>\`)`,...h.parameters?.docs?.source}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`sample(\`<div class="sample"><strong>Getting going</strong>
  <div class="slider" aria-hidden="true"><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar off"></span><span class="dot off"></span><span class="bar off"></span><span class="dot off"></span></div>
  <div class="ends"><span>Just starting</span><span>Very experienced</span></div></div>\`)`,...g.parameters?.docs?.source}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  render: () => ({
    setup: () => ({
      grid: [0, 25, 50, 75, 100],
      points: '8,62 60,56 112,58 164,44 216,40 268,30'
    }),
    template: \`<div class="cp-mobile"><div class="sample">
      <svg viewBox="0 0 290 100" class="chart" role="img" aria-label="Weight from 72 kg to 70 kg over three months">
        <text x="0" y="8" class="tick">kg</text>
        <g v-for="line in grid" :key="line">
          <line :x1="28" :x2="290" :y1="12 + line * 0.64" :y2="12 + line * 0.64" class="grid" />
          <text x="0" :y="16 + line * 0.64" class="tick">{{ 74 - line * 0.06 }}</text>
        </g>
        <polyline :points="points" class="line" transform="translate(28 0)" />
      </svg>
      <div class="ends"><span>1 Aug</span><span>1 Nov</span></div></div></div>\`
  })
}`,..._.parameters?.docs?.source}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`sample(\`<div class="empty"><strong>No weight yet</strong>
  <span>Log it once to see your chart. It stays on this phone, and only you see it.</span>
  <button type="button" class="btn primary sm">Log your weight</button></div>\`)`,...v.parameters?.docs?.source},description:{story:`Shown instead of the chart when nothing is logged.`,...v.parameters?.docs?.description}}}})))()}b();export{d as Alert,s as Button,l as Card,c as Chip,v as EmptyChartCard,_ as LineChart,h as NumberField,m as OptionSheet,p as SelectField,g as StepSlider,u as Stepper,f as TextField,y as __namedExportsOrder,a as default};
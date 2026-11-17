import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./story-DTpPuXkn.js";var r,i,a,o,s,c;function l(){return(l=e((()=>{n(),r={title:`Mobile/Components/Tentacle ring`,render:t(`MobileTentacleRing`,void 0,{content:``}),args:{lit:6,label:`6 of 8 lit · 1 full octopus`,fresh:!1,size:132,picture:`../../../poulpy/mood_happy.svg`,pictureAlt:`Poulpy, happy`},argTypes:{lit:{control:{type:`range`,min:0,max:8}},size:{control:`inline-radio`,options:[132,64]},picture:{control:`text`}},parameters:{docs:{canvas:{sourceState:`none`},description:{component:"`MobileTentacleRing`: Poulpy's eight tentacles as a ring, `lit` of them in primary. `fresh` lights the last one brighter, for the tentacle the session just lit. A `picture` of Poulpy sits inside the large ring (132, Home); the small one (64) closes a session. `label` says the count in words."}}}},i={},a={args:{picture:`../../../poulpy/mood_recovering.svg`,pictureAlt:`Poulpy, recovering`}},o={args:{lit:7,fresh:!0,size:64,picture:void 0,pictureAlt:void 0,label:`7 of 8 tentacles lit`}},s={args:{lit:0,label:`0 of 8 lit`}},c=[`Home`,`Resting`,`JustLit`,`NoneLit`],i.parameters={...i.parameters,docs:{...i.parameters?.docs,source:{originalSource:`{}`,...i.parameters?.docs?.source}}},a.parameters={...a.parameters,docs:{...a.parameters?.docs,source:{originalSource:`{
  args: {
    picture: '../../../poulpy/mood_recovering.svg',
    pictureAlt: 'Poulpy, recovering'
  }
}`,...a.parameters?.docs?.source}}},o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  args: {
    lit: 7,
    fresh: true,
    size: 64,
    picture: undefined,
    pictureAlt: undefined,
    label: '7 of 8 tentacles lit'
  }
}`,...o.parameters?.docs?.source}}},s.parameters={...s.parameters,docs:{...s.parameters?.docs,source:{originalSource:`{
  args: {
    lit: 0,
    label: '0 of 8 lit'
  }
}`,...s.parameters?.docs?.source}}}})))()}l();export{i as Home,o as JustLit,s as NoneLit,a as Resting,c as __namedExportsOrder,r as default};
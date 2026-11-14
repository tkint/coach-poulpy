import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{nt as t,t as n}from"./src-Bujm5Dv6.js";var r,i,a,o,s;function c(){return(c=e((()=>{n(),{expect:r,within:i}=__STORYBOOK_MODULE_TEST__,a={title:`Display/BodyMap`,component:t,args:{figures:[{caption:`Front`,paths:[{d:`M60 40 a14 14 0 1 0 0.01 0z`,outline:!0},{d:`M36 60 h48 v70 h-48z`,heat:3},{d:`M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z`,heat:1}]},{caption:`Back`,paths:[{d:`M60 40 a14 14 0 1 0 0.01 0z`,outline:!0},{d:`M36 60 h48 v70 h-48z`,heat:2},{d:`M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z`}]}],description:`Legs and chest worked most`,less:`Less`,more:`More`}},o={play:async({canvasElement:e})=>{r(i(e).getByRole(`img`)).toHaveAccessibleName(`Legs and chest worked most`)}},s=[`Default`],o.parameters={...o.parameters,docs:{...o.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Legs and chest worked most');
  }
}`,...o.parameters?.docs?.source}}}})))()}c();export{o as Default,s as __namedExportsOrder,a as default};
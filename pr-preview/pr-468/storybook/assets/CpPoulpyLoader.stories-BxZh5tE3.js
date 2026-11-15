import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{F as t,i as n}from"./iframe-aIY02B71.js";var r;function i(){return(i=e((()=>{r='`CpPoulpyLoader` is the loading indicator: Poulpy\'s outline seen from above, the flower of his eight tentacles, swinging half a turn one way and then half a turn the other way, forever, once every 3.6 s (once every 12 s when the page asks for reduced motion). Each tentacle hangs loose: it bends about its base behind the turn and follows a little later than the one before. `spin="clockwise"` is the other version: it only turns clockwise, a full turn every cycle, with the tentacles trailing behind. It replaces spinners. `size` is its width in pixels (48 by default). It is a status for a screen reader: `label` says what is loading, and says "Loading" in the page\'s language when it is left out. The phones\' `PoulpyLoader` moves the same way, from `LoaderSwing` in the shared code.\n\nThe picture is `design/poulpy-3d/pictures/loader_tentacle.png`, one tentacle painted by `design/build-poulpy-pictures.mjs` in the brand blue and see-through, drawn eight times round the middle, so it sits on a light or a dark page.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPoulpyLoader>` | `span.cp-poulpy-loader` around eight `span.cp-poulpy-loader-tentacle` |\n'})))()}var a,o,s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),{expect:a,within:o}=__STORYBOOK_MODULE_TEST__,s={title:`Web/Components/Display/Poulpy loader`,component:t,argTypes:{spin:{control:`inline-radio`,options:[`swing`,`clockwise`]},size:{control:{type:`range`,min:16,max:160,step:8}}},parameters:{docs:{description:{component:r}}}},c={play:async({canvasElement:e})=>{a(o(e).getByRole(`status`,{name:`Loading`})).toBeInTheDocument()}},l={args:{spin:`clockwise`}},u={args:{size:112}},d={args:{size:16}},f={args:{label:`Loading your sessions`},play:async({canvasElement:e})=>{a(o(e).getByRole(`status`,{name:`Loading your sessions`})).toBeInTheDocument()}},p=[`Default`,`Clockwise`,`Large`,`Small`,`SaysWhatLoads`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    // A status a screen reader announces, in the page's language.
    expect(within(canvasElement).getByRole('status', {
      name: 'Loading'
    })).toBeInTheDocument();
  }
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    spin: 'clockwise'
  }
}`,...l.parameters?.docs?.source}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    size: 112
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    size: 16
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    label: 'Loading your sessions'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('status', {
      name: 'Loading your sessions'
    })).toBeInTheDocument();
  }
}`,...f.parameters?.docs?.source}}}})))()}m();export{l as Clockwise,c as Default,u as Large,f as SaysWhatLoads,d as Small,p as __namedExportsOrder,s as default};
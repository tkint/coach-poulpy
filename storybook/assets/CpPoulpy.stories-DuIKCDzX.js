import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{i as t,z as n}from"./iframe-Dm9LBo0W.js";var r;function i(){return(i=e((()=>{r="`CpPoulpy` shows one of Poulpy's pictures, decorative unless it has a `label`; `size` is its width in pixels. His moods and poses are painted from the 3D model by `design/build-poulpy-pictures.mjs`, and the loader, Poulpy from above, transparent so they sit on a light or a dark page: see Core, Poulpy for all of them, and `PoulpyDrawing` for the names the `drawing` prop takes.\n\n`CpPoulpyCard` is the card for empty lists, no results and gone pages: see Web, Patterns, Poulpy card.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpPoulpy>` | `img.cp-poulpy` from `design/poulpy-3d/pictures/` |\n| `<CpPoulpyCard>` | `cp-card bg-secondary text-secondary-content cp-poulpy-card` |\n"})))()}var a,o,s,c,l,u,d,f;function p(){return(p=e((()=>{t(),i(),{expect:a,within:o}=__STORYBOOK_MODULE_TEST__,s={title:`Web/Components/Display/Poulpy`,component:n,parameters:{docs:{description:{component:r}}}},c={args:{drawing:`mood_happy`}},l={args:{drawing:`mood_recovering`}},u={args:{drawing:`mood_grumpy`}},d={args:{drawing:`pose_thinking`,label:`Poulpy is thinking`},play:async({canvasElement:e})=>{a(o(e).getByAltText(`Poulpy is thinking`)).toBeInTheDocument()}},f=[`Happy`,`Recovering`,`Grumpy`,`Described`],c.parameters={...c.parameters,docs:{...c.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_happy'
  }
}`,...c.parameters?.docs?.source}}},l.parameters={...l.parameters,docs:{...l.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_recovering'
  }
}`,...l.parameters?.docs?.source},description:{story:`The blue side of pain and rehab: Poulpy is never guilt.`,...l.parameters?.docs?.description}}},u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'mood_grumpy'
  }
}`,...u.parameters?.docs?.source}}},d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  args: {
    drawing: 'pose_thinking',
    label: 'Poulpy is thinking'
  },
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByAltText('Poulpy is thinking')).toBeInTheDocument();
  }
}`,...d.parameters?.docs?.source}}}})))()}p();export{d as Described,u as Grumpy,c as Happy,l as Recovering,f as __namedExportsOrder,s as default};
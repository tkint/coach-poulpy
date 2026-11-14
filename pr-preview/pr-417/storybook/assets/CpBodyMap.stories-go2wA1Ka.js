import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{B as t,a as n}from"./iframe-BN6o35g7.js";import{ot as r,t as i}from"./src-Bh6CW92w.js";var a;function o(){return(o=e((()=>{a='`CpBodyMap`: the muscles worked, as the front and back figure with each area lit from heat 1 (a little) to 3 (most), and a Less to More legend. The figures come from the generated body map shapes; `description` says in words what the picture shows.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpBodyMap>` | `div.cp-body-map` (`role="img"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3) and `path.cp-body-outline`, then `p.cp-heat-legend` (left out when `less` and `more` are) |\n| `<CpBodyMap interactive>` | `role="group"`; an area with a `key` and a `label` is a `path.cp-body-part.cp-body-part-pick[role=button]` (`aria-pressed` says whether it is lit) and says its key with `pick`, so the picture is a field: the screen writes its own legend, or none |\n'})))()}var s,c,l,u,d,f,p;function m(){return(m=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Core/Body map`,parameters:{docs:{description:{component:a}}},component:r,args:{figures:[{caption:`Front`,paths:[{d:`M60 40 a14 14 0 1 0 0.01 0z`,outline:!0},{d:`M36 60 h48 v70 h-48z`,heat:3},{d:`M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z`,heat:1}]},{caption:`Back`,paths:[{d:`M60 40 a14 14 0 1 0 0.01 0z`,outline:!0},{d:`M36 60 h48 v70 h-48z`,heat:2},{d:`M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z`}]}],description:`Legs and chest worked most`,less:`Less`,more:`More`}},d={play:async({canvasElement:e})=>{s(l(e).getByRole(`img`)).toHaveAccessibleName(`Legs and chest worked most`)}},f={args:{interactive:!0,less:void 0,more:void 0},render:e=>({components:{CpBodyMap:r},setup:()=>{let n=t([`chest`]),r=[{key:`chest`,label:`Chest`,d:`M36 60 h48 v40 h-48z`},{key:`legs`,label:`Legs`,d:`M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z`}];return{args:e,figures:()=>[{caption:`Front`,paths:[{d:`M60 40 a14 14 0 1 0 0.01 0z`,outline:!0},...r.map(e=>({...e,heat:n.value.includes(e.key)?3:void 0}))]}],pick:e=>n.value=n.value.includes(e)?n.value.filter(t=>t!==e):[...n.value,e]}},template:`<CpBodyMap v-bind="args" :figures="figures()" @pick="pick" />`}),play:async({canvasElement:e})=>{let t=l(e).getByRole(`button`,{name:`Legs`});s(t).toHaveAttribute(`aria-pressed`,`false`),await c.click(t),s(t).toHaveAttribute(`aria-pressed`,`true`)}},p=[`Default`,`Interactive`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Legs and chest worked most');
  }
}`,...d.parameters?.docs?.source}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    interactive: true,
    less: undefined,
    more: undefined
  },
  render: args => ({
    components: {
      CpBodyMap
    },
    setup: () => {
      const lit = ref<string[]>(['chest']);
      const areas = [{
        key: 'chest',
        label: 'Chest',
        d: 'M36 60 h48 v40 h-48z'
      }, {
        key: 'legs',
        label: 'Legs',
        d: 'M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z'
      }];
      const figures = () => [{
        caption: 'Front',
        paths: [{
          d: 'M60 40 a14 14 0 1 0 0.01 0z',
          outline: true
        }, ...areas.map(area => ({
          ...area,
          heat: lit.value.includes(area.key) ? 3 as const : undefined
        }))]
      }];
      const pick = (key: string) => lit.value = lit.value.includes(key) ? lit.value.filter(one => one !== key) : [...lit.value, key];
      return {
        args,
        figures,
        pick
      };
    },
    template: '<CpBodyMap v-bind="args" :figures="figures()" @pick="pick" />'
  }),
  play: async ({
    canvasElement
  }) => {
    const legs = within(canvasElement).getByRole('button', {
      name: 'Legs'
    });
    expect(legs).toHaveAttribute('aria-pressed', 'false');
    await userEvent.click(legs);
    expect(legs).toHaveAttribute('aria-pressed', 'true');
  }
}`,...f.parameters?.docs?.source},description:{story:`The picture is a field: an area with a key and a label is a button that says its key when it is pressed.`,...f.parameters?.docs?.description}}}})))()}m();export{d as Default,f as Interactive,p as __namedExportsOrder,u as default};